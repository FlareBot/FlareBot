package stream.flarebot.flarebot.metrics;

import net.dv8tion.jda.core.JDA;
import org.slf4j.LoggerFactory;
import stream.flarebot.flarebot.FlareBot;
import stream.flarebot.flarebot.Getters;

public class BotMetrics {

    private long guildCount;
    private long userCount;
    private long textChannelCount;
    private long voiceChannelCount;

    public boolean count() {
        LoggerFactory.getLogger(BotMetrics.class).info("count() called");

        if (FlareBot.instance().getShardManager() == null) return false;
        for (JDA jda : Getters.getShards()) {
            if (jda.getStatus() != JDA.Status.CONNECTED)
                return false;
        }

        this.guildCount = Getters.getGuildCache().size();
        this.userCount = Getters.getUserCache().size();
        this.textChannelCount = Getters.getTextChannelCache().size();
        this.voiceChannelCount = Getters.getVoiceChannelCache().size();

        return true;
    }

    public long getGuildCount() {
        return guildCount;
    }

    public long getUserCount() {
        return userCount;
    }

    public long getTextChannelCount() {
        return textChannelCount;
    }

    public long getVoiceChannelCount() {
        return voiceChannelCount;
    }
}
