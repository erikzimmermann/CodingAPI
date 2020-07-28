package de.codingair.codingapi.tools.time;

import de.codingair.codingapi.API;
import de.codingair.codingapi.utils.Ticker;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Predicate;

public class TimeSet<E> extends HashSet<E> implements Ticker {
    private final HashMap<E, Long> time = new HashMap<>();
    private final ReentrantLock lock = new ReentrantLock();

    public TimeSet(int initialCapacity) {
        super(initialCapacity);
        API.addTicker(this);
    }

    public TimeSet() {
        API.addTicker(this);
    }

    public TimeSet(Collection<? extends E> c) {
        super(c);
        API.addTicker(this);
    }

    public void unregister() {
        API.removeTicker(this);
    }

    @Override
    public void onSecond() {
        try {
            lock.tryLock(100, TimeUnit.MILLISECONDS);

            try {
                time.entrySet().removeIf(entry -> {
                    if(entry.getValue() <= System.currentTimeMillis()) {
                        super.remove(entry.getKey());
                        timeout(entry.getKey());
                        return true;
                    } else return false;
                });
            } finally {
                lock.unlock();
            }
        } catch(InterruptedException ignored) {
        }
    }

    @Override
    public void onTick() {
    }

    public void timeout(E value) {
    }

    public boolean add(E e, long expire) {
        lock.lock();
        try {
            this.time.put(e, System.currentTimeMillis() + expire);
        } finally {
            lock.unlock();
        }
        return super.add(e);
    }

    public boolean add(E e, int expire) {
        return add(e, expire * 1000L);
    }

    public boolean addAll(Collection<? extends E> c, int expire) {
        boolean suc = true;

        for(E e : c) {
            if(!add(e, expire)) suc = false;
        }

        return suc;
    }

    @Override
    public boolean remove(Object o) {
        lock.lock();
        try {
            this.time.remove(o);
        } finally {
            lock.unlock();
        }
        return super.remove(o);
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        lock.lock();
        try {
            for(Object o : c) {
                this.time.remove(o);
            }
        } finally {
            lock.unlock();
        }

        return super.removeAll(c);
    }

    @Override
    public void clear() {
        lock.lock();
        try {
            this.time.clear();
        } finally {
            lock.unlock();
        }

        super.clear();
    }

    @Override
    public boolean removeIf(Predicate<? super E> filter) {
        if(super.removeIf(filter)) {
            lock.lock();
            try {
                time.entrySet().removeIf(entry -> filter.test(entry.getKey()));
            } finally {
                lock.unlock();
            }
            return true;
        } else return false;
    }

    @Override
    public boolean contains(Object o) {
        lock.lock();
        try {
            Long t = time.get(o);
            if(t != null) {
                if(t < System.currentTimeMillis()) {
                    //remove
                    remove(o);
                    timeout((E) o);
                    return false;
                }
            }
        } finally {
            lock.unlock();
        }

        return super.contains(o);
    }
}
