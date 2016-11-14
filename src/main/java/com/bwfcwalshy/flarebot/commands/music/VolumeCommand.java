package com.bwfcwalshy.flarebot.commands.music;

import com.bwfcwalshy.flarebot.MessageUtils;
import com.bwfcwalshy.flarebot.commands.Command;
import com.bwfcwalshy.flarebot.commands.CommandType;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IUser;

public class VolumeCommand implements Command {

    @Override
    public void onCommand(IUser sender, IChannel channel, IMessage message, String[] args) {
        MessageUtils.sendMessage(channel, "Sorry, " + sender + ", but we removed that command. It was creating a CPU overhead.\n" +
                "Do not worry, you can still change the volume. Here is how: https://youtu.be/pWv2AXlOlk4");
    }

    @Override
    public String getCommand() {
        return "volume";
    }

    @Override
    public String getDescription() {
        return "Change the volume of the music.";
    }

    @Override
    public CommandType getType() { return CommandType.MUSIC; }
}
