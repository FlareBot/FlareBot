package com.bwfcwalshy.flarebot.commands.music;

import com.bwfcwalshy.flarebot.FlareBot;
import com.bwfcwalshy.flarebot.MessageUtils;
import com.bwfcwalshy.flarebot.commands.Command;
import com.bwfcwalshy.flarebot.commands.CommandType;
import com.bwfcwalshy.flarebot.music.Player;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IUser;

public class LoopCommand implements Command {
    @Override
    public void onCommand(IUser sender, IChannel channel, IMessage message, String[] args) {
        Player player = FlareBot.getInstance().getMusicManager().getPlayer(channel.getGuild().getID());
        if(!player.getLooping()){
            player.setLooping(true);
            MessageUtils.sendMessage(channel, "Looping: **ON**");
        } else {
            player.setLooping(false);
            MessageUtils.sendMessage(channel, "Looping: **OFF**");
        }
    }

    @Override
    public String getCommand() {
        return "loop";
    }

    @Override
    public String getDescription() {
        return "Toggles looping of the current playlist";
    }

    @Override
    public CommandType getType() {
        return CommandType.MUSIC;
    }

    @Override
    public String getPermission() {
        return "flarebot.loop";
    }
}
