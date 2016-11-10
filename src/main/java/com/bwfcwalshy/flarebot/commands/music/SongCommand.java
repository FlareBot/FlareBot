package com.bwfcwalshy.flarebot.commands.music;

import com.bwfcwalshy.flarebot.FlareBot;
import com.bwfcwalshy.flarebot.MessageUtils;
import com.bwfcwalshy.flarebot.commands.Command;
import com.bwfcwalshy.flarebot.commands.CommandType;
import com.bwfcwalshy.flarebot.music.MusicManager;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IUser;

public class SongCommand implements Command {

    private MusicManager manager;
    public SongCommand(FlareBot bot){
        this.manager = bot.getMusicManager();
    }

    @Override
    public void onCommand(IUser sender, IChannel channel, IMessage message, String[] args) {
        if(manager.getPlayers().containsKey(channel.getGuild().getID()) && !manager.getPlayers().get(channel.getGuild().getID()).getPlaylist().isEmpty()){
            MessageUtils.sendMessage(channel, sender.mention() + " The song currently playing is: **"
                    + manager.getPlayers().get(channel.getGuild().getID()).getCurrentTrack().getMetadata().get("name") + "**");
            System.out.println();
        }else{
            MessageUtils.sendMessage(channel, sender.mention() + " There is no song currently playing!");
        }
    }

    @Override
    public String getCommand() {
        return "song";
    }

    @Override
    public String getDescription() {
        return "Get the current song playing.";
    }

    @Override
    public CommandType getType() { return CommandType.MUSIC; }
}
