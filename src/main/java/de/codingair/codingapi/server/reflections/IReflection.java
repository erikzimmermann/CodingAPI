package de.codingair.codingapi.server.reflections;

import de.codingair.codingapi.server.specification.Type;
import de.codingair.codingapi.server.specification.Version;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.*;
import java.util.*;
import java.util.function.Supplier;

/**
 * -== IReflection ==-
 * <p>
 * This library was created by @Ingrim4 and allows you to use the reflection-api implemented in java easily for spigot plugins
 * You are welcome to use it and redistribute it under the following conditions:
 * * Don't claim this class as your own
 * * Don't remove this disclaimer
 *
 * @author Ingrim4
 * @version 1.2.8
 */
public class IReflection {

    /**
     * @param useSupplier The flag that determines whether the supplier should be called or not. Usually, this involves a version check.
     * @param s           The supplier that should be called.
     * @param <V>         The type of the object that could be supplied.
     * @return The supplied object or null if the flag is false.
     */
    public static <V> V wrap(boolean useSupplier, Supplier<V> s) {
        if (useSupplier) return s.get();
        else return null;
    }

    public static void setValue(@NotNull Object instance, @NotNull String fieldName, @NotNull Object value) throws IllegalArgumentException, IllegalAccessException, NoSuchFieldException, SecurityException {
        FieldAccessor<?> field = getField(instance.getClass(), fieldName);
        field.set(instance, value);
    }

    @NotNull
    public static Class<?> getSaveClass(@NotNull ServerPacket packet, @NotNull String name) throws ClassNotFoundException {
        return getSaveClass(packet.toString(), name);
    }

    @NotNull
    public static Class<?> getSaveClass(@NotNull String packet, @NotNull String name) throws ClassNotFoundException {
        return Class.forName(packet + name);
    }

