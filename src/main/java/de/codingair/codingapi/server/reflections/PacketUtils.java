package de.codingair.codingapi.server.reflections;

import de.codingair.codingapi.nms.NmsLoader;
import de.codingair.codingapi.server.specification.Version;
import de.codingair.codingapi.tools.items.XMaterial;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Modifier;
import java.util.stream.Stream;

public class PacketUtils {
    public static final Class<?> CraftPlayerClass = getClass(IReflection.ServerPacket.CRAFTBUKKIT_PACKAGE, "entity.CraftPlayer");
    public static final Class<?> CraftEntityClass = getClass(IReflection.ServerPacket.CRAFTBUKKIT_PACKAGE, "entity.CraftEntity");
    public static final Class<?> CraftServerClass = getClass(IReflection.ServerPacket.CRAFTBUKKIT_PACKAGE, "CraftServer");
    public static final Class<?> CraftBlockClass = getClass(IReflection.ServerPacket.CRAFTBUKKIT_BLOCK, "CraftBlock");
    public static final Class<?> MinecraftServerClass = getClass(IReflection.ServerPacket.MINECRAFT_PACKAGE("net.minecraft.server"), "MinecraftServer");
    public static final Class<?> DedicatedServerClass = getClass(IReflection.ServerPacket.MINECRAFT_PACKAGE("net.minecraft.server.dedicated"), "DedicatedServer");
    public static final Class<?> CraftWorldClass = getClass(IReflection.ServerPacket.CRAFTBUKKIT_PACKAGE, "CraftWorld");
    public static final Class<?> WorldClass = getClass(IReflection.ServerPacket.WORLD_LEVEL, Version.choose("Level", "World"));
    public static final Class<?> WorldServerClass = getClass(IReflection.ServerPacket.MINECRAFT_PACKAGE("net.minecraft.server.level"), Version.choose("ServerLevel", "WorldServer"));
    public static final Class<?> BlockPositionClass = getClass(IReflection.ServerPacket.CORE, Version.choose("BlockPos", "BlockPosition"));
    public static final Class<?> BlockClass = getClass(IReflection.ServerPacket.BLOCK, "Block");
    public static final Class<?> BlocksClass = getClass(IReflection.ServerPacket.BLOCK, "Blocks");
    public static final Class<?> IBlockDataClass = getClass(IReflection.ServerPacket.MINECRAFT_PACKAGE("net.minecraft.world.level.block.state"), Version.choose("BlockState", "IBlockData"));
    public static final Class<?> TileEntityClass = getClass(IReflection.ServerPacket.MINECRAFT_PACKAGE("net.minecraft.world.level.block.entity"), Version.choose("BlockEntity", "TileEntity"));
    public static final Class<?> TileEntitySignClass = getClass(IReflection.ServerPacket.MINECRAFT_PACKAGE("net.minecraft.world.level.block.entity"), Version.choose("SignBlockEntity", "TileEntitySign"));
    public static final Class<?> NBTTagCompoundClass = getClass(IReflection.ServerPacket.NBT, Version.choose("CompoundTag", "NBTTagCompound"));

    public static final Class<?> EntityClass = getClass(IReflection.ServerPacket.MINECRAFT_PACKAGE("net.minecraft.world.entity"), "Entity");
    public static final Class<?> EntityPlayerClass = getClass(IReflection.ServerPacket.MINECRAFT_PACKAGE("net.minecraft.server.level"), Version.choose("ServerPlayer", "EntityPlayer"));
    public static final Class<?> EntityLivingClass = getClass(IReflection.ServerPacket.MINECRAFT_PACKAGE("net.minecraft.world.entity"), Version.choose("LivingEntity", "EntityLiving"));
    public static final Class<?> EntityHumanClass = getClass(IReflection.ServerPacket.MINECRAFT_PACKAGE("net.minecraft.world.entity.player"), Version.choose("Player", "EntityHuman"));

