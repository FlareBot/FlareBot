package com.bwfcwalshy.flarebot.music.extractors;

import com.arsenarsen.lavaplayerbridge.player.Player;
import com.bwfcwalshy.flarebot.MessageUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IUser;

import java.net.URLEncoder;
import java.util.List;

public class YouTubeSearchExtractor extends YouTubeExtractor {
    public static final String SEARCH_URL = "https://www.youtube.com/results?search_query=";

    @Override
    public void process(String input, Player player, IMessage message, IUser user) throws Exception {
        Document doc = Jsoup.connect(SEARCH_URL + URLEncoder.encode(input, "UTF-8")).get();
        int i = 0;
        String link = null;
        List<Element> videoElements = doc.getElementsByClass("yt-lockup-title");
        for (Element result : videoElements) {
            link = result.select("a").first().attr("abs:href");
            if(super.valid(link))
                break;
        }
        if (link == null) {
            MessageUtils.editMessage(MessageUtils.getEmbed(user).withDesc("Could not find any results for **" + input + "**!").build(), message);
            return;
        }
        super.process(link, player, message, user);
    }
}
