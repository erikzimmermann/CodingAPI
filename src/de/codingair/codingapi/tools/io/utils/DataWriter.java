package de.codingair.codingapi.tools.io.utils;

import de.codingair.codingapi.tools.io.lib.JSONArray;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.Date;
import java.util.Set;

public interface DataWriter {

    default Object put(String key, Object value) {
        if(value == null) return remove(key);
        else {
            if(value.getClass().isEnum()) value = value.toString();
            else if(value instanceof Number && ((Number) value).doubleValue() == 0) return remove(key);
            else if(value instanceof Boolean && !((Boolean) value)) return remove(key);
            else if(value.getClass().isArray() && Array.getLength(value) == 0) return remove(key);
            else if(value instanceof Collection && ((Collection<?>) value).isEmpty()) return remove(key);

            if(value instanceof Date) value = ((Date) value).getTime();

            return finalCommit(key, value);
        }
    }

    default <T> T getLocation(String key) {
        throw new IllegalStateException("Only for spigot purpose! (SpigotDataWriter)");
    }

    default <T> T getItemBuilder(String key) {
        throw new IllegalStateException("Only for spigot purpose! (SpigotDataWriter)");
    }

    default <T> T getItemStack(String key) {
        throw new IllegalStateException("Only for spigot purpose! (SpigotDataWriter)");
    }

    Set keySet();

    Object remove(String key);

    Object finalCommit(String key, Object value);

    Boolean getBoolean(String key);

    Integer getInteger(String key);

    JSONArray getList(String key);

    Long getLong(String key);

    Date getDate(String key);

    Double getDouble(String key);

    Float getFloat(String key);

    <T extends Serializable> T getSerializable(String key, Serializable serializable);

    default <T> T get(String key) {
        return get(key, (T) null);
    }

    default <T> T getRaw(String key) {
        return get(key, null, true);
    }

    default <T> T get(String key, T def) {
        return get(key, def, false);
    }

    <T> T get(String key, T def, boolean raw);

    default <T extends Enum<T>> T get(String key, Class<T> def) {
        Object o = get(key);

        if(o == null) return null;
        if(!(o instanceof String)) throw new IllegalArgumentException("Value isn't a String. Can't search for a enum!");
        String name = (String) o;

        for(T e : def.getEnumConstants()) {
            if(e.name().equals(name)) return e;
        }

        return null;
    }
}