    public static final Class<?> ItemStackClass = getClass(IReflection.ServerPacket.MINECRAFT_PACKAGE("net.minecraft.world.item"), "ItemStack");
    public static final Class<?> CraftItemStackClass = getClass(IReflection.ServerPacket.CRAFTBUKKIT_PACKAGE, "inventory.CraftItemStack");
    public static final IReflection.MethodAccessor getCustomModelData = getMethod(ItemMeta.class, "getCustomModelData", int.class, new Class[0], true);
    public static final IReflection.MethodAccessor hasCustomModelData = getMethod(ItemMeta.class, "hasCustomModelData", boolean.class, new Class[0], true);
    public static final IReflection.MethodAccessor setCustomModelData = getMethod(ItemMeta.class, "setCustomModelData", new Class[]{int.class}, true);

    public static final Class<?> PlayerConnectionClass = getClass(IReflection.ServerPacket.MINECRAFT_PACKAGE("net.minecraft.server.network"), Version.choose("ServerGamePacketListenerImpl", "PlayerConnection"));
    public static final Class<?> NetworkManagerClass = getClass(IReflection.ServerPacket.MINECRAFT_PACKAGE("net.minecraft.network"), Version.choose("Connection", "NetworkManager"));

    public static final Class<?> DataWatcherClass = getClass(IReflection.ServerPacket.MINECRAFT_PACKAGE("net.minecraft.network.syncher"), Version.choose("SynchedEntityData", "DataWatcher"));
    public static final Class<?> DataWatcherObjectClass = IReflection.wrap(Version.atLeast(19.3), () -> getClass(IReflection.ServerPacket.MINECRAFT_PACKAGE("net.minecraft.network.syncher"), Version.choose("EntityDataAccessor", "DataWatcherObject")));

    public static final Class<?> PacketClass = getClass(IReflection.ServerPacket.PROTOCOL, "Packet");
    public static final Class<?> PacketPlayOutAttachEntityClass = getClass(IReflection.ServerPacket.PACKETS, "PacketPlayOutAttachEntity");
    public static final Class<?> PacketPlayOutEntityDestroyClass = getClass(IReflection.ServerPacket.PACKETS, "PacketPlayOutEntityDestroy");
    public static final Class<?> PacketPlayOutEntityTeleportClass = getClass(IReflection.ServerPacket.PACKETS, "PacketPlayOutEntityTeleport");
    public static final Class<?> PacketPlayOutEntityVelocityClass = getClass(IReflection.ServerPacket.PACKETS, "PacketPlayOutEntityVelocity");
    public static final Class<?> PacketPlayOutEntityMetadataClass = getClass(IReflection.ServerPacket.PACKETS, "PacketPlayOutEntityMetadata");
    public static final Class<?> PacketPlayOutSpawnEntityLivingClass = getClass(IReflection.ServerPacket.PACKETS, "PacketPlayOutSpawnEntityLiving", true);
    public static final Class<?> PacketPlayOutSpawnEntityClass = getClass(IReflection.ServerPacket.PACKETS, "PacketPlayOutSpawnEntity");
    public static final Class<?> PacketPlayOutBlockActionClass = getClass(IReflection.ServerPacket.PACKETS, "PacketPlayOutBlockAction");
    public static final Class<?> PacketPlayInUpdateSignClass = getClass(IReflection.ServerPacket.PACKETS, "PacketPlayInUpdateSign");
    public static final Class<?> PacketPlayOutOpenSignEditorClass = getClass(IReflection.ServerPacket.PACKETS, "PacketPlayOutOpenSignEditor");

    public static final Class<?> ChatSerializerClass = getClass(IReflection.ServerPacket.CHAT, Version.choose("Component$Serializer", "IChatBaseComponent$ChatSerializer"));
    public static final Class<?> IChatMutableComponentClass = IReflection.wrap(Version.atLeast(16), () -> getClass(IReflection.ServerPacket.CHAT, Version.choose("MutableComponent", "IChatMutableComponent")));
    public static final Class<?> IChatBaseComponentClass = getClass(IReflection.ServerPacket.CHAT, Version.choose("Component", "IChatBaseComponent"));

