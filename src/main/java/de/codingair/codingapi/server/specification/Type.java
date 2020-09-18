package de.codingair.codingapi.server.specification;

public enum Type {
    BUKKIT("Bukkit"),
    SPIGOT("Spigot"),
    PAPER("Paper"),
    PAPER_SPIGOT("PaperSpigot"),
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
        if(name == null) return UNKNOWN;

        for(Type value : values()) {
            if(value == UNKNOWN) continue;
            if(value.getName().equals(name)) return value;
        }

        return UNKNOWN;
    }
}
