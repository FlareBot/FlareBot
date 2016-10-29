package com.bwfcwalshy.flarebot.commands.music;

import com.bwfcwalshy.flarebot.MessageUtils;
import com.bwfcwalshy.flarebot.commands.Command;
import com.bwfcwalshy.flarebot.commands.CommandType;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.util.audio.AudioPlayer;

public class LoopCommand implements Command {
    @Override
    public void onCommand(IUser sender, IChannel channel, IMessage message, String[] args) {
        AudioPlayer player = AudioPlayer.getAudioPlayerForGuild(channel.getGuild());
        if(!player.isLooping()){
            player.setLoop(true);
            MessageUtils.sendMessage(channel, "Looping: **ON**");
        } else {
            player.setLoop(false);
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
}
