package de.codingair.codingapi.tools.io.utils;

import de.codingair.codingapi.tools.io.JSON.JSON;
import de.codingair.codingapi.tools.io.JSON.JSONParser;
import de.codingair.codingapi.tools.io.lib.JSONArray;
import de.codingair.codingapi.tools.io.lib.ParseException;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

public interface DataMask {

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

    Set<String> keySet(boolean depth);

    Object remove(String key);

    Object finalCommit(String key, Object value);

    Boolean getBoolean(String key, Boolean def);

    default Boolean getBoolean(String key) {
        return getBoolean(key, false);
    }

    Integer getInteger(String key, Integer def);

    default Integer getInteger(String key) {
        return getInteger(key, 0);
    }

    default Byte getByte(String key) {
        return getByte(key, (byte) 0);
    }

    default Byte getByte(String key, Byte def) {
        Number n = getInteger(key, null);
        return n == null ? def : n.byteValue();
    }

    JSONArray getList(String key);

    Long getLong(String key, Long def);

    default Long getLong(String key) {
        return getLong(key, 0L);
    }

    default Date getDate(String key, Date def) {
        Long o = getLong(key, null);
        return o == null ? def : new Date(o);
    }

    default Date getDate(String key) {
        return getDate(key, null);
    }

    Double getDouble(String key, Double def);

    default Double getDouble(String key) {
        return getDouble(key, 0D);
    }

    Float getFloat(String key, Float def);

    default Float getFloat(String key) {
        return getFloat(key, 0F);
    }

    default String getString(String key, String def) {
        Object o = get(key);
        return o == null ? def : o + "";
    }

    default String getString(String key) {
        return getString(key, null);
    }

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

    default void clear() {
        keySet(false).forEach(this::remove);
    }
}
