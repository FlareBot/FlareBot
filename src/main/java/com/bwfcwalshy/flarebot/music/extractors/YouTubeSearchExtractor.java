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
        List<Element> videoElements = doc.getElementsByClass("yt-lockup-title");
        if (videoElements.isEmpty()) {
            MessageUtils.editMessage(MessageUtils.getEmbed(user).withDesc("Could not find any results for **" + input + "**!").build(), message);
            return;
        }
        Element videoElement = videoElements.get(i);
        String link = null;
        for (Element e : videoElement.children()) {
            if (e.select("a") != null && e.select("a").first() != null
                    && e.select("a").first().attr("abs:href") != null
                    && super.valid(e.select("a").first().attr("abs:href"))) {
                link = e.select("a").first().attr("abs:href");
                break;
            }
        }
        if (link == null) {
            MessageUtils.editMessage(MessageUtils.getEmbed(user).withDesc("Could not find any results for **" + input + "**!").build(), message);
            return;
        }
        super.process(link, player, message, user);
    }
}
