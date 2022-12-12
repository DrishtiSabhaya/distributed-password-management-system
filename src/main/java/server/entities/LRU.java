package server.entities;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

//An LRU map with capacity 10 based on
// https://leetcode.com/problems/lru-cache/discuss/45939/Laziest-implementation:-Java%27s-LinkedHashMap-takes-care-of-everything
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