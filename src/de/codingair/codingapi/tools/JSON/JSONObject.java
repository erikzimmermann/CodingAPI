package de.codingair.codingapi.tools.JSON;

import de.codingair.codingapi.tools.Location;
import de.codingair.codingapi.tools.items.ItemBuilder;
import org.bukkit.inventory.ItemStack;
import org.json.simple.parser.ParseException;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.Date;
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
            else if(value instanceof Number && ((Number) value).doubleValue() == 0) return remove(key);
            else if(value instanceof Boolean && !((Boolean) value)) return remove(key);
            else if(value.getClass().isArray() && Array.getLength(value) == 0) return remove(key);
            else if(value instanceof Collection && ((Collection) value).isEmpty()) return remove(key);

            if(value instanceof Location) value = ((Location) value).toJSONString(4);
            if(value instanceof Date) value = ((Date) value).getTime();
            if(value instanceof ItemStack) value = new ItemBuilder((ItemStack) value).toJSONString();

            return super.put(key, value);
        }
    }

    public <T> T get(String key) {
        return get(key, (T) null);
    }

    public <T> T getRaw(String key) {
        return get(key, null, true);
    }

    public Location getLocation(String key) {
        JSONObject data = get(key);
        return data == null ? null : new Location(data);
    }

    public ItemStack getItemStack(String key) {
        JSONObject data = get(key);
        return data == null ? null : ItemBuilder.getFromJSON(data).getItem();
    }

    public Boolean getBoolean(String key) {
        Boolean b = get(key);
        return b == null ? false : b;
    }

    public Integer getInteger(String key) {
        Integer i = get(key);
        return i == null ? 0 : i;
    }

    public Long getLong(String key) {
        return get(key, 0L, true);
    }

    public Date getDate(String key) {
        Long l = getLong(key);
        return l == null ? null : new Date(l);
    }

    public Double getDouble(String key) {
        Double d = get(key);
        return d == null ? 0 : d;
    }

    public Float getFloat(String key) {
        Double d = getDouble(key);
        return d.floatValue();
    }

    public <T> T get(String key, T def) {
        return get(key, def, false);
    }

    public <T> T get(String key, T def, boolean raw) {
        Object o = super.get(key);

        if(!raw) {
            if(o instanceof Long) {
                long l = (long) o;
                if(l <= Integer.MAX_VALUE && l >= Integer.MIN_VALUE) return (T) (Object) Math.toIntExact(l);
            }

            if(o instanceof String) {
                try {
                    Object result = new JSONParser().parse((String) o);
                    if(result != null) o = result;
                } catch(ParseException ignored) {
                }
            }

            if(o instanceof org.json.simple.JSONObject) return (T) new JSONObject((org.json.simple.JSONObject) o);
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
