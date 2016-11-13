package com.bwfcwalshy.flarebot.music;

import com.bwfcwalshy.flarebot.FlareBot;
import com.bwfcwalshy.flarebot.MessageUtils;
import com.google.gson.JsonParseException;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sun.jna.ptr.PointerByReference;
import org.apache.commons.io.IOUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.MissingPermissionsException;
import sx.blah.discord.util.RequestBuffer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.regex.Pattern;

public class VideoThread extends Thread {

    private static MusicManager manager;
    public static final ThreadGroup VIDEO_THREADS = new ThreadGroup("Video Threads");
    private String searchTerm;
    private IUser user;
    private IChannel channel;
    private boolean isUrl = false;
    private boolean isShortened = false;
    private List<String> playlist = null;

    public VideoThread(String term, IUser user, IChannel channel) {
        this.searchTerm = term;
        this.user = user;
        this.channel = channel;
        if (manager == null) manager = FlareBot.getInstance().getMusicManager();
        setName("Video Thread " + VIDEO_THREADS.activeCount());
        start();
    }

    public VideoThread(String termOrUrl, IUser user, IChannel channel, boolean url, boolean shortened) {
        this.searchTerm = termOrUrl;
        this.user = user;
        this.channel = channel;
        this.isUrl = url;
        this.isShortened = shortened;
        if (manager == null) manager = FlareBot.getInstance().getMusicManager();
        setName("Video Thread " + VIDEO_THREADS.activeCount());
        start();
    }

    public VideoThread(List<String> playlist, IUser sender, IChannel channel, String name) {
        this.searchTerm = name;
        this.user = sender;
        this.channel = channel;
        this.playlist = playlist;
        start();
    }

    // Making sure these stay across all threads.
    private static final String SEARCH_URL = "https://www.youtube.com/results?search_query=";
    private static final String YOUTUBE_URL = "https://www.youtube.com";
    private static final String PLAYLIST_URL = "https://www.youtube.com/playlist?list=";
    private static final String WATCH_URL = "https://www.youtube.com/watch?v=";
    private static final String EXTENSION = ".opus";
    public static String ANY_YT_URL = "(?:https?://)?(?:(?:(?:(?:www\\.)?(?:youtube\\.com))/(?:(?:watch\\?v=([^?&\\n]+)(?:&(?:[^?&\\n]+=(?:[^?&\\n]+)+))?)|(?:playlist\\?list=([^&?]+))(?:&[^&]*=[^&]+)?))|(?:youtu\\.be/(.*)))";
    public static Pattern YT_PATTERN = Pattern.compile(ANY_YT_URL);
    public static final String ANY_PLAYLIST = "https?://(www\\.)?youtube\\.com/playlist\\?list=([0-9A-z+-]*)(&.*=.*)*";
    private static final long MAX_DURATION = 30;

    private final ConcurrentHashMap<Integer, PointerByReference> encoders = new ConcurrentHashMap<>();

    @Override
    public void run() {
        long a = System.currentTimeMillis();
        // TODO: Severely clean this up!!!
        // ^ EDIT BY Arsen: A space goes there..
        try {
            if (playlist == null) {
                IMessage message;
                String videoName;
                String link;
                String videoId;
                if (!searchTerm.matches(ANY_PLAYLIST)) {
                    if (isUrl) {
                        message = MessageUtils.sendMessage(channel, "Getting video from URL.");
                        if (message == null)
                            return;
                        if (isShortened) {
                            searchTerm = WATCH_URL + searchTerm.replaceFirst("http(s)?://youtu\\.be/", "");
                        }

                        Document doc = Jsoup.connect(searchTerm).get();
                        videoId = searchTerm.replaceFirst("http(s)?://(www\\.)?youtube\\.com/watch\\?v=", "");
                        if(doc.title().equals("YouTube")){
                            MessageUtils.editMessage(message, "The YouTube video provided does not seem to be available." +
                                    "\nSorrry about that :-(");
                            return;
                        }
                        videoName = doc.title().substring(0, doc.title().length() - 10);
                    } else {
                        message = MessageUtils.sendMessage(channel, "Searching YouTube for '" + searchTerm + "'");
                        if (message == null)
                            return;
                        Document doc = Jsoup.connect(SEARCH_URL + URLEncoder.encode(searchTerm, "UTF-8")).get();

                        int i = 0;
                        List<Element> videoElements = doc.getElementsByClass("yt-lockup-title");
                        if (videoElements.isEmpty()) {
                            MessageUtils.editMessage(message, "Could not find any results for **" + searchTerm + "**!");
                            return;
                        }
                        Element videoElement = videoElements.get(i);
                        for (Element e : videoElement.children()) {
                            if (e.toString().contains("href=\"https://googleads")) {
                                videoElement = doc.getElementsByClass("yt-lockup-title").get(++i);
                            } else if (videoElement.select("a").first().attr("href").startsWith("/user/")) {
                                videoElement = doc.getElementsByClass("yt-lockup-title").get(++i);
                            } else if (videoElement.select("a").first().attr("href").startsWith("/channel/")) {
                                videoElement = doc.getElementsByClass("yt-lockup-title").get(++i);
                            } else if (!e.select(".yt-badge-live").isEmpty()) {
                                videoElement = doc.getElementsByClass("yt-lockup-title").get(++i);
                            } else break;
                        }
                        link = videoElement.select("a").first().attr("href");
                        Document doc2 = Jsoup.connect((link.startsWith("http") ? "" : YOUTUBE_URL) + link).get();
                        if(doc.title().equals("YouTube")){
                            MessageUtils.editMessage(message, "The YouTube video provided does not seem to be available." +
                                    "\nSorrry about that :-(");
                            return;
                        }
                        // - YouTube
                        videoName = doc2.title().substring(0, doc2.title().length() - 10);
                        // I check the index of 2 chars so I need to add 2
                        videoId = link.substring(link.indexOf("v=") + 2);
                    }
                    // Playlist
                    if (videoId.contains("&")) videoId = videoId.substring(0, videoId.indexOf("&"));
                    link = WATCH_URL + videoId;
                    try {
                        if (manager.addSong(link, channel.getGuild().getID(), videoName, videoId, null)) {
                            MessageUtils.editMessage(message, user + " added: **" + videoName + "** to the playlist!");
                        } else MessageUtils.editMessage(message, "Failed to add **" + videoName + "**!");
                    } catch (FriendlyException e){
                        MessageUtils.editMessage(message, "Failed to add **" + videoName + "**!\n YouTube said: " + e.getMessage());
                    }
                } else {
                    loadPlaylist(searchTerm);
                }
            } else {
                Playlist p = new Playlist();
                p.title = searchTerm;
                p.entries = new ArrayList<>();
                for (String s : playlist) {
                    Playlist.PlaylistEntry e = p.new PlaylistEntry();
                    Document doc = Jsoup.connect(WATCH_URL + s).get();
                    if(doc.title().equals("YouTube")){
                        continue;
                    }
                    e.id = s;
                    e.title = doc.title().substring(0, doc.title().length() - 10);
                    p.entries.add(e);
                }
                addAll(p, MessageUtils.sendMessage(channel, "Loading in the playlist!"));
            }
        } catch (Exception e) {
            MessageUtils.sendException("Could not add/download songs!", e, channel);
            FlareBot.LOGGER.error("Error occured! searchTerm: " + searchTerm + '\n'
                    + "isShortened: " + isShortened + "\nisUrl: " + isUrl + "\nplaylist: " + playlist + "\nchannel: "
                    + channel + "\nsender: " + user.getName() + '#' + user.getDiscriminator() + " ( " + user + " )", e);
        }
        long b = System.currentTimeMillis();
        FlareBot.LOGGER.debug("Process took " + (b - a) + " milliseconds");
    }

    private void loadPlaylist(String searchTerm) throws IOException, InterruptedException, ExecutionException {
        IMessage message = MessageUtils.sendMessage(channel, "Getting playlist from URL.");
        loadPlaylist(searchTerm, message);
    }

    private void loadPlaylist(String searchTerm, IMessage message) throws IOException, InterruptedException, ExecutionException {
        ProcessBuilder builder = new ProcessBuilder("youtube-dl", "-i", "-4", "--dump-single-json", "--flat-playlist", searchTerm);
        Process process = builder.start();
        Playlist playlist;
        try {
            playlist = FlareBot.GSON.fromJson(new InputStreamReader(process.getInputStream()), Playlist.class);
        } catch (JsonParseException e) {
            RequestBuffer.request(() -> {
                try {
                    message.edit("Could not parse the playlist!");
                } catch (MissingPermissionsException | DiscordException e1) {
                    FlareBot.LOGGER.error("Edit own message!", e1);
                }
            });
            FlareBot.LOGGER.error("Could not parse playlist!", e);
            return;
        }
        process.waitFor();
        if (process.exitValue() != 0) {
            MessageUtils.editMessage(message, "Could not parse the playlist! Sorry about that :-(");
            FlareBot.LOGGER.error("Could not parse playlist <" + searchTerm + '>');
            return;
        }

        addAll(playlist, message);
    }

    private void addAll(Playlist playlist, IMessage message) throws IOException, InterruptedException, ExecutionException {
        int i = 0;
        long time = System.currentTimeMillis();
        for (Playlist.PlaylistEntry e : playlist.entries) {
            i++;
            if (e != null) {
                try {
                    manager.addSong(WATCH_URL + e.id, message.getChannel().getGuild().getID(), e.title, e.id, null);
                } catch (FriendlyException ignored){}
            }
            if (System.currentTimeMillis() - time >= 1000) {
                time = System.currentTimeMillis();
                MessageUtils.editMessage(message, user + " added **" + i + "** out of **" + playlist.entries.size() + "** songs to the queue");
            }
        }
        MessageUtils.editMessage(message, user + " Added the playlist **" + playlist.title + "** to the queue");
    }

    private boolean checkDuration(String link, IMessage message) {
        ProcessBuilder builder = new ProcessBuilder("youtube-dl", "-J", "--no-playlist", link);
        String gson;
        String out = null;
        try {
            Process p = builder.start();
            String err = processInput(p, "YT-DL-Duration", true);
            out = IOUtils.toString(p.getInputStream(), Charset.defaultCharset());
            Song song = FlareBot.GSON.fromJson(out, Song.class);
            if (song == null || song.duration == null) {
                MessageUtils.sendMessage(FlareBot.getInstance().getUpdateChannel(), "Could not check duration for " + link + "!\n"+err);
                MessageUtils.editMessage(message, err);
            }
            return song != null && song.duration < (MAX_DURATION * 60);
        } catch (JsonParseException | IOException e) {
            FlareBot.LOGGER.error("Could not parse song duration" + link + "!\n" + out, e);
            return false;
        }
    }

    private String processInput(Process downloadProcess, String process, boolean err) throws IOException {
        String out = "";
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(err ?
                downloadProcess.getErrorStream() :
                downloadProcess.getInputStream()))) {
            while (downloadProcess.isAlive()) {
                String line;
                if ((line = reader.readLine()) != null) {
                    if (line.contains("[download]"))
                        continue;
                    FlareBot.LOGGER.info("[" + process + "] " + line);
                    out += line;
                }
            }
        }
        return out;
    }

    private class Playlist {
        public List<PlaylistEntry> entries;

        public class PlaylistEntry {
            public String id;
            public String title;
        }

        public String title;
    }

    private class Song {
        public Long duration;
    }
}
