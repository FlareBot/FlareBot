package com.bwfcwalshy.flarebot.commands.music;

import com.arsenarsen.lavaplayerbridge.PlayerManager;
import com.bwfcwalshy.flarebot.FlareBot;
import com.bwfcwalshy.flarebot.commands.Command;
import com.bwfcwalshy.flarebot.commands.CommandType;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IUser;

public class StopCommand implements Command {

    private PlayerManager musicManager;
    public StopCommand(FlareBot bot){
        this.musicManager = bot.getMusicManager();
    }

    @Override
    public void onCommand(IUser sender, IChannel channel, IMessage message, String[] args) {
        musicManager.getPlayer(channel.getGuild().getID()).stop();
    }

    @Override
    public String getCommand() {
        return "stop";
    }

    @Override
    public String getDescription() {
        return "Stops your playlist.";
    }

    @Override
    public CommandType getType() { return CommandType.MUSIC; }

    @Override
    public String getPermission() {
        return "flarebot.stop";
    }
}
