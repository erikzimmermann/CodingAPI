package de.codingair.codingapi.tools.items.expansions;

import de.codingair.codingapi.server.specification.Version;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;

public class DamageableValue {
    public static int getDamage(ItemMeta meta) {
        if (Version.atLeast(13)) {
            if (meta instanceof Damageable) {
                Damageable d = (Damageable) meta;
                return d.getDamage();
            }
        }

        return 0;
    }

    public static ItemMeta setDamage(ItemMeta meta, int damage) {
        if (Version.atLeast(13)) {
            if (meta instanceof Damageable) {
                Damageable d = (Damageable) meta;
                d.setDamage(damage);
            }
        }

        return meta;
    }
}
