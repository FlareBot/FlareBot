package stream.flarebot.flarebot.mod.automod;

import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageEmbed;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;
import org.eclipse.jetty.util.ConcurrentHashSet;
import stream.flarebot.flarebot.FlareBot;
import stream.flarebot.flarebot.mod.Option;
import stream.flarebot.flarebot.mod.modlog.ModAction;
import stream.flarebot.flarebot.mod.modlog.ModlogEvent;
import stream.flarebot.flarebot.util.GeneralUtils;

import java.awt.Color;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Deprecated
public class AutoModConfig {

    //TODO: Disable by default. Enabled for testing
    private boolean enabled = false;

    private String modLogChannel;
    //TODO: Implement this
    private boolean showMessageInModlog;
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


    public int getMaxMessagesPerMinute() {
        return this.maxMessagesPerMinute;
    }

    public Map<Integer, Punishment> getPunishments() {
        return punishments;
    }

    public void resetPunishments() {
        punishments.put(3, new Punishment(ModAction.TEMP_MUTE, 259200));
        punishments.put(5, new Punishment(ModAction.TEMP_BAN, 604800));
        punishments.put(10, new Punishment(ModAction.BAN));
    }

    public boolean hasModLog() {
        return modLogChannel != null && !modLogChannel.isEmpty() && getModLogChannel() != null;
    }

    public void postToModLog(User user, User responsible, Punishment punishment, String reason) {
        //postToModLog(punishment.getActionEmbed(user, responsible, reason));
    }

    public void postToModLog(User user, User responsible, Punishment punishment, boolean showNoReason) {
        //postToModLog(punishment.getActionEmbed(user, responsible, null, !showNoReason));
    }

    // Shouldn't really be used - Good for stuff like the purge command though
    public void postToModLog(MessageEmbed embed) {
        if (hasModLog()) {
            EmbedBuilder eb = new EmbedBuilder();
            if (embed.getTitle() != null)
                eb.setTitle(embed.getTitle());
            if (embed.getDescription() != null)
                eb.appendDescription(embed.getDescription());
            if (embed.getColor() != null)
                eb.setColor(embed.getColor());
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
            /*if (FlareBotManager.getInstance().getGuild(getModLogChannel().getGuild().getId()).isEventEnabled(event)) {
                if (FlareBotManager.getInstance().getGuild(getModLogChannel().getGuild().getId()).isEventCompact(event)) {
                    postToModLog(GeneralUtils.embedToText(embed));
                } else {
                    postToModLog(embed);
                }
            }*/
        }
    }

    public void postAutoModAction(User user, Punishment punishment) {
        //if (hasModLog())
            //getModLogChannel().sendMessage(punishment.getActionEmbed(user, null, null)).queue();
    }

    public void postAutoModAction(User user, Punishment punishment, String reason) {
        //if (hasModLog())
            //getModLogChannel().sendMessage(punishment.getActionEmbed(user, null, reason)).queue();
    }

    public Option getOption(String s) {
        for (Option option : options)
            if (option.getKey().equals(s)) return option;
        return null;
    }
}
