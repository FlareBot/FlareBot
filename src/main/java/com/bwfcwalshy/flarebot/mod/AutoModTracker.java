package com.bwfcwalshy.flarebot.mod;

import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class AutoModTracker extends ListenerAdapter {

    private Map<String, ConcurrentHashMap<String, Integer>> spamCounter = new ConcurrentHashMap<>();

    public void onGuildMessageReceived(GuildMessageReceivedEvent event) {
        String userId = event.getAuthor().getId();
        ConcurrentHashMap<String, Integer> counter = new ConcurrentHashMap<>();
        if(spamCounter.containsKey(event.getGuild().getId()))
            counter = spamCounter.get(event.getGuild().getId());

        counter.put(userId, (counter.containsKey(userId) ? counter.get(userId) + 1 : 1));
    }

    public ConcurrentHashMap<String,Integer> getSpamCounter(String guild) {
        return spamCounter.getOrDefault(guild, new ConcurrentHashMap<>());
    }

    public int getMessages(String guild, String userId){
        return getSpamCounter(guild).getOrDefault(userId, 0);
    }
}
