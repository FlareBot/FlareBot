package com.bwfcwalshy.flarebot.commands.music;

import com.arsenarsen.lavaplayerbridge.player.Player;
import com.bwfcwalshy.flarebot.FlareBot;
import com.bwfcwalshy.flarebot.MessageUtils;
import com.bwfcwalshy.flarebot.commands.Command;
import com.bwfcwalshy.flarebot.commands.CommandType;
import com.bwfcwalshy.flarebot.commands.RandomSongManager;
import com.bwfcwalshy.flarebot.music.VideoThread;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IUser;

import java.util.Set;
import java.util.stream.Collectors;

public class RandomCommand implements Command {

    private RandomSongManager manager = RandomSongManager.getInstance();

    @Override
    public void onCommand(IUser sender, IChannel channel, IMessage message, String[] args) {
        if (args.length != 1) {
            loadSongs(25, channel, sender);
        } else {
            int amount;
            try {
                amount = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                MessageUtils.sendErrorMessage(MessageUtils.getEmbed(sender).withDesc("Invalid amount!"), channel);
                return;
            }
            if (amount <= 0)
                MessageUtils.sendErrorMessage(MessageUtils.getEmbed(sender).withDesc("Invalid amount!"), channel);
            loadSongs(amount, channel, sender);
        }
    }

    private void loadSongs(int amount, IChannel channel, IUser sender) {
        Set<String> songs = manager.getRandomSongs(amount, channel);
        Player player = FlareBot.getInstance().getMusicManager().getPlayer(channel.getGuild().getID());
        VideoThread.getThread(songs.stream()
                .collect(Collectors.joining(",")), channel, sender).start();
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
