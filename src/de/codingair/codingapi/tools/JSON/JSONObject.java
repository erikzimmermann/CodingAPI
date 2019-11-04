package de.codingair.codingapi.tools.JSON;

import java.util.Map;

public class JSONObject extends org.json.simple.JSONObject {
    public JSONObject() {
    }

    public JSONObject(Map map) {
        super(map);
    }

    @Override
    public Object put(Object key, Object value) {
        if(value == null) return remove(key);
        else {
            if(value.getClass().isEnum()) value = value.toString();
            return super.put(key, value);
        }
    }

    public <T> T get(String key) {
        return get(key, (T) null);
    }

    public <T> T get(String key, T def) {
        Object o = super.get(key);

        if(o instanceof Long) {
            long l = (long) o;
            if(l <= Integer.MAX_VALUE && l >= Integer.MIN_VALUE) return (T) (Object) Math.toIntExact(l);
        }

        return o == null ? def : (T) o;
    }

    public <T extends Enum<T>> T get(String key, Class<T> def) {
        Object o = super.get(key);

        if(o == null) return null;
        if(!(o instanceof String)) throw new IllegalArgumentException("Value isn't a String. Can't search for a enum!");
        String name = (String) o;

        for(T e : def.getEnumConstants()) {
            if(e.name().equals(name)) return e;
        }

        return null;
    }
}
