package de.codingair.codingapi.server.specification;

import org.bukkit.Bukkit;

public enum Version {
    UNKNOWN(0),
    v1_7(7),
    v1_8(8),
    v1_9(9),
    v1_10(10),
    v1_11(11),
    v1_12(12),
    v1_13(13),
    v1_14(14),
    v1_15(15),
    v1_16(16),
    ;

    private static Version VERSION = null;
    private static String NAME = null;
    private static String SPECIFICATION = null;
    private static Type TYPE = null;
    private final int id;

    Version(int id) {
        this.id = id;
    }

    public static Version get() {
        return VERSION;
    }

    public static Type type() {
        return TYPE;
    }

    public static void load() {
        if(VERSION == null) {
            //server type
            String s = Bukkit.getVersion();
            int i = s.indexOf('-') + 1;
            TYPE = Type.getByName(Bukkit.getVersion().substring(i, s.indexOf("-", i)));

            //version
            NAME = Bukkit.getBukkitVersion().split("-", -1)[0];
            int versionId = Integer.parseInt(NAME.split("\\.")[1]);
            SPECIFICATION = Bukkit.getBukkitVersion().replace(NAME + "-", "");

            for(Version value : values()) {
                if(value.id == versionId) {
                    VERSION = value;
                    break;
                }
            }
        }
    }

    public int getId() {
        return id;
    }

    public String fullVersion() {
        return NAME + " (" + SPECIFICATION + ", " + TYPE + ")";
    }

    public String getShortVersionName() {
        return Bukkit.getBukkitVersion().split("-")[0];
    }

    public boolean isBiggerThan(Version version) {
        return id > version.id;
    }

    public boolean isBiggerThan(int version) {
        return id > version;
    }
}
