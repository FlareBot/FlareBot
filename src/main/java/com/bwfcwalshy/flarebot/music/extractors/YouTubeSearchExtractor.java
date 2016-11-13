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
        String link;
        for (Element e : videoElement.children()) {
            if (e.toString().contains("href=\"https://googleads")) {
                videoElement = doc.getElementsByClass("yt-lockup-title").get(++i);
            } else if (videoElement.select("a").first().attr("href").startsWith("/user/")) {
                videoElement = doc.getElementsByClass("yt-lockup-title").get(++i);
            } else if (videoElement.select("a").first().attr("href").startsWith("/channel/")) {
                videoElement = doc.getElementsByClass("yt-lockup-title").get(++i);
            } else if (!e.select(".yt-badge-live").isEmpty()) {
                videoElement = doc.getElementsByClass("yt-lockup-title").get(++i);
            } else break;
        }
        link = "http://youtube.com" + videoElement.select("a").first().attr("href");
        super.process(link, player, message, user);
    }
}
