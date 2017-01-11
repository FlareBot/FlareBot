package com.bwfcwalshy.flarebot.commands.music;

import com.arsenarsen.lavaplayerbridge.PlayerManager;
import com.arsenarsen.lavaplayerbridge.player.Track;
import com.bwfcwalshy.flarebot.FlareBot;
import com.bwfcwalshy.flarebot.MessageUtils;
import com.bwfcwalshy.flarebot.commands.Command;
import com.bwfcwalshy.flarebot.commands.CommandType;
import com.bwfcwalshy.flarebot.music.extractors.YouTubeExtractor;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IUser;

public class SongCommand implements Command {

    private PlayerManager manager;

    public SongCommand(FlareBot bot) {
        this.manager = bot.getMusicManager();
    }

    @Override
    public void onCommand(IUser sender, IChannel channel, IMessage message, String[] args) {
        if (manager.getPlayer(channel.getGuild().getID()).getPlayingTrack() != null) {
            Track track = manager.getPlayer(channel.getGuild().getID()).getPlayingTrack();
            MessageUtils.sendMessage(MessageUtils.getEmbed(sender)
                    .appendField("Current song: ", getLink(track), false)
                    .appendField("Amount Played: ",
                            (int) (100f / track.getTrack().getDuration() * track.getTrack().getPosition()) + "%", true)
                    .appendField("Requested by:", String.format("<@!%s>", track.getMeta().get("requester")), false), channel);
        } else {
            MessageUtils.sendMessage(MessageUtils.getEmbed(sender)
                    .appendField("Current song: ", "**No song playing right now!**", false), channel);
        }
    }

    public static String getLink(Track track) {
        String name = String.valueOf(track.getTrack().getInfo().title);
        String link = YouTubeExtractor.WATCH_URL + track.getTrack().getIdentifier();
        return String.format("[`%s`](%s)", name, link);
    }

    @Override
    public String getCommand() {
        return "song";
    }

    @Override
    public String getDescription() {
        return "Get the current song playing.";
    }

    @Override
    public CommandType getType() {
        return CommandType.MUSIC;
    }
}
