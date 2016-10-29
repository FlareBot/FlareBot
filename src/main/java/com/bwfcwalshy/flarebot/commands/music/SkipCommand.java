package com.bwfcwalshy.flarebot.commands.music;

import com.bwfcwalshy.flarebot.FlareBot;
import com.bwfcwalshy.flarebot.MessageUtils;
import com.bwfcwalshy.flarebot.commands.Command;
import com.bwfcwalshy.flarebot.commands.CommandType;
import com.bwfcwalshy.flarebot.music.MusicManager;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IUser;

public class SkipCommand implements Command {

    private MusicManager musicManager;
    public SkipCommand(FlareBot bot){
        this.musicManager = bot.getMusicManager();
    }

    @Override
    public void onCommand(IUser sender, IChannel channel, IMessage message, String[] args) {
        if(args.length == 1){
            String arg = args[0];
            try {
                int num = Integer.parseInt(arg);
                musicManager.skip(channel.getGuild().getID(), num);
            } catch (NumberFormatException e){
                MessageUtils.sendMessage(channel, "Must be a number!");
            }
            return;
        }
        musicManager.skip(channel.getGuild().getID());
    }

    @Override
    public String getCommand() {
        return "skip";
    }

    @Override
    public String getDescription() {
        return "Skip the current song playing.";
    }

    @Override
    public CommandType getType() { return CommandType.MUSIC; }
}
