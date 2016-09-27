package com.bwfcwalshy.flarebot.commands.music;

import com.bwfcwalshy.flarebot.FlareBot;
import com.bwfcwalshy.flarebot.MessageUtils;
import com.bwfcwalshy.flarebot.commands.Command;
import com.bwfcwalshy.flarebot.commands.CommandType;
import com.bwfcwalshy.flarebot.music.MusicManager;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IUser;

public class VolumeCommand implements Command {

    private MusicManager musicManager;
    public VolumeCommand(FlareBot bot){
        this.musicManager = bot.getMusicManager();
    }

    @Override
    public void onCommand(IUser sender, IChannel channel, IMessage message, String[] args) {
        if(args.length == 0){
            MessageUtils.sendMessage(channel, sender.mention() + " Usage: " + FlareBot.COMMAND_CHAR + "volume " + "<volume>");
        }else{
            try{
                int volume = Integer.parseInt(args[0]);
                if(volume >= 0 && volume <= 100){
                    musicManager.setVolume(channel.getGuild().getID(), volume);
                }else{
                    MessageUtils.sendMessage(channel, sender.mention() + " The volume must be between 0-100");
                }
            }catch(NumberFormatException e){
                MessageUtils.sendMessage(channel, sender.mention() + " That is not a number!");
            }
        }
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

    @Override
    public String getPermission(){
        return "flarebot.volume";
    }
}
