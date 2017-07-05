package stream.flarebot.flarebot.util;

import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class ExpiringMap<K, V> {

    private final TreeMap<Long, ConcurrentMap<K, V>> elem;
    private final long expireAfterMS;

    public ExpiringMap(long expireAfterMS) {
        this.expireAfterMS = expireAfterMS;
        elem = new TreeMap<>((o1, o2) -> {
            if (o1 < o2)
                return 1;
            else
                return -1;
        });
    }

    public void purge() {
        long issueMS = System.currentTimeMillis();
        Iterator<Map.Entry<Long, ConcurrentMap<K, V>>> e = elem.entrySet().iterator();
        while (e.hasNext()) {
            Map.Entry<Long, ConcurrentMap<K, V>> a = e.next();
            if (issueMS >= a.getKey()) {
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

    public static void main(String[] args) {
        ExpiringMap<Integer, String> test = new ExpiringMap<>(1000);
        for (int i = 0; i < 1000000; i++) {
            test.put(i, String.valueOf(i));
        }

        //debug
        while (!test.elem.isEmpty()) {
            int size = test.elem.size();
            test.purge();
            if (size - test.elem.size() > 0) {
                System.out.println("Removed elements. New size: " + test.elem.size());
            }
        }
    }
}