package com.bwfcwalshy.flarebot.commands.administrator;

import com.bwfcwalshy.flarebot.FlareBot;
import com.bwfcwalshy.flarebot.MessageUtils;
import com.bwfcwalshy.flarebot.commands.Command;
import com.bwfcwalshy.flarebot.commands.CommandType;
import com.bwfcwalshy.flarebot.permissions.Group;
import com.bwfcwalshy.flarebot.util.Parser;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IUser;

import java.io.IOException;

public class PermissionsCommand implements Command {

    @Override
    public void onCommand(IUser sender, IChannel channel, IMessage message, String[] args) {
        if (args.length == 0) {
            MessageUtils.sendMessage(channel, getDescription());
            return;
        }
        switch (args[0].toLowerCase()) {
            case "givegroup":
                if (args.length < 3) {
                    MessageUtils.sendMessage(channel, getDescription());
                    return;
                }
                IUser user = Parser.mention(args[1]);
                if (user == null) {
                    MessageUtils.sendMessage(channel, "No such user!");
                    return;
                }
                if (getPermissions(channel).hasGroup(args[1])) {
                    MessageUtils.sendMessage(channel, "No such group!");
                    return;
                }
                Group group = getPermissions(channel).getGroup(args[2]);
                if (getPermissions(channel).getUser(user).addGroup(group))
                    MessageUtils.sendMessage(channel, "Success");
                else MessageUtils.sendMessage(channel, "User already had that group!");
                break;
            case "revokegroup":
                if (args.length < 3) {
                    MessageUtils.sendMessage(channel, getDescription());
                    return;
                }
                IUser user2 = Parser.mention(args[1]);
                if (user2 == null) {
                    MessageUtils.sendMessage(channel, "No such user!");
                    return;
                }
                if (getPermissions(channel).hasGroup(args[1])) {
                    MessageUtils.sendMessage(channel, "No such group!");
                    return;
                }
                Group group2 = getPermissions(channel).getGroup(args[2]);
                if (getPermissions(channel).getUser(user2).removeGroup(group2))
                    MessageUtils.sendMessage(channel, "Success");
                else MessageUtils.sendMessage(channel, "User never had that group!");
                break;
            case "addpermission":
                if (args.length < 3) {
                    MessageUtils.sendMessage(channel, getDescription());
                    return;
                }
                if (getPermissions(channel).hasGroup(args[1])) {
                    MessageUtils.sendMessage(channel, "No such group!");
                    return;
                }
                Group group3 = getPermissions(channel).getGroup(args[1]);
                if (getPermissions(channel).removePermission(group3.getName(), args[2]))
                    MessageUtils.sendMessage(channel, "Success");
                else MessageUtils.sendMessage(channel, "Group never had that permission");
                break;
            case "removepermission":
                if (args.length < 3) {
                    MessageUtils.sendMessage(channel, getDescription());
                    return;
                }
                if (getPermissions(channel).hasGroup(args[1])) {
                    MessageUtils.sendMessage(channel, "No such group!");
                    return;
                }
                Group group4 = getPermissions(channel).getGroup(args[1]);
                if (getPermissions(channel).removePermission(group4.getName(), args[2]))
                    MessageUtils.sendMessage(channel, "Success");
                else MessageUtils.sendMessage(channel, "Group never had that permission");
                break;
            case "save":
                try {
                    FlareBot.getInstance().getPermissions().save();
                } catch (IOException e) {
                    MessageUtils.sendException("Could not save permissions!", e, channel);
                }
                break;
            default:
                MessageUtils.sendMessage(channel, getDescription());
                break;
        }
    }

    @Override
    public String getCommand() {
        return "permissions";
    }

    @Override
    public String[] getAliases() {
        return new String[]{"perm", "perms"};
    }

    @Override
    public String getDescription() {
        return "permissions givegroup | revokegroup <user> <group> for user management or addpermission | removepermission <group> <permission> for group management. permissions save to save.";
    }

    @Override
    public CommandType getType() {
        return CommandType.ADMINISTRATIVE;
    }

    @Override
    public String getPermission() {
        return "flarebot.permissions";
    }
}
