package de.codingair.codingapi.tools.time;

import java.util.HashMap;
import java.util.Map;

public class TimeMap<K, V> extends HashMap<K, V> {
    private TimeList<K> time;

    public TimeMap() {
        initTimeList();
    }

    public TimeMap(Map<? extends K, ? extends V> m) {
        super(m);
        initTimeList();
    }

    public void addListener(TimeListener e) {
        this.time.addListener(e);
    }

    public void removeListener(TimeListener e) {
        this.time.removeListener(e);
    }

    private void initTimeList() {
        time = new TimeList<>();

        time.addListener(new TimeListener() {
            @Override
            public void onRemove(Object item) {
                TimeMap.this.remove(item);
            }

            @Override
            public void onTick(Object item, int timeLeft) {
            }
        });
    }

    public V put(K key, V value, int expire) {
        this.time.add(key, expire);
        return super.put(key, value);
    }

    public void putAll(Map<? extends K, ? extends V> m, int expire) {
        this.time.addAll(m.keySet(), expire);
        super.putAll(m);
    }

    @Override
    public V remove(Object key) {
        time.remove(key);
        return super.remove(key);
    }

    @Override
    public void clear() {
        time.clear();
        super.clear();
    }

    @Override
    public boolean remove(Object key, Object value) {
        return super.remove(key, value) ? time.remove(key) : false;
    }

    public int getExpire(K key) {
        return this.time.getExpire(key);
    }

    public boolean hasExpire(K key) {
        return this.time.hasExpire(key);
    }

    public boolean setExpire(K key, int expire) {
        if(hasExpire(key)) {
            if(expire > 0) this.time.setExpire(key, expire);
            else {
                remove(key);
            }

            return true;
        }

        return false;
    }
}
