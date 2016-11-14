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

public class PlaylistCommand implements Command {

    private MusicManager manager;

    public PlaylistCommand(FlareBot flareBot) {
        this.manager = flareBot.getMusicManager();
    }

    @Override
    public void onCommand(IUser sender, IChannel channel, IMessage message, String[] args) {
        if (args.length == 0) {
            if (manager.getPlayers().containsKey(channel.getGuild().getID()) && !manager.getPlayers().get(channel.getGuild().getID()).getPlaylist().isEmpty()) {
                int i = 1;
                StringBuilder sb = new StringBuilder();
                sb.append("**Current Playlist**\n```fix\n");
                for (Player.Track track : manager.getPlayers().get(channel.getGuild().getID()).getPlaylist()) {
                    if (new StringBuilder(sb).append(i).append(". ").append(track.getMetadata().get("name")).append("\n").length() >= 1997)
                        break;
                    sb.append(i).append(". ").append(track.getMetadata().get("name")).append("\n");
                    i++;
                }
                sb.append("```");
                MessageUtils.sendMessage(channel, sb.toString());
            } else {
                MessageUtils.sendMessage(channel, "No songs in the playlist!");
            }
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
