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
        YouTubeExtractor.Playlist playlist = new YouTubeExtractor.Playlist();
        playlist.title = input.substring(0, input.indexOf('\u200B'));
        input = input.substring(input.indexOf('\u200B') + 1);
        for (String s : input.split(",")) {
            String url = YouTubeExtractor.WATCH_URL + s;
            Document doc;
            try {
                doc = Jsoup.connect(url).get();
            } catch (Exception e) {
                continue;
            }
            if (doc.title().equals("YouTube")) {
                continue;
            }
            YouTubeExtractor.Playlist.PlaylistEntry e = new YouTubeExtractor.Playlist.PlaylistEntry();
            e.id = s;
            e.title = doc.title().substring(0, doc.title().length() - 10);
            playlist.entries.add(e);
        }
        for (YouTubeExtractor.Playlist.PlaylistEntry e : playlist) {
            if (e != null && e.id != null && e.title != null) {
                Track track;
                try {
                    track = new Track((AudioTrack) player.resolve(YouTubeExtractor.WATCH_URL + e.id));
                    track.getMeta().put("name", e.title);
                    track.getMeta().put("id", e.id);
                    player.queue(track);
                } catch (FriendlyException ignored) {
                }
            }
        }
        MessageUtils.editMessage("", MessageUtils.getEmbed(user)
                .withDesc(String.format("*Loaded %s songs!*", playlist.entries.size())).build(), message);
    }

    @Override
    public boolean valid(String input) {
        return input.matches(".+\u200B([^,]+,)*[^,]+");
    }
}
