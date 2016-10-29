package com.bwfcwalshy.flarebot.commands.music;

import com.bwfcwalshy.flarebot.commands.Command;
import com.bwfcwalshy.flarebot.commands.CommandType;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IUser;

/**
 * <br>
 * Created by Arsen on 29.10.16..
 */
public class SavedCommand implements Command {
    @Override
    public void onCommand(IUser sender, IChannel channel, IMessage message, String[] args) {

    }

    @Override
    public String getCommand() {
        return null;
    }

    @Override
    public String getDescription() {
        return null;
    }

    @Override
    public CommandType getType() {
        return null;
    }
}
