package com.bwfcwalshy.flarebot.music.extractors;

import com.arsenarsen.lavaplayerbridge.player.Player;
import com.arsenarsen.lavaplayerbridge.player.Track;
import com.bwfcwalshy.flarebot.MessageUtils;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioItem;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IUser;

public class RandomExtractor implements Extractor {
    @Override
    public Class<? extends AudioSourceManager> getSourceManagerClass() {
        return YoutubeAudioSourceManager.class;
    }

    @SuppressWarnings("Duplicates") // I dont give a damn
    @Override
    public void process(String input, Player player, IMessage message, IUser user) throws Exception {
        int i = 0;
        for (String s : input.split(",")) {
            try {
                AudioItem probablyATrack =  player.resolve(s);
                if(probablyATrack == null)
                    continue;
                Track track = new Track((AudioTrack) probablyATrack);
                track.getMeta().put("requester", user.getID());
                player.queue(track);
                i++;
            } catch (FriendlyException ignored) {
            }
        }
        MessageUtils.editMessage(MessageUtils.getEmbed()
                        .withDesc("Added " + i + " random songs to the playlist!").build(), message);
    }

    @Override
    public boolean valid(String input) {
        return input.matches("([^,]{11},)*[^,]{11}");
    }
}
