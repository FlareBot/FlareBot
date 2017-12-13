package stream.flarebot.flarebot.util;

import net.dv8tion.jda.core.JDA;
import stream.flarebot.flarebot.FlareBot;

public class ShardUtils {

    private static final FlareBot flareBot = FlareBot.getInstance();

    public static int getShardCount() {
        return flareBot.getShardManager().getShards().size();
    }

    public static JDA getShardById(int shardId) {
        return flareBot.getShardManager().getShardById(shardId);
    }

    public static long getLastEventTime(int shardId) {
        return System.currentTimeMillis() - FlareBot.getInstance().getEvents().getShardEventTime().get(shardId);
    }

    public static boolean isReconnecting(JDA jda) {
        return isReconnecting(jda.getShardInfo().getShardId());
    }

    public static boolean isReconnecting(int shardId) {
        if (shardId < 0 || shardId > getShardCount()) return false;
        return getShardById(shardId).getStatus() == JDA.Status.RECONNECT_QUEUED ||
                getShardById(shardId).getStatus() == JDA.Status.ATTEMPTING_TO_RECONNECT;
    }

    public static boolean isDead(JDA jda) {
        return isDead(jda.getShardInfo().getShardId());
    }

    public static boolean isDead(int shardId) {
        if (shardId < 0 || shardId > getShardCount()) return false;
        return getLastEventTime(shardId) >= 5000 && !isReconnecting(shardId);
    }

    public static long[] getPingsForShards() {
        long[] pings = new long[flareBot.getShards().size()];
        for (int shardId = 0; shardId < pings.length; shardId++)
            pings[shardId] = getShardById(shardId).getPing();
        return pings;
    }
}