    @NotNull
    public static Class<?> getClass(@NotNull String name) {
        try {
            return Class.forName(name);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    @NotNull
    public static Class<?> getClass(@NotNull ServerPacket packet, @NotNull String name) {
        return getClass(packet.toString(), name);
    }

    @NotNull
    public static Class<?> getClass(@NotNull String path, @NotNull String name) {
        try {
            return Class.forName(path + name);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    @Nullable
    public static ConstructorAccessor getConstructor(@NotNull Class<?> clazz, @NotNull Class<?> @NotNull ... parameterTypes) {
        Class<?>[] p = DataType.convertToPrimitive(parameterTypes);
        for (Constructor<?> c : clazz.getDeclaredConstructors()) {
            if (DataType.equalsArray(DataType.convertToPrimitive(c.getParameterTypes()), p)) {
                c.setAccessible(true);
                return new ConstructorAccessor() {

                    @Override
                    public Object newInstance(Object... args) {
                        try {
                            return c.newInstance(args);
                        } catch (IllegalAccessException e) {
                            throw new IllegalStateException("Cannot use reflection.", e);
                        } catch (InvocationTargetException e) {
                            throw new RuntimeException("An internal error occured.", e.getCause());
                        } catch (InstantiationException e) {
                            throw new RuntimeException("Cannot instantiate object.", e);
                        }
                    }

                    @Override
                    public Constructor<?> getConstructor() {
                        return c;
                    }
                };
            }
        }

        if (clazz.getSuperclass() != null)
            return IReflection.getConstructor(clazz.getSuperclass(), parameterTypes);

        return null;
    }

    public static @NotNull ConstructorAccessor @NotNull [] getConstructors(@NotNull Class<?> clazz) {
        List<ConstructorAccessor> cons = new ArrayList<>();

        for (Constructor<?> c : clazz.getDeclaredConstructors()) {
            c.setAccessible(true);

            cons.add(new ConstructorAccessor() {

                @Override
                public Object newInstance(Object... args) {
                    try {
                        return c.newInstance(args);
                    } catch (IllegalAccessException e) {
                        throw new IllegalStateException("Cannot use reflection.", e);
                    } catch (InvocationTargetException e) {
                        throw new RuntimeException("An internal error occured.", e.getCause());
                    } catch (InstantiationException e) {
                        throw new RuntimeException("Cannot instantiate object.", e);
                    }
                }

                @Override
                public Constructor<?> getConstructor() {
                    return c;
                }
            });
        }

        return cons.toArray(new ConstructorAccessor[0]);
    }

    @NotNull
    public static MethodAccessor getMethod(Class<?> target, @NotNull Class<?> @NotNull ... parameterTypes) {
        return getMethod(target, null, null, parameterTypes);
    }

    @NotNull
    public static MethodAccessor getMethod(Class<?> target, @Nullable Class<?> returnType, @NotNull Class<?> @NotNull ... parameterTypes) {
        return getMethod(target, null, returnType, parameterTypes);
    }

    @NotNull
    public static MethodAccessor getMethod(Class<?> target, @Nullable String methodName, @NotNull Class<?> @NotNull ... parameterTypes) {
        return IReflection.getMethod(target, methodName, null, parameterTypes);
    }

    @NotNull
    public static MethodAccessor getMethod(Class<?> target, @Nullable String methodName, @Nullable Class<?> returnType, @NotNull Class<?> @Nullable ... parameterTypes) {
        if (target == null) throw new IllegalArgumentException("Target class cannot be null.");

        Class<?>[] primitiveParameter = DataType.convertToPrimitive(parameterTypes);
        for (Method method : target.getDeclaredMethods())
            if ((methodName == null || method.getName().equals(methodName)) && (returnType == null || method.getReturnType().equals(returnType)) && ((primitiveParameter.length == 0 && method.getParameterTypes().length == 0) || DataType.equalsArray(DataType.convertToPrimitive(method.getParameterTypes()), primitiveParameter))) {
                method.setAccessible(true);
                return new MethodAccessor() {

                    @Override
                    public Object invoke(Object target, Object... args) {
                        try {
                            return method.invoke(target, args);
                        } catch (IllegalAccessException e) {
                            throw new IllegalStateException("Cannot use reflection.", e);
                        } catch (InvocationTargetException e) {
                            throw new RuntimeException("An internal error occured.", e.getCause());
                        }
                    }

                    @Override
                    public Method getMethod() {
                        return method;
                    }
                };
            }
        if (target.getSuperclass() != null)
            return IReflection.getMethod(target.getSuperclass(), methodName, returnType, parameterTypes);

        throw new IllegalStateException(String.format("Unable to find method %s (%s).", methodName, Arrays.toString(parameterTypes)));
    }

    public static MethodAccessor getSaveMethod(@NotNull Class<?> target, @Nullable String methodName, @Nullable Class<?> returnType, @NotNull Class<?> @NotNull ... parameterTypes) throws IllegalStateException {
        Class<?>[] primitiveParameter = DataType.convertToPrimitive(parameterTypes);
        for (Method method : target.getDeclaredMethods())
            if ((methodName == null || method.getName().equals(methodName)) && (returnType == null || method.getReturnType().equals(returnType)) && (primitiveParameter.length == 0 || DataType.equalsArray(DataType.convertToPrimitive(method.getParameterTypes()), primitiveParameter))) {
                method.setAccessible(true);
                return new MethodAccessor() {

                    @Override
                    public Object invoke(Object target, Object... args) {
                        try {
                            return method.invoke(target, args);
                        } catch (IllegalAccessException e) {
                            throw new IllegalStateException("Cannot use reflection.", e);
                        } catch (InvocationTargetException e) {
                            throw new RuntimeException("An internal error occured.", e.getCause());
                        }
                    }

                    @Override
                    public Method getMethod() {
                        return method;
                    }
                };
            }
        if (target.getSuperclass() != null)
            return IReflection.getSaveMethod(target.getSuperclass(), methodName, returnType, parameterTypes);

        return null;
    }

    public static <T> FieldAccessor<T> getField(Class<?> target, String fieldName) {
        return IReflection.getField(target, fieldName, null, 0, false);
    }

    public static <T> FieldAccessor<T> getField(Class<?> target, String fieldName, double since, Class<T> fieldType, int index, boolean ignoreStatic) {
        if (Version.atLeast(since)) return IReflection.getField(target, null, fieldType, index, ignoreStatic);
        return IReflection.getField(target, fieldName, null, 0, ignoreStatic);
    }

    public static <T> FieldAccessor<T> getField(Class<?> target, Class<T> fieldType, int index) {
        return IReflection.getField(target, null, fieldType, index, false);
    }

    public static <T> FieldAccessor<T> getNonStaticField(Class<?> target, String fieldName) {
        return IReflection.getField(target, fieldName, null, 0, true);
    }

    public static <T> FieldAccessor<T> getNonStaticField(Class<?> target, Class<T> fieldType, int index) {
        return IReflection.getField(target, null, fieldType, index, true);
    }

    private static <T> FieldAccessor<T> getField(Class<?> target, String fieldName, Class<T> fieldType, int index, boolean ignoreStatic) {
        for (Field field : target.getDeclaredFields()) {
            if (ignoreStatic && Modifier.isStatic(field.getModifiers())) continue;

            if ((fieldName == null || fieldName.equals(field.getName())) && (fieldType == null || (fieldType.isAssignableFrom(field.getType()) && index-- <= 0))) {
                field.setAccessible(true);
                return new FieldAccessor<T>() {

                    @Override
                    @SuppressWarnings("unchecked")
                    public T get(Object target) {
                        try {
                            return (T) field.get(target);
                        } catch (IllegalAccessException e) {
                            throw new IllegalStateException("Cannot use reflection.", e);
                        }
                    }

                    @Override
                    public void set(Object target, Object value) {
                        try {
                            field.set(target, value);
                        } catch (IllegalAccessException e) {
                            throw new IllegalStateException("Cannot use reflection.", e);
                        }
                    }

                    @Override
                    public Field getField() {
                        return field;
                    }
                };
            }
        }

        if (target.getSuperclass() != null)
            return IReflection.getField(target.getSuperclass(), fieldName, fieldType, index, ignoreStatic);

        throw new IllegalStateException(String.format("Unable to find field %s (%s).", fieldName, fieldType));
    }

    public enum DataType {
        BYTE(byte.class, Byte.class), SHORT(short.class, Short.class), INTEGER(int.class,
                Integer.class), LONG(long.class, Long.class), CHARACTER(char.class, Character.class), FLOAT(float.class,
                Float.class), DOUBLE(double.class, Double.class), BOOLEAN(boolean.class, Boolean.class);

        private static final Map<Class<?>, DataType> CLASS_MAP = new HashMap<>();

        static {
            for (DataType t : DataType.values()) {
                DataType.CLASS_MAP.put(t.primitive, t);
                DataType.CLASS_MAP.put(t.reference, t);
            }
        }

        private final Class<?> primitive;
        private final Class<?> reference;

        DataType(Class<?> primitive, Class<?> reference) {
            this.primitive = primitive;
            this.reference = reference;
        }

        public static DataType fromClass(Class<?> c) {
            return DataType.CLASS_MAP.get(c);
        }

        public static Class<?> getPrimitive(Class<?> c) {
            DataType t = DataType.fromClass(c);
            return t == null ? c : t.getPrimitive();
        }

        public static Class<?> getReference(Class<?> c) {
            DataType t = DataType.fromClass(c);
            return t == null ? c : t.getReference();
        }

        public static Class<?>[] convertToPrimitive(@NotNull Class<?> @Nullable [] classes) {
            int length = classes == null ? 0 : classes.length;
            Class<?>[] types = new Class<?>[length];
            for (int i = 0; i < length; i++)
                types[i] = DataType.getPrimitive(classes[i]);
            return types;
        }

        public static boolean equalsArray(Class<?>[] a1, Class<?>[] a2) {
            if (a1 == null || a2 == null || a1.length != a2.length)
                return false;
            for (int i = 0; i < a1.length; i++)
                if (!a1[i].equals(a2[i]) && !a1[i].isAssignableFrom(a2[i]))
                    return false;
            return true;
        }

        public Class<?> getPrimitive() {
            return this.primitive;
        }

        public Class<?> getReference() {
            return this.reference;
        }
    }

    public enum ServerPacket {
        @Deprecated
        MINECRAFT_PACKAGE("net.minecraft.server." + minecraftVersion()),
        MOJANG_AUTHLIB("com.mojang.authlib"),
        CRAFTBUKKIT_PACKAGE(Bukkit.getServer().getClass().getPackage().getName()),
        CRAFTBUKKIT_UTILS(CRAFTBUKKIT_PACKAGE.path + ".util"),
        CRAFTBUKKIT_BLOCK(CRAFTBUKKIT_PACKAGE.path + ".block"),
        BUKKIT_PACKET("org.bukkit"),

        PROTOCOL(17, "net.minecraft.network.protocol"),
        PACKETS(17, "net.minecraft.network.protocol.game"),
        CHAT(17, "net.minecraft.network.chat"),
        NETWORK(17, "net.minecraft.network"),
        NBT(17, "net.minecraft.nbt"),
        CORE(17, "net.minecraft.core"),
        COMPONENT(17, "net.minecraft.core.component"),
        PARTICLES(17, "net.minecraft.core.particles"),
        SERVER_LEVEL(17, "net.minecraft.server.level"),
        WORLD_LEVEL(17, "net.minecraft.world.level"),
        WORLD_ITEM(17, "net.minecraft.world.item"),
        INVENTORY(17, "net.minecraft.world.inventory"),
        BLOCK(17, "net.minecraft.world.level.block"),
        BLOCK_ENTITY(17, "net.minecraft.world.level.block.entity"),
        COMMANDS(17, "net.minecraft.commands"),
        ;

        private final String path;

        ServerPacket(String path) {
            this.path = path;
        }

        ServerPacket(int version, String path) {
            if (Version.before(version)) this.path = "net.minecraft.server." + minecraftVersion();
            else this.path = path;
        }

        @NotNull
        private static String minecraftVersion() {
            if (Version.type().equals(Type.PAPER) && Version.atLeast(20.5)) {
                switch (Version.get().getShortVersionName()) {
                    case "1.20.5":
                    case "1.20.6":
                        return "1_20_R4";
                    case "1.21":
                        return "1_21_R1";
                    default:
                        return "UNKNOWN";
                }
            }
            String name = Bukkit.getServer().getClass().getPackage().getName();
            if (name.length() > 23) return name.substring(23);
            return "";
        }

        @Override
        public String toString() {
            return this.path + ".";
        }

        public static String MINECRAFT_PACKAGE(String path) {
            if (Version.before(17)) return MINECRAFT_PACKAGE.toString();
            else return path + ".";
        }
    }

    public interface ConstructorAccessor {

        Object newInstance(Object... args);

        Constructor<?> getConstructor();

    }

    public interface MethodAccessor {

        Object invoke(Object target, Object... args);

        Method getMethod();

    }

    public interface FieldAccessor<T> {

        T get(Object target);

        void set(Object target, Object value);

        Field getField();

    }
}