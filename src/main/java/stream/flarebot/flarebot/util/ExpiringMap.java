package stream.flarebot.flarebot.util;

import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Function;

public class ExpiringMap<K, V> {

    private final TreeMap<Long, ConcurrentMap<K, V>> elem;
    private final long expireAfterMS;

    public ExpiringMap(long expireAfterMS) {
        this.expireAfterMS = expireAfterMS;
        elem = new TreeMap<>();
    }

    public void purge() {
        this.purge(false);
    }

    public void purge(boolean force) {
        long issueMS = System.currentTimeMillis();
        Iterator<Map.Entry<Long, ConcurrentMap<K, V>>> e = elem.entrySet().iterator();
        while (e.hasNext()) {
            Map.Entry<Long, ConcurrentMap<K, V>> a = e.next();
            if (issueMS >= a.getKey() || force) {
                e.remove();
                System.out.println(e);
            }
            else
                break;
        }
    }

    public void put(K k, V v) {
        long ms = System.currentTimeMillis() + expireAfterMS;
        ConcurrentMap<K, V> c = elem.get(ms);
        if (c == null) {
            c = new ConcurrentHashMap<>();
            c.put(k, v);
            elem.put(ms, c);
        } else {
            c.put(k, v);
        }
    }

    public boolean containsKey(K k) {
        for(ConcurrentMap<K, V> map : elem.values()) {
            if(map.containsKey(k))
                return true;
        }
        return false;
    }

    public boolean containsValue(V v) {
        for(ConcurrentMap<K, V> map : elem.values()) {
            if(map.containsValue(v))
                return true;
        }
        return false;
    }

    public V get(K k) {
        for(ConcurrentMap<K, V> map : elem.values()) {
            if(map.get(k) != null)
                return map.get(k);
        }
        return null;
    }

    public long getValue(K k) {
        for(Long l : elem.keySet()){
            if(elem.get(l).get(k) != null)
                return l;
        }
        return -1;
    }

    public V computeIfAbsent(K key, Function<? super K, ? extends V> mappingFunction) {
        if (key == null || mappingFunction == null) {
            throw new NullPointerException();
        }

        for(ConcurrentMap<K, V> map : elem.values()) {
            if(map.get(key) != null)
                return map.get(key);
        }

        V val = mappingFunction.apply(key);

        this.put(key, val);
        return val;
    }
}