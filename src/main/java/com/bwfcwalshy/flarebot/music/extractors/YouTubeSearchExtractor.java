package com.bwfcwalshy.flarebot.music.extractors;

import com.bwfcwalshy.flarebot.MessageUtils;
import com.bwfcwalshy.flarebot.music.Player;
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
            MessageUtils.editMessage(message, "Could not find any results for **" + input + "**!");
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
            MessageUtils.editMessage(message, "Could not find any results for **" + input + "**!");
            return;
        }
        super.process(link, player, message, user);
    }
}
