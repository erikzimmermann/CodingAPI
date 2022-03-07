package de.codingair.codingapi.server.specification;

import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;

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
    v1_17(17),
    v1_17_1(17.1),
    v1_18(18, 18.1),
    v1_18_2(18.2)
    ;

    private static Version VERSION = null;
    private static String NAME = null;
    private static String SPECIFICATION = null;
    private static Type TYPE = null;

    static {
        load();
    }

    private final double[] id;

    Version(double... id) {
        this.id = id;
    }

    private static void load() {
        if (VERSION == null) {
            //server type
            try {
                String s = Bukkit.getVersion();
                int from = s.indexOf('-');

                if (from >= 0) {
                    from += 1;
                    int to = s.indexOf("-", from);

                    if (to >= 0) {
                        TYPE = Type.getByName(Bukkit.getVersion().substring(from, to));
                    } else TYPE = Type.UNKNOWN;
                } else TYPE = Type.UNKNOWN;
            } catch (StringIndexOutOfBoundsException ex) {
                TYPE = Type.UNKNOWN;
            }

            //version
            NAME = Bukkit.getBukkitVersion().split("-", -1)[0];
            double version = Double.parseDouble(NAME.substring(2));
            SPECIFICATION = Bukkit.getBukkitVersion().replace(NAME + "-", "");

            VERSION = byId(version);
        }
    }

    public static Version get() {
        return VERSION;
    }

    public static Type type() {
        return TYPE;
    }

    public static boolean atLeast(double version) {
        return get().id[get().id.length - 1] >= version;
    }

    public static boolean less(double version) {
        return get().id[0] < version;
    }

    private static @NotNull Version byId(double version) {
        for (Version value : Version.values()) {
            for (double id : value.id) {
                if (id == version) return value;
            }
        }

        //1.16.5 -> 1.16
        int casted = (int) version;
        for (Version value : Version.values()) {
            for (double id : value.id) {
                if (id == casted) return value;
            }
        }

        throw new IllegalArgumentException("Version not found: " + version);
    }

    @SafeVarargs
    public static <T> T since(double version, T old, T... updated) {
        Version v = byId(version);

        int diff = Version.get().ordinal() - v.ordinal();
        if (diff >= 0) {
            if (diff >= updated.length || updated[diff] == null) {
                diff = Math.min(diff, updated.length - 1);

                //go back to fewer value
                for (int i = diff; i >= 0; i--) {
                    if (updated[i] != null) return updated[i];
                }
            } else return updated[diff];
        }

        return old;
    }

    public double getId() {
        return id[id.length - 1];
    }

    public String fullVersion() {
        return NAME + " (" + SPECIFICATION + ", " + TYPE + ")";
    }

    public String getShortVersionName() {
        return Bukkit.getBukkitVersion().split("-")[0];
    }

    public boolean isBiggerThan(Version version) {
        return ordinal() > version.ordinal();
    }

    public boolean isBiggerThan(double version) {
        return id[id.length - 1] > version;
    }
}
