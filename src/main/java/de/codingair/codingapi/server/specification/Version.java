package de.codingair.codingapi.server.specification;

import de.codingair.codingapi.API;
import de.codingair.codingapi.server.reflections.IReflection;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
    v1_18_2(18.2),
    v1_19(19),
    v1_19_1(19.1),
    v1_19_2(19.2),
    v1_19_3(19.3),
    v1_19_4(19.4),
    v1_20(20, 20.1),
    v1_20_2(20.2),
    v1_20_4(20.4),
    v1_20_6(20.5, 20.6),
    v1_21(21),
    ;

    private static boolean supportWarning = true;
    private static Version VERSION = null;
    private static String NAME = null;
    private static String SPECIFICATION = null;
    private static Type TYPE = Type.UNKNOWN;

    static {
        load();
    }

    private final double[] id;

    Version(double... id) {
        this.id = id;
    }

    private static void load() {
        if (VERSION == null) {
            if (ServerBuildInfo.isAvailable()) {
                TYPE = Type.PAPER;
                NAME = "Paper";
                SPECIFICATION = ServerBuildInfo.minecraftVersionName();

                double version = Double.parseDouble(ServerBuildInfo.minecraftVersionId().substring(2));
                VERSION = byId(version);
            } else {
                // version
                String bukkitVersion = Bukkit.getVersion();

                Pattern p = Pattern.compile("\\(MC: \\d\\.\\d\\d?(\\.\\d\\d)?");
                Matcher match = p.matcher(bukkitVersion);
                if (match.find()) {
                    double version = Double.parseDouble(match.group().substring(7));
                    VERSION = byId(version);
                }

                // specification
                SPECIFICATION = Bukkit.getVersion();

                // server type
                try {
                    int from = bukkitVersion.indexOf('-');

                    if (from >= 0) {
                        from += 1;
                        int to = bukkitVersion.indexOf("-", from);

                        if (to >= 0) {
                            NAME = bukkitVersion.substring(from, to);
                            TYPE = Type.getByName(NAME);
                        }
                    }
                } catch (StringIndexOutOfBoundsException ignored) {
                }
            }
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

    public static boolean atMost(double version) {
        return get().id[0] <= version;
    }

    public static boolean later(double version) {
        return get().id[0] < version;
    }

    public static boolean before(double version) {
        return get().id[0] < version;
    }

    @Deprecated
    public static boolean less(double version) {
        return before(version);
    }

    public static boolean between(double min, double max) {
        return atLeast(min) && atMost(max);
    }

    private static @NotNull Version byId(double version) {
        Version highest = null;
        for (Version value : Version.values()) {
            for (double id : value.id) {
                if (id == version) return value;
            }

            highest = value;
        }

        //1.16.5 -> 1.16
        int casted = (int) version;
        if (highest != null && ((int) highest.id[highest.id.length - 1]) == casted) return highest;

        for (Version value : Version.values()) {
            for (double id : value.id) {
                if (id == casted) return value;
            }
        }

        if (highest != null && version > highest.getId()) {
            if (supportWarning) {
                API.getInstance().getMainPlugin().getLogger().warning("Detected version " + version + " which is not yet fully supported (highest: " + highest.getId() + ")! Use with caution!");
                supportWarning = false;
            }
            return highest;
        }

        throw new IllegalArgumentException("Version not found: " + version);
    }

    public static <T> T choose(T def, double version1, T value1) {
        return choose(def, version1, value1, 0, null);
    }

    public static <T> T choose(T def, double version1, T value1, double version2, T value2) {
        return choose(def, version1, value1, version2, value2, 0, null);
    }

    public static <T> T choose(T def, double version1, T value1, double version2, T value2, double version3, T value3) {
        return choose(def, version1, value1, version2, value2, version3, value3, 0, null);
    }

    public static <T> T choose(T def, double version1, T value1, double version2, T value2, double version3, T value3, double version4, T value4) {
        return choose(def, version1, value1, version2, value2, version3, value3, version4, value4, 0, null);
    }

    public static <T> T choose(T def, double version1, T value1, double version2, T value2, double version3, T value3, double version4, T value4, double version5, T value5) {
        return choose(def, version1, value1, version2, value2, version3, value3, version4, value4, version5, value5, 0, null);
    }

    public static <T> T choose(T def, double version1, T value1, double version2, T value2, double version3, T value3, double version4, T value4, double version5, T value5, double version6, T value6) {
        //noinspection unchecked
        T[] values = (T[]) new Object[]{def, value1, value2, value3, value4, value5, value6};
        double[] versions = new double[]{-1, version1, version2, version3, version4, version5, version6};

        for (int i = 0; i < versions.length; i++) {
            double version = versions[i];

            if (version == -1) continue;
            if (version == 0) return values[i - 1];

            int diff = Version.get().ordinal() - byId(version).ordinal();
            if (diff < 0) return values[i - 1];
        }

        return values[values.length - 1];
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

    /**
     * Wrapper for Paper's new info class.
     * Introduced in version 1.20.5 on 16th May 2024
     * (<a href="https://discord.com/channels/289587909051416579/1077385604012179486/1240455346582196348">Source</a>).
     */
    private static class ServerBuildInfo {
        private static final Object info;
        private static final IReflection.MethodAccessor minecraftVersionId;
        private static final IReflection.MethodAccessor minecraftVersionName;

        static {
            Class<?> infoClass;

            try {
                infoClass = Class.forName("io.papermc.paper.ServerBuildInfo");
            } catch (ClassNotFoundException e) {
                infoClass = null;
            }

            if (infoClass == null) {
                info = null;
                minecraftVersionId = null;
                minecraftVersionName = null;
            } else {
                IReflection.MethodAccessor buildInfo = IReflection.getMethod(infoClass, infoClass, new Class[0]);
                info = buildInfo.invoke(null);

                minecraftVersionId = IReflection.getMethod(infoClass, "minecraftVersionId", String.class, new Class[0]);
                minecraftVersionName = IReflection.getMethod(infoClass, "minecraftVersionName", String.class, new Class[0]);
            }
        }

        private static boolean isAvailable() {
            return info != null;
        }

        @NotNull
        private static String minecraftVersionId() {
            if (minecraftVersionId == null) throw new IllegalStateException();
            return (String) minecraftVersionId.invoke(info);
        }

        @NotNull
        private static String minecraftVersionName() {
            if (minecraftVersionName == null) throw new IllegalStateException();
            return (String) minecraftVersionName.invoke(info);
        }
    }
}
