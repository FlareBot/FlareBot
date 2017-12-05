package stream.flarebot.flarebot.mod;

import io.netty.util.internal.ConcurrentSet;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.TextChannel;
import org.eclipse.jetty.util.ConcurrentHashSet;
import stream.flarebot.flarebot.FlareBot;
import stream.flarebot.flarebot.mod.modlog.ModlogAction;
import stream.flarebot.flarebot.mod.modlog.ModlogEvent;
import stream.flarebot.flarebot.objects.GuildWrapper;

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
        if (!isEventEnabled(wrapper, event)) {
            if (channelId == -1) return false;
            TextChannel tc = FlareBot.getInstance().getChannelById(channelId);
            if (tc != null && wrapper.getGuild().getTextChannelById(channelId) != null) {
                getEnabledActions().add(event.getAction(channelId));
                return true;
            }
        }
        return false;
    }

    public void enableAllEvents(GuildWrapper wrapper, long channelId) {
        for (ModlogEvent event : ModlogEvent.values())
            enableEvent(wrapper, channelId, event);
    }

    public void disableEvent(ModlogEvent event) {
        for (ModlogAction action : getEnabledActions()) {
            if (action.getEvent() == event)
                getEnabledActions().remove(action);
        }
    }

    private void initDefaultActions(long channelId) {
        Set<ModlogAction> actions = new ConcurrentHashSet<>();
        for (ModlogEvent event : ModlogEvent.values())
            if (event.isDefaultEvent())
                actions.add(event.getAction(channelId));
        this.enabledActions = actions;
    }

    public void muteUser(GuildWrapper guild, Member member) {
        if (guild.getMutedRole() != null)
            guild.getGuild().getController().addRolesToMember(member, guild.getMutedRole()).queue();
    }
}
