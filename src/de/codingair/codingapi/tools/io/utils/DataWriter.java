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

    default Boolean getBoolean(String key, Boolean def) {
        Boolean o = getBoolean(key);
        return o == null ? def : o;
    }

    Boolean getBoolean(String key);

    default Integer getInteger(String key, Integer def) {
        Integer o = getInteger(key);
        return o == null ? def : o;
    }

    Integer getInteger(String key);

    default Byte getByte(String key) {
        Number n = getInteger(key);
        return n == null ? 0 : n.byteValue();
    }

    JSONArray getList(String key);

    default Long getLong(String key, Long def) {
        Long o = getLong(key);
        return o == null ? def : o;
    }

    Long getLong(String key);

    default Date getDate(String key, Date def) {
        Date o = getDate(key);
        return o == null ? def : o;
    }

    Date getDate(String key);

    default Double getDouble(String key, Double def) {
        Double o = getDouble(key);
        return o == null ? def : o;
    }

    Double getDouble(String key);

    default Float getFloat(String key, Float def) {
        Float o = getFloat(key);
        return o == null ? def : o;
    }

    Float getFloat(String key);

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
}
