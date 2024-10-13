package de.codingair.codingapi.server.specification;

import de.codingair.codingapi.API;

import java.util.logging.Level;

public enum Type {
    BUKKIT("Bukkit"),
    SPIGOT("Spigot"),
    PAPER("Paper"),
    PAPER_SPIGOT("PaperSpigot"),
    PURPUR("Purpur"),
    UNKNOWN("Unknown");

    private final String name;

    Type(String name) {
        this.name = name;
    }

    public static Type getByName(String name) {
        if (name == null) return UNKNOWN;

        for (Type value : values()) {
            if (value == UNKNOWN) continue;
            if (value.getName().equals(name)) return value;
        }

        if (API.getInstance().getMainPlugin() != null)
            API.getInstance().getMainPlugin().getLogger().log(Level.INFO, "Could not detect server type \"" + name + "\".");
        return UNKNOWN;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return name;
    }
}
