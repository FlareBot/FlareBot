package stream.flarebot.flarebot.mod;

import org.eclipse.jetty.util.ConcurrentHashSet;

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
}
