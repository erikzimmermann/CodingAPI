package de.codingair.codingapi.tools.items;

import de.codingair.codingapi.server.specification.Version;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

public class ItemHelper {

    public static int getDurability(@NotNull ItemStack itemStack) {
        if (Version.atLeast(13)) {
            ItemMeta meta = itemStack.getItemMeta();
            if (meta instanceof Damageable) return ((Damageable) meta).getDamage();
            else return 0;
        } else {
            //deprecated since 1.13
            //noinspection deprecation
            return itemStack.getDurability();
        }
    }

    public static void setDurability(@NotNull ItemStack itemStack, int damage) {
        if (Version.atLeast(13)) {
            ItemMeta meta = itemStack.getItemMeta();
            if (meta instanceof Damageable) ((Damageable) meta).setDamage(damage);
        } else {
            //deprecated since 1.13
            //noinspection deprecation
            itemStack.setDurability((short) damage);
        }
    }

    public static byte getData(@NotNull ItemStack itemStack) {
        if (Version.less(13) && itemStack.getData() != null) {
            //already deprecated in 1.8

            //noinspection deprecation
            return itemStack.getData().getData();
        } else return 0;
    }

    public static void setData(@NotNull ItemStack itemStack, byte data) {
        if (Version.less(13) && itemStack.getData() != null) {
            //already deprecated in 1.8

            //noinspection deprecation
            itemStack.getData().setData(data);
        }
    }

}
