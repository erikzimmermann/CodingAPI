package de.codingair.codingapi.tools.items.expansions;

import org.bukkit.DyeColor;
import org.bukkit.block.banner.Pattern;
import org.bukkit.block.banner.PatternType;
import org.bukkit.inventory.meta.BannerMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BannerValue {
    private static boolean available;

    static {
        try {
            Class.forName("org.bukkit.inventory.meta.BannerMeta");
            available = true;
        } catch (ClassNotFoundException e) {
            available = false;
        }
    }

    public static @Nullable List<Map<String, Object>> serialize(@NotNull ItemMeta itemMeta) {
        if (!available || !(itemMeta instanceof BannerMeta)) return null;

        List<Map<String, Object>> data = new ArrayList<>();

        BannerMeta meta = (BannerMeta) itemMeta;
        for (Pattern pattern : meta.getPatterns()) {
            Map<String, Object> map = new HashMap<>();
            map.put("color", pattern.getColor().name());
            map.put("pattern", pattern.getPattern().getIdentifier());
            data.add(map);
        }

        return data;
    }

    public static void apply(ItemMeta itemMeta, List<Map<String, Object>> data) {
        if (!available || !(itemMeta instanceof BannerMeta)) return;

        BannerMeta meta = (BannerMeta) itemMeta;
        for (Map<String, Object> map : data) {
            try {
                DyeColor dyeColor = DyeColor.valueOf((String) map.get("color"));
                PatternType pattern = PatternType.getByIdentifier((String) map.get("pattern"));
                if (pattern != null) meta.addPattern(new Pattern(dyeColor, pattern));
            } catch (IllegalArgumentException ignored) {
            }
        }
    }
}
