package de.codingair.codingapi.server.specification;

import de.codingair.codingapi.server.reflections.IReflection;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Version {
    private static Double version = null;
    private static String name = null;
    private static String specification = null;
    private static Type type = Type.UNKNOWN;

    static {
        load();
    }

    private Version() {
    }

    private static void load() {
        if (version == null) {
            if (ServerBuildInfo.isAvailable()) {
                type = Type.PAPER;
                name = "Paper";
                specification = ServerBuildInfo.minecraftVersionName();

                String parts = ServerBuildInfo.minecraftVersionId();
                String minorVersion = parts.split("\\.")[1];
                String subVersion = parts.contains(".") ? parts.split("\\.")[2] : "0";

                String newVersion = minorVersion + "." + (Integer.parseInt(subVersion) < 10 ? "0" + subVersion : subVersion);

                version = Double.parseDouble(newVersion);
            } else {
                // version
                String bukkitVersion = Bukkit.getVersion();

                Pattern p = Pattern.compile("\\(MC: \\d\\.\\d\\d?(\\.\\d\\d?)?");
                Matcher match = p.matcher(bukkitVersion);
                if (match.find()) {
                    String parts = match.group().substring(7);
                    String majorVersion = parts.split("\\.")[0];
                    String subVersion = parts.contains(".") ? parts.split("\\.")[1] : "0";

                    String newVersion = majorVersion + "." + (Integer.parseInt(subVersion) < 10 ? "0" + subVersion : subVersion);

                    version = Double.parseDouble(newVersion);
                }

                // specification
                specification = Bukkit.getVersion();

                // server type
                try {
                    int from = bukkitVersion.indexOf('-');

                    if (from >= 0) {
                        from += 1;
                        int to = bukkitVersion.indexOf("-", from);

                        if (to >= 0) {
                            name = bukkitVersion.substring(from, to);
                            type = Type.getByName(name);
                        }
                    }
                } catch (Exception ignored) {
                }
            }
        }
    }

    public static double get() {
        if (version == null) throw new IllegalStateException("Version could not be loaded.");
        return version;
    }

    private static String getRightVersion() {
        String ver = String.valueOf(version);
        if(ver.split("\\.")[1].length() == 1) ver += "0";
        return ver.replace(".0", ".");
    }

    public static String versionTag() {
        return "1." + getRightVersion();
    }

    public static String fullVersion() {
        if (name == null || name.equals(type.toString())) return type + " (" + specification + ")";
        return name + " (" + specification + ", " + type + ")";
    }

    public static Type type() {
        return type;
    }

    public static boolean atLeast(double version) {
        return version <= get();
    }

    public static boolean after(double version) {
        return version < get();
    }

    /**
     * @param version The maximum version that will be accepted. Attention: When on 1.8.8, the method call with version
     *                `8` will return false because 'v1.8.8' transforms into the float 8.8 which is higher than 8.
     * @return True, if the current version is at most the given version.
     */
    public static boolean atMost(double version) {
        return get() <= version;
    }

    public static boolean before(double version) {
        return get() < version;
    }

    public static boolean equals(double version) {
        return get() == version;
    }

    public static boolean between(double min, double max) {
        return atLeast(min) && atMost(max);
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
            if (before(version)) return values[i - 1];
        }

        return values[values.length - 1];
    }

    public static <T> T choose(T mojangMapped, T def) {
        return choose(mojangMapped, def, 0, null);
    }

    public static <T> T choose(T mojangMapped, T def, double version1, T value1) {
        return choose(mojangMapped, def, version1, value1, 0, null);
    }

    public static <T> T choose(T mojangMapped, T def, double version1, T value1, double version2, T value2) {
        return choose(mojangMapped, def, version1, value1, version2, value2, 0, null);
    }

    public static <T> T choose(T mojangMapped, T def, double version1, T value1, double version2, T value2, double version3, T value3) {
        return choose(mojangMapped, def, version1, value1, version2, value2, version3, value3, 0, null);
    }

    public static <T> T choose(T mojangMapped, T def, double version1, T value1, double version2, T value2, double version3, T value3, double version4, T value4) {
        return choose(mojangMapped, def, version1, value1, version2, value2, version3, value3, version4, value4, 0, null);
    }

    public static <T> T choose(T mojangMapped, T def, double version1, T value1, double version2, T value2, double version3, T value3, double version4, T value4, double version5, T value5) {
        return choose(mojangMapped, def, version1, value1, version2, value2, version3, value3, version4, value4, version5, value5, 0, null);
    }

    public static boolean mojangMapped() {
        return atLeast(20.5) && type() == Type.PAPER;
    }

    public static <T> T choose(T mojangMapped, T def, double version1, T value1, double version2, T value2, double version3, T value3, double version4, T value4, double version5, T value5, double version6, T value6) {
        if (mojangMapped()) return mojangMapped;

        //noinspection unchecked
        T[] values = (T[]) new Object[]{def, value1, value2, value3, value4, value5, value6};
        double[] versions = new double[]{-1, version1, version2, version3, version4, version5, version6};

        for (int i = 0; i < versions.length; i++) {
            double version = versions[i];

            if (version == -1) continue;
            if (version == 0) return values[i - 1];
            if (before(version)) return values[i - 1];
        }

        return values[values.length - 1];
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
