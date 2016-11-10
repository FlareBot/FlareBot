package com.bwfcwalshy.flarebot.music;

import com.bwfcwalshy.flarebot.FlareBot;
import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;
import com.sedmelluq.discord.lavaplayer.track.playback.AudioFrame;
import sx.blah.discord.handle.audio.IAudioProvider;

import java.io.File;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutionException;

/**
 * <br>
 * Created by Arsen on 10.11.16..
 */
public class AudioEvents extends AudioEventAdapter implements IAudioProvider {

    private boolean looping = false;
    private ConcurrentLinkedQueue<Track> tracks = new ConcurrentLinkedQueue<>();
    private AudioPlayer player = FlareBot.getInstance().getMusicManager().getPlayerManager().createPlayer();
    private Track currentTrack;
    private int volume;

    @Override
    public void onTrackEnd(AudioPlayer player, AudioTrack track, AudioTrackEndReason endReason) {
        if(endReason == AudioTrackEndReason.REPLACED)
            return;
        skip();
    }

    public void skip() {
        if(getLooping()){
            tracks.add(getCurrentTrack().makeClone());
        }
        Track track = tracks.poll();
        player.playTrack(track.getTrack());
        currentTrack = track;
    }

    public boolean getLooping(){
        return looping;
    }

    public void setLooping(boolean loop){
        looping = loop;
    }

    public Track getCurrentTrack(){
        return currentTrack;
    }

    public void queue(Track track){
        if(tracks.isEmpty() && currentTrack == null) {
            currentTrack = track;
            player.playTrack(track.getTrack());
            setPaused(false);
        } else tracks.add(track);
    }

    @Override
    public boolean isReady() {
        return true;
    }

    @Override
    public byte[] provide() {
        AudioFrame frame = player.provideDirectly();
        return frame != null ? frame.data : new byte[0];
    }

    @Override
    public AudioEncodingType getAudioEncodingType() {
        return AudioEncodingType.OPUS;
    }

    public void setPaused(boolean paused) {
        player.setPaused(paused);
    }

    public boolean getPaused(){
        return player.isPaused();
    }

    public Queue<Track> getPlaylist() {
        return tracks;
    }

    public void shuffle() {
        List<Track> trackList = new ArrayList<>(tracks.size());
        player.setPaused(true);
        trackList.addAll(tracks);
        trackList.add(currentTrack.makeClone());
        Collections.shuffle(trackList);
        tracks.clear();
        tracks.addAll(trackList);
    }

    public void setVolume(int volume) {
        player.setVolume(volume);
    }

    public int getVolume() {
        return player.getVolume();
    }

    public AudioTrack getTrack(File audio) throws ExecutionException, InterruptedException {
        AudioTrack[] track = new AudioTrack[1];
        FlareBot.getInstance().getMusicManager().getPlayerManager().loadItem(audio.getAbsolutePath(), new AudioLoadResultHandler() {
            @Override
            public void trackLoaded(AudioTrack loaded) {
                track[0] = loaded;
            }

            @Override
            public void playlistLoaded(AudioPlaylist playlist) {
            }

            @Override
            public void noMatches() {
                FlareBot.LOGGER.error("Could not find match for " + audio);
            }

            @Override
            public void loadFailed(FriendlyException exception) {
                FlareBot.LOGGER.error("Could not load track!", exception);
            }
        }).get();
        return track[0];
    }

    public int getPlaylistSize() {
        return getPlaylist().size();
    }

    public static class Track {
        private AudioTrack track;
        private Map<String, String> metadata = new ConcurrentHashMap<>();

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Track track1 = (Track) o;
            return track.equals(track1.track) && metadata.equals(track1.metadata);
        }

        @Override
        public int hashCode() {
            int result = track.hashCode();
            result = 31 * result + metadata.hashCode();
            return result;
        }

        public Track(AudioTrack track){
            this.track = track;
        }

        public AudioTrack getTrack() {
            return track;
        }

        public Map<String, String> getMetadata() {
            return metadata;
        }

        public Track makeClone(){
            Track track = new Track(this.track.makeClone());
            track.getMetadata().putAll(metadata);
            return track;
        }
    }
}
