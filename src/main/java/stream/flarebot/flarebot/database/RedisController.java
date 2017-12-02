package stream.flarebot.flarebot.database;

import io.github.binaryoverload.JSONConfig;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisMonitor;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import stream.flarebot.flarebot.FlareBot;

import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public class RedisController {

    private static JedisPool jedisPool;

    public static BlockingQueue<RedisSetData> setQueue = new LinkedBlockingQueue<>();

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
            String response = jedis.ping();
            if (!("PONG".equals(response))) throw new IOException("Ping to server failed!");
            FlareBot.LOGGER.info("Redis started with a DB Size of {}", jedis.dbSize());
        } catch (Exception e) {
            FlareBot.LOGGER.error("Could not connect to redis!", e);
            return;
        }
        new Thread(() -> {
            try (Jedis jedis = RedisController.getJedisPool().getResource()) {
                jedis.monitor(new JedisMonitor() {
                    public void onCommand(String command) {
                        String finalCommand = command;
                        if (command.contains("AUTH")) finalCommand = "AUTH";
                        FlareBot.LOGGER.debug("Executing redis command: {}", command.substring(finalCommand.lastIndexOf("]") + 1).trim());
                    }
                });
            }
        }, "Redis-Monitor").start();
        Thread setThread = new Thread(() -> {
            while (!FlareBot.EXITING.get()) {
                try {
                    RedisSetData data = RedisController.setQueue.poll(2, TimeUnit.SECONDS);
                    if (data != null) {
                        try (Jedis jedis = RedisController.getJedisPool().getResource()) {
                            data.set(jedis);
                            FlareBot.LOGGER.debug("Saving redis value with key: " + data.getKey());
                        }
                    }

                } catch (Exception e) {
                    FlareBot.LOGGER.error("Error in set thread!", e);
                }
            }
        }, "Redis-SetThread");
        setThread.start();
    }

    public static JedisPool getJedisPool() {
        return jedisPool;
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

    /**
     * Sets a value with a specific key in the datebase
     *
     * @param key   The key to set
     * @param value The value to set at the key
     */
    public static void set(String key, String value) {
        setQueue.add(new RedisSetData(key, value));
    }

    /**
     * Sets a value with a specific key in the datebase
     *
     * @param key   The key to set
     * @param value The value to set at the key
     * @param nxxx  {@code NX} to set the key only if doesn't exist <br>
     *              {@code XX} to set the key only if it exists <br>
     *              Otherwise use {@link RedisController#set(String, String)}
     */
    public static void set(String key, String value, String nxxx) {
        setQueue.add(new RedisSetData(key, value, nxxx));
    }

    /**
     * Sets a value with a specific key in the datebase
     *
     * @param key   The key to set
     * @param value The value to set at the key
     * @param nxxx  {@code NX} to set the key only if doesn't exist <br>
     *              {@code XX} to set the key only if it exists <br>
     *              Otherwise use empty value
     * @param pxex  {@code PX} to set the expiry in milliseconds <br>
     *              {@code EX} to set the expiry in seconds <br>
     *              Otherwise use {@link RedisController#set(String, String, String)}
     * @param time  The expiry time to set
     */
    public static void set(String key, String value, String nxxx, String pxex, long time) {
        setQueue.add(new RedisSetData(key, value, nxxx, pxex, time));
    }

    /**
     * Gets a value from the database
     *
     * @param key The key to get from redis
     * @return The value of the key or {@code null} if the key doesn't exist
     */
    public static String get(String key) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.get(key);
        }
    }

    /**
     * Deletes one or more keys from Eedis
     *
     * @param keys The keys to remove
     * @return The amount of keys that were removed
     */
    public static Long del(String... keys) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.del(keys);
        }
    }

    /**
     * Check whether a key exists
     *
     * @param key The key to check
     * @return Whether the specified key exists or not
     */
    public static boolean exists(String key) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.exists(key);
        }
    }


}
