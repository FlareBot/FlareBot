package com.bwfcwalshy.flarebot.commands.music;

import com.arsenarsen.lavaplayerbridge.player.Player;
import com.bwfcwalshy.flarebot.FlareBot;
import com.bwfcwalshy.flarebot.MessageUtils;
import com.bwfcwalshy.flarebot.commands.Command;
import com.bwfcwalshy.flarebot.commands.CommandType;
import com.bwfcwalshy.flarebot.commands.FlareBotManager;
import com.sedmelluq.discord.lavaplayer.track.AudioItem;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IUser;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutionException;

public class RandomCommand implements Command {

    private FlareBotManager manager = FlareBotManager.getInstance();

    private Set<String> loadedSongs = new HashSet<>();

    @Override
    public void onCommand(IUser sender, IChannel channel, IMessage message, String[] args) {
        if (args.length != 1) {
            loadSongs(25, channel);
        } else {
            int amount;
            try {
                amount = Integer.parseInt(args[0]);
            } catch(NumberFormatException e) {
                MessageUtils.sendErrorMessage(MessageUtils.getEmbed(sender).withDesc("Invalid amount!"), channel);
                return;
            }
            loadSongs(amount, channel);
        }
    }

    private void loadSongs(int amount, IChannel channel){
        Set<String> songs = manager.getRandomSongs(amount, channel);
        Player player = FlareBot.getInstance().getMusicManager().getPlayer(channel.getGuild().getID());
        for(String id : songs){
            AudioItem item = null;
            try {
                item = player.resolve(id);
            } catch (InterruptedException | ExecutionException e) {
                FlareBot.LOGGER.error("Error loading song!", e);
            }

            if(item != null){
                player.queue((AudioTrack) item);
            }else{
                MessageUtils.sendErrorMessage(MessageUtils.getEmbed().withDesc("Make sure you have at least a song in the playlist first!"), channel);
                return;
            }
        }
        MessageUtils.sendMessage(MessageUtils.getEmbed().withDesc("Added " + amount + " random songs to the playlist!").build(), channel);
    }

    @Override
    public String getCommand() {
        return "random";
    }

    @Override
    public String getDescription() {
        return "Put random songs into your playlist.";
    }

    @Override
    public CommandType getType() {
        return CommandType.MUSIC;
    }
}
