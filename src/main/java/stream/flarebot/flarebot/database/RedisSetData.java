package stream.flarebot.flarebot.database;

import redis.clients.jedis.Jedis;

public class RedisSetData {

    private String key;
    private String value;
    private String nxxx;
    private String pxex;
    private Long time;

    public RedisSetData(String key, String value) {
        this(key, value, null, null, null);
    }

    public RedisSetData(String key, String value, String nxxx) {
        this(key, value, nxxx, null, null);
    }

    public RedisSetData(String key, String value, String nxxx, String pxex, Long time) {
        this.key = key;
        this.value = value;
        this.nxxx = nxxx;
        this.pxex = pxex;
        this.time = time;
    }

    public String getNxxx() {
        return nxxx;
    }

    public String getValue() {
        return value;
    }

    public String getKey() {
        return key;
    }

    public String getPxex() {
        return pxex;
    }

    public long getTime() {
        return time;
    }

    public void set(Jedis jedis) {
        if (getNxxx() == null && getPxex() == null) {
            jedis.set(getKey(), getValue());
        } else if (getNxxx() != null && getPxex() == null) {
            jedis.set(getKey(), getValue(), getNxxx());
        } else if (getNxxx() != null && getPxex() != null) {
            jedis.set(getKey(), getValue(), getNxxx(), getPxex(), getTime());
        }
    }
}
