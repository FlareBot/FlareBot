package com.bwfcwalshy.flarebot.music;

import com.bwfcwalshy.flarebot.FlareBot;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.local.LocalAudioSourceManager;
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
        playerManager.registerSourceManager(new LocalAudioSourceManager());
        players = new ConcurrentHashMap<>();
        this.bot = flareBot;
    }

    public boolean addSong(String guildId, String musicFile, String trackName, String ext) throws IOException, ExecutionException, InterruptedException {
        AudioEvents player = players.computeIfAbsent(guildId, id -> {
            AudioEvents playa = new AudioEvents();
            playa.setVolume(20);
            return playa;
        });
        File audio = new File("cached" + File.separator + musicFile);
        AudioEvents.Track track = new AudioEvents.Track(player.getTrack(audio));
        track.getMetadata().put("name", trackName);
        track.getMetadata().put("id", musicFile.substring(0, musicFile.length() - ext.length()));
        player.queue(track);
        AudioPlayer.getAudioPlayerForGuild(bot.getClient().getGuildByID(guildId)).queue(player);
        return true;
    }

    public void pause(String guildId) {
        if (players.containsKey(guildId))
            players.get(guildId).setPaused(true);
    }

    public void play(String guildId) {
        if (players.containsKey(guildId))
            players.get(guildId).setPaused(false);
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
