package de.codingair.codingapi.tools.io.utils;

import de.codingair.codingapi.tools.Location;
import de.codingair.codingapi.tools.items.ItemBuilder;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.Date;

public interface SpigotDataMask extends DataMask {

    @Override
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

    @Override
    default Location getLocation(String key) {
        Location l = new Location();
        getSerializable(key, l);

        if(l.isEmpty()) return null;
        return l;
    }

    @Override
    default ItemBuilder getItemBuilder(String key) {
        ItemBuilder builder = new ItemBuilder();
        return getSerializable(key, builder);
    }

    @Override
    default ItemStack getItemStack(String key) {
        ItemBuilder data = getItemBuilder(key);
        return data == null ? null : data.getItem();
    }
}
