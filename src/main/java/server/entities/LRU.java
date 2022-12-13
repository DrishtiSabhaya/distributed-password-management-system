package server.entities;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * The LRU caching scheme is to remove the least recently used frame when the cache is full and a new page is
 * referenced which is not there in the cache.
 */
public class LRU<K, V> {
    private final Map<K, V> map;

    public LRU(final int capacity) {
        this.map = Collections.synchronizedMap(new LinkedHashMap<K, V>
            (capacity, 0.75f, true) {
            protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
                return size() > capacity;
            }
        });
    }

    public Boolean containsKey(K key) {
        return map.containsKey(key);
    }

    public void put(K key, V value) {
        map.put(key, value);
    }

    public V get(K key) {
        return map.get(key);
    }

    public V remove(K key) {
        return map.remove(key);
    }
}