package de.codingair.codingapi.tools.nbt;

import de.codingair.codingapi.server.reflections.IReflection;

import java.lang.reflect.Field;
import java.util.List;

public class NBTBase<T> {
    private T value;

    public NBTBase(T value) {
        this.value = value;
    }

    public T getValue() {
        return value;
    }

    Object invoke() {
        Type t = Type.getByObject(this.value);

        if(t == Type.UNKNOWN) return null;

        IReflection.ConstructorAccessor con = IReflection.getConstructor(t.getClazz(), t.getObjectClazz());

        if(con == null) return null;

        return con.newInstance(this.value);
    }
    
    private enum Type {
        BYTE(Byte.class, IReflection.getClass(IReflection.ServerPacket.MINECRAFT_PACKAGE, "NBTTagByte")),
        BYTE_ARRAY(Byte[].class, IReflection.getClass(IReflection.ServerPacket.MINECRAFT_PACKAGE, "NBTTagByteArray")),
        DOUBLE(Double.class, IReflection.getClass(IReflection.ServerPacket.MINECRAFT_PACKAGE, "NBTTagDouble")),
        END(null, IReflection.getClass(IReflection.ServerPacket.MINECRAFT_PACKAGE, "NBTTagEnd")),
        FLOAT(Float.class, IReflection.getClass(IReflection.ServerPacket.MINECRAFT_PACKAGE, "NBTTagFloat")),
        INT(Integer.class, IReflection.getClass(IReflection.ServerPacket.MINECRAFT_PACKAGE, "NBTTagInt")),
        INT_ARRAY(Integer[].class, IReflection.getClass(IReflection.ServerPacket.MINECRAFT_PACKAGE, "NBTTagIntArray")),
        LIST(List.class, IReflection.getClass(IReflection.ServerPacket.MINECRAFT_PACKAGE, "NBTTagList")),
        LONG(Long.class, IReflection.getClass(IReflection.ServerPacket.MINECRAFT_PACKAGE, "NBTTagLong")),
        LONG_ARRAY(Long[].class, IReflection.getClass(IReflection.ServerPacket.MINECRAFT_PACKAGE, "NBTTagLongArray")),
        SHORT(Short.class, IReflection.getClass(IReflection.ServerPacket.MINECRAFT_PACKAGE, "NBTTagShort")),
        STRING(String.class, IReflection.getClass(IReflection.ServerPacket.MINECRAFT_PACKAGE, "NBTTagString")),
        UNKNOWN(null, null);
        
        private Class<?> objectClazz;
        private Class<?> clazz;

        Type(Class<?> objectClazz, Class<?> clazz) {
            this.objectClazz = objectClazz;
            this.clazz = clazz;
        }

        public static Type getByObject(Object o) {
            if(o == null) return UNKNOWN;

            for(Type value : values()) {
                if(value.objectClazz == null) continue;

                if(o.getClass().equals(value.objectClazz)) return value;
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
