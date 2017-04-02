package com.bwfcwalshy.flarebot.mod;

import com.bwfcwalshy.flarebot.FlareBot;
import com.bwfcwalshy.flarebot.MessageUtils;
import com.bwfcwalshy.flarebot.commands.FlareBotManager;
import com.bwfcwalshy.flarebot.scheduler.FlarebotTask;
import com.mashape.unirest.http.Unirest;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;

import java.awt.*;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class AutoModTracker extends ListenerAdapter {

    private Map<String, ConcurrentHashMap<String, Integer>> spamCounter = new ConcurrentHashMap<>();

    public AutoModTracker(){
        new FlarebotTask("AutoModTracker") {
            @Override
            public void run() {
                spamCounter.forEach((s, map) -> map.clear());
            }
        }.repeat(60_000, 60_000);
    }

    public void onGuildMessageReceived(GuildMessageReceivedEvent event) {
        long start = System.currentTimeMillis();
        if(event.getMessage() == null || event.getAuthor().isBot() || event.getAuthor().isFake()) return;
        String userId = event.getAuthor().getId();
        AutoModGuild guild = FlareBotManager.getInstance().getAutoModGuild(event.getGuild().getId());
        if(!guild.getConfig().isEnabled()) return;
        ConcurrentHashMap<String, Integer> counter = new ConcurrentHashMap<>();
        if(spamCounter.containsKey(event.getGuild().getId()))
            counter = spamCounter.get(event.getGuild().getId());

        counter.put(userId, (counter.containsKey(userId) ? counter.get(userId) + 1 : 1));
        spamCounter.put(event.getGuild().getId(), counter);

        // Checks
        for(Action action : Action.values){
            if(action.check(event.getMessage())){
                if(action == Action.LINKS){
                    if(event.getMessage().getContent().startsWith(FlareBot.getPrefix(event.getGuild().getId()) + "search")){
                        if(MessageUtils.hasYouTubeLink(event.getMessage())) {
                            return;
                        }
                    }
                }

                guild.addPoints(userId, guild.getConfig().getActions().get(action));
                sendMessage(event.getChannel(), event.getAuthor(), action, guild, guild.getConfig());

                if(action == Action.SPAM){
                    spamCounter.get(event.getGuild().getId()).put(userId, 0);
                    List<Message> messageList = event.getChannel().getHistory().retrievePast(guild.getConfig().getMaxMessagesPerMinute() * 2).complete();
                    event.getChannel().deleteMessages(messageList.stream().filter(message -> message.getAuthor().getId().equals(userId))
                            .limit(guild.getConfig().getMaxMessagesPerMinute()).collect(Collectors.toList())).queue();
                }else
                    event.getMessage().delete().queue();
                break;
            }
        }
        long end = System.currentTimeMillis();
        event.getChannel().sendMessage("Check took " + (end - start) + "ms").queue();
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
        postToModLog(channel.getGuild(), user, action);
    }

    public void postToModLog(Guild guild, User user, Action action){
        AutoModGuild modGuild = FlareBotManager.getInstance().getAutoModGuild(guild.getId());
        AutoModConfig config = modGuild.getConfig();
        if(config.isEnabled()){
            if(config.getModLogChannel() != null && !config.getModLogChannel().isEmpty() && guild.getTextChannelById(config.getModLogChannel()) != null){
                guild.getTextChannelById(config.getModLogChannel()).sendMessage(new EmbedBuilder().setTitle("FlareBot AutoMod", null).setDescription(user.getAsMention()
                        + " Has been given " + config.getActions().get(action) + " points for " + action.getName() + ".\n" + user.getName() + " now has "
                        + String.valueOf(modGuild.getPointsForUser(user.getId())) + "points.").setColor(Color.white).build()).queue();
            }
        }
    }
}
