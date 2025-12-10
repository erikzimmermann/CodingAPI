package de.codingair.codingapi.tools.nbt;

import de.codingair.codingapi.nms.NmsLoader;
import de.codingair.codingapi.server.reflections.IReflection;
import de.codingair.codingapi.server.reflections.PacketUtils;
import de.codingair.codingapi.server.specification.Version;
import org.bukkit.inventory.ItemStack;

import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;

public class NBTTagCompound {
    protected static Class<?> TAG;
    protected static IReflection.MethodAccessor SET;
    protected static IReflection.MethodAccessor GET;
    protected static Function<Object, Object> TAG_FIELD_GET;
    protected static BiConsumer<Object, Object> TAG_FIELD_SET;
    protected static IReflection.FieldAccessor<?> MAP_FIELD;
    protected static IReflection.MethodAccessor asBukkitCopy;

    static {
        TAG = IReflection.getClass(IReflection.ServerPacket.NBT, Version.choose("NBTBase", 21.11, "Tag"));

        if (Version.atLeast(20.05)) {
            SET = IReflection.getMethod(PacketUtils.NBTTagCompoundClass, TAG, new Class[]{String.class, TAG});
            GET = IReflection.getMethod(PacketUtils.NBTTagCompoundClass, TAG, new Class[]{String.class});

            Class<?> customDataClass = IReflection.getClass("net.minecraft.world.item.component.", "CustomData");
            Class<?> typeClass = IReflection.getClass(IReflection.ServerPacket.COMPONENT, "DataComponentType");
            Class<?> typesClass = IReflection.getClass(IReflection.ServerPacket.COMPONENT, "DataComponents");

            Object customDataType = IReflection.getField(typesClass, typeClass, 0).get(null);

            IReflection.MethodAccessor getMethod = IReflection.getMethod(PacketUtils.ItemStackClass, Object.class, new Class[]{typeClass});
            IReflection.MethodAccessor copyTag = IReflection.getMethod(customDataClass, PacketUtils.NBTTagCompoundClass, new Class[]{});
            TAG_FIELD_GET = itemStack -> {
                Object customData = getMethod.invoke(itemStack, customDataType);
                if (customData == null) return null;
                return copyTag.invoke(customData);
            };

            IReflection.ConstructorAccessor newCustomData = IReflection.getConstructor(customDataClass, PacketUtils.NBTTagCompoundClass);
            if (newCustomData == null) throw new IllegalStateException("Cannot find custom data constructor!");

            IReflection.MethodAccessor setMethod = IReflection.getMethod(PacketUtils.ItemStackClass, (Class<?>) null, new Class[]{typeClass, Object.class});

            TAG_FIELD_SET = (itemStack, tag) -> {
                Object customData = newCustomData.newInstance(tag);
                setMethod.invoke(itemStack, customDataType, customData);
            };
        } else {
            IReflection.FieldAccessor<?> tagField = IReflection.getField(PacketUtils.ItemStackClass, PacketUtils.NBTTagCompoundClass, 0);
            TAG_FIELD_GET = tagField::get;
            TAG_FIELD_SET = tagField::set;

            if (Version.atLeast(18)) {
                SET = IReflection.getMethod(PacketUtils.NBTTagCompoundClass, "a", TAG, new Class[]{String.class, TAG});
                GET = IReflection.getMethod(PacketUtils.NBTTagCompoundClass, "c", TAG, new Class[]{String.class});
            } else {
                if (Version.atLeast(14)) {
                    SET = IReflection.getMethod(PacketUtils.NBTTagCompoundClass, "set", TAG, new Class[]{String.class, TAG});
                } else {
                    SET = IReflection.getMethod(PacketUtils.NBTTagCompoundClass, "set", new Class[]{String.class, TAG});
                }
                GET = IReflection.getMethod(PacketUtils.NBTTagCompoundClass, "get", TAG, new Class[]{String.class});
            }
        }

        MAP_FIELD = IReflection.getField(PacketUtils.NBTTagCompoundClass, Map.class, 0);
        asBukkitCopy = IReflection.getMethod(PacketUtils.CraftItemStackClass, "asBukkitCopy", ItemStack.class, new Class[]{PacketUtils.ItemStackClass});
    }

    protected Object tag;
    private Object itemStack;

    public NBTTagCompound(ItemStack item) {
        itemStack = PacketUtils.getItemStack(item);
        if (itemStack == null) return;

        this.tag = TAG_FIELD_GET.apply(itemStack);
        if (tag == null) {
            tag = create();
            TAG_FIELD_SET.accept(itemStack, tag);
        }
    }

    @NmsLoader
    public NBTTagCompound() {
        tag = create();
    }

    public NBTTagCompound(Object tag) {
        this.tag = tag;
    }

    public ItemStack getItem() {
        return itemStack == null ? null : (ItemStack) asBukkitCopy.invoke(null, itemStack);
    }

    public Object getTag() {
        return tag;
    }

    public Object set(String key, NBTBase<?> value) {
        if (this.tag == null) return null;
        Object instance = value.invoke();
        if (instance == null) return null;

        return SET.invoke(this.tag, key, instance);
    }

    public Object get(String key) {
        if (this.tag == null) return null;

        return GET.invoke(this.tag, key);
    }

    public Map<String, Object> getMap() {
        //noinspection unchecked
        return (Map<String, Object>) MAP_FIELD.get(this.tag);
    }

    public NBTTagCompound setNBT(NBTTagCompound nbtTagCompound) {
        this.tag = nbtTagCompound;
        return this;
    }

    protected Object create() {
        return IReflection.getConstructor(PacketUtils.NBTTagCompoundClass).newInstance();
    }
}
