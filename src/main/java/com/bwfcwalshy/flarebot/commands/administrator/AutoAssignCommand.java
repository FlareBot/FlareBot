package com.bwfcwalshy.flarebot.commands.administrator;

import com.bwfcwalshy.flarebot.FlareBot;
import com.bwfcwalshy.flarebot.MessageUtils;
import com.bwfcwalshy.flarebot.commands.Command;
import com.bwfcwalshy.flarebot.commands.CommandType;
import sx.blah.discord.handle.obj.*;

import java.util.ArrayList;
import java.util.List;

public class AutoAssignCommand implements Command {

    private FlareBot flareBot;

    public AutoAssignCommand(FlareBot bot) {
        this.flareBot = bot;
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public void onCommand(IUser sender, IChannel channel, IMessage message, String[] args) {
        if (channel.getGuild().getOwner().getID().equals(sender.getID()) || flareBot.getPermissions(channel).hasPermission(sender, "flarebot.commands.autoassign")) {
            if (args.length == 0) {
                MessageUtils.sendErrorMessage(MessageUtils.getEmbed(sender)
                        .withDesc(sender.mention() + " Usage: " + FlareBot.getPrefixes().get(channel.getGuild().getID()) + "autoassign <add/remove/list> (role)"), channel);
            } else if (args.length == 1) {
                if (args[0].equalsIgnoreCase("list")) {
                    if (flareBot.getAutoAssignRoles().containsKey(channel.getGuild().getID())) {
                        StringBuilder sb = new StringBuilder();
                        sb.append("**Currently Auto Assigned Roles**\n```\n");
                        // This is there just in case they remove it.
                        // noinspection ConstantConditions
                        flareBot.getAutoAssignRoles().get(channel.getGuild().getID()).stream()
                                .filter(role -> getRole(channel.getGuild(), role) != null)
                                .forEach(role -> sb.append(getRole(channel.getGuild(), role).getName()).append("\n"));
                        sb.append("```");
                        MessageUtils.sendMessage(sb.toString(), channel);
                    } else {
                        MessageUtils.sendErrorMessage(MessageUtils.getEmbed(sender).withDesc("This server has no roles being assigned."), channel);
                    }
                } else if (args[0].equalsIgnoreCase("add") || args[0].equalsIgnoreCase("remove")) {
                    MessageUtils.sendErrorMessage(MessageUtils.getEmbed(sender)
                            .withDesc(sender.mention() + " Usage: " + FlareBot.getPrefixes().get(channel.getGuild().getID()) + "autoassign " + args[0] + " <role>"), channel);
                } else {
                    MessageUtils.sendErrorMessage(MessageUtils.getEmbed(sender).withDesc(sender.mention() + " Invalid argument!"), channel);
                }
            } else if (args.length >= 2) {
                String passedRole = "";
                for (int i = 1; i < args.length; i++)
                    passedRole += args[i] + ' ';
                passedRole = passedRole.trim();
                if (args[0].equalsIgnoreCase("add")) {
                    if (!validRole(channel.getGuild(), passedRole)) {
                        MessageUtils.sendErrorMessage(MessageUtils.getEmbed(sender).withDesc(sender.mention() + " That is not a valid role!"), channel);
                        return;
                    }
                    IRole role = getRole(channel.getGuild(), passedRole);
                    List<String> roles;
                    if (flareBot.getAutoAssignRoles().containsKey(channel.getGuild().getID()))
                        roles = flareBot.getAutoAssignRoles().get(channel.getGuild().getID());
                    else
                        roles = new ArrayList<>();
                    if (!roles.contains(role.getID())) {
                        roles.add(role.getID());
                        flareBot.getAutoAssignRoles().put(channel.getGuild().getID(), roles);
                        MessageUtils.sendMessage(MessageUtils.getEmbed(sender).withDesc("Added " + role.getName() + " to your auto assigned roles!"), channel);
                    } else {
                        MessageUtils.sendErrorMessage(MessageUtils.getEmbed(sender).withDesc(role.getName() + " is already being assigned!"), channel);
                    }
                } else if (args[0].equalsIgnoreCase("remove")) {
                    if (!validRole(channel.getGuild(), passedRole)) {
                        MessageUtils.sendErrorMessage(MessageUtils.getEmbed(sender).withDesc(sender.mention() + " That is not a valid role!"), channel);
                        return;
                    }
                    IRole role = getRole(channel.getGuild(), passedRole);
                    List<String> roles;
                    if (flareBot.getAutoAssignRoles().containsKey(channel.getGuild().getID())) {
                        roles = flareBot.getAutoAssignRoles().get(channel.getGuild().getID());
                        if (roles.contains(role.getID())) {
                            roles.remove(role.getID());
                            MessageUtils.sendMessage(MessageUtils.getEmbed(sender).withDesc("Removed " + role.getName() + " from your auto assigned roles"), channel);
                        } else {
                            MessageUtils.sendErrorMessage(MessageUtils.getEmbed(sender).withDesc("That role is not being auto assigned!"), channel);
                        }
                    } else {
                        MessageUtils.sendErrorMessage(MessageUtils.getEmbed(sender).withDesc("This server has no roles being assigned."), channel);
                    }
                } else {
                    MessageUtils.sendErrorMessage(MessageUtils.getEmbed(sender).withDesc(sender.mention() + " Invalid argument!"), channel);
                }
            } else {
                MessageUtils.sendErrorMessage(MessageUtils.getEmbed(sender)
                        .withDesc(sender.mention() + " Usage: " + FlareBot.getPrefixes().get(channel.getGuild().getID()) + "autoassign <add/remove/list> (role)"), channel);
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
    public CommandType getType() {
        return CommandType.ADMINISTRATIVE;
    }

    @Override
    public String getPermission() {
        return "flarebot.assign";
    }

    @Override
    public String[] getAliases() {
        return new String[]{"autorole"};
    }

    private boolean validRole(IGuild guild, String role) {
        for (IRole iRole : guild.getRoles()) {
            if (iRole.getID().equals(role) || iRole.getName().equalsIgnoreCase(role))
                return true;
        }
        return false;
    }

    private IRole getRole(IGuild guild, String role) {
        // Since I know it is valid I can get the id from the name or just return the id since that will be the only other thing passed.
        for (IRole iRole : guild.getRoles()) {
            if (iRole.getID().equals(role) || iRole.getName().equalsIgnoreCase(role))
                return iRole;
        }
        return null;
    }
}
