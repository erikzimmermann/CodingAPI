package de.codingair.codingapi.tools.items;

import de.codingair.codingapi.server.Version;
import de.codingair.codingapi.server.reflections.IReflection;
import de.codingair.codingapi.server.reflections.PacketUtils;
import org.bukkit.inventory.ItemStack;

public class NBTTagCompound {
    private static Class<?> COMPOUND;
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

    private void initialize() {
        if(COMPOUND != null) return;

        COMPOUND = IReflection.getClass(IReflection.ServerPacket.MINECRAFT_PACKAGE, "NBTTagCompound");
        TAG = IReflection.getClass(IReflection.ServerPacket.MINECRAFT_PACKAGE, "NBTBase");

        if(Version.getVersion().isBiggerThan(Version.v1_13)) {
            SET = IReflection.getMethod(COMPOUND, "set", TAG, new Class[] {String.class, TAG});
        } else {
            SET = IReflection.getMethod(COMPOUND, "set", new Class[] {String.class, TAG});
        }

        getTag = IReflection.getMethod(PacketUtils.ItemStackClass, "getTag", COMPOUND, new Class[] {});
        setTag = IReflection.getMethod(PacketUtils.ItemStackClass, "setTag", new Class[] {COMPOUND});
        asBukkitCopy = IReflection.getMethod(PacketUtils.CraftItemStackClass, "asBukkitCopy", ItemStack.class, new Class[] {PacketUtils.ItemStackClass});
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
