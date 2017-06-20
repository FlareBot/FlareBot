package stream.flarebot.flarebot.music.extractors;

import com.arsenarsen.lavaplayerbridge.player.Player;
import com.arsenarsen.lavaplayerbridge.player.Playlist;
import com.arsenarsen.lavaplayerbridge.player.Track;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.track.AudioItem;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.User;
import stream.flarebot.flarebot.errors.YoutubeAccessException;
import stream.flarebot.flarebot.util.GeneralUtils;
import stream.flarebot.flarebot.util.MessageUtils;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class YouTubeExtractor implements Extractor {
    public static final String YOUTUBE_URL = "https://www.youtube.com";
    public static final String PLAYLIST_URL = "https://www.youtube.com/playlist?list=";
    public static final String WATCH_URL = "https://www.youtube.com/watch?v=";
    public static final String ANY_YT_URL = "(?:https?://)?(?:(?:(?:(?:(?:www\\.)|(?:m\\.))?(?:youtube\\.com))/(?:(?:watch\\?v=([^?&\\n]+)(?:&(?:[^?&\\n]+=(?:[^?&\\n]+)))*)|(?:playlist\\?list=([^&?]+))(?:&[^&]*=[^&]+)?))|(?:youtu\\.be/(.*)))";

    @Override
    public Class<? extends AudioSourceManager> getSourceManagerClass() {
        return YoutubeAudioSourceManager.class;
    }

    @Override
    public void process(String input, Player player, Message message, User user) throws Exception {
        AudioItem item;
        try {
            item = GeneralUtils.resolveItem(player, input);
        } catch (IllegalArgumentException e) {
            MessageUtils.editMessage("", MessageUtils.getEmbed(user)
                    .setDescription("Could not get that video/playlist! Make sure the URL is correct!")
                    .setColor(Color.RED), message);
            return;
        } catch (YoutubeAccessException e) {
            MessageUtils.editMessage("", MessageUtils.getEmbed(user)
                    .setDescription("Youtube could not be reached! Try again in a few minutes!\n" +
                            "If the error continues, join our support discord: https://discord.gg/TTAUGvZ")
                    .setColor(Color.RED), message);
            return;
        }

        List<AudioTrack> audioTracks = new ArrayList<>();
        String name;
        if (item instanceof AudioPlaylist) {
            AudioPlaylist audioPlaylist = (AudioPlaylist) item;
            audioTracks.addAll(audioPlaylist.getTracks());
            name = audioPlaylist.getName();
        } else {
            AudioTrack track = (AudioTrack) item;
            if (track.getInfo().length == 0 || track.getInfo().isStream) {
                EmbedBuilder builder = MessageUtils.getEmbed(user).setDescription("Cannot queue a livestream!");
                MessageUtils.editMessage("", builder, message);
                return;
            }
            audioTracks.add(track);
            name = track.getInfo().title;
        }
        if (name != null) {
            List<Track> tracks = audioTracks.stream().map(Track::new).map(track -> {
                track.getMeta().put("requester", user.getId());
                track.getMeta().put("guildId", player.getGuildId());
                return track;
            }).collect(Collectors.toList());
            if (tracks.size() > 1) { // Double `if` https://giphy.com/gifs/ng1xAzwIkDgfm
                Playlist p = new Playlist(tracks);
                player.queue(p);
            } else {
                player.queue(tracks.get(0));
            }
            EmbedBuilder builder = MessageUtils.getEmbed(user);
            builder.setDescription(String.format("%s added the %s [`%s`](%s)", user.getAsMention(), audioTracks
                            .size() == 1 ? "song" : "playlist",
                    name, input));
            if (audioTracks.size() > 1)
                builder.addField("Song count:", String.valueOf(audioTracks.size()), true);
            MessageUtils.editMessage("", builder, message);
        }
    }

    @Override
    public boolean valid(String input) {
        return input.matches(ANY_YT_URL);
    }

    @Override
    public AudioSourceManager newSourceManagerInstance() throws Exception {
        YoutubeAudioSourceManager manager = new YoutubeAudioSourceManager();
        manager.setPlaylistPageCount(100);
        return manager;
    }
}
