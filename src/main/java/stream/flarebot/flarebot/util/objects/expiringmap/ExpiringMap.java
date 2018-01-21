package stream.flarebot.flarebot.util.objects.expiringmap;

import stream.flarebot.flarebot.util.Pair;

import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.function.Function;

/**
 * A map that allows keys to expire dynamically.
 *
 * @param <K> The key for retrieval of entities.
 * @param <V> The value associated with the key.
 */
public class ExpiringMap<K, V> {

    // Expire time, Pair<ConcurrentMap<K, V>, Last retrieved>
    private final ConcurrentSkipListMap<Long, Pair<ConcurrentMap<K, V>, Long>> elem;
    private final ExpiredEvent<K, V> expiredEvent;

    public ExpiringMap() {
        elem = new ConcurrentSkipListMap<>();
        this.expiredEvent = new ExpiredEvent<K, V>() {
            @Override
            public void run(K k, V v, long expired, long last_retrieved) {
            }
        };
    }

    public ExpiringMap(ExpiredEvent<K, V> expiredEvent) {
        elem = new ConcurrentSkipListMap<>();
        this.expiredEvent = expiredEvent;
    }

    /**
     * Runs the purge operation <i>without</i> forcing it.
     *
     * @see ExpiringMap#purge(boolean)
     */
    public synchronized void purge() {
        this.purge(false);
    }

    /**
     * Iterates all map entries and determines whether to purge them. If the {@link ExpiredEvent} is set
     * then it is run upon each purge. Should the event cancel, the purge operation is also cancelled.
     * <p>
     * If force is set, <b>all</b> elements will be purged (Providing the associated {@link ExpiredEvent}
     * does not cancel it).
     *
     * @param force Whether to force the operation.
     */
    public synchronized void purge(boolean force) {
        long issueMS = System.currentTimeMillis();
        Iterator<Map.Entry<Long, Pair<ConcurrentMap<K, V>, Long>>> entryIterator = elem.entrySet().iterator();
        while (entryIterator.hasNext()) {
            Map.Entry<Long, Pair<ConcurrentMap<K, V>, Long>> entry = entryIterator.next();
            if (issueMS >= entry.getKey() || force) {
                if (expiredEvent != null) {
                    for (K k : entry.getValue().getKey().keySet()) {
                        expiredEvent.run(k, entry.getValue().getKey().get(k), entry.getKey(), entry.getValue().getValue());
                    }
                    if (!expiredEvent.isCancelled())
                        entryIterator.remove();
                }
            } else
                break;
        }
    }

    /**
     * Enters a specific key and value into the map with a specified time to expire after
     *
     * @param k             The key to enter into the map of type <K>.
     * @param v             The value associated with the map to enter of type <V>.
     * @param expireAfterMS The milliseconds after which the key-value will expire.
     */
    public void put(K k, V v, long expireAfterMS) {
        long ms = System.currentTimeMillis() + expireAfterMS;
        Pair<ConcurrentMap<K, V>, Long> pair = elem.get(ms);
        if (pair == null) {
            pair = new Pair<>();
            pair.setKeyValue(new ConcurrentHashMap<>(), ms);
            pair.getKey().put(k, v);
            elem.put(ms, pair);
        } else {
            pair.getKey().put(k, v);
        }
    }

    /**
     * Checks whether the map contains a key-value mapping for the specified key.
     *
     * @param k The key to check existence of of type <K>
     * @return Whether the key-value mapping is present in the map
     */
    public boolean containsKey(K k) {
        return get(k) != null;
    }

    @Deprecated
    public boolean containsValue(V v) {
        for (Pair<ConcurrentMap<K, V>, Long> pair : elem.values()) {
            return pair.getKey().containsValue(v);
        }
        return false;
    }

    /**
     * Returns the value for the key-value mapping indicated by the key.
     * <p>
     * If there is no key-value mapping for the key, the value returned is {@code null}.
     *
     * @param k The key to retrieve the value for.
     * @return The value associated with the key.
     */
    public V get(K k) {
        for (Pair<ConcurrentMap<K, V>, Long> pair : elem.values()) {
            if (pair.getKey().containsKey(k))
                pair.setValue(System.currentTimeMillis());
            return pair.getKey().get(k);
        }
        return null;
    }

    /**
     * Returns an existing key-value mapping provided by the key, if this mapping does not exist then
     * it uses the provided {@link Function} to provide a value. If the value does not exist, it also
     * uses the provided milliseconds to specify the expiry.
     *
     * @param key             The key to check.
     * @param mappingFunction The function to provide a value if the mapping does not exist.
     * @param expireAfterMs   The milliseconds to expire after if the value does not exist.
     * @return The value either provided by the mapping function or the existing key-value mapping.
     */
    public V computeIfAbsent(K key, Function<? super K, ? extends V> mappingFunction, long expireAfterMs) {
        if (key == null || mappingFunction == null) {
            throw new NullPointerException();
        }

        if (containsKey(key)) {
            return get(key);
        }

        V val = mappingFunction.apply(key);

        this.put(key, val, expireAfterMs);
        return val;
    }

    /**
     * Removes the key-value mapping from this map using the provided key. If the mapping does
     * not exist, this function does nothing.
     *
     * @param k The key to remove.
     */
    public void remove(K k) {
        for (Pair<ConcurrentMap<K, V>, Long> pair : elem.values()) {
            if (pair.getKey().containsKey(k))
                pair.getKey().remove(k);
        }
    }

    /**
     * Fetches a immutable set of all the keys associated in this map regardless of expiry time.
     *
     * @return The immutable set of all keys in this map.
     */
    public Set<K> keySet() {
        Set<K> set = new HashSet<>();
        for (Long l : elem.keySet()) {
            set.addAll(this.elem.get(l).getKey().keySet());
        }
        return Collections.unmodifiableSet(set);
    }

    /**
     * Returns the last unix time value for when the specified key was retrieved using {@link ExpiringMap#get(Object)}
     *
     * @param k The key to get the last retrieved time for.
     * @return The last retrieved time in milliseconds. If the key doesn't exist this returns -1.
     */
    public long getLastRetrieved(K k) {
        for (Long l : elem.keySet()) {
            if (elem.get(l).getKey().get(k) != null)
                return l;
        }
        return -1;
    }

    /**
     * Resets the time for the specified key to expire at. If the key doesn't exist in the map
     * this method does nothing.
     *
     * @param k             The key to reset the time for.
     * @param expireAfterMS The time after which the key should expire.
     */
    public void resetTime(K k, long expireAfterMS) {
        for (Long l : elem.keySet()) {
            if (this.elem.get(l).getKey().containsKey(k)) {
                this.elem.put(System.currentTimeMillis() + expireAfterMS, this.elem.get(l));
                this.elem.remove(l);
            }
        }
    }

    /**
     * Returns the size of this map ignoring expiry times.
     *
     * @return The size of the entire map.
     * @see ConcurrentMap#size()
     */
    public int size() {
        int size = 0;
        for (Pair<ConcurrentMap<K, V>, Long> pair : elem.values()) {
            size += pair.getKey().size();
        }
        return size;
    }
}
