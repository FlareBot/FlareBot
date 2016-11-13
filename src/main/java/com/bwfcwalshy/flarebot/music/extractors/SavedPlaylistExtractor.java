package com.bwfcwalshy.flarebot.music.extractors;

import com.bwfcwalshy.flarebot.music.Player;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
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
                doc = Jsoup.connect(YouTubeExtractor.WATCH_URL + s).get();
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
                Player.Track track = new Player.Track(player.getTrack(YouTubeExtractor.WATCH_URL + e.id));
                track.getMetadata().put("name", e.title);
                track.getMetadata().put("id", e.id);
                try {
                    player.queue(track);
                } catch (FriendlyException ignored) {
                }
            }
        }
    }

    @Override
    public boolean valid(String input) {
        return input.matches(".+\u200B([^,]+,)*[^,]+");
    }
}