    public static final IReflection.MethodAccessor getHandle = getMethod(CraftPlayerClass, "getHandle", EntityPlayerClass, new Class[]{});
    public static final IReflection.MethodAccessor getHandleEntity = getMethod(CraftEntityClass, "getHandle", EntityClass, new Class[]{});
    public static final IReflection.MethodAccessor getBukkitEntity = getMethod(EntityClass, "getBukkitEntity", CraftEntityClass, new Class[]{});
    public static final IReflection.MethodAccessor getHandleOfCraftWorld = getMethod(CraftWorldClass, "getHandle", WorldServerClass, new Class[]{});
    public static final IReflection.MethodAccessor sendPacket;
    public static final IReflection.MethodAccessor getEntityId = getMethod(CraftPlayerClass, "getEntityId", int.class, new Class[]{});
    public static final IReflection.MethodAccessor getTileEntity = getMethod(WorldClass, null, TileEntityClass, new Class[]{BlockPositionClass});
    public static final IReflection.FieldAccessor<Integer> getId = IReflection.getNonStaticField(EntityClass, int.class, 0);

    public static final IReflection.FieldAccessor<?> playerConnection = IReflection.getField(EntityPlayerClass, Version.choose("playerConnection", 17, "b"), 20, PlayerConnectionClass, 0, m -> !Modifier.isStatic(m));

    // 1.20.5+
    public static final Class<?> HolderLookupProvider = IReflection.wrap(Version.atLeast(20.5), () -> getClass(IReflection.ServerPacket.CORE, Version.choose("HolderLookup$Provider", "HolderLookup$a")));

    static {
        if (Version.atLeast(20.2)) {
            Class<?> packetSendListenerClass = IReflection.getClass(IReflection.ServerPacket.NETWORK, "PacketSendListener");
            sendPacket = IReflection.getMethod(PlayerConnectionClass, (Class<?>) null, new Class[]{PacketClass, packetSendListenerClass});
        } else {
            sendPacket = getMethod(PlayerConnectionClass, Version.choose("sendPacket", 18, "a"), new Class[]{PacketClass});
        }

        // test functions
        getChatMessage("Test");
        getIChatBaseComponent("Test");
        getMinecraftServer();
        getCraftServer();
        getWorldServer();
    }

    @NmsLoader
    private PacketUtils() {
    }

    public static IReflection.MethodAccessor getMethod(Class<?> target, String methodName, Class<?>[] parameterTypes) {
        return getMethod(target, methodName, null, parameterTypes, false);
    }

    public static IReflection.MethodAccessor getMethod(Class<?> target, String methodName, Class<?> returnType, Class<?>[] parameterTypes) {
        return getMethod(target, methodName, returnType, parameterTypes, false);
    }

    public static IReflection.MethodAccessor getMethod(Class<?> target, String methodName, Class<?>[] parameterTypes, boolean acceptNull) {
        return getMethod(target, methodName, null, parameterTypes, acceptNull);
    }

    public static IReflection.MethodAccessor getMethod(Class<?> target, String methodName, Class<?> returnType, Class<?>[] parameterTypes, boolean acceptNull) {
        if (acceptNull) {
            try {
                return IReflection.getMethod(target, methodName, returnType, parameterTypes);
            } catch (Exception ex) {
                return null;
            }
        } else return IReflection.getMethod(target, methodName, returnType, parameterTypes);
    }

    public static Class<?> getClass(IReflection.ServerPacket packet, String className) {
        return getClass(packet, className, false);
    }

    public static Class<?> getClass(IReflection.ServerPacket packet, String className, boolean acceptNull) {
        return getClass(packet.toString(), className, acceptNull);
    }

    public static Class<?> getClass(String packet, String className) {
        return getClass(packet, className, false);
    }

    public static Class<?> getClass(String packet, String className, boolean acceptNull) {
        if (acceptNull) {
            try {
                return IReflection.getClass(packet, className);
            } catch (Throwable t) {
                return null;
            }
        } else {
                return IReflection.getClass(packet, className);
        }
    }

    public static void sendBlockChange(@NotNull Player player, @NotNull Location location, @NotNull XMaterial data) {
        Object iBlockData = PacketUtils.getIBlockData(data);
        sendBlockChange(player, location, iBlockData);
    }

    public static void sendBlockChange(@NotNull Player player, @NotNull Location location, @NotNull Block data) {
        Object iBlockData;
        if (Version.atLeast(13)) {
            IReflection.MethodAccessor getState = IReflection.getMethod(PacketUtils.CraftBlockClass, PacketUtils.IBlockDataClass, new Class[0]);
            iBlockData = getState.invoke(data);
        } else {
            IReflection.MethodAccessor getState = IReflection.getMethod(PacketUtils.CraftBlockClass, PacketUtils.BlockClass, new Class[0]);
            IReflection.MethodAccessor getBlockData = IReflection.getMethod(PacketUtils.BlockClass, PacketUtils.IBlockDataClass, new Class[0]);
            Object nmsBlock = getState.invoke(data);
            iBlockData = getBlockData.invoke(nmsBlock);
        }

        sendBlockChange(player, location, iBlockData);
    }

