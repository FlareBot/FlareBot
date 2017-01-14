package com.bwfcwalshy.flarebot.music.extractors;

import com.arsenarsen.lavaplayerbridge.player.Player;
import com.arsenarsen.lavaplayerbridge.player.Track;
import com.bwfcwalshy.flarebot.MessageUtils;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IUser;

public class SavedPlaylistExtractor implements Extractor {
    @Override
    public Class<? extends AudioSourceManager> getSourceManagerClass() {
        return YoutubeAudioSourceManager.class;
    }

    @Override
    public void process(String input, Player player, IMessage message, IUser user) throws Exception {
        String name = input.substring(0, input.indexOf('\u200B'));
        input = input.substring(input.indexOf('\u200B') + 1);
        int i = 0;
        for (String s : input.split(",")) {
            String url = YouTubeExtractor.WATCH_URL + s;
            Document doc;
            try {
                doc = Jsoup.connect(url).get();
            } catch (Exception e) {
                continue;
            }
            if (!doc.title().endsWith("YouTube") || doc.title().equals("YouTube")) {
                continue;
            }
            try {
                Track track = new Track((AudioTrack) player.resolve(url));
                track.getMeta().put("requester", user.getID());
                track.getMeta().put("guildId", player.getGuildId());
                player.queue(track);
                i++;
            } catch (FriendlyException ignored) {
            }
        }
        MessageUtils.editMessage("", MessageUtils.getEmbed(user)
                .withDesc(String.format("*Loaded %s songs!*", i)), message);
    }

    @Override
    public boolean valid(String input) {
        return input.matches(".+\u200B([^,]{11},)*[^,]{11}");
    }
}
