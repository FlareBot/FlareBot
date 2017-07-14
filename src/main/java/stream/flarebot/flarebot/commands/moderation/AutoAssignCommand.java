package stream.flarebot.flarebot.commands.moderation;

import net.dv8tion.jda.core.entities.*;
import stream.flarebot.flarebot.FlareBot;
import stream.flarebot.flarebot.commands.Command;
import stream.flarebot.flarebot.commands.CommandType;
import stream.flarebot.flarebot.objects.GuildWrapper;
import stream.flarebot.flarebot.util.MessageUtils;

import java.awt.*;

public class AutoAssignCommand implements Command {

    @Override
    public void onCommand(User sender, GuildWrapper guild, TextChannel channel, Message message, String[] args, Member member) {
        if (args.length == 0) {
            MessageUtils.getUsage(this, channel, sender).queue();
        } else if (args.length == 1) {
            if (args[0].equalsIgnoreCase("list")) {
                if (guild.getAutoAssignRoles().contains(channel.getGuild().getId())) {
                    StringBuilder sb = new StringBuilder();
                    sb.append("**Currently Auto Assigned Roles**\n```\n");
                    guild.getAutoAssignRoles().stream()
                            .filter(role -> getRole(channel.getGuild(), role) != null)
                            .forEach(role -> sb.append(getRole(channel.getGuild(), role).getName()).append("\n"));
                    sb.append("```");
                    channel.sendMessage(sb.toString()).queue();
                } else {
                    MessageUtils.sendAutoDeletedMessage(MessageUtils.getEmbed(sender)
                            .setDescription("This server has no roles being assigned!").setColor(Color.RED).build(), 5000, channel);
                }
            } else if (args[0].equalsIgnoreCase("add") || args[0].equalsIgnoreCase("remove")) {
                MessageUtils.sendErrorMessage(MessageUtils.getEmbed(sender)
                        .setDescription(sender
                                .getAsMention() + " Usage: " + FlareBot
                                .getPrefixes().get(channel.getGuild()
                                        .getId()) + "autoassign " + args[0] + " <role>"), channel);
            } else {
                MessageUtils.sendErrorMessage(MessageUtils.getEmbed(sender).setDescription(sender
                        .getAsMention() + " Invalid argument!"), channel);
            }
        } else if (args.length >= 2) {
            String passedRole = "";
            for (int i = 1; i < args.length; i++)
                passedRole += args[i] + ' ';
            passedRole = passedRole.trim();
            if (args[0].equalsIgnoreCase("add")) {
                Role role = getRole(channel.getGuild(), passedRole);
                if(role == null){
                    MessageUtils.sendErrorMessage(MessageUtils.getEmbed(sender).setDescription(sender
                            .getAsMention() + " That is not a valid role!"), channel);
                    return;
                }
                if (!guild.getAutoAssignRoles().contains(role.getId())) {
                    guild.getAutoAssignRoles().add(role.getId());
                    channel.sendMessage(MessageUtils.getEmbed(sender)
                            .setDescription("Added " + role
                                    .getName() + " to your auto assigned roles!").build())
                            .queue();
                } else {
                    MessageUtils.sendErrorMessage(MessageUtils.getEmbed(sender).setDescription(role
                            .getName() + " is already being assigned!"), channel);
                }
            } else if (args[0].equalsIgnoreCase("remove")) {
                Role role = getRole(channel.getGuild(), passedRole);
                if(role == null) {
                    MessageUtils.sendErrorMessage(MessageUtils.getEmbed(sender).setDescription(sender
                            .getAsMention() + " That is not a valid role!"), channel);
                    return;
                }
                if(guild.getAutoAssignRoles().isEmpty()){

                if (guild.getAutoAssignRoles().contains(role.getId())) {
                    guild.getAutoAssignRoles().remove(role.getId());
                    channel.sendMessage(MessageUtils.getEmbed(sender)
                            .setDescription("Removed " + role.getName() + " from your auto assigned roles")
                            .build()).queue();
                    } else {
                        MessageUtils.sendErrorMessage(MessageUtils.getEmbed(sender)
                                .setDescription("That role is not being auto assigned!"), channel);
                    }
                } else {
                    MessageUtils.sendErrorMessage(MessageUtils.getEmbed(sender)
                            .setDescription("This server has no roles being assigned."), channel);
                }
            } else {
                MessageUtils.sendErrorMessage(MessageUtils.getEmbed(sender).setDescription(sender
                        .getAsMention() + " Invalid argument!"), channel);
            }
        } else {
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

    private Role getRole(Guild guild, String role) {
        // Since I know it is valid I can get the id from the name or just return the id since that will be the only other thing passed.
        for (Role iRole : guild.getRoles()) {
            if (iRole.getId().equals(role) || iRole.getName().equalsIgnoreCase(role))
                return iRole;
        }
        return null;
    }
}