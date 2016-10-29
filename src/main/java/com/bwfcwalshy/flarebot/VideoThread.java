package com.bwfcwalshy.flarebot;

import com.bwfcwalshy.flarebot.music.MusicManager;
import com.google.gson.JsonParseException;
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
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class VideoThread extends Thread {

    private static MusicManager manager;
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
        start();
    }

    public VideoThread(String termOrUrl, IUser user, IChannel channel, boolean url, boolean shortened) {
        this.searchTerm = termOrUrl;
        this.user = user;
        this.channel = channel;
        this.isUrl = url;
        this.isShortened = shortened;
        if (manager == null) manager = FlareBot.getInstance().getMusicManager();
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
    private static final String WATCH_URL = "https://www.youtube.com/watch?v=";
    private static final String EXTENSION = ".mp3";
    public static String ANY_YT_URL = "(?:https?://)?(?:(?:(?:(?:www\\.)?(?:youtube\\.com))/(?:(?:watch\\?v=(.+)(?:&list=.+)?(?:&index=.+)?)|(?:playlist\\?list=(.+))))|(?:youtu\\.be/(.*)))";
    public static Pattern YT_PATTERN = Pattern.compile(ANY_YT_URL);
    private static final long MAX_DURATION = 30;

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
                if (!searchTerm.matches("https?://(www\\.)?youtube\\.com\\/playlist\\?list=([0-9A-z+-]*)(&.*=.*)*")) {
                    if (isUrl) {
                        message = MessageUtils.sendMessage(channel, "Getting video from URL.");
                        if (isShortened) {
                            searchTerm = WATCH_URL + searchTerm.replaceFirst("http(s)?://youtu\\.be", "");
                        }

                        Document doc = Jsoup.connect(searchTerm).get();
                        videoId = searchTerm.replaceFirst("http(s)?://(www\\.)?youtube\\.com/watch\\?v=", "");
                        // Playlist
                        if (videoId.contains("&list")) videoId = videoId.substring(0, videoId.indexOf("&list"));
                        if (videoId.contains("&index")) videoId = videoId.substring(0, videoId.indexOf("&index"));
                        if (videoId.contains("&app")) videoId = videoId.substring(0, videoId.indexOf("&app"));
                        videoName = MessageUtils.escapeFile(doc.title().substring(0, doc.title().length() - 10));
                        link = WATCH_URL + videoId;
                    } else {
                        message = MessageUtils.sendMessage(channel, "Searching YouTube for '" + searchTerm + "'");
                        Document doc = Jsoup.connect(SEARCH_URL + URLEncoder.encode(searchTerm, "UTF-8")).get();

                        int i = 0;
                        Element videoElement = doc.getElementsByClass("yt-lockup-title").get(i);
                        boolean hasAd = true;
                        while (hasAd) {
                            for (Element e : videoElement.children()) {
                                if (e.toString().contains("href=\"https://googleads")) {
                                    videoElement = doc.getElementsByClass("yt-lockup-title").get(++i);
                                    break;
                                }
                            }
                            hasAd = false;
                        }
                        link = videoElement.select("a").first().attr("href");
                        Document doc2 = Jsoup.connect((link.startsWith("http") ? "" : YOUTUBE_URL) + link).get();
                        videoName = MessageUtils.escapeFile(doc2.title().substring(0, doc2.title().length() - 10));
                        // I check the index of 2 chars so I need to add 2
                        videoId = link.substring(link.indexOf("v=") + 2);

                        link = YOUTUBE_URL + link;
                    }
                    File video = new File("cached" + File.separator + videoId + EXTENSION);
                    if (!video.exists()) {
                        MessageUtils.editMessage(message, "Downloading video!");
                        if (!checkDuration(link)) {
                            MessageUtils.editMessage(message, "That song is over **" + MAX_DURATION + " minute(s)!**");
                            return;
                        }
                        ProcessBuilder builder = new ProcessBuilder("youtube-dl", "-o",
                                "cached" + File.separator + "%(id)s.%(ext)s",
                                "--extract-audio", "--audio-format"
                                , "mp3", link);
                        FlareBot.LOGGER.debug("Downloading");
                        builder.redirectErrorStream(true);
                        Process process = builder.start();
                        processInput(process);
                        process.waitFor();
                        if (process.exitValue() != 0) {
                            MessageUtils.editMessage(message, "Could not download **" + videoName + "**!");
                            return;
                        }
                    }
                    if (manager.addSong(channel.getGuild().getID(), video.getName(), videoName, EXTENSION)) {
                        MessageUtils.editMessage(message, user + " added: **" + videoName + "** to the playlist!");
                    } else MessageUtils.editMessage(message, "Failed to add **" + videoName + "**!");
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
                    e.id = s;
                    e.title = doc.title().substring(0, doc.title().length() - 10);
                    p.entries.add(e);
                }
                addAll(p, MessageUtils.sendMessage(channel, "Loading in the playlist!"));
            }
        } catch (Exception e) {
            MessageUtils.sendException("Could not add/download songs!", e, channel);
            FlareBot.LOGGER.error("Error occured! searchTerm: " + searchTerm + '\n'
                    + "isShortened: " + isShortened + "\nisUrl" + isUrl + "\nplaylist: " + playlist, e);
        }
        long b = System.currentTimeMillis();
        FlareBot.LOGGER.debug("Process took " + (b - a) + " milliseconds");
    }

    private void loadPlaylist(String searchTerm) throws IOException, InterruptedException {
        IMessage message = MessageUtils.sendMessage(channel, "Getting playlist from URL.");
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

    private void addAll(Playlist playlist, IMessage message) throws IOException, InterruptedException {
        int i = 0;
        long time = System.currentTimeMillis();
        for (Playlist.PlaylistEntry e : playlist.entries) {
            if (e != null) {
                if (!new File("cached" + File.separator + e.id + EXTENSION).exists()) {
                    if (!checkDuration(WATCH_URL + e.id)) {
                        continue;
                    }
                    ProcessBuilder entryDownload = new ProcessBuilder("youtube-dl", "-o",
                            "cached" + File.separator + "%(id)s.%(ext)s",
                            "--extract-audio", "--audio-format"
                            , "mp3", WATCH_URL + e.id);
                    entryDownload.redirectErrorStream(true);
                    Process downloadProcess = entryDownload.start();
                    processInput(downloadProcess);
                    downloadProcess.waitFor();
                    if (downloadProcess.exitValue() != 0)
                        continue;
                }
                FlareBot.getInstance().getMusicManager().addSong(message.getChannel().getGuild().getID(), e.id + EXTENSION, e.title, EXTENSION);
            }
            if (System.currentTimeMillis() - time >= 1000) {
                time = System.currentTimeMillis();
                MessageUtils.editMessage(message, user + " added **" + i + "** out of **" + playlist.entries.size() + "** songs to the queue");
            }
        }
        MessageUtils.editMessage(message, user + " Added the playlist **" + playlist.title + "** to the queue");
    }

    private boolean checkDuration(String link) {
        ProcessBuilder builder = new ProcessBuilder("youtube-dl", "-J", link);
        try {
            Process p = builder.start();
            Song song = FlareBot.GSON.fromJson(new InputStreamReader(p.getInputStream()), Song.class);
            return song != null && song.duration < (MAX_DURATION * 60);
        } catch (JsonParseException | IOException e) {
            return false;
        }
    }

    private void processInput(Process downloadProcess) throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(downloadProcess.getInputStream()))) {
            while (downloadProcess.isAlive()) {
                String line;
                if ((line = reader.readLine()) != null) {
                    FlareBot.LOGGER.info("[YT-DL] " + line);
                }
            }
        }
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
        public long duration;
    }
}
