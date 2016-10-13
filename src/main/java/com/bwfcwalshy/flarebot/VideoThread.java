package com.bwfcwalshy.flarebot;

import com.bwfcwalshy.flarebot.music.MusicManager;
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
            String videoFile;
            String link;
            String videoId;
            if (isUrl) {
                if (isShortened) {
                    searchTerm = YOUTUBE_URL + searchTerm.replaceFirst("http(s)?://youtu\\.be", "");
                }
                message = MessageUtils.sendMessage(channel, "Getting video from URL.");
                Document doc = Jsoup.connect(searchTerm).get();
                videoId = searchTerm.replaceFirst("http(s)?://(www\\.)?youtube\\.com/watch\\?v=", "");
                // Playlist
                if (videoId.contains("&list")) videoId.substring(0, videoId.indexOf("&list") + 5);
                videoName = MessageUtils.escapeFile(doc.title().replace(" - YouTube", ""));
                videoFile = videoName + "-" + videoId;
                link = searchTerm;
            } else {
                message = MessageUtils.sendMessage(channel, "Searching YouTube for '" + searchTerm + "'");
                Document doc = Jsoup.connect(SEARCH_URL + URLEncoder.encode(searchTerm, "UTF-8")).get();

                int i = 0;
                Element videoElement = doc.getElementsByClass("yt-lockup-title").get(i);
                Element lookedAt = videoElement.children().first();
                while(lookedAt.hasClass("ad-badge")){
                    videoElement = doc.getElementsByClass("yt-lockup-title").get(i);
                    lookedAt = videoElement.children().first();
                }
                link = videoElement.select("a").first().attr("href");
                // I check the index of 2 chars so I need to add 2
                Document doc2 = Jsoup.connect((link.startsWith("http") ? "" : YOUTUBE_URL) + link).get();
                videoName = MessageUtils.escapeFile(doc2.title().substring(0, doc2.title().length() - 10));
                videoId = link.substring(link.indexOf("v=") + 2);
                videoFile = videoName + "-" + videoId;

                link = YOUTUBE_URL + link;
            }
            File video = new File("cached" + File.separator + videoFile + EXTENSION);
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
                        "cached" + File.separator + "%(title)s-%(id)s.%(ext)s",
                        "--extract-audio", "--audio-format"
                        , "mp3", link);
                FlareBot.LOGGER.debug("Downloading");
                builder.redirectErrorStream(true);
                Process process = builder.start();
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                    while (process.isAlive()) {
                        String line;
                        if ((line = reader.readLine()) != null) {
                            FlareBot.LOGGER.info("[YT-DL] " + line);
                        }
                    }
                }
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
            if (manager.addSong(channel.getGuild().getID(), video.getName())) {
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
        } catch (IOException | InterruptedException e) {
            FlareBot.LOGGER.error(e.getMessage(), e);
        }
        long b = System.currentTimeMillis();
        FlareBot.LOGGER.debug("Process took " + (b - a) + " milliseconds");
    }
}
