package de.codingair.codingapi.tools.items;

import de.codingair.codingapi.server.reflections.IReflection;
import de.codingair.codingapi.server.reflections.PacketUtils;
import org.bukkit.inventory.ItemStack;

public class NBTTagCompound {
    private static Class<?> COMPOUND = IReflection.getClass(IReflection.ServerPacket.MINECRAFT_PACKAGE, "NBTTagCompound");
    private static Class<?> TAG = IReflection.getClass(IReflection.ServerPacket.MINECRAFT_PACKAGE, "NBTBase");
    private static IReflection.MethodAccessor SET = IReflection.getMethod(COMPOUND, "set", TAG, new Class[] {String.class, TAG});
    private static IReflection.MethodAccessor getTag = IReflection.getMethod(PacketUtils.ItemStackClass, "getTag", COMPOUND, new Class[] {});
    private static IReflection.MethodAccessor setTag = IReflection.getMethod(PacketUtils.ItemStackClass, "setTag", new Class[] {COMPOUND});
    private static IReflection.MethodAccessor asBukkitCopy = IReflection.getMethod(PacketUtils.CraftItemStackClass, "asBukkitCopy", ItemStack.class, new Class[] {PacketUtils.ItemStackClass});

    private Object tag;
    private Object itemStack;

    public NBTTagCompound(ItemStack item) {
        this.tag = getTag.invoke(itemStack = PacketUtils.getItemStack(item));
        if(tag == null) {
            tag = create();
            setTag.invoke(itemStack, tag);
        }
    }

    public ItemStack getItem() {
        return (ItemStack) asBukkitCopy.invoke(null, itemStack);
    }

    public Object set(String key, Object value) {
        return SET.invoke(this.tag, key, value);
    }

    public NBTTagCompound setNBT(NBTTagCompound nbtTagCompound) {
        this.tag = nbtTagCompound;
        return this;
    }

    private Object create() {
        return IReflection.getConstructor(COMPOUND).newInstance();
    }
}
