package com.bwfcwalshy.flarebot.commands.music;

import com.arsenarsen.lavaplayerbridge.PlayerManager;
import com.arsenarsen.lavaplayerbridge.player.Track;
import com.bwfcwalshy.flarebot.FlareBot;
import com.bwfcwalshy.flarebot.MessageUtils;
import com.bwfcwalshy.flarebot.commands.Command;
import com.bwfcwalshy.flarebot.commands.CommandType;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.util.EmbedBuilder;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class PlaylistCommand implements Command {

    private PlayerManager manager;

    public PlaylistCommand(FlareBot flareBot) {
        this.manager = flareBot.getMusicManager();
    }

    @Override
    public void onCommand(IUser sender, IChannel channel, IMessage message, String[] args) {
        if (!manager.getPlayer(channel.getGuild().getID()).getPlaylist().isEmpty()) {
            List<String> songs = new ArrayList<>();
            int i = 1;
            StringBuilder sb = new StringBuilder();
            Iterator<Track> it = manager.getPlayer(channel.getGuild().getID()).getPlaylist().iterator();
            while (it.hasNext() && songs.size() < 25) {
                Track next = it.next();
                String toAppend = String.format("%s. %s\n", i++, next.getTrack().getInfo().title);
                if (sb.length() + toAppend.length() > 1024) {
                    songs.add(sb.toString());
                    sb = new StringBuilder();
                }
                sb.append(toAppend);
            }
            songs.add(sb.toString());
            EmbedBuilder builder = MessageUtils.getEmbed(sender);
            i = 1;
            for (String s : songs) {
                builder.appendField("Page " + i++, s, false);
            }
            MessageUtils.sendMessage(builder.build(), channel);
        } else {
            MessageUtils.sendMessage(channel, "No songs in the playlist!");
        }
    }

    @Override
    public String getCommand() {
        return "playlist";
    }

    @Override
    public String getDescription() {
        return "View the songs currently on your playlist. NOTE: If too many it shows only the amount that can fit";
    }

    @Override
    public CommandType getType() {
        return CommandType.MUSIC;
    }
}
