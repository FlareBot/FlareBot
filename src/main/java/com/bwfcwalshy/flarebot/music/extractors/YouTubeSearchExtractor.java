package com.bwfcwalshy.flarebot.music.extractors;

import com.arsenarsen.lavaplayerbridge.player.Player;
import com.bwfcwalshy.flarebot.FlareBot;
import com.bwfcwalshy.flarebot.MessageUtils;
import com.mashape.unirest.http.Unirest;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.User;
import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URLEncoder;

public class YouTubeSearchExtractor extends YouTubeExtractor {
    public static final String SEARCH_URL = "https://www.youtube.com/results?search_query=";

    @Override
    public void process(String input, Player player, Message message, User user) throws Exception {
        JSONArray results = Unirest.get(String.format("https://www.googleapis.com/youtube/v3/search" +
                        "?q=%s&part=snippet&key=%s&type=video,playlist",
                URLEncoder.encode(input, "UTF-8"), FlareBot.getYoutubeKey())).asJson().getBody()
                .getObject().getJSONArray("items");
        String link = null;
        for (Object res : results) {
            if (res instanceof JSONObject) {
                JSONObject result = (JSONObject) res;
                if (!result.getJSONObject("snippet").getString("liveBroadcastContent").contains("none"))
                    continue;
                JSONObject id = result.getJSONObject("id");
                if (id.getString("kind").equals("youtube#playlist")) {
                    link = PLAYLIST_URL + id.getString("playlistId");
                } else {
                    link = WATCH_URL + id.getString("videoId");
                }
                break;
            }
        }
        if (link == null) {
            MessageUtils.editMessage("", MessageUtils.getEmbed(user)
                    .setDescription(String.format("Could not find any results for `%s`", input)), message);
            return;
        }
        super.process(link, player, message, user);
    }
}
