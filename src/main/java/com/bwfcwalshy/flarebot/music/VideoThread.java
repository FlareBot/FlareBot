package com.bwfcwalshy.flarebot.music;

import com.arsenarsen.lavaplayerbridge.PlayerManager;
import com.arsenarsen.lavaplayerbridge.libraries.LibraryFactory;
import com.arsenarsen.lavaplayerbridge.libraries.UnknownBindingException;
import com.bwfcwalshy.flarebot.FlareBot;
import com.bwfcwalshy.flarebot.MessageUtils;
import com.bwfcwalshy.flarebot.music.extractors.*;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IUser;

import java.util.Arrays;
import java.util.List;

public class VideoThread extends Thread {

    private static PlayerManager manager;
    private static final List<Class<? extends Extractor>> extractors = Arrays.asList(YouTubeExtractor.class,
            SavedPlaylistExtractor.class);
    public static final ThreadGroup VIDEO_THREADS = new ThreadGroup("Video Threads");
    private IUser user;
    private IChannel channel;
    private String url;
    private Extractor extractor;

    private VideoThread() {
        if (manager == null) try {
            manager = PlayerManager.getPlayerManager(LibraryFactory.getLibrary(FlareBot.getInstance().getClient()));
        } catch (UnknownBindingException e) {
            e.printStackTrace(System.out);
        }
        setName("Video Thread " + VIDEO_THREADS.activeCount());
    }

    @Override
    public void run() {
        IMessage message = MessageUtils.sendMessage(channel, "Processing..");
        try {
            if (extractor == null)
                for (Class<? extends Extractor> clazz : extractors) {
                    Extractor extractor = clazz.newInstance();
                    if (!extractor.valid(url))
                        continue;
                    this.extractor = extractor;
                    break;
                }
            if (extractor == null) {
                MessageUtils.editMessage(message, "Could not find a way to process that..");
                return;
            }
            manager.getManager().registerSourceManager(extractor.getSourceManagerClass().newInstance());
            extractor.process(url, manager.getPlayer(channel.getGuild().getID()), message, user);
        } catch (Exception e) {
            FlareBot.LOGGER.error("Could not init extractor for '{}'".replace("{}", url), e);
            MessageUtils.editMessage(message, "Something went wrong. This incident has been reported. Sorry :/");
        }
    }

    @Override
    public void start() {
        if (url == null)
            throw new IllegalStateException("URL Was not set!");
        super.start();
    }

    public static VideoThread getThread(String url, IChannel channel, IUser user) {
        VideoThread thread = new VideoThread();
        thread.url = url;
        thread.channel = channel;
        thread.user = user;
        return thread;
    }

    public static VideoThread getSearchThread(String term, IChannel channel, IUser user) {
        VideoThread thread = new VideoThread();
        thread.url = term;
        thread.channel = channel;
        thread.user = user;
        thread.extractor = new YouTubeSearchExtractor();
        return thread;
    }
}