    public static void sendBlockChange(@NotNull Player player, @NotNull Location location, @NotNull Object iBlockData) {
        Class<?> packetPlayOutBlockChange = IReflection.getClass(IReflection.ServerPacket.PACKETS, "PacketPlayOutBlockChange");

        Object packet;
        Object blockPos = PacketUtils.getBlockPosition(location);

        if (Version.atLeast(17)) {
            IReflection.ConstructorAccessor con = IReflection.getConstructor(packetPlayOutBlockChange, PacketUtils.BlockPositionClass, PacketUtils.IBlockDataClass);
            if (con == null)
                throw new NullPointerException("Cannot prepare temporary sign: Could not find PacketPlayOutBlockChange constructor.");

            packet = con.newInstance(blockPos, iBlockData);
        } else {
            IReflection.ConstructorAccessor con = IReflection.getConstructor(packetPlayOutBlockChange);
            if (con == null)
                throw new NullPointerException("Cannot prepare temporary sign: Could not find PacketPlayOutBlockChange constructor.");

            packet = con.newInstance();

            IReflection.FieldAccessor<?> blockPosition = IReflection.getField(packetPlayOutBlockChange, PacketUtils.BlockPositionClass, 0);
            IReflection.FieldAccessor<?> blockData = IReflection.getField(packetPlayOutBlockChange, PacketUtils.IBlockDataClass, 0);

            blockPosition.set(packet, blockPos);
            blockData.set(packet, iBlockData);
        }

        sendPacket(player, packet);
    }

    public static void sendPacketToAll(Object packet) {
        Bukkit.getOnlinePlayers().forEach(p -> sendPacket(p, packet));
    }

    public static void sendPacket(Player target, Object packet) {
        if (!target.isOnline()) return;

        if (Version.atLeast(20.2)) {
            sendPacket.invoke(getPlayerConnection(target), packet, null);
        } else {
            sendPacket.invoke(getPlayerConnection(target), packet);
        }
    }

    public static Object getPlayerConnection(Player player) {
        Object entityPlayer = getHandle.invoke(CraftPlayerClass.cast(player));
        return playerConnection.get(entityPlayer);
    }

    public static void sendPacket(Object packet, Player... target) {
        for (Player player : target) {
            if (player == null) continue;
            sendPacket(player, packet);
        }
    }

    public static Object getEntityPlayer(Player p) {
        if (p == null) return null;
        return getHandle.invoke(CraftPlayerClass.cast(p));
    }

    public static Object getTileEntity(Location loc) {
        return getTileEntity.invoke(getWorldServer(loc.getWorld()), getBlockPosition(loc));
    }

    public static Object getCraftPlayer(Player p) {
        return CraftPlayerClass.cast(p);
    }

    public static int getEntityId(Player p) {
        Object cp = getCraftPlayer(p);
        return (int) getEntityId.invoke(cp);
    }

    public static Object getEntity(Entity entity) {
        return getHandleEntity.invoke(CraftEntityClass.cast(entity));
    }

    public static Entity getBukkitEntity(Object entity) {
        return (Entity) getBukkitEntity.invoke(entity);
    }

    public static Object getIChatBaseComponent(String text) {
        return getRawIChatBaseComponent("{\"text\":\"" + text + "\"}");
    }

    public static Object getRawIChatBaseComponent(String jsonFormat) {
        if (Version.atLeast(20.5)) {
            IReflection.MethodAccessor fromJson = IReflection.getMethod(ChatSerializerClass, IChatMutableComponentClass, new Class[]{String.class, HolderLookupProvider});
            return fromJson.invoke(null, jsonFormat, emptyHolderLookupProvider());
        } else {
            IReflection.MethodAccessor a;

            if (Version.atLeast(16)) {
                a = IReflection.getMethod(ChatSerializerClass, "a", IChatMutableComponentClass, new Class[]{String.class});
            } else {
                a = IReflection.getMethod(ChatSerializerClass, "a", IChatBaseComponentClass, new Class[]{String.class});
            }
            return a.invoke(IChatBaseComponentClass, jsonFormat);
        }
    }

