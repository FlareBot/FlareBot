package stream.flarebot.flarebot.mod;

import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;
import stream.flarebot.flarebot.FlareBot;
import stream.flarebot.flarebot.FlareBotManager;
import stream.flarebot.flarebot.scheduler.FlarebotTask;
import stream.flarebot.flarebot.util.MessageUtils;

import java.awt.Color;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class AutoModTracker extends ListenerAdapter {

    private Map<String, ConcurrentHashMap<String, Integer>> spamCounter = new ConcurrentHashMap<>();

    public AutoModTracker() {
        new FlarebotTask("AutoModTracker") {
            @Override
            public void run() {
                spamCounter.forEach((s, map) -> map.clear());
            }
        }.repeat(TimeUnit.MINUTES.toMillis(1), TimeUnit.MINUTES.toMillis(1));
    }

    @Override
    public void onGuildMessageReceived(GuildMessageReceivedEvent event) {
        if (event.getMessage() == null || event.getAuthor().isBot() || event.getAuthor().isFake() || event
                .getGuild() == null || event.getMessage().getRawContent().isEmpty()) return;
        String message = event.getMessage().getRawContent();
        // 218ns performance :thumbsup:
        String command = message.substring(1);
        int index = message.indexOf(' ');
        if (index > 0)
            command = command.substring(0, index - 1);
        if (FlareBot.getInstance().getCommand(command, event.getAuthor()) != null) return;

        String userId = event.getAuthor().getId();
        AutoModGuild guild = FlareBotManager.getInstance().getGuild(event.getGuild().getId()).getAutoModGuild();
        if (!guild.getConfig().isEnabled()) return;
        ConcurrentHashMap<String, Integer> counter = new ConcurrentHashMap<>();
        if (spamCounter.containsKey(event.getGuild().getId()))
            counter = spamCounter.get(event.getGuild().getId());

        counter.put(userId, (counter.containsKey(userId) ? counter.get(userId) + 1 : 1));
        spamCounter.put(event.getGuild().getId(), counter);

        outer:
        for (Action action : Action.values) {
            if (action.check(event.getMessage(), guild.getConfig())) {
                if (action == Action.LINKS) {
                    if (event.getMessage().getContent()
                            .startsWith(FlareBot.getPrefix(event.getGuild().getId()) + "search")) {
                        if (MessageUtils.hasYouTubeLink(event.getMessage())) {
                            return;
                        }
                    }
                }

                if (action == Action.LINKS || action == Action.INVITE_LINKS || action == Action.PROFANITY) {
                    for (String whitelistItem : guild.getConfig().getWhitelist(action))
                        if (event.getMessage().getContent().contains(whitelistItem))
                            continue outer;
                }

                String resp = guild.addPoints(event.getGuild(), userId, guild.getConfig().getActions().get(action));
                if(resp == null) {
                    if(event.getChannel().canTalk())
                        sendMessage(event.getChannel(), event.getAuthor(), action, event.getMessage(), guild, guild.getConfig());
                }else{
                    MessageUtils.sendErrorMessage(resp, event.getChannel());
                    event.getGuild().getOwner().getUser().openPrivateChannel().complete().sendMessage(resp).queue();
                }

                if (action == Action.SPAM) {
                    spamCounter.get(event.getGuild().getId()).put(userId, 0);
                    List<Message> messageList = event.getChannel().getHistory()
                            .retrievePast(guild.getConfig().getMaxMessagesPerMinute() * 2)
                            .complete();
                    event.getChannel().deleteMessages(messageList.stream().filter(msg -> msg.getAuthor().getId()
                            .equals(userId))
                            .limit(guild.getConfig().getMaxMessagesPerMinute())
                            .collect(Collectors.toList())).queue();
                } else
                    event.getMessage().delete().queue();
                break;
            }
        }
    }

    public ConcurrentHashMap<String, Integer> getSpamCounter(String guild) {
        return spamCounter.getOrDefault(guild, new ConcurrentHashMap<>());
    }

    public int getMessages(String guild, String userId) {
        return getSpamCounter(guild).getOrDefault(userId, 0);
    }

    public void sendMessage(TextChannel channel, User user, Action action, Message message, AutoModGuild guild, AutoModConfig config) {
        MessageUtils.sendAutoDeletedMessage(new EmbedBuilder().setTitle("FlareBot AutoMod", null)
                .setDescription(user.getAsMention()
                        + " Your message contained content not allowed on this server! Due to this you have been given " + config
                        .getActions().get(action) + " points.")
                .addField("Reason", action.getName(), true)
                .addField("Points given", config.getActions().get(action)
                        .toString(), true)
                .addField("New Point Total", String.valueOf(guild
                        .getPointsForUser(user.getId())), true)
                .setColor(Color.white).build(), 10_000, channel);
        config.postToModLog(channel, user, action, message);
    }
}
