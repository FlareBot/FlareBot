package stream.flarebot.flarebot.commands.administrator;

import stream.flarebot.flarebot.FlareBot;
import stream.flarebot.flarebot.MessageUtils;
import stream.flarebot.flarebot.commands.Command;
import stream.flarebot.flarebot.commands.CommandType;
import net.dv8tion.jda.core.entities.*;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class AutoAssignCommand implements Command {

    private FlareBot flareBot;

    public AutoAssignCommand(FlareBot bot) {
        this.flareBot = bot;
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public void onCommand(User sender, TextChannel channel, Message message, String[] args, Member member) {
        if (channel.getGuild().getOwner().getUser().getId().equals(sender.getId())) {
            if (args.length == 0) {
                MessageUtils.sendUsage(this, channel);
            } else if (args.length == 1) {
                if (args[0].equalsIgnoreCase("list")) {
                    if (flareBot.getAutoAssignRoles().containsKey(channel.getGuild().getId())) {
                        StringBuilder sb = new StringBuilder();
                        sb.append("**Currently Auto Assigned Roles**\n```\n");
                        // This is there just in case they remove it.
                        // noinspection ConstantConditions
                        flareBot.getAutoAssignRoles().get(channel.getGuild().getId()).stream()
                                .filter(role -> getRole(channel.getGuild(), role) != null)
                                .forEach(role -> sb.append(getRole(channel.getGuild(), role).getName()).append("\n"));
                        sb.append("```");
                        channel.sendMessage(sb.toString()).queue();
                    } else {
                        MessageUtils.sendErrorMessage(MessageUtils.getEmbed(sender).setDescription("This server has no roles being assigned."), channel);
                    }
                } else if (args[0].equalsIgnoreCase("add") || args[0].equalsIgnoreCase("remove")) {
                    MessageUtils.sendErrorMessage(MessageUtils.getEmbed(sender)
                            .setDescription(sender.getAsMention() + " Usage: " + FlareBot.getPrefixes().get(channel.getGuild().getId()) + "autoassign " + args[0] + " <role>"), channel);
                } else {
                    MessageUtils.sendErrorMessage(MessageUtils.getEmbed(sender).setDescription(sender.getAsMention() + " Invalid argument!"), channel);
                }
            } else if (args.length >= 2) {
                String passedRole = "";
                for (int i = 1; i < args.length; i++)
                    passedRole += args[i] + ' ';
                passedRole = passedRole.trim();
                if (args[0].equalsIgnoreCase("add")) {
                    if (!validRole(channel.getGuild(), passedRole)) {
                        MessageUtils.sendErrorMessage(MessageUtils.getEmbed(sender).setDescription(sender.getAsMention() + " That is not a valid role!"), channel);
                        return;
                    }
                    Role role = getRole(channel.getGuild(), passedRole);
                    CopyOnWriteArrayList<String> roles = flareBot.getAutoAssignRoles()
                            .computeIfAbsent(channel.getGuild().getId(), c -> new CopyOnWriteArrayList<>());
                    if (!roles.contains(role.getId())) {
                        roles.add(role.getId());
                        flareBot.getAutoAssignRoles().put(channel.getGuild().getId(), roles);
                        channel.sendMessage(MessageUtils.getEmbed(sender)
                                .setDescription("Added " + role.getName() + " to your auto assigned roles!").build()).queue();
                    } else {
                        MessageUtils.sendErrorMessage(MessageUtils.getEmbed(sender).setDescription(role.getName() + " is already being assigned!"), channel);
                    }
                } else if (args[0].equalsIgnoreCase("remove")) {
                    if (!validRole(channel.getGuild(), passedRole)) {
                        MessageUtils.sendErrorMessage(MessageUtils.getEmbed(sender).setDescription(sender.getAsMention() + " That is not a valid role!"), channel);
                        return;
                    }
                    Role role = getRole(channel.getGuild(), passedRole);
                    List<String> roles;
                    if (flareBot.getAutoAssignRoles().containsKey(channel.getGuild().getId())) {
                        roles = flareBot.getAutoAssignRoles().get(channel.getGuild().getId());
                        if (roles.contains(role.getId())) {
                            roles.remove(role.getId());
                            channel.sendMessage(MessageUtils.getEmbed(sender)
                                    .setDescription("Removed " + role.getName() + " from your auto assigned roles").build()).queue();
                        } else {
                            MessageUtils.sendErrorMessage(MessageUtils.getEmbed(sender).setDescription("That role is not being auto assigned!"), channel);
                        }
                    } else {
                        MessageUtils.sendErrorMessage(MessageUtils.getEmbed(sender).setDescription("This server has no roles being assigned."), channel);
                    }
                } else {
                    MessageUtils.sendErrorMessage(MessageUtils.getEmbed(sender).setDescription(sender.getAsMention() + " Invalid argument!"), channel);
                }
            } else {
                MessageUtils.sendErrorMessage(MessageUtils.getEmbed(sender)
                        .setDescription(sender.getAsMention() + " Usage: " + FlareBot.getPrefixes().get(channel.getGuild().getId()) + "autoassign <add/remove/list> (role)"), channel);
            }
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
        return "autoassign <add/remove/list> [role]";
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
