package stream.flarebot.flarebot.mod;

import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.MessageEmbed;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;
import org.eclipse.jetty.util.ConcurrentHashSet;
import stream.flarebot.flarebot.FlareBot;
import stream.flarebot.flarebot.FlareBotManager;
import stream.flarebot.flarebot.util.GeneralUtils;

import java.awt.Color;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class AutoModConfig {

    //TODO: Disable by default. Enabled for testing
    private boolean enabled = false;

    private String modLogChannel;
    //TODO: Implement this
    private boolean showMessageInModlog;
    private Map<Action, Integer> actions = new ConcurrentHashMap<>();
    private Map<Action, ConcurrentHashSet<String>> whitelist = new ConcurrentHashMap<>();
    private Map<Integer, Punishment> punishments = new ConcurrentHashMap<>();
    private int maxMessagesPerMinute = 10;

    private Set<Option> options = new ConcurrentHashSet<>();

    public AutoModConfig() {
        options.add(new Option("caps-percentage", "Maximum percentage of the message can be capitalized", 40));
    }

    public boolean isEnabled() {
        return this.enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getModLog() {
        return this.modLogChannel;
    }

    public TextChannel getModLogChannel() {
        return FlareBot.getInstance().getChannelByID(modLogChannel);
    }

    public void setModLogChannel(String modLogChannel) {
        this.modLogChannel = modLogChannel;
    }

    public Map<Action, Integer> getActions() {
        return this.actions;
    }

    public ConcurrentHashSet<String> getWhitelist(Action action) {
        if (!whitelist.containsKey(action))
            whitelist.put(action, new ConcurrentHashSet<>());
        return this.whitelist.get(action);
    }

    public Map<Action, ConcurrentHashSet<String>> getWhitelist() {
        return this.whitelist;
    }

    public int getMaxMessagesPerMinute() {
        return this.maxMessagesPerMinute;
    }

    public Map<Integer, Punishment> getPunishments() {
        return punishments;
    }

    public void resetPunishments() {
        punishments.put(3, new Punishment(ModlogAction.TEMP_MUTE, 259200));
        punishments.put(5, new Punishment(ModlogAction.TEMP_BAN, 604800));
        punishments.put(10, new Punishment(ModlogAction.BAN));
    }

    public boolean hasModLog() {
        return modLogChannel != null && !modLogChannel.isEmpty() && getModLogChannel() != null;
    }

    public void postToModLog(TextChannel channel, User user, Action action, Message message) {
        if (hasModLog()) {
            getModLogChannel()
                    .sendMessage(new EmbedBuilder().setTitle("FlareBot AutoMod", null)
                            .setDescription("Message sent by "
                                    + user
                                    .getAsMention() + " has been automatically deleted in " + channel
                                    .getAsMention() + " and has been given " + getActions()
                                    .get(action) + " points.")
                            .addField("Reason", action.getName(), true)
                            .addField("Message", message.getContent(), true)
                            .setColor(Color.white)
                            .build()).queue();
        }
    }

    public void postToModLog(User user, User responsible, Punishment punishment, String reason) {
        postToModLog(punishment.getActionEmbed(user, responsible, reason));
    }

    public void postToModLog(User user, User responsible, Punishment punishment, boolean showNoReason) {
        postToModLog(punishment.getActionEmbed(user, responsible, null, !showNoReason));
    }

    // Shouldn't really be used - Good for stuff like the purge command though
    public void postToModLog(MessageEmbed embed) {
        if (hasModLog()) {
            EmbedBuilder eb = new EmbedBuilder();
            if (embed.getTitle() != null)
                eb.setTitle(embed.getTitle());
            if(embed.getDescription() != null)
                eb.appendDescription(embed.getDescription());
            for (MessageEmbed.Field field : embed.getFields()) {
                eb.addField(field);
            }
            eb.setFooter(GeneralUtils.formatTime(LocalDateTime.now()), null);
            getModLogChannel().sendMessage(eb.build()).queue();
        }
    }

    public void postToModLog(String message) {
        if (hasModLog()) {
            message += ("*" + GeneralUtils.formatTime(LocalDateTime.now()) + "*");
            getModLogChannel().sendMessage(message).queue();
        }

    }

    public void postToModLog(MessageEmbed embed, ModlogEvent event) {
        if (hasModLog()) {
            if (FlareBotManager.getInstance().getGuild(getModLogChannel().getGuild().getId()).eventEnabled(event)) {
                if (event.isCompact()) {
                    postToModLog(GeneralUtils.embedToText(embed));
                } else {
                    postToModLog(embed);
                }
            }
        }
    }

    public void postAutoModAction(User user, Punishment punishment) {
        if (hasModLog())
            getModLogChannel().sendMessage(punishment.getActionEmbed(user, null, null)).queue();
    }

    public void postAutoModAction(User user, Punishment punishment, String reason) {
        if (hasModLog())
            getModLogChannel().sendMessage(punishment.getActionEmbed(user, null, reason)).queue();
    }

    public Option getOption(String s) {
        for (Option option : options)
            if (option.getKey().equals(s)) return option;
        return null;
    }
}
