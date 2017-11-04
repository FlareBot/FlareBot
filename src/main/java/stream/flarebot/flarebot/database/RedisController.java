package stream.flarebot.flarebot.database;

import io.github.binaryoverload.JSONConfig;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

public class RedisController {
    private static JedisPool jedisPool;

    private RedisController(){}

    public RedisController(JSONConfig config) {
        jedisPool = new JedisPool(
                new JedisPoolConfig(),
                config.getString("redis.host").get(),
                Integer.parseInt(config.getString("redis.port").get()));
    }

    public static JedisPool getJedisPool() {
        return jedisPool;
    }

    public static void runTask(RedisTask task) {
        try(Jedis jedis = jedisPool.getResource()) {
            task.execute(jedis);
        }
    }

    public static String queryRedis(String key) {
        try(Jedis jedis = jedisPool.getResource()) {
            return jedis.get(key);
        }
    }

}
