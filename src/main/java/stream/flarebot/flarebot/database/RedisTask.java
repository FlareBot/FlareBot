package stream.flarebot.flarebot.database;

import redis.clients.jedis.Jedis;

@FunctionalInterface
public interface RedisTask {

    void execute(Jedis session);
}
