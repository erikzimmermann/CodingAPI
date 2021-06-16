package de.codingair.codingapi.tools.nbt;

import de.codingair.codingapi.server.reflections.IReflection;
import de.codingair.codingapi.server.reflections.PacketUtils;
import de.codingair.codingapi.server.specification.Version;
import org.bukkit.inventory.ItemStack;

import java.util.Map;

public class NBTTagCompound {
    protected static Class<?> TAG;
    protected static IReflection.MethodAccessor SET;
    protected static IReflection.MethodAccessor GET;
    protected static IReflection.FieldAccessor<?> TAG_FIELD;
    protected static IReflection.FieldAccessor<Map<String, ?>> MAP_FIELD;
    protected static IReflection.MethodAccessor asBukkitCopy;

    protected Object tag;
    private Object itemStack;

    public NBTTagCompound(ItemStack item) {
        initialize();

        itemStack = PacketUtils.getItemStack(item);
        if (itemStack == null) return;

        this.tag = TAG_FIELD.get(itemStack);
        if (tag == null) {
            tag = create();
            TAG_FIELD.set(itemStack, tag);
        }
    }

    public NBTTagCompound() {
        initialize();
        tag = create();
    }

    public NBTTagCompound(Object tag) {
        initialize();
        this.tag = tag;
    }

    protected void initialize() {
        if (TAG != null) return;

        TAG = IReflection.getClass(IReflection.ServerPacket.NBT, "NBTBase");

        if (Version.get().isBiggerThan(Version.v1_13)) {
            SET = IReflection.getMethod(PacketUtils.NBTTagCompoundClass, "set", TAG, new Class[] {String.class, TAG});
        } else {
            SET = IReflection.getMethod(PacketUtils.NBTTagCompoundClass, "set", new Class[] {String.class, TAG});
        }
        GET = IReflection.getMethod(PacketUtils.NBTTagCompoundClass, "get", TAG, new Class[] {String.class});

        TAG_FIELD = IReflection.getField(PacketUtils.ItemStackClass, Version.since(17, "tag", "w"));
        MAP_FIELD = IReflection.getField(PacketUtils.NBTTagCompoundClass, Version.since(17, "map", "x"));
        asBukkitCopy = IReflection.getMethod(PacketUtils.CraftItemStackClass, "asBukkitCopy", ItemStack.class, new Class[] {PacketUtils.ItemStackClass});
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
