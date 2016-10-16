package com.bwfcwalshy.flarebot.commands.music;

import com.bwfcwalshy.flarebot.FlareBot;
import com.bwfcwalshy.flarebot.MessageUtils;
import com.bwfcwalshy.flarebot.commands.Command;
import com.bwfcwalshy.flarebot.commands.CommandType;
import com.bwfcwalshy.flarebot.music.MusicManager;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.util.audio.AudioPlayer;

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
                int j = 0;
                StringBuilder sb = new StringBuilder();
                sb.append("**Current Playlist**\n```xl\n");
                for (AudioPlayer.Track track : manager.getPlayers().get(channel.getGuild().getID()).getPlaylist()) {
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
        } else if (args.length >= 1) {
            if (args[0].equals("remove")) {
                if (manager.getPlayers().containsKey(channel.getGuild().getID()) && !manager.getPlayers().get(channel.getGuild().getID()).getPlaylist().isEmpty()) {
                    AudioPlayer player = manager.getPlayers().get(channel.getGuild().getID());
                    if (args.length == 2) {
                        try {
                            int i = Integer.parseInt(args[1]);
                            if (i > player.getPlaylist().size()) {
                                MessageUtils.sendMessage(channel, "There aren't even that many songs in the playlist!");
                            } else {
                                MessageUtils.sendMessage(channel, "Removed **" + player.getPlaylist().get((i - 1)).getMetadata().get("name") + "** from the playlist!");
                                if (i == 1)
                                    player.skip();
                                else
                                    player.getPlaylist().remove((i - 1));
                            }
                        } catch (NumberFormatException e) {
                            MessageUtils.sendMessage(channel, sender.mention() + " Usage: " + FlareBot.COMMAND_CHAR + "playlist remove (#)");
                        }
                    } else {
                        MessageUtils.sendMessage(channel, sender.mention() + " Usage: " + FlareBot.COMMAND_CHAR + "playlist remove (#)");
                    }
                } else {
                    MessageUtils.sendMessage(channel, "There are no songs in the playlist!");
                }
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