    public static Object getChatMessage(String text) {
        if (Version.atLeast(20.5)) return getIChatBaseComponent(text);
        else if (Version.atLeast(19)) {
            IReflection.MethodAccessor creator = IReflection.getMethod(IChatBaseComponentClass, IChatBaseComponentClass, new Class[]{String.class});
            return creator.invoke(null, text);
        } else {
            Class<?> ChatMessageClass = getClass(IReflection.ServerPacket.CHAT, "ChatMessage");
            IReflection.ConstructorAccessor chatMessageCon = IReflection.getConstructor(ChatMessageClass, String.class, Object[].class);
            return chatMessageCon.newInstance(text, new Object[]{});
        }
    }

    public static Object getMinecraftServer() {
        IReflection.MethodAccessor getServer = IReflection.getSaveMethod(CraftServerClass, "getServer", MinecraftServerClass);
        if (getServer == null)
            getServer = getMethod(CraftServerClass, "getServer", DedicatedServerClass, new Class[]{});
        return getServer.invoke(getCraftServer());
    }

    public static Object getCraftServer() {
        return CraftServerClass.cast(Bukkit.getServer());
    }

    public static Object getWorldServer() {
        return getWorldServer(Bukkit.getWorlds().get(0));
    }

    public static Object getWorldServer(World world) {
        return getHandleOfCraftWorld.invoke(CraftWorldClass.cast(world));
    }

    public static Object getItemStack(ItemStack item) {
        IReflection.MethodAccessor asNMSCopy = IReflection.getMethod(CraftItemStackClass, "asNMSCopy", ItemStackClass, new Class[]{ItemStack.class});
        return asNMSCopy.invoke(null, item);
    }

    public static ItemStack getItemStack(Object item) {
        IReflection.MethodAccessor asNMSCopy = IReflection.getMethod(CraftItemStackClass, "asBukkitCopy", ItemStack.class, new Class[]{ItemStackClass});
        return (ItemStack) asNMSCopy.invoke(null, item);
    }

    public static Object getBlockPosition(Location location) {
        if (Version.atLeast(19.4)) {
            IReflection.ConstructorAccessor con = IReflection.getConstructor(BlockPositionClass, int.class, int.class, int.class);
            return con.newInstance(location.getBlockX(), location.getBlockY(), location.getBlockZ());
        } else {
            IReflection.ConstructorAccessor con = IReflection.getConstructor(BlockPositionClass, Double.class, Double.class, Double.class);
            return con.newInstance(location.getX(), location.getY(), location.getZ());
        }
    }

    public static Object getIBlockData(XMaterial material) {
        IReflection.MethodAccessor getBlockData = IReflection.getMethod(PacketUtils.BlockClass, PacketUtils.IBlockDataClass, new Class[]{});

        if (Version.atLeast(13)) {
            Class<?> craftMagicNumbers = IReflection.getClass(IReflection.ServerPacket.CRAFTBUKKIT_UTILS, "CraftMagicNumbers");
            IReflection.MethodAccessor getBlock = IReflection.getMethod(craftMagicNumbers, PacketUtils.BlockClass, new Class[]{Material.class});
            Object block = getBlock.invoke(null, material.parseMaterial());
            return getBlockData.invoke(block);
        } else {
            if (material.name().contains("_SIGN")) {
                Class<?> blocks = IReflection.getClass(IReflection.ServerPacket.PACKETS, "Blocks");

                String name = Version.choose("STANDING_SIGN", 13, "SIGN");
                Object standingSign = IReflection.getField(blocks, name).get(null);
                return getBlockData.invoke(standingSign);
            } else {
                IReflection.MethodAccessor getByCombinedId = IReflection.getMethod(BlockClass, "getByCombinedId", IBlockDataClass, new Class[]{int.class});
                return getByCombinedId.invoke(null, getCombinedId(material.getId(), (byte) 0));
            }
        }
    }

    public static int getCombinedId(int id, byte data) {
        return id + (data << 12);
    }

    public static class Blocks {
        public static Object findByName(String name) {
            IReflection.FieldAccessor<?> field = IReflection.getField(BlocksClass, name);

            return field.get(null);
        }
    }

