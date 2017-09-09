package stream.flarebot.flarebot.util.expiringmap;

import stream.flarebot.flarebot.util.Pair;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Function;

public class ExpiringMap<K, V> {

    // Expire time, Pair<ConcurrentMap<K, V>, Last retrieved>
    private final TreeMap<Long, Pair<ConcurrentMap<K, V>, Long>> elem;
    private final long expireAfterMS;
    private final ExpiredEvent<K, V> expiredEvent;

    public ExpiringMap(long expireAfterMS) {
        this.expireAfterMS = expireAfterMS;
        elem = new TreeMap<>();
        this.expiredEvent = new ExpiredEvent<K, V>() {
            public void run(K k, V v, long expired, long last_retrieved) {}
        };
    }

    public ExpiringMap(long expireAfterMS, ExpiredEvent<K, V> expiredEvent) {
        this.expireAfterMS = expireAfterMS;
        elem = new TreeMap<>();
        this.expiredEvent = expiredEvent;
    }

    public void purge() {
        this.purge(false);
    }

    public void purge(boolean force) {
        long issueMS = System.currentTimeMillis();
        Iterator<Map.Entry<Long, Pair<ConcurrentMap<K, V>, Long>>> e = elem.entrySet().iterator();
        while (e.hasNext()) {
            Map.Entry<Long, Pair<ConcurrentMap<K, V>, Long>> a = e.next();
            if (issueMS >= a.getKey() || force) {
                if(expiredEvent != null) {
                    for (K k : a.getValue().getKey().keySet()) {
                        expiredEvent.run(k, a.getValue().getKey().get(k), a.getKey(), a.getValue().getValue());
                    }
                    if (!expiredEvent.isCancelled())
                        e.remove();
                }
            } else
                break;
        }
    }

    public void put(K k, V v) {
        long ms = System.currentTimeMillis() + expireAfterMS;
        Pair<ConcurrentMap<K, V>, Long> c = elem.get(ms);
        if (c == null) {
            c = new Pair<>();
            c.setKeyValue(new ConcurrentHashMap<>(), ms);
            c.getKey().put(k, v);
            elem.put(ms, c);
        } else {
            c.getKey().put(k, v);
        }
    }

    public boolean containsKey(K k) {
        for (Pair<ConcurrentMap<K, V>, Long> pair : elem.values()) {
            if (pair.getKey().containsKey(k))
                return true;
        }
        return false;
    }

    public boolean containsValue(V v) {
        for (Pair<ConcurrentMap<K, V>, Long> pair : elem.values()) {
            if (pair.getKey().containsValue(v))
                return true;
        }
        return false;
    }

    public V get(K k) {
        for (Pair<ConcurrentMap<K, V>, Long> pair : elem.values()) {
            if (pair.getKey().containsKey(k))
                return pair.getKey().get(k);
        }
        return null;
    }

    public long getValue(K k) {
        for (Long l : elem.keySet()) {
            if (elem.get(l).getKey().get(k) != null)
                return l;
        }
        return -1;
    }

    public V computeIfAbsent(K key, Function<? super K, ? extends V> mappingFunction) {
        if (key == null || mappingFunction == null) {
            throw new NullPointerException();
        }

        for (Pair<ConcurrentMap<K, V>, Long> pair : elem.values()) {
            if (pair.getKey().get(key) != null)
                return pair.getKey().get(key);
        }

        V val = mappingFunction.apply(key);

        this.put(key, val);
        return val;
    }

    public void remove(K k) {
        for (Pair<ConcurrentMap<K, V>, Long> pair : elem.values()) {
            if (pair.getKey().containsKey(k))
                pair.getKey().remove(k);
        }
    }

    public Set<K> keySet() {
        Set<K> set = new HashSet<>();
        for(long l : this.elem.keySet()) {
            set.addAll(this.elem.get(l).getKey().keySet());
        }
        return set;
    }

    public long getLastRetrieved(K k) {
        for(Pair<ConcurrentMap<K, V>, Long> pair : elem.values()) {
            if(pair.getKey().equals(k))
                return pair.getValue();
        }
        return -1;
    }

    public void resetTime(K k) {
        for(long l : this.elem.keySet()) {
            if(this.elem.get(l).getKey().containsKey(k)) {
                this.elem.put(System.currentTimeMillis() + expireAfterMS, this.elem.get(l));
                this.elem.remove(l);
            }
        }
    }

    public int size() {
        int size = 0;
        for(Pair<ConcurrentMap<K, V>, Long> pair : elem.values()) {
            size += pair.getKey().size();
        }
        return size;
    }
}