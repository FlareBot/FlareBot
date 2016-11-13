package com.bwfcwalshy.flarebot.commands.music;

import com.bwfcwalshy.flarebot.FlareBot;
import com.bwfcwalshy.flarebot.MessageUtils;
import com.bwfcwalshy.flarebot.commands.Command;
import com.bwfcwalshy.flarebot.commands.CommandType;
import com.bwfcwalshy.flarebot.music.Player;
import com.bwfcwalshy.flarebot.music.MusicManager;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IUser;

public class SkipCommand implements Command {

    private MusicManager musicManager;

    public SkipCommand(FlareBot bot) {
        this.musicManager = bot.getMusicManager();
    }

    @Override
    public void onCommand(IUser sender, IChannel channel, IMessage message, String[] args) {
        if(channel.getGuild().getConnectedVoiceChannel() == null){
            MessageUtils.sendMessage(channel, "I am not streaming!");
            return;
        }
        if(!sender.getConnectedVoiceChannels().contains(channel.getGuild().getConnectedVoiceChannel())){
            MessageUtils.sendMessage(channel, "You must be in the channel in order to skip songs!");
        }
        if (args.length == 1) {
            String arg = args[0];
            if (arg.startsWith("#")) {
                arg = arg.substring(1);
                try {
                    int num = Integer.parseInt(arg) - 1;
                    Player pl = musicManager.getPlayers().get(channel.getGuild().getID());
                    if (pl != null && pl.getPlaylistSize() > num && num >= 0) {
                        int i = 0;
                        for(Player.Track t : pl.getPlaylist()){
                            if(++i == num){
                                pl.getPlaylist().remove(t);
                                break;
                            }
                        }
                    }
                } catch (NumberFormatException e) {
                    MessageUtils.sendMessage(channel, "Must be a number!");
                }
            } else
                try {
                    int num = Integer.parseInt(arg);
                    musicManager.skip(channel.getGuild().getID(), num);
                } catch (NumberFormatException e) {
                    MessageUtils.sendMessage(channel, "Must be a number!");
                }
        } else if (args.length == 0) {
            musicManager.skip(channel.getGuild().getID());
        } else MessageUtils.sendMessage(channel, "Incorrect usage! " + getDescription());
    }

    @Override
    public String getCommand() {
        return "skip";
    }

    @Override
    public String getDescription() {
        return "Skip the current song playing, or if specified #NUMBER for the song under NUMBER or just NUMBER for next NUMBER songs.";
    }

    @Override
    public CommandType getType() {
        return CommandType.MUSIC;
    }

    @Override
    public String getPermission() {
        return "flarebot.skip";
    }
}
