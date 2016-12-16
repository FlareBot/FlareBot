package com.bwfcwalshy.flarebot.music.extractors;

import com.arsenarsen.lavaplayerbridge.player.Player;
import com.bwfcwalshy.flarebot.FlareBot;
import com.mashape.unirest.http.Unirest;
import org.json.JSONObject;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IUser;

import java.net.URLEncoder;

public class YouTubeSearchExtractor extends YouTubeExtractor {
    public static final String SEARCH_URL = "https://www.youtube.com/results?search_query=";

    @Override
    public void process(String input, Player player, IMessage message, IUser user) throws Exception {
        JSONObject result = Unirest.get(String.format("https://www.googleapis.com/youtube/v3/search?q=%s&part=snippet&key=%s&maxResults=1",
                URLEncoder.encode(input, "UTF-8"), FlareBot.getYoutubeKey())).asJson().getBody()
                .getObject().getJSONArray("items").getJSONObject(0);
        JSONObject id = result.getJSONObject("id");
        String link;
        if(id.getString("kind").equals("youtube#playlist")){
            link = PLAYLIST_URL + id.getString("id");
        } else {
            link = WATCH_URL + id.getString("id");
        }
        super.process(link, player, message, user);
    }
}