    public static Object emptyHolderLookupProvider() {
        IReflection.MethodAccessor create = IReflection.getMethod(HolderLookupProvider, HolderLookupProvider, new Class[]{Stream.class});
        return create.invoke(null, Stream.empty());
    }

    public static class EntityPackets {
        public static void spawnEntity(Object entity, int entityTypeId, Player... players) {
            spawnEntity(entity, entityTypeId, 0, players);
        }

        public static void spawnEntity(Object entity, int entityTypeId, int entityThrowerId, Player... players) {
            Object packet = IReflection.getConstructor(PacketPlayOutSpawnEntityClass, EntityClass, int.class, int.class).newInstance(entity, entityTypeId, entityThrowerId);
            sendPacket(packet, players);
        }

        public static void destroyEntity(Object entity, Player... players) {
            Object packet = IReflection.getConstructor(PacketUtils.PacketPlayOutEntityDestroyClass, int[].class).newInstance(new int[]{getId(entity)});
            sendPacket(packet, players);
        }

        public static Object getTeleportPacket(Object entity, Location location) {
            IReflection.MethodAccessor getId = IReflection.getMethod(PacketUtils.EntityPlayerClass, "getId", int.class, null);
            int id = (int) getId.invoke(entity);

            Object packet = IReflection.getConstructor(PacketPlayOutEntityTeleportClass).newInstance();

            IReflection.FieldAccessor<?> a = IReflection.getField(PacketPlayOutEntityTeleportClass, "a");
            IReflection.FieldAccessor<?> b = IReflection.getField(PacketPlayOutEntityTeleportClass, "b");
            IReflection.FieldAccessor<?> c = IReflection.getField(PacketPlayOutEntityTeleportClass, "c");
            IReflection.FieldAccessor<?> d = IReflection.getField(PacketPlayOutEntityTeleportClass, "d");
            IReflection.FieldAccessor<?> e = IReflection.getField(PacketPlayOutEntityTeleportClass, "e");
            IReflection.FieldAccessor<?> f = IReflection.getField(PacketPlayOutEntityTeleportClass, "f");
            IReflection.FieldAccessor<?> g = IReflection.getField(PacketPlayOutEntityTeleportClass, "g");

            a.set(packet, id);

            if (Version.atLeast(9)) {
                //new
                b.set(packet, location.getX());
                c.set(packet, location.getY());
                d.set(packet, location.getZ());
            } else {
                //old
                b.set(packet, toFixedPointNumber(location.getX()));
                c.set(packet, toFixedPointNumber(location.getY()));
                d.set(packet, toFixedPointNumber(location.getZ()));
            }

            e.set(packet, toAngle(location.getYaw()));
            f.set(packet, toAngle(location.getPitch()));
            g.set(packet, false);

            return packet;
        }

        public static int getId(Object entity) {
            return getId.get(entity);
        }

        public static Packet getVelocityPacket(Object entity, Vector vector) {
            int id = getId(entity);

            Packet packet = new Packet(PacketPlayOutEntityVelocityClass);
            packet.initialize(id, vector.getX(), vector.getY(), vector.getZ());

            return packet;
        }

        public static Packet getPassengerPacket(Object vehicle, Object passenger) {
            Packet packet = new Packet(PacketPlayOutAttachEntityClass);

            if (Version.atLeast(9)) {
                packet.initialize(new Class[]{EntityClass, EntityClass}, vehicle, passenger);
            } else {
                packet.initialize(new Class[]{int.class, EntityClass, EntityClass}, 0, passenger, vehicle);
            }

            return packet;
        }

        public static Packet getEjectPacket(Object vehicle) {
            Packet packet = new Packet(PacketPlayOutAttachEntityClass);

            if (Version.atLeast(9)) {
                packet.initialize(new Class[]{EntityClass, EntityClass}, vehicle, null);
                return packet;
            } else {
                packet.initialize(new Class[]{int.class, EntityClass, EntityClass}, 0, vehicle, null);
                return packet;
            }
        }

        private static int toFixedPointNumber(double value) {
            return (int) Math.floor(value * 32D);
        }

        private static byte toAngle(float value) {
            return (byte) ((int) (value * 256.0F / 360.0F));
        }
    }
}
