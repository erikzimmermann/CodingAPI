package de.codingair.codingapi.tools.time;

import de.codingair.codingapi.API;
import de.codingair.codingapi.utils.Ticker;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

public class TimeMap<K, V> extends HashMap<K, V> implements Ticker {
    private final HashMap<K, Long> time = new HashMap<>();
    private final ReentrantLock lock = new ReentrantLock();

    public TimeMap(int initialCapacity, float loadFactor) {
        super(initialCapacity, loadFactor);
        API.addTicker(this);
    }

    public TimeMap(int initialCapacity) {
        super(initialCapacity);
        API.addTicker(this);
    }

    public TimeMap() {
        API.addTicker(this);
    }

    public TimeMap(Map<? extends K, ? extends V> m) {
        super(m);
        API.addTicker(this);
    }

    public void unregister() {
        API.removeTicker(this);
    }

    @Override
    public void onTick() {

    }

    @Override
    public void onSecond() {
        try {
            lock.tryLock(100, TimeUnit.MILLISECONDS);

            try {
                time.entrySet().removeIf(entry -> {
                    if(entry.getValue() <= System.currentTimeMillis()) {
                        timeout(entry.getKey(), super.get(entry.getKey()));
                        super.remove(entry.getKey());
                        return true;
                    } else return false;
                });
            } finally {
                lock.unlock();
            }
        } catch(InterruptedException ignored) {
        }
    }

    public void timeout(K key, V value) {
    }

    public V put(K key, V value, long expire) {
        lock.lock();
        try {
            this.time.put(key, System.currentTimeMillis() + expire);
        } finally {
            lock.unlock();
        }
        return super.put(key, value);
    }

    private boolean checkValue(Object key) {
        lock.lock();
        try {
            Long t = time.get(key);
            if(t != null) {
                if(t < System.currentTimeMillis()) {
                    //remove
                    remove(key);
                    return true;
                }
            }
        } finally {
            lock.unlock();
        }


        return false;
    }

    @Override
    public boolean containsKey(Object key) {
        if(checkValue(key)) return false;
        return super.containsKey(key);
    }

    @Override
    public V get(Object key) {
        if(checkValue(key)) return null;
        return super.get(key);
    }

    @Override
    public V remove(Object key) {
        lock.lock();
        try {
            time.remove(key);
        } finally {
            lock.unlock();
        }
        return super.remove(key);
    }

    @Override
    public void clear() {
        lock.lock();
        try {
            time.clear();
        } finally {
            lock.unlock();
        }
        super.clear();
    }

    @Override
    public boolean remove(Object key, Object value) {
        if(super.remove(key, value)) {
            lock.lock();
            try {
                this.time.remove(key);
            } finally {
                lock.unlock();
            }
            return true;
        } else return false;
    }
}
