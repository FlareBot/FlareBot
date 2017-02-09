package com.bwfcwalshy.flarebot.commands.music;

import com.arsenarsen.lavaplayerbridge.PlayerManager;
import com.arsenarsen.lavaplayerbridge.player.Track;
import com.bwfcwalshy.flarebot.FlareBot;
import com.bwfcwalshy.flarebot.MessageUtils;
import com.bwfcwalshy.flarebot.commands.Command;
import com.bwfcwalshy.flarebot.commands.CommandType;
import com.bwfcwalshy.flarebot.music.extractors.YouTubeExtractor;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;

public class SongCommand implements Command {

    private PlayerManager manager;

    public SongCommand(FlareBot bot) {
        this.manager = bot.getMusicManager();
    }

    @Override
    public void onCommand(User sender, TextChannel channel, Message message, String[] args, Member member) {
        if (manager.getPlayer(channel.getGuild().getId()).getPlayingTrack() != null) {
            Track track = manager.getPlayer(channel.getGuild().getId()).getPlayingTrack();
            channel.sendMessage(MessageUtils.getEmbed(sender)
                    .addField("Current song: ", getLink(track), false)
                    .addField("Amount Played: ",
                            (int) (100f / track.getTrack().getDuration() * track.getTrack().getPosition()) + "%", true)
                    .addField("Requested by:", String.format("<@!%s>", track.getMeta().get("requester")), false).build()).queue();
        } else {
            channel.sendMessage(MessageUtils.getEmbed(sender)
                    .addField("Current song: ", "**No song playing right now!**", false).build()).queue();
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
