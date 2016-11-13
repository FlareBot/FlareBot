package com.bwfcwalshy.flarebot.music;

import com.bwfcwalshy.flarebot.FlareBot;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeAudioSourceManager;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.util.audio.AudioPlayer;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;

public class MusicManager {

    // Guild ID | Queue
    private Map<String, AudioEvents> players;
    private AudioPlayerManager playerManager = new DefaultAudioPlayerManager();

    private FlareBot bot;

    public MusicManager(FlareBot flareBot) {
        playerManager.registerSourceManager(new YoutubeAudioSourceManager());
        players = new ConcurrentHashMap<>();
        this.bot = flareBot;
    }

    public boolean addSong(String url, String guildId, String title, String id, IMessage message) throws IOException, ExecutionException, InterruptedException {
        AudioEvents player = players.computeIfAbsent(guildId, gid -> {
            AudioEvents ply = new AudioEvents();
            AudioPlayer.getAudioPlayerForGuild(bot.getClient().getGuildByID(gid)).queue(ply);
            return ply;
        });
        File audio = new File("cached" + File.separator + title);
        AudioEvents.Track track = new AudioEvents.Track(player.getTrack(url));
        track.getMetadata().put("name", title);
        track.getMetadata().put("id", id);
        player.queue(track);
        return true;
    }

    public void pause(String guildId) {
        if (players.containsKey(guildId))
            players.get(guildId).setPaused(true);
    }

    public void play(String guildId) {
        if (players.containsKey(guildId))
            players.get(guildId).play();
    }

    public void skip(String guildId) {
        if (players.containsKey(guildId))
            players.get(guildId).skip();
    }

    public void skip(String guildId, int songs) {
        if (players.containsKey(guildId))
            for (int i = 0; i < songs; i++)
                players.get(guildId).skip();
    }

    public void stop(String guildId) {
        if (players.containsKey(guildId)) {
            players.get(guildId).setPaused(true);
            players.get(guildId).getPlaylist().clear();
            boolean looping = players.get(guildId).getLooping();
            players.get(guildId).setLooping(false);
            players.get(guildId).skip();
            players.get(guildId).setLooping(looping);
        }
    }

    public void shuffle(String guildId) {
        if (players.containsKey(guildId)) {
            players.get(guildId).shuffle();
        }
    }

    public void setVolume(String guildId, int i) {
        if (i >= 0 && i <= 100) {
            if (!players.containsKey(guildId)) {
                return;
            }
            players.get(guildId).setVolume(i);
        }
    }

    public int getVolume(String guildId) {
        if (!players.containsKey(guildId)) {
            return 20;
        }
        return (players.get(guildId).getVolume());
    }

    public Map<String, AudioEvents> getPlayers() {
        return players;
    }

    public AudioPlayerManager getPlayerManager() {
        return playerManager;
    }
}
