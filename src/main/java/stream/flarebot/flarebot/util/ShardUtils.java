package stream.flarebot.flarebot.util;

import net.dv8tion.jda.core.JDA;
import stream.flarebot.flarebot.FlareBot;

public class ShardUtils {

    private static final FlareBot flareBot = FlareBot.getInstance();
    private static final long POSSIBLE_DEAD_SHARD_TIMEOUT = 15_000L;

    private static int getShardCount() {
        return flareBot.getShardManager().getShards().size();
    }

    /**
     * Get the shard ID of a JDA instance, if the JDA instance doesn't have ShardInfo (aka not sharded) then it will
     * return 0.
     *
     * @param jda The JDA instance of a certain shard.
     * @return The JDA shard ID as an integer.
     */
    public static int getShardId(JDA jda) {
        if (jda.getShardInfo() == null) return 0;
        return jda.getShardInfo().getShardId();
    }

    /**
     * Get the "display" shard ID, this is basically the normal shard ID + 1 so that it is no longer 0 indexed.
     *
     * @param jda The JDA instance of a certain shard.
     * @return The JDA shard ID as an integer + 1.
     */
    public static int getDisplayShardId(JDA jda) {
        return getShardId(jda) + 1;
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
        return shardId >= 0 && shardId <= getShardCount() && (getShardById(shardId).getStatus() ==
                JDA.Status.RECONNECT_QUEUED || getShardById(shardId).getStatus() == JDA.Status.ATTEMPTING_TO_RECONNECT);
    }

    public static boolean isDead(JDA jda) {
        return isDead(jda.getShardInfo().getShardId(), POSSIBLE_DEAD_SHARD_TIMEOUT);
    }

    public static boolean isDead(int shardId) {
        return isDead(shardId, POSSIBLE_DEAD_SHARD_TIMEOUT);
    }

    public static boolean isDead(JDA jda, long timeout) {
        return isDead(jda.getShardInfo().getShardId(), timeout);
    }

    public static boolean isDead(int shardId, long timeout) {
        return shardId >= 0 && shardId <= getShardCount() && getLastEventTime(shardId) >= timeout && !isReconnecting(shardId);
    }

    public static long[] getPingsForShards() {
        long[] pings = new long[flareBot.getShards().size()];
        for (int shardId = 0; shardId < pings.length; shardId++)
            pings[shardId] = getShardById(shardId).getPing();
        return pings;
    }
}
