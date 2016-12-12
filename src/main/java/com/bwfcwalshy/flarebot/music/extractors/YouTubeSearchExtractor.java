package com.bwfcwalshy.flarebot.music.extractors;

import com.arsenarsen.lavaplayerbridge.player.Player;
import com.bwfcwalshy.flarebot.MessageUtils;
import com.sedmelluq.discord.lavaplayer.track.AudioItem;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IUser;

public class YouTubeSearchExtractor extends YouTubeExtractor {
    public static final String SEARCH_URL = "https://www.youtube.com/results?search_query=";

    @Override
    public void process(String input, Player player, IMessage message, IUser user) throws Exception {
        String link = null;
        AudioItem result = player.resolve("ytsearch: " + input);
        if (result instanceof AudioPlaylist) {
            AudioPlaylist res = (AudioPlaylist) result;
            if(!res.getTracks().isEmpty()){
                link = WATCH_URL + res.getTracks().get(0).getIdentifier();
            }
        }
        if (link == null) {
            MessageUtils.editMessage("", MessageUtils.getEmbed(user)
                    .withDesc(String.format("Found no results for **%s**!", input))
                    .build(), message);
            return;
        }
        super.process(link, player, message, user);
    }
}
