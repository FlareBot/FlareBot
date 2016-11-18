package com.bwfcwalshy.flarebot.music.extractors;

import com.bwfcwalshy.flarebot.FlareBot;
import com.bwfcwalshy.flarebot.MessageUtils;
import com.bwfcwalshy.flarebot.music.Player;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IUser;

import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class YouTubeExtractor implements Extractor {
    public static final String YOUTUBE_URL = "https://www.youtube.com";
    public static final String PLAYLIST_URL = "https://www.youtube.com/playlist?list=";
    public static final String WATCH_URL = "https://www.youtube.com/watch?v=";
    public static String ANY_YT_URL = "(?:https?://)?(?:(?:(?:(?:www\\.)?(?:youtube\\.com))/(?:(?:watch\\?v=([^?&\\n]+)(?:&(?:[^?&\\n]+=(?:[^?&\\n]+)+))?)|(?:playlist\\?list=([^&?]+))(?:&[^&]*=[^&]+)?))|(?:youtu\\.be/(.*)))";
    public static Pattern YT_PATTERN = Pattern.compile(ANY_YT_URL);
    public static final String ANY_PLAYLIST = "https?://(www\\.)?youtube\\.com/playlist\\?list=([0-9A-z+-]*)(&.*=.*)*";
    private static final Pattern MIX_PATTERN = Pattern.compile("(?:https?://)(?:www\\.)youtube\\.com/watch\\?v=(.+)&list=RD(.+)");

    @Override
    public Class<? extends AudioSourceManager> getSourceManagerClass() {
        return YoutubeAudioSourceManager.class;
    }

    @Override
    public void process(String input, Player player, IMessage message, IUser user) throws Exception {
        Document doc = Jsoup.connect(input).get();
        if (doc.title().equals("YouTube")) {
            MessageUtils.editMessage(message, "Unable to retrieve the video/playlist :-(");
            return;
        }
        String title = doc.title().substring(0, doc.title().length() - 10);
        if (input.matches(ANY_PLAYLIST) || isMix(input)) {
            ProcessBuilder bld = new ProcessBuilder("youtube-dl", "-i", "-4", "-J", "--flat-playlist", input);
            Playlist playlist = FlareBot.GSON.fromJson(new InputStreamReader(bld.start().getInputStream()), Playlist.class);
            for (Playlist.PlaylistEntry e : playlist) {
                if (e != null && e.id != null) {
                    try {
                        if(e.title == null) {
                            String title2 = Jsoup.connect(WATCH_URL + e.id).get().title();
                            if(title2.equals("YouTube"))
                                continue;
                            e.title = title2.substring(0, doc.title().length() - 10);
                        }
                        Player.Track track = new Player.Track(player.getTrack(WATCH_URL + e.id));
                        track.getMetadata().put("name", e.title);
                        track.getMetadata().put("id", e.id);
                        player.queue(track);
                    } catch (Exception ignored) {
                    }
                }
            }
            MessageUtils.editMessage(message, user + " added the playlist **" + title + "** to the playlist!");
        } else {
            try {
                Player.Track track = new Player.Track(player.getTrack(input));
                track.getMetadata().put("name", title);
                if (input.contains("&")) input = input.substring(input.indexOf('&'));
                input = input.substring(input.indexOf("?v=") + 3);
                track.getMetadata().put("id", input);
                player.queue(track);
                MessageUtils.editMessage(message, user + " added the video **" + title + "** to the playlist!");
            } catch (FriendlyException e) {
                MessageUtils.editMessage(message, "Could not get the song! YouTube said: " + (e.getMessage().contains("\n") ?
                        e.getMessage().substring(e.getMessage().indexOf('\n')) :
                        e.getMessage()));

            }
        }
    }

    @Override
    public boolean valid(String input) {
        return input.matches(ANY_YT_URL) || isMix(input);
    }

    public boolean isMix(String url) {
        Matcher m = MIX_PATTERN.matcher(url);
        return m.matches() && m.group(1).equals(m.group(2));
    }

    public static class Playlist implements Iterable<Playlist.PlaylistEntry> {
        public List<Playlist.PlaylistEntry> entries = new ArrayList<>();

        @Override
        public Iterator<PlaylistEntry> iterator() {
            return entries.iterator();
        }

        public static class PlaylistEntry {
            public String id;
            public String title;
        }

        public String title;
    }

    private class Song {
        public Long duration;
    }
}
