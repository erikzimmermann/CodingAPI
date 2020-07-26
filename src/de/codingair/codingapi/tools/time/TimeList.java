package de.codingair.codingapi.tools.time;

import de.codingair.codingapi.API;
import de.codingair.codingapi.utils.Ticker;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Predicate;

public class TimeList<E> extends ArrayList<E> implements Ticker {
    private final HashMap<E, Long> time = new HashMap<>();
    private final ReentrantLock lock = new ReentrantLock();

    public TimeList(int initialCapacity) {
        super(initialCapacity);
        API.addTicker(this);
    }

    public TimeList() {
        API.addTicker(this);
    }

    public TimeList(Collection<? extends E> c) {
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

    public void add(int index, E e, long expire) {
        lock.lock();
        try {
            this.time.put(e, System.currentTimeMillis() + expire);
        } finally {
            lock.unlock();
        }
        super.add(index, e);
    }

    public void add(int index, E e, int expire) {
        add(index, e, expire * 1000L);
    }

    public boolean addAll(Collection<? extends E> c, int expire) {
        boolean suc = true;

        for(E e : c) {
            if(!add(e, expire)) suc = false;
        }

        return suc;
    }

    public void addAll(int index, Collection<? extends E> c, int expire) {
        for(E e : c) {
            add(index, e, expire);
        }
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
    public E remove(int index) {
        E e = super.remove(index);
        if(e != null) {
            lock.lock();
            try {
                time.remove(e);
            } finally {
                lock.unlock();
            }
        }
        return e;
    }

    @Override
    protected void removeRange(int fromIndex, int toIndex) {
        lock.lock();
        try {
            for(int i = fromIndex; i < toIndex; i++) {
                this.time.remove(super.get(i));
            }
        } finally {
            super.removeRange(fromIndex, toIndex);
            lock.unlock();
        }
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

    public void setExpire(E e, int expire) {
        lock.lock();
        try {
            if(expire > 0) this.time.replace(e, System.currentTimeMillis() + expire * 1000);
            else remove(e);
        } finally {
            lock.unlock();
        }
    }
}
