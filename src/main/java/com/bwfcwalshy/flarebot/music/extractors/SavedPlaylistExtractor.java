package com.bwfcwalshy.flarebot.music.extractors;

import com.arsenarsen.lavaplayerbridge.player.Player;
import com.arsenarsen.lavaplayerbridge.player.Playlist;
import com.arsenarsen.lavaplayerbridge.player.Track;
import com.bwfcwalshy.flarebot.MessageUtils;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.User;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.util.ArrayList;

public class SavedPlaylistExtractor implements Extractor {
    @Override
    public Class<? extends AudioSourceManager> getSourceManagerClass() {
        return YoutubeAudioSourceManager.class;
    }

    @Override
    public void process(String input, Player player, Message message, User user) throws Exception {
        String name = input.substring(0, input.indexOf('\u200B'));
        input = input.substring(input.indexOf('\u200B') + 1);
        int i = 0;
        ArrayList<Track> playlist = new ArrayList<>();
        for (String s : input.split(",")) {
            String url = YouTubeExtractor.WATCH_URL + s;
            Document doc;
            try {
                doc = Jsoup.connect(url).get();
            } catch (Exception e) {
                continue;
            }
            if (!doc.title().endsWith("YouTube") || doc.title().equals("YouTube")) {
                continue;
            }
            try {
                Track track = new Track((AudioTrack) player.resolve(url));
                track.getMeta().put("requester", user.getId());
                track.getMeta().put("guildId", player.getGuildId());
                playlist.add(track);
                if(playlist.size() == 10){
                    player.queue(new Playlist(playlist));
                    playlist.clear();
                }
                i++;
            } catch (FriendlyException ignored) {
            }
        }
        if(!playlist.isEmpty()){
            player.queue(new Playlist(playlist));
        }
        MessageUtils.editMessage("", MessageUtils.getEmbed(user)
                .setDescription(String.format("*Loaded %s songs!*", i)), message);
    }

    @Override
    public boolean valid(String input) {
        return input.matches(".+\u200B([^,]{11},)*[^,]{11}");
    }
}
