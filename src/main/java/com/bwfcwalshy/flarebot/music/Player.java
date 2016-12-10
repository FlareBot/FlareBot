package com.bwfcwalshy.flarebot.music;

/**
 * <br>
 * Created by Arsen on 10.11.16..
 */
@Deprecated
public class Player  {
/*
    private boolean looping = false;
    private Queue<Track> tracks = new ConcurrentLinkedQueue<>();
    private AudioPlayer player = FlareBot.getInstance().getMusicManager().getPlayerManager().createPlayer();
    private Track currentTrack;

    {
        player.addListener(this);
    }

    @Override
    public void onTrackEnd(AudioPlayer player, AudioTrack track, AudioTrackEndReason endReason) {
        if (endReason.mayStartNext)
            skip();
    }

    @Override
    public void onTrackStuck(AudioPlayer player, AudioTrack track, long thresholdMs) {
        skip();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Player that = (Player) o;

        if (looping != that.looping) return false;
        if (!tracks.equals(that.tracks)) return false;
        if (!player.equals(that.player)) return false;
        return currentTrack != null ? currentTrack.equals(that.currentTrack) : that.currentTrack == null;

    }

    @Override
    public int hashCode() {
        int result = (looping ? 1 : 0);
        result = 31 * result + tracks.hashCode();
        result = 31 * result + player.hashCode();
        result = 31 * result + (currentTrack != null ? currentTrack.hashCode() : 0);
        return result;
    }

    public void skip() {
        if (getLooping()) {
            if (getCurrentTrack() != null)
                tracks.add(getCurrentTrack().makeClone());
        }
        Track track = tracks.poll();
        if (track != null)
            player.playTrack(track.getTrack());
        else player.playTrack(null);
        currentTrack = track;
    }

    public boolean getLooping() {
        return looping;
    }

    public void setLooping(boolean loop) {
        looping = loop;
    }

    public Track getCurrentTrack() {
        return currentTrack;
    }

    public void queue(Track track) {
        if (tracks.isEmpty() && currentTrack == null) {
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
        AudioFrame frame = player.provide();
        return frame != null ? frame.data : new byte[0];
    }

    @Override
    public AudioEncodingType getAudioEncodingType() {
        return AudioEncodingType.OPUS;
    }

    public void setPaused(boolean paused) {
        player.setPaused(paused);
    }

    public boolean getPaused() {
        return player.isPaused();
    }

    public Queue<Track> getPlaylist() {
        return tracks;
    }

    public void shuffle() {
        List<Track> trackList = new ArrayList<>(tracks.size());
        player.stopTrack();
        trackList.addAll(tracks);
        if (currentTrack != null)
            trackList.add(currentTrack.makeClone());
        currentTrack = null;
        Collections.shuffle(trackList);
        tracks.clear();
        tracks.addAll(trackList);
        play();
    }

    public AudioTrack getTrack(String audio) throws FriendlyException, ExecutionException, InterruptedException {
        AudioTrack[] track = new AudioTrack[1];
        final FriendlyException[] e = {null};
        FlareBot.getInstance().getMusicManager().getPlayerManager().loadItem(audio, new AudioLoadResultHandler() {
            @Override
            public void trackLoaded(AudioTrack loaded) {
                track[0] = loaded;
            }

            @Override
            public void playlistLoaded(AudioPlaylist playlist) {
            }

            @Override
            public void noMatches() {
                e[0] = new FriendlyException("No match found for " + audio, FriendlyException.Severity.COMMON, null);
                FlareBot.LOGGER.error("Could not find match for " + audio);
            }

            @Override
            public void loadFailed(FriendlyException exception) {
                if (exception.severity != FriendlyException.Severity.COMMON)
                    FlareBot.LOGGER.error("Could not load track!", exception);
                e[0] = exception;
            }
        }).get();
        if (e[0] != null)
            throw new FriendlyException(e[0].getMessage(), e[0].severity, e[0]);
        return track[0];
    }

    public int getPlaylistSize() {
        return getPlaylist().size();
    }

    public void play() {
        if (currentTrack == null) {
            skip();
        }
        setPaused(false);
    }

    public void stop() {
        currentTrack = null;
        player.stopTrack();
        tracks.clear();
        setPaused(true);
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

        public Track(AudioTrack track) {
            this.track = track;
        }

        public AudioTrack getTrack() {
            return track;
        }

        public Map<String, String> getMetadata() {
            return metadata;
        }

        public Track makeClone() {
            Track track = new Track(this.track.makeClone());
            track.getMetadata().putAll(metadata);
            return track;
        }
    }*/
}
