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
        else return super.put(key, value);
    }

    public <T> T get(String key) {
        Object o = super.get(key);

        if(o instanceof Long) {
            long l = (long) o;
            if(l <= Integer.MAX_VALUE && l >= Integer.MIN_VALUE) return (T) (Object) Math.toIntExact(l);
        }

        return o == null ? null : (T) o;
    }
}
