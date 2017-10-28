package de.codingair.codingapi.tools;

import de.codingair.codingapi.API;
import de.codingair.codingapi.bungeecord.BungeeAPI;
import de.codingair.codingapi.utils.Ticker;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.function.Predicate;

public class TimeList<E> extends ArrayList<E> implements Ticker {
    private HashMap<E, Integer> time = new HashMap<>();

    public TimeList() {
        super();

        if(BungeeAPI.isEnabled()) BungeeAPI.addTicker(this);
        else API.addTicker(this);
    }

    public TimeList(Collection<? extends E> c) {
        super(c);

        if(BungeeAPI.isEnabled()) BungeeAPI.addTicker(this);
        else API.addTicker(this);
    }

    @Override
    public void onTick() {}

    @Override
    public void onSecond() {
        HashMap<E, Integer> time = new HashMap<>(this.time);

        for(E key : time.keySet()) {
            setExpire(key, getExpire(key) - 1);
        }

        time.clear();
    }

    @Override
    public Object getInstance() {
        return this;
    }

    public boolean add(E e, int expire) {
        this.time.put(e, expire);
        return super.add(e);
    }

    public void add(int index, E e, int expire) {
        this.time.put(e, expire);
        super.add(index, e);
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
        if(this.time.containsKey(o)) this.time.remove(o);
        return super.remove(o);
    }

    @Override
    public E remove(int index) {
        E e = super.get(index);
        remove(e);
        return e;
    }

    @Override
    protected void removeRange(int fromIndex, int toIndex) {
        for(int i = fromIndex; i < toIndex; i++) {
            if(this.time.containsKey(super.get(i))) this.time.remove(super.get(i));
        }

        super.removeRange(fromIndex, toIndex);
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        for(Object o : c) {
            if(this.time.containsKey(o)) this.time.remove(o);
        }

        return super.removeAll(c);
    }

    @Override
    public void clear() {
        super.clear();
        this.time.clear();

        if(BungeeAPI.isEnabled()) BungeeAPI.removeTicker(this);
        else API.removeTicker(this);
    }

    @Override
    public boolean removeIf(Predicate<? super E> filter) {
        if(super.removeIf(filter)) {
            HashMap<E, Integer> time = new HashMap<>(this.time);

            for(E key : time.keySet()) {
                if(!super.contains(key)) this.time.remove(key);
            }

            time.clear();

            return true;
        }

        return false;
    }

    public int getExpire(E e) {
        return this.time.get(e);
    }

    public boolean hasExpire(E e) {
        return this.time.containsKey(e);
    }

    public boolean setExpire(E e, int expire) {
        if(hasExpire(e)) {
            if(expire > 0) this.time.replace(e, expire);
            else {
                remove(e);
            }

            return true;
        }

        return false;
    }
}
