package stream.flarebot.flarebot.database;

import redis.clients.jedis.Jedis;

public class RedisSetData {

    private String key;
    private String value;
    private String nxxx;
    private String pxex;
    private Long time = 0L;

    public RedisSetData(String key, String value) {
        this(key, value, "", "", 0L);
    }

    public RedisSetData(String key, String value, String nxxx) {
        this(key, value, nxxx, "", 0L);
    }

    public RedisSetData(String key, String value, String nxxx, String pxex, Long time) {
        this.key = key;
        this.value = (value == null ? "" : value);
        this.nxxx = (nxxx == null ? "" : nxxx);
        this.pxex = (pxex == null ? "" : pxex);
        this.time = (time == null ? 0L : time);
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
        if ((getNxxx() != null && getPxex() != null) || (getNxxx() == null && getPxex() != null)) {
            jedis.set(getKey(), getValue(), getNxxx(), getPxex(), getTime());
        } else if (getNxxx() != null) {
            jedis.set(getKey(), getValue(), getNxxx());
        } else {
            jedis.set(getKey(), getValue());
        }
    }
}
