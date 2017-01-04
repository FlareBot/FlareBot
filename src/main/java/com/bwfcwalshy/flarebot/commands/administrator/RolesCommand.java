package com.bwfcwalshy.flarebot.commands.administrator;

import com.bwfcwalshy.flarebot.MessageUtils;
import com.bwfcwalshy.flarebot.commands.Command;
import com.bwfcwalshy.flarebot.commands.CommandType;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IRole;
import sx.blah.discord.handle.obj.IUser;

public class RolesCommand implements Command {

    @Override
    public void onCommand(IUser sender, IChannel channel, IMessage message, String[] args) {
        if (channel.isPrivate()) {
            MessageUtils.sendMessage("**DM's in Discord can not have roles!**", channel);
            return;
        }
        StringBuilder sb = new StringBuilder();
        sb.append("**Server Roles**\n```\n");
        for (IRole role : channel.getGuild().getRoles()) {
            sb.append(role.getName()).append(" (").append(role.getID()).append(")\n");
        }
        sb.append("```");

        MessageUtils.sendMessage(sb.toString(), channel);
    }

    @Override
    public String getCommand() {
        return "roles";
    }

    @Override
    public String getDescription() {
        return "Get roles on the server";
    }

    @Override
    public CommandType getType() {
        return CommandType.ADMINISTRATIVE;
    }

    @Override
    public String getPermission() {
        return "flarebot.roles";
    }
}
