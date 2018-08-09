package de.codingair.codingapi.tools.items;

import org.bukkit.DyeColor;
import org.bukkit.Material;

public enum MultiItemType {
    IRON_FENCE("IRON_FENCE", "IRON_BARS"),
    SIGN_POST("SIGN_POST", "SIGN"),
    RECORD_5("RECORD_5", "MUSIC_DISC_WAIT"),
    FLOWER_POT_ITEM("FLOWER_POT_ITEM", "FLOWER_POT"),
    WATCH("WATCH", "CLOCK"),
    LEAVES("LEAVES", "OAK_LEAVES"),
    EYE_OF_ENDER("EYE_OF_ENDER", "ENDER_EYE"),
    SKULL_ITEM("SKULL_ITEM", "PLAYER_HEAD"),
    WOOL("WOOL", "WOOL", true),
    STAINED_GLASS("STAINED_GLASS", "STAINED_GLASS", true),
    STAINED_GLASS_PANE("STAINED_GLASS_PANE", "STAINED_GLASS_PANE", true),
    STAINED_CLAY("STAINED_CLAY", "TERRACOTTA", true),
    ;

    private String oldName;
    private String newName;
    private boolean colorable;

    MultiItemType(String oldName, String newName) {
        this(oldName, newName, false);
    }

    MultiItemType(String oldName, String newName, boolean colorable) {
        this.oldName = oldName;
        this.newName = newName;
        this.colorable = colorable;
    }

    public String getOldName() {
        return oldName;
    }

    public String getNewName() {
        return newName;
    }

    public boolean isColorable() {
        return colorable;
    }

    public Material getMaterial() {
        assert !isColorable();

        Material m;
        try {
            m = Material.matchMaterial(newName);
            if(m == null) m = Material.matchMaterial(oldName);
        } catch(Exception ex) {
            m = Material.matchMaterial(oldName);
        }

        return m;
    }

    public Material getMaterial(DyeColor color) {
        Material m;

        try {
            m = Material.matchMaterial(color + "_" + newName);
            if(m == null) m = Material.matchMaterial(oldName);
        } catch(Exception ex) {
            m = Material.matchMaterial(oldName);
        }

        return m;
    }
}
