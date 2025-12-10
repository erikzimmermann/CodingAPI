package de.codingair.codingapi.tools.nbt;

import de.codingair.codingapi.server.reflections.IReflection;
import de.codingair.codingapi.server.specification.Version;

import java.util.List;

public class NBTBase<T> {
    private final T value;

    public NBTBase(T value) {
        this.value = value;
    }

    public T getValue() {
        return value;
    }

    Object invoke() {
        Type t = Type.getByObject(this.value);

        if (t == Type.UNKNOWN) return null;

        IReflection.ConstructorAccessor con = IReflection.getConstructor(t.getClazz(), t.getObjectClazz());

        if (con == null) return null;

        return con.newInstance(this.value);
    }

    private enum Type {
        BYTE(Byte.class, IReflection.getClass(IReflection.ServerPacket.NBT, Version.choose("NBTTagByte", 21.11, "ByteTag"))),
        BYTE_ARRAY(Byte[].class, IReflection.getClass(IReflection.ServerPacket.NBT, Version.choose("NBTTagByteArray", 21.11, "ByteArrayTag"))),
        DOUBLE(Double.class, IReflection.getClass(IReflection.ServerPacket.NBT, Version.choose("NBTTagDouble", 21.11, "DoubleTag"))),
        END(null, IReflection.getClass(IReflection.ServerPacket.NBT, Version.choose("NBTTagEnd", 21.11, "EndTag"))),
        FLOAT(Float.class, IReflection.getClass(IReflection.ServerPacket.NBT, Version.choose("NBTTagFloat", 21.11, "FloatTag"))),
        INT(Integer.class, IReflection.getClass(IReflection.ServerPacket.NBT, Version.choose("NBTTagInt", 21.11, "IntTag"))),
        INT_ARRAY(Integer[].class, IReflection.getClass(IReflection.ServerPacket.NBT, Version.choose("NBTTagIntArray", 21.11, "IntArrayTag"))),
        LIST(List.class, IReflection.getClass(IReflection.ServerPacket.NBT, Version.choose("NBTTagList", 21.11, "ListTag"))),
        LONG(Long.class, IReflection.getClass(IReflection.ServerPacket.NBT, Version.choose("NBTTagLong", 21.11, "LongTag"))),
        LONG_ARRAY(Long[].class, IReflection.getClass(IReflection.ServerPacket.NBT, Version.choose("NBTTagLongArray", 21.11, "LongArrayTag"))),
        SHORT(Short.class, IReflection.getClass(IReflection.ServerPacket.NBT, Version.choose("NBTTagShort", 21.11, "ShortTag"))),
        STRING(String.class, IReflection.getClass(IReflection.ServerPacket.NBT, Version.choose("NBTTagString", 21.11, "StringTag"))),
        UNKNOWN(null, null);

        private final Class<?> objectClazz;
        private final Class<?> clazz;

        Type(Class<?> objectClazz, Class<?> clazz) {
            this.objectClazz = objectClazz;
            this.clazz = clazz;
        }

        public static Type getByObject(Object o) {
            if (o == null) return UNKNOWN;

            for (Type value : values()) {
                if (value.objectClazz == null) continue;

                if (o.getClass().equals(value.objectClazz)) return value;
            }

            return UNKNOWN;
        }

        public Class<?> getObjectClazz() {
            return objectClazz;
        }

        public Class<?> getClazz() {
            return clazz;
        }
    }
}
