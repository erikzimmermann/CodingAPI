package de.codingair.codingapi.server;

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

    public static Version version = null;
    private int id;

    Version(int id) {
        this.id = id;
    }

    public static Version getVersion() {
        if(version == null) {
            int versionId = Integer.parseInt(Bukkit.getBukkitVersion().split("-", -1)[0].split("\\.")[1]);

            for(Version value : values()) {
                if(value.id == versionId) {
                    version = value;
                    break;
                }
            }
        }

        return version;
    }

    public int getId() {
        return id;
    }

    public String getVersionName() {
        String name = Bukkit.getBukkitVersion().split("-")[0];
        name += " (" + Bukkit.getBukkitVersion().replace(name + "-", "") + ")";
        return name;
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
