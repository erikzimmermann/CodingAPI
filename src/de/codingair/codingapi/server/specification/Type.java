package de.codingair.codingapi.server.specification;

public enum Type {
    BUKKIT("Bukkit"),
    SPIGOT("Spigot"),
    PAPER("Paper"),
    UNKNOWN()
    ;

    private final String name;

    Type(String name) {
        this.name = name;
    }

    Type() {
        this(null);
    }

    public String getName() {
        return name;
    }

    public static Type getByName(String name) {
        for(Type value : values()) {
            if(value == UNKNOWN) continue;
            if(value.getName().equals(name)) return value;
        }

        return UNKNOWN;
    }
}
