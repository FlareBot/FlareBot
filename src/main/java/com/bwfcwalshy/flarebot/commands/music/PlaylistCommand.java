package com.bwfcwalshy.flarebot.commands.music;

import com.arsenarsen.lavaplayerbridge.PlayerManager;
import com.arsenarsen.lavaplayerbridge.player.Track;
import com.bwfcwalshy.flarebot.FlareBot;
import com.bwfcwalshy.flarebot.MessageUtils;
import com.bwfcwalshy.flarebot.commands.Command;
import com.bwfcwalshy.flarebot.commands.CommandType;
import sun.plugin2.os.windows.FLASHWINFO;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.util.EmbedBuilder;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Queue;

public class PlaylistCommand implements Command {

    private PlayerManager manager;

    public PlaylistCommand(FlareBot flareBot) {
        this.manager = flareBot.getMusicManager();
    }

    @Override
    public void onCommand(IUser sender, IChannel channel, IMessage message, String[] args) {
        System.out.println(args.length);
        System.out.println(args.length < 1 && args.length > 2);
        if (args.length < 1 || args.length > 2) {
            if (!manager.getPlayer(channel.getGuild().getID()).getPlaylist().isEmpty()) {
                List<String> songs = new ArrayList<>();
                int i = 1;
                StringBuilder sb = new StringBuilder();
                Iterator<Track> it = manager.getPlayer(channel.getGuild().getID()).getPlaylist().iterator();
                while (it.hasNext() && songs.size() < 25) {
                    Track next = it.next();
                    String toAppend = String.format("%s. `%s` | Requested by <@!%s>\n", i++,
                            next.getTrack().getInfo().title, next.getMeta().get("requester"));
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
                MessageUtils.sendPM(sender, builder.build());
            } else {
                MessageUtils.sendErrorMessage(MessageUtils.getEmbed().withDesc("No songs in the playlist!"), channel);
            }
        } else {
            if (args.length == 1) {
                if (args[0].equalsIgnoreCase("clear")) {
                    manager.getPlayer(channel.getGuild().getID()).getPlaylist().clear();
                    MessageUtils.sendMessage("Cleared the current playlist!", channel);
                } else if (args[0].equalsIgnoreCase("remove")) {
                    MessageUtils.sendErrorMessage(MessageUtils.getEmbed().withDesc("Usage: " + FlareBot.getPrefix(channel.getGuild().getID()) + "playlist remove (number)"), channel);
                } else {
                    MessageUtils.sendErrorMessage(MessageUtils.getEmbed().withDesc("Incorrect usage! " + getDescription()), channel);
                }
            } else if (args.length == 2) {
                if (args[0].equalsIgnoreCase("remove")) {
                    int number;
                    try {
                        number = Integer.parseInt(args[1]);
                    } catch (NumberFormatException e) {
                        MessageUtils.sendErrorMessage("That is an invalid number!", channel);
                        return;
                    }

                    Queue<Track> queue = manager.getPlayer(channel.getGuild().getID()).getPlaylist();

                    if (number < 1 || number > queue.size()) {
                        MessageUtils.sendErrorMessage("There is no song with that index. Make sure your number is at least 1 and either " + queue.size() + " or below!", channel);
                        return;
                    }

                    List<Track> playlist = new ArrayList<>(queue);
                    playlist.remove(number - 1);
                    queue.clear();
                    queue.addAll(playlist);

                    MessageUtils.sendMessage(MessageUtils.getEmbed(sender).withDesc("Removed number " + number + " from the playlist!").build(), channel);
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
        return "View the songs currently on your playlist. " +
                "NOTE: If too many it shows only the amount that can fit. You can use `playlist clear` to remove all songs.";
    }

    @Override
    public CommandType getType() {
        return CommandType.MUSIC;
    }
}
