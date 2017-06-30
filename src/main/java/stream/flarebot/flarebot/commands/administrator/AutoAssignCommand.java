package stream.flarebot.flarebot.commands.administrator;

import net.dv8tion.jda.core.entities.*;
import stream.flarebot.flarebot.FlareBot;
import stream.flarebot.flarebot.commands.Command;
import stream.flarebot.flarebot.commands.CommandType;
import stream.flarebot.flarebot.commands.FlareBotManager;
import stream.flarebot.flarebot.objects.GuildWrapper;
import stream.flarebot.flarebot.util.MessageUtils;

import java.awt.*;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class AutoAssignCommand implements Command {

    private FlareBot flareBot;
    private FlareBotManager flareBotManager;

    public AutoAssignCommand(FlareBot bot) {
        this.flareBot = bot;
        this.flareBotManager = bot.getManager();
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public void onCommand(User sender, GuildWrapper guild, TextChannel channel, Message message, String[] args, Member member) {
        if (args.length == 0) {
            MessageUtils.getUsage(this, channel, sender).queue();
        } else {
            String guildID = guild.getGuild().getId();
            if (args[0].equalsIgnoreCase("list")) {
                if (flareBot.getAutoAssignRoles().containsKey(guildID)) {
                    StringBuilder sb = new StringBuilder();
                    sb.append("**")
                            .append(flareBotManager.getLang("autoassign.list.title", guildID))
                            .append("**\n```\n");
                    // This is there just in case they remove it.
                    // noinspection ConstantConditions
                    flareBot.getAutoAssignRoles().get(guildID).stream()
                            .filter(role -> getRole(channel.getGuild(), role) != null)
                            .forEach(role -> sb.append(getRole(channel.getGuild(), role).getName()).append("\n"));
                    sb.append("```");
                    channel.sendMessage(sb.toString()).queue();
                } else {
                    MessageUtils.sendAutoDeletedMessage(MessageUtils.getEmbed(sender)
                            .setDescription(flareBotManager.getLang("autoassign.list.none", guildID))
                            .setColor(Color.RED)
                            .build(), 5000, channel);
                }
            } else if (args[0].equalsIgnoreCase("add")) {
                if (args.length >= 2) {
                    String passedRole = MessageUtils.getMessage(args, 1);
                    if (!validRole(channel.getGuild(), passedRole)) {
                        MessageUtils.sendErrorMessage(MessageUtils.getEmbed(sender)
                                .setDescription(flareBotManager.getLang("autoassign.add.invalid-role", guildID)), channel);
                        return;
                    }
                    Role role = getRole(channel.getGuild(), passedRole);
                    CopyOnWriteArrayList<String> roles = flareBot.getAutoAssignRoles()
                            .computeIfAbsent(channel.getGuild()
                                    .getId(), c -> new CopyOnWriteArrayList<>());
                    if (!roles.contains(role.getId())) {
                        roles.add(role.getId());
                        flareBot.getAutoAssignRoles().put(channel.getGuild().getId(), roles);
                        channel.sendMessage(MessageUtils.getEmbed(sender)
                                .setDescription(
                                        String.format(flareBotManager.getLang("autoassign.add.success", guildID), role.getName())).build())
                                .queue();
                    } else {
                        MessageUtils.sendErrorMessage(MessageUtils.getEmbed(sender)
                                .setDescription(String.format(flareBotManager.getLang("autoassign.add.already-exists", guildID), role.getName())), channel);
                    }
                }
            } else if (args[0].equalsIgnoreCase("remove")) {
                if (args.length >= 2) {
                    String passedRole = MessageUtils.getMessage(args, 1);
                    if (!validRole(channel.getGuild(), passedRole)) {
                        MessageUtils.sendErrorMessage(MessageUtils.getEmbed(sender).setDescription(sender
                                .getAsMention() + " That is not a valid role!"), channel);
                        return;
                    }
                    Role role = getRole(channel.getGuild(), passedRole);
                    List<String> roles;
                    if (flareBot.getAutoAssignRoles().containsKey(channel.getGuild().getId())) {
                        roles = flareBot.getAutoAssignRoles().get(channel.getGuild().getId());
                        if (roles.contains(role.getId())) {
                            roles.remove(role.getId());
                            channel.sendMessage(MessageUtils.getEmbed(sender)
                                    .setDescription("Removed " + role
                                            .getName() + " from your auto assigned roles")
                                    .build()).queue();
                        } else {
                            MessageUtils.sendErrorMessage(MessageUtils.getEmbed(sender)
                                    .setDescription("That role is not being auto assigned!"), channel);
                        }
                    } else {
                        MessageUtils.sendErrorMessage(MessageUtils.getEmbed(sender)
                                .setDescription("This server has no roles being assigned."), channel);
                    }
                }
            }
            MessageUtils.getUsage(this, channel, sender).queue();
        }
    }

    @Override
    public String getCommand() {
        return "autoassign";
    }

    @Override
    public String getDescription() {
        return "Auto assign roles to users when they join.";
    }

    @Override
    public String getUsage() {
        return "`{%}autoassign <add/remove> <role>` - Add or Remove roles from AutoAssign\n"
                + "`{%}autoassign list` - List roles that are currently AutoAssigned";
    }

    @Override
    public CommandType getType() {
        return CommandType.MODERATION;
    }

    @Override
    public String[] getAliases() {
        return new String[]{"autorole"};
    }

    @Override
    public boolean isDefaultPermission() {
        return false;
    }

    private boolean validRole(Guild guild, String role) {
        for (Role iRole : guild.getRoles()) {
            if (iRole.getId().equals(role) || iRole.getName().equalsIgnoreCase(role))
                return true;
        }
        return false;
    }

    private Role getRole(Guild guild, String role) {
        // Since I know it is valid I can get the id from the name or just return the id since that will be the only other thing passed.
        for (Role iRole : guild.getRoles()) {
            if (iRole.getId().equals(role) || iRole.getName().equalsIgnoreCase(role))
                return iRole;
        }
        return null;
    }
}
