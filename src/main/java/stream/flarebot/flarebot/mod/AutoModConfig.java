package stream.flarebot.flarebot.mod;

import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;
import org.eclipse.jetty.util.ConcurrentHashSet;

import java.awt.*;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class AutoModConfig {

    //TODO: Disable by default. Enabled for testing
    private boolean enabled = true;

    private String modLogChannel;
    private Map<Action, Integer> actions = new ConcurrentHashMap<>();
    private Map<Action, ConcurrentHashSet<String>> whitelist = new ConcurrentHashMap<>();
    private Map<Integer, Punishment> punishments = new ConcurrentHashMap<>();
    private int maxMessagesPerMinute = 10;

    public boolean isEnabled() {
        return this.enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getModLogChannel() {
        return this.modLogChannel;
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
        punishments.put(3, new Punishment(Punishment.EPunishment.TEMP_MUTE, 259200));
        punishments.put(5, new Punishment(Punishment.EPunishment.TEMP_BAN, 604800));
        punishments.put(10, new Punishment(Punishment.EPunishment.BAN));
    }

    public void postToModLog(TextChannel channel, User user, Action action) {
        if (isEnabled()) {
            if (getModLogChannel() != null && !getModLogChannel().isEmpty() && channel.getGuild()
                    .getTextChannelById(getModLogChannel()) != null) {
                channel.getGuild().getTextChannelById(getModLogChannel())
                        .sendMessage(new EmbedBuilder().setTitle("FlareBot AutoMod", null)
                                .setDescription("Message sent by "
                                        + user
                                        .getAsMention() + " has been automatically deleted in " + channel
                                        .getAsMention() + " and has been given " + getActions()
                                        .get(action) + " points.")
                                .addField("Reason", action.getName(), true).setColor(Color.white)
                                .build()).queue();
            }
        }
    }

    public void postToModLog(TextChannel channel, User user, User responsible, Punishment.EPunishment action, String reason) {
        if (isEnabled()) {
            if (getModLogChannel() != null && !getModLogChannel().isEmpty() && channel.getGuild()
                    .getTextChannelById(getModLogChannel()) != null) {
                String desc = "";
                switch (action) {
                    case PURGE:
                        desc = user.getName() + " has been purged by " + responsible.getName();
                        break;
                    case TEMP_MUTE:
                        desc = user.getName() + " has been temporarily muted by " + responsible.getName();
                        break;
                    case MUTE:
                        desc = user.getName() + " has been muted by " + responsible.getName();
                        break;
                    case KICK:
                        desc = user.getName() + " has been kicked by " + responsible.getName();
                        break;
                    case TEMP_BAN:
                        desc = user.getName() + " has been temporarily banned by " + responsible.getName();
                        break;
                    case BAN:
                        desc = user.getName() + " has been banned by " + responsible.getName();
                        break;
                }

                channel.getGuild().getTextChannelById(getModLogChannel())
                        .sendMessage(new EmbedBuilder().setTitle("FlareBot AutoMod", null)
                                .setDescription(desc + "\nReason: " + reason)
                                .setColor(Color.white).build()).queue();
            }
        }
    }
}
