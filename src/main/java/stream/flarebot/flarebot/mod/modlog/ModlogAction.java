package stream.flarebot.flarebot.mod.modlog;

import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.TextChannel;

public class ModlogAction {

    // Modlog channel ID
    private long modlogChannelId;
    // Event of the action.
    private ModlogEvent event;
    // If it should be compacted
    private boolean compacted;

    protected ModlogAction(long channelId, ModlogEvent event, boolean compacted) {
        this.modlogChannelId = channelId;
        this.event = event;
        this.compacted = compacted;
    }

    public ModlogEvent getEvent() {
        return event;
    }

    public boolean isCompacted() {
        return compacted;
    }

    public void setCompacted(boolean compacted) {
        this.compacted = compacted;
    }

    public long getModlogChannelId() {
        return modlogChannelId;
    }

    public void setModlogChannelId(long channelId) {
        this.modlogChannelId = channelId;
    }

    public TextChannel getModlogChannel(Guild guild) {
        return guild.getTextChannelById(modlogChannelId);
    }
}
