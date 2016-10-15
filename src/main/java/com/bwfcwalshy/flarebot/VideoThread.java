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
import java.util.List;

public class VideoThread extends Thread {

    // Keep this instance across all threads. Efficiency bitch!
    private static MusicManager manager;

    private String searchTerm;
    private IUser user;
    private IChannel channel;
    private boolean isUrl = false;
    private boolean isShortened = false;

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

    // Making sure these stay across all threads.
    private static final String SEARCH_URL = "https://www.youtube.com/results?search_query=";
    private static final String YOUTUBE_URL = "https://www.youtube.com";
    private static final String EXTENSION = ".mp3";

    @Override
    public void run() {
        long a = System.currentTimeMillis();
        // TODO: Severely clean this up!!!
        // ^ EDIT BY Arsen: A space goes there..
        try {
            IMessage message;
            String videoName;
            String link;
            String videoId;
            if (!searchTerm.matches("https?://(www\\.)?youtube.com/playlist\\?list=([0-9A-z+-]*)")) {
                if (isUrl) {
                    message = MessageUtils.sendMessage(channel, "Getting video from URL.");
                    if (isShortened) {
                        searchTerm = YOUTUBE_URL + searchTerm.replaceFirst("http(s)?://youtu\\.be", "");
                    }

                    Document doc = Jsoup.connect(searchTerm).get();
                    videoId = searchTerm.replaceFirst("http(s)?://(www\\.)?youtube\\.com/watch\\?v=", "");
                    // Playlist
                    if (videoId.contains("&list")) videoId.substring(0, videoId.indexOf("&list") + 5);
                    videoName = MessageUtils.escapeFile(doc.title().replace(" - YouTube", ""));
                    link = searchTerm;
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
//            if (video.exists()) {
//                manager.addSong(channel.getGuild().getID(), videoFile + EXTENSION);
//                RequestBuffer.request(() -> {
//                    try {
//                        message.edit(user.mention() + " added: **" + videoName + "** to the playlist!");
//                    } catch (MissingPermissionsException | DiscordException e) {
//                        FlareBot.LOGGER.error("Could not edit own message!", e);
//                    }
//                });
                if (!video.exists()) {
                    RequestBuffer.request(() -> {
                        try {
                            message.edit("Downloading video!");
                        } catch (MissingPermissionsException | DiscordException e) {
                            FlareBot.LOGGER.error("Could not edit own message!", e);
                        }
                    });
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
                        RequestBuffer.request(() -> {
                            try {
                                message.edit("Could not download **" + videoName + "**!");
                            } catch (MissingPermissionsException | DiscordException e) {
                                FlareBot.LOGGER.error("Could not edit own message!", e);
                            }
                        });
                        return;
                    }
                }
                if (manager.addSong(channel.getGuild().getID(), video.getName(), videoName)) {
                    RequestBuffer.request(() -> {
                        try {
                            message.edit(user.mention() + " added: **" + videoName + "** to the playlist!");
                        } catch (MissingPermissionsException | DiscordException e) {
                            FlareBot.LOGGER.error("Could not edit own message!", e);
                        }
                    });
                } else RequestBuffer.request(() -> {
                    try {
                        message.edit("Failed to add **" + videoName + "**!");
                    } catch (MissingPermissionsException | DiscordException e) {
                        FlareBot.LOGGER.error("Could not edit own message!", e);
                    }
                });
            } else {
                message = MessageUtils.sendMessage(channel, "Getting playlist from URL.");
                ProcessBuilder builder = new ProcessBuilder("youtube-dl", "-i", "-4", "--dump-single-json", searchTerm);
                searchTerm = searchTerm.replaceFirst("https?://(www\\.)?youtube.com/playlist\\?list=", "");
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
                RequestBuffer.request(() -> {
                    try {
                        message.edit("Downloading **" + playlist.title + "**");
                    } catch (MissingPermissionsException | DiscordException e1) {
                        FlareBot.LOGGER.error("Edit own message!", e1);
                    }
                });
                for (Playlist.PlaylistEntry e : playlist.entries) {
                    if(!new File("cached" + File.separator + e.id + EXTENSION).exists()){
                        ProcessBuilder entryDownload = new ProcessBuilder("youtube-dl", "-o",
                                "cached" + File.separator + "%(id)s.%(ext)s",
                                "--extract-audio", "--audio-format"
                                , "mp3", YOUTUBE_URL + "/watch?v="+e.id);
                        FlareBot.LOGGER.debug("Downloading");
                        builder.redirectErrorStream(true);
                        Process downloadProcess = builder.start();
                        processInput(downloadProcess);
                        process.waitFor();
                    }
                    FlareBot.getInstance().getMusicManager().addSong(message.getChannel().getGuild().getID(), e.id + EXTENSION, e.title);
                }
            }
        } catch (IOException | InterruptedException e) {
            FlareBot.LOGGER.error(e.getMessage(), e);
        }

        long b = System.currentTimeMillis();
        FlareBot.LOGGER.debug("Process took " + (b - a) + " milliseconds");
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
}
