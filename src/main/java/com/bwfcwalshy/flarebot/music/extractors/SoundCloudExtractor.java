package com.bwfcwalshy.flarebot.music.extractors;

import com.bwfcwalshy.flarebot.FlareBot;
import com.bwfcwalshy.flarebot.MessageUtils;
import com.bwfcwalshy.flarebot.music.Player;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.soundcloud.SoundCloudAudioSourceManager;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IUser;

import java.io.InputStreamReader;
import java.util.Iterator;
import java.util.List;

public class SoundCloudExtractor implements Extractor {
    // Taken from sedmelluq and lavaplayer
    private static final String TRACK_URL_REGEX = "^(?:http://|https://|)(?:www\\.|)soundcloud\\.com/([a-zA-Z0-9-_]+)/([a-zA-Z0-9-_]+)(?:\\?.*|)$";
    private static final String PLAYLIST_URL_REGEX = "^(?:http://|https://|)(?:www\\.|)soundcloud\\.com/([a-zA-Z0-9-_]+)/sets/([a-zA-Z0-9-_]+)(?:\\?.*|)$";

    @Override
    public Class<? extends AudioSourceManager> getSourceManagerClass() {
        return SoundCloudAudioSourceManager.class;
    }

    @Override
    public void process(String input, Player player, IMessage message, IUser user) throws Exception {
        if (input.matches(PLAYLIST_URL_REGEX)) {
            ProcessBuilder bld = new ProcessBuilder("youtube-dl", "--flat-playlist", "-J", input);
            Process pr = bld.start();
            Playlist p = FlareBot.GSON.fromJson(new InputStreamReader(pr.getInputStream()), Playlist.class);
            for (Playlist.PlaylistEntry e : p) {
                try {
                    processUrl(e.url, player, null, null);
                } catch (Exception ignored) {
                }
            }
            MessageUtils.editMessage(message, user + " added the playlist **" + p.title + "** to the playlist!");
        } else {
            processUrl(input, player, message, user);
        }
    }

    private void processUrl(String url, Player player, IMessage message, IUser user) throws Exception {
        Document document = Jsoup.connect(url).get();
        String title = document.title().substring(0, document.title().lastIndexOf('|') - 1);
        Player.Track track = new Player.Track(player.getTrack(url));
        track.getMetadata().put("name", title);
        player.queue(track);
        if(message != null && user != null)
            MessageUtils.editMessage(message, user + " added **" + title + "** to the playlist!");
    }

    @Override
    public boolean valid(String input) {
        return input.matches(TRACK_URL_REGEX) || input.matches(PLAYLIST_URL_REGEX);
    }

    public static class Playlist implements Iterable<Playlist.PlaylistEntry> {
        public String title;
        public List<PlaylistEntry> entries;

        @Override
        public Iterator<PlaylistEntry> iterator() {
            return entries.iterator();
        }

        public static class PlaylistEntry {
            public String url;
        }
    }
}
