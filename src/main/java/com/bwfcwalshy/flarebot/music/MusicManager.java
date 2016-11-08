package com.bwfcwalshy.flarebot.music;

import com.bwfcwalshy.flarebot.FlareBot;
import sx.blah.discord.util.audio.AudioPlayer;
import sx.blah.discord.util.audio.providers.AudioInputStreamProvider;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class MusicManager {

    // Guild ID | Queue
    private Map<String, AudioPlayer> players;

    private FlareBot bot;

    public MusicManager(FlareBot flareBot) {
        players = new ConcurrentHashMap<>();
        this.bot = flareBot;
    }

    public boolean addSong(String guildId, String musicFile, String trackName, String ext) throws IOException {
        AudioPlayer player = players.computeIfAbsent(guildId, id -> {
            AudioPlayer playa = AudioPlayer.getAudioPlayerForGuild(bot.getClient().getGuildByID(id));
            playa.setVolume(0.2f);
            return playa;
        });
        try {
            AudioInputStream input = AudioSystem.getAudioInputStream(new File("cached" + File.separator + musicFile));
            //FileProvider file = new FileProvider("cached" + File.separator + musicFile);
            File audio = new File("cached" + File.separator + musicFile);
            AudioPlayer.Track track = new AudioPlayer.Track(new AudioInputStreamProvider(null){

            });
            track.getMetadata().put("name", trackName);
            track.getMetadata().put("id", musicFile.substring(0, musicFile.length() - ext.length()));
            //track.getMetadata().put("duration", duration);
            player.queue(track);
        } catch (UnsupportedAudioFileException e) {
            FlareBot.LOGGER.error("Could not add song", e);
            return false;
        }
        players.put(guildId, player);
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
            players.get(guildId).setVolume((float) i / 100);
        }
    }

    public Map<String, AudioPlayer> getPlayers() {
        return players;
    }
}
