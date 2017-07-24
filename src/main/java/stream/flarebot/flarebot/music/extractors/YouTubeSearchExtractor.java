package stream.flarebot.flarebot.music.extractors;

import com.arsenarsen.lavaplayerbridge.player.Player;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeAudioSourceManager;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.User;
import okhttp3.Response;
import org.json.JSONArray;
import org.json.JSONObject;
import stream.flarebot.flarebot.FlareBot;
import stream.flarebot.flarebot.util.MessageUtils;
import stream.flarebot.flarebot.util.WebUtils;

import java.io.IOException;
import java.net.URLEncoder;

public class YouTubeSearchExtractor extends YouTubeExtractor {

    @Override
    public void process(String input, Player player, Message message, User user) throws Exception {
        Response response = WebUtils.get(String.format("https://www.googleapis.com/youtube/v3/search" +
                        "?q=%s&part=snippet&key=%s&type=video,playlist",
                URLEncoder.encode(input, "UTF-8"), FlareBot.getYoutubeKey()));
        if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);
        JSONArray results = new JSONObject(response.body().string()).getJSONArray("items");
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
                    .setDescription(String
                            .format("Could not find any results for `%s`", input)), message);
            return;
        }
        super.process(link, player, message, user);
    }

    @Override
    public AudioSourceManager newSourceManagerInstance() throws Exception {
        YoutubeAudioSourceManager manager = new YoutubeAudioSourceManager();
        manager.setPlaylistPageCount(100);
        return manager;
    }
}
