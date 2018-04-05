package stream.flarebot.flarebot.objects;

import org.eclipse.jetty.util.ConcurrentHashSet;

import java.util.Arrays;
import java.util.Set;

/**
 * I'm not sure the best way to handle settings as of yet, so for now edit the SettingsCommand when changed.
 */
public class GuildSettings {

    private boolean deleteCommands;
    private Set<Long> channelBlacklist;
    private Set<Long> userBlacklist;

    public GuildSettings() {
        this.deleteCommands = true;
        this.channelBlacklist = new ConcurrentHashSet<>();
        this.userBlacklist = new ConcurrentHashSet<>();
    }

    public boolean shouldDeleteCommands() {
        return this.deleteCommands;
    }

    public void setDeleteCommands(boolean deleteCommands) {
        this.deleteCommands = deleteCommands;
    }

    public Set<Long> getChannelBlacklist() {
        return channelBlacklist;
    }

    public void addChannelToBlacklist(long channelId) {
        this.channelBlacklist.add(channelId);
    }

    public void removeChannelFromBlacklist(long channelId) {
        this.channelBlacklist.remove(channelId);
    }

    public Set<Long> getUserBlacklist() {
        return userBlacklist;
    }

    public void addUserToBlacklist(long userId) {
        this.userBlacklist.add(userId);
    }

    public void removeUserFromBlacklist(long userId) {
        this.userBlacklist.remove(userId);
    }

    @Override
    public String toString() {
        return String.format("{deleteCommands: %b, channelBlacklist: %s, userBlacklist: %s}",
                deleteCommands,
                Arrays.toString(channelBlacklist.toArray()),
                Arrays.toString(userBlacklist.toArray())
        );
    }
}
