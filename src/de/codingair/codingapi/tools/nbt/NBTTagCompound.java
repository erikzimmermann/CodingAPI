package de.codingair.codingapi.tools.nbt;

import de.codingair.codingapi.server.Version;
import de.codingair.codingapi.server.reflections.IReflection;
import de.codingair.codingapi.server.reflections.PacketUtils;
import org.bukkit.inventory.ItemStack;

public class NBTTagCompound {
    private static Class<?> TAG;
    private static IReflection.MethodAccessor SET;
    private static IReflection.MethodAccessor getTag;
    private static IReflection.MethodAccessor setTag;
    private static IReflection.MethodAccessor asBukkitCopy;

    private Object tag;
    private Object itemStack;

    public NBTTagCompound(ItemStack item) {
        initialize();

        this.tag = getTag.invoke(itemStack = PacketUtils.getItemStack(item));
        if(tag == null) {
            tag = create();
            setTag.invoke(itemStack, tag);
        }
    }

    public NBTTagCompound() {
        initialize();
        tag = create();
    }

    private void initialize() {
        if(TAG != null) return;

        TAG = IReflection.getClass(IReflection.ServerPacket.MINECRAFT_PACKAGE, "NBTBase");

        if(Version.getVersion().isBiggerThan(Version.v1_13)) {
            SET = IReflection.getMethod(PacketUtils.NBTTagCompoundClass, "set", TAG, new Class[] {String.class, TAG});
        } else {
            SET = IReflection.getMethod(PacketUtils.NBTTagCompoundClass, "set", new Class[] {String.class, TAG});
        }

        getTag = IReflection.getMethod(PacketUtils.ItemStackClass, "getTag", PacketUtils.NBTTagCompoundClass, new Class[] {});
        setTag = IReflection.getMethod(PacketUtils.ItemStackClass, "setTag", new Class[] {PacketUtils.NBTTagCompoundClass});
        asBukkitCopy = IReflection.getMethod(PacketUtils.CraftItemStackClass, "asBukkitCopy", ItemStack.class, new Class[] {PacketUtils.ItemStackClass});
    }

    public ItemStack getItem() {
        return (ItemStack) asBukkitCopy.invoke(null, itemStack);
    }

    public Object getTag() {
        return tag;
    }

    public Object set(String key, NBTBase<?> value) {
        Object instance = value.invoke();
        if(instance == null) return null;

        return SET.invoke(this.tag, key, instance);
    }

    public NBTTagCompound setNBT(NBTTagCompound nbtTagCompound) {
        this.tag = nbtTagCompound;
        return this;
    }

    private Object create() {
        return IReflection.getConstructor(PacketUtils.NBTTagCompoundClass).newInstance();
    }
}
