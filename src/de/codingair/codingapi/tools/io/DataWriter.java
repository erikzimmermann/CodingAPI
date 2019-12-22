package de.codingair.codingapi.tools.io;

import de.codingair.codingapi.tools.Location;
import de.codingair.codingapi.tools.io.JSON.JSON;
import de.codingair.codingapi.tools.items.ItemBuilder;
import org.bukkit.inventory.ItemStack;
import org.json.simple.JSONArray;

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
            if(value instanceof ItemStack) value = new ItemBuilder((ItemStack) value);

            return finalCommit(key, value);
        }
    }

    default Location getLocation(String key) {
        Location l = new Location();
        getSerializable(key, l);

        if(l.isEmpty()) return null;
        return l;
    }

    default ItemBuilder getItemBuilder(String key) {
        ItemBuilder builder = new ItemBuilder();
        return getSerializable(key, builder);
    }

    default ItemStack getItemStack(String key) {
        ItemBuilder data = getItemBuilder(key);
        return data == null ? null : data.getItem();
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

    <T extends Enum<T>> T get(String key, Class<T> def);
}
