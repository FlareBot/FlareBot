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
import stream.flarebot.flarebot.FlareBot;
import stream.flarebot.flarebot.util.GeneralUtils;
import stream.flarebot.flarebot.util.MessageUtils;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

public class YouTubeExtractor implements Extractor {

    private Random random = new Random();

    public static final String PLAYLIST_URL = "https://www.youtube.com/playlist?list=";
    public static final String WATCH_URL = "https://www.youtube.com/watch?v=";
    public static final String ANY_YT_URL =
            "(?:https?://)?(?:(?:(?:(?:(?:www\\.)|(?:m\\.))?(?:youtube\\.com))/(?:(?:watch\\?v=([^?&\\n]+)(?:&(?:[^?&\\n]+=(?:[^?&\\n]+)))*)|(?:playlist\\?list=([^&?]+))(?:&[^&]*=[^&]+)?))|(?:youtu\\.be/(.*)))";

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
            MessageUtils.editMessage(null, MessageUtils.getEmbed(user)
                    .setDescription("Could not get that video/playlist! Make sure the URL is correct!")
                    .setColor(Color.RED), message);
            return;
        } catch (IllegalStateException e) {
            if (e.getMessage().contains("Vevo")) {
                MessageUtils.editMessage(null, MessageUtils.getEmbed(user)
                        .setDescription("We are blocked from playing this video as it is from Vevo! " +
                                "Sorry for any inconvenience.")
                        .setColor(Color.RED), message);
                return;
            }
            MessageUtils.editMessage(null, MessageUtils.getEmbed(user)
                    .setDescription("There was a problem with that video!\n" +
                            "If the error continues, join our support discord: " + FlareBot.INVITE_URL + "\n" +
                            "Input: " + input + "\n" +
                            "Error Message: " + e.getMessage() + "\n" +
                            "Stacktrace: " + MessageUtils.paste(GeneralUtils.getStackTrace(e)))
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
            /*if (track.getInfo().length == 0 || track.getInfo().isStream) {
                EmbedBuilder builder = MessageUtils.getEmbed(user).setDescription("Cannot queue a livestream!");
                MessageUtils.editMessage(null, builder, message);
                return;
            }*/
            audioTracks.add(track);
            name = track.getInfo().title;
            if (track.getInfo().identifier.equals("dQw4w9WgXcQ") && (random.nextInt(1000) + 1) == 1000) {
                GeneralUtils.sendImage("https://flarebot.stream/img/rick_roll.jpg", "rick_roll.jpg", user);
                FlareBot.getInstance().logEG("You can't rick roll me!", null, message.getGuild(), user);
            }
        }
        if (name != null) {
            List<Track> tracks = audioTracks.stream().map(Track::new).peek(track -> {
                track.getMeta().put("requester", user.getId());
                track.getMeta().put("guildId", player.getGuildId());
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
                    name.replace("`", "'"), input));
            if (audioTracks.size() > 1)
                builder.addField("Song count:", String.valueOf(audioTracks.size()), true);
            MessageUtils.editMessage(null, builder, message);
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
