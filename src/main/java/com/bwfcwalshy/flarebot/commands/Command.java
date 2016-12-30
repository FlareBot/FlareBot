package com.bwfcwalshy.flarebot.commands;

import com.bwfcwalshy.flarebot.FlareBot;
import com.bwfcwalshy.flarebot.permissions.PerGuildPermissions;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IUser;

public interface Command {

    void onCommand(IUser sender, IChannel channel, IMessage message, String[] args);

    String getCommand();

    String getDescription();

    CommandType getType();

    default String getPermission() {
        return "flarebot." + getCommand();
    }

    default String[] getAliases() {
        return new String[]{};
    }

    default PerGuildPermissions getPermissions(IChannel chan) {
        return FlareBot.getInstance().getPermissions(chan);
    }
}
