package stream.flarebot.flarebot.database;

import io.github.binaryoverload.JSONConfig;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import stream.flarebot.flarebot.FlareBot;

public class RedisController {
    private static JedisPool jedisPool;

    private RedisController() {
    }

    public RedisController(JSONConfig config) {
        jedisPool = new JedisPool(
                new JedisPoolConfig(),
                config.getString("redis.host").get(),
                Integer.parseInt(config.getString("redis.port").get()),
                3000,
                config.getString("redis.password").get());
        try (Jedis jedis = jedisPool.getResource()) {
            FlareBot.LOGGER.info("Redis started with a DB Size of {}", jedis.dbSize());
        } catch (Exception e) {
            FlareBot.LOGGER.error("Could not connect to redis!", e);
        }
    }

    public static JedisPool getJedisPool() {
        return jedisPool;
    }

    public static String queryRedis(String key) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.get(key);
        }
    }


    /**
     * Expires a key after a certain amount of time
     *
     * @param key     The key to set the expiry on
     * @param seconds The amount of seconds to expire after
     * @return {@code 0} if the key does not exist. <br>
     * {@code 1} if the expiry was successfully set.
     */
    public static Long expire(String key, int seconds) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.expire(key, seconds);
        }
    }

    /**
     * Expires a key after a certain amount of milliseconds
     *
     * @param key    The key to set the expiry on
     * @param millis The number of milliseconds to expire after
     * @return {@code 0} if the key does not exist. <br>
     * {@code 1} if the expiry was successfully set.
     */
    public static Long pExpire(String key, long millis) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.pexpire(key, millis);
        }
    }

    /**
     * Expires a key after at a certain unix time in seconds.
     * <i>If the unix time is in the past the key is deleted</i>
     *
     * @param key      The key to set the expiry on
     * @param unixTime The unix time in seconds to expire the key at
     * @return {@code 0} if the key does not exist. <br>
     * {@code 1} if the expiry was successfully set.
     */
    public static Long expireAt(String key, long unixTime) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.expireAt(key, unixTime);
        }
    }

    /**
     * Expires a key after at a certain unix time in milliseconds.
     * <i>If the unix time is in the past the key is deleted</i>
     *
     * @param key      The key to set the expiry on
     * @param unixTime The unix time in milliseconds to expire the key at
     * @return {@code 0} if the key does not exist. <br>
     * {@code 1} if the expiry was successfully set.
     */
    public static Long pExpireAt(String key, long unixTime) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.pexpireAt(key, unixTime);
        }
    }

    /**
     * Gets the TTL in seconds for the specified key
     *
     * @param key The key to check for TTL
     * @return The TTL of the specified key in seconds. <br>
     * {@code -2} if the key does not exist. <br>
     * {@code -1} if the key exists but has no associated expire.
     */
    public static Long ttl(String key) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.ttl(key);
        }
    }

    /**
     * Gets the TTL in milliseconds for the specified key
     *
     * @param key The key to check for TTL
     * @return The TTL of the specified key in milliseconds. <br>
     * {@code -2} if the key does not exist. <br>
     * {@code -1} if the key exists but has no associated expire.
     */
    public static Long pttl(String key) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.pttl(key);
        }
    }

}
