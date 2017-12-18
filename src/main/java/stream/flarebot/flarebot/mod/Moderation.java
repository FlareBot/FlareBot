package stream.flarebot.flarebot.mod;

import io.netty.util.internal.ConcurrentSet;
import net.dv8tion.jda.core.entities.Member;
import stream.flarebot.flarebot.mod.modlog.ModlogAction;
import stream.flarebot.flarebot.mod.modlog.ModlogEvent;
import stream.flarebot.flarebot.objects.GuildWrapper;

import java.util.Iterator;
import java.util.Set;

public class Moderation {

    // Having this as something like a Map<Long, Set<ModlogAction>> and having that as the channel ID would make a
    // little more sense memory wise but efficiency wise it is much better to get the channel from the action itself.
    private Set<ModlogAction> enabledActions;

    /**
     * Check if the passed channel ID is a "valid", this means that the channel ID belongs to that guild and that it
     * still is a valid channel.
     *
     * @param wrapper   GuildWrapper that needs to be checked
     * @param channelId The channel ID that should belong to that guild.
     * @return If the channel ID is valid and belongs to the associated Guild.
     */
    private boolean isValidChannelId(GuildWrapper wrapper, long channelId) {
        return wrapper.getGuild().getTextChannelById(channelId) != null;
    }

    /**
     * This is a map of the enabled modlog actions and which channel they post to.
     * This will never be an none-set channel (-1).
     *
     * @return The map of channelId(s) and actions to log to them channels.
     */
    public Set<ModlogAction> getEnabledActions() {
        if (enabledActions == null)
            enabledActions = new ConcurrentSet<>();
        return enabledActions;
    }

    public boolean isEventEnabled(GuildWrapper wrapper, ModlogEvent event) {
        for (ModlogAction action : getEnabledActions()) {
            if (action.getEvent() == event) {
                if (isValidChannelId(wrapper, action.getModlogChannelId()))
                    return true;
                else {
                    getEnabledActions().remove(action);
                    return false;
                }
            }
        }
        return false;
    }

    /**
     * Enable an event, the channel passed here cannot be -1 or an invalid channel ID.
     * If the channel ID is invalid or the channel ID does not belong to the Guild that is associated ith the
     * GuildWrapper passed then this method will return false.
     *
     * @param wrapper   GuildWrapper of the executed guild.
     * @param channelId The channel ID that the user wants the event enabled in.
     * @param event     The Event to be enabled and set in a certain channel.
     * @return This will either return true or false which indicated if it was successful.
     */
    public boolean enableEvent(GuildWrapper wrapper, long channelId, ModlogEvent event) {
        return channelId != -1 && isValidChannelId(wrapper, channelId) && getEnabledActions().add(event.getAction(channelId));
    }

    public void enableAllEvents(GuildWrapper wrapper, long channelId) {
        for (ModlogEvent event : ModlogEvent.values)
            enableEvent(wrapper, channelId, event);
    }

    public void enableDefaultEvents(GuildWrapper guild, long channelId) {
        for (ModlogEvent event : ModlogEvent.values) {
            if (event.isDefaultEvent())
                enableEvent(guild, channelId, event);
        }
    }

    public void disableEvent(ModlogEvent event) {
        for (ModlogAction action : getEnabledActions()) {
            if (action.getEvent() == event)
                getEnabledActions().remove(action);
        }
    }

    public void disableAllEvents() {
        this.enabledActions = new ConcurrentSet<>();
    }

    public void disableDefaultEvents() {
        for (ModlogEvent event : ModlogEvent.values) {
            if (event.isDefaultEvent())
                disableEvent(event);
        }
    }

    public boolean isEventCompacted(ModlogEvent modlogEvent) {
        for (ModlogAction action : getEnabledActions()) {
            if (action.getEvent() == modlogEvent)
                return action.isCompacted();
        }
        return false;
    }

    public boolean setEventCompact(ModlogEvent modlogEvent, boolean b) {
        for (ModlogAction action : getEnabledActions())
            if (action.getEvent() == modlogEvent)
                action.setCompacted(b);
        return b;
    }

    public void muteUser(GuildWrapper guild, Member member) {
        if (guild.getMutedRole() != null)
            guild.getGuild().getController().addRolesToMember(member, guild.getMutedRole()).queue();
    }
}
