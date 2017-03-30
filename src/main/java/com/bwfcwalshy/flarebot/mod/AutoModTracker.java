package com.bwfcwalshy.flarebot.mod;

import com.bwfcwalshy.flarebot.MessageUtils;
import com.bwfcwalshy.flarebot.commands.FlareBotManager;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;

import java.awt.*;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class AutoModTracker extends ListenerAdapter {

    private Map<String, ConcurrentHashMap<String, Integer>> spamCounter = new ConcurrentHashMap<>();

    public void onGuildMessageReceived(GuildMessageReceivedEvent event) {
        if(event.getMessage() == null || event.getAuthor().isFake()) return;
        String userId = event.getAuthor().getId();
        AutoModGuild guild = FlareBotManager.getInstance().getAutoModGuild(event.getGuild().getId());
        if(!guild.getConfig().isEnabled()) return;
        ConcurrentHashMap<String, Integer> counter = new ConcurrentHashMap<>();
        if(spamCounter.containsKey(event.getGuild().getId()))
            counter = spamCounter.get(event.getGuild().getId());

        counter.put(userId, (counter.containsKey(userId) ? counter.get(userId) + 1 : 1));
        //spamCounter.put(event.getGuild().getId(), counter);

        // Checks
        for(Action action : Action.values){
            if(action.check(event.getMessage())){
                guild.addPoints(userId, guild.getConfig().getActions().get(action));
                event.getMessage().delete().queue();
                sendMessage(event.getChannel(), event.getAuthor(), action, guild, guild.getConfig());
                break;
            }
        }
    }

    public ConcurrentHashMap<String,Integer> getSpamCounter(String guild) {
        return spamCounter.getOrDefault(guild, new ConcurrentHashMap<>());
    }

    public int getMessages(String guild, String userId){
        return getSpamCounter(guild).getOrDefault(userId, 0);
    }

    public void sendMessage(TextChannel channel, User user, Action action, AutoModGuild guild, AutoModConfig config){
        MessageUtils.sendAutoDeletedMessage(new EmbedBuilder().setTitle("FlareBot AutoMod", null).setDescription(user.getAsMention()
                + " Your message contained content not allowed on this server! Due to this you have been given " + config.getActions().get(action) + " points.")
                .addField("Reason", action.getName(), true).addField("Points given", config.getActions().get(action).toString(), true)
                .addField("New Point Total", String.valueOf(guild.getPointsForUser(user.getId())), true).setColor(Color.white).build(), 10_000, channel);

        //config.getModLogChannel()
    }
}
