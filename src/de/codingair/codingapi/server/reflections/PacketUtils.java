package de.codingair.codingapi.server.reflections;

import com.mojang.authlib.GameProfile;
import de.codingair.codingapi.server.Version;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.MaterialData;
import org.bukkit.util.Vector;

import java.util.List;

/**
 * Removing of this disclaimer is forbidden.
 *
 * @author codingair
 * @verions: 1.0.0
 **/

public class PacketUtils {
    public static final Class<?> CraftPlayerClass = getClass(IReflection.ServerPacket.CRAFTBUKKIT_PACKAGE, "entity.CraftPlayer");
    public static final Class<?> CraftEntityClass = getClass(IReflection.ServerPacket.CRAFTBUKKIT_PACKAGE, "entity.CraftEntity");
    public static final Class<?> CraftServerClass = getClass(IReflection.ServerPacket.CRAFTBUKKIT_PACKAGE, "CraftServer");
    public static final Class<?> MinecraftServerClass = getClass(IReflection.ServerPacket.MINECRAFT_PACKAGE, "MinecraftServer");
    public static final Class<?> CraftWorldClass = getClass(IReflection.ServerPacket.CRAFTBUKKIT_PACKAGE, "CraftWorld");
    public static final Class<?> WorldClass = getClass(IReflection.ServerPacket.MINECRAFT_PACKAGE, "World");
    public static final Class<?> WorldServerClass = getClass(IReflection.ServerPacket.MINECRAFT_PACKAGE, "WorldServer");
    public static final Class<?> BlockPositionClass = getClass(IReflection.ServerPacket.MINECRAFT_PACKAGE, "BlockPosition");
    public static final Class<?> BlockClass = getClass(IReflection.ServerPacket.MINECRAFT_PACKAGE, "Block");
    public static final Class<?> BlocksClass = getClass(IReflection.ServerPacket.MINECRAFT_PACKAGE, "Blocks");
    public static final Class<?> IBlockDataClass = getClass(IReflection.ServerPacket.MINECRAFT_PACKAGE, "IBlockData");
    public static final Class<?> TileEntityClass = getClass(IReflection.ServerPacket.MINECRAFT_PACKAGE, "TileEntity");
    public static final Class<?> TileEntitySignClass = getClass(IReflection.ServerPacket.MINECRAFT_PACKAGE, "TileEntitySign");
    public static final Class<?> PlayerListClass = getClass(IReflection.ServerPacket.MINECRAFT_PACKAGE, "PlayerList");
    public static final Class<?> DedicatedPlayerListClass = getClass(IReflection.ServerPacket.MINECRAFT_PACKAGE, "DedicatedPlayerList");
    public static final Class<?> DimensionManagerClass = getClass(IReflection.ServerPacket.MINECRAFT_PACKAGE, "DimensionManager");

    public static final Class<?> EntityClass = getClass(IReflection.ServerPacket.MINECRAFT_PACKAGE, "Entity");
    public static final Class<?> EntityPlayerClass = getClass(IReflection.ServerPacket.MINECRAFT_PACKAGE, "EntityPlayer");
    public static final Class<?> EntityLivingClass = getClass(IReflection.ServerPacket.MINECRAFT_PACKAGE, "EntityLiving");
    public static final Class<?> EntityInsentientClass = getClass(IReflection.ServerPacket.MINECRAFT_PACKAGE, "EntityInsentient");
    public static final Class<?> EntityHumanClass = getClass(IReflection.ServerPacket.MINECRAFT_PACKAGE, "EntityHuman");
    public static final Class<?> EntityFallingBlockClass = getClass(IReflection.ServerPacket.MINECRAFT_PACKAGE, "EntityFallingBlock");

    public static final Class<?> ItemStackClass = getClass(IReflection.ServerPacket.MINECRAFT_PACKAGE, "ItemStack");
    public static final Class<?> CraftItemStackClass = getClass(IReflection.ServerPacket.CRAFTBUKKIT_PACKAGE, "inventory.CraftItemStack");

    public static final Class<?> PlayerConnectionClass = getClass(IReflection.ServerPacket.MINECRAFT_PACKAGE, "PlayerConnection");
    public static final Class<?> NetworkManagerClass = getClass(IReflection.ServerPacket.MINECRAFT_PACKAGE, "NetworkManager");

    public static final Class<?> DataWatcherClass = getClass(IReflection.ServerPacket.MINECRAFT_PACKAGE, "DataWatcher");
    public static final Class<?> DataWatcherRegistryClass = getClass(IReflection.ServerPacket.MINECRAFT_PACKAGE, "DataWatcherRegistry");
    public static final Class<?> DataWatcherSerializerClass = getClass(IReflection.ServerPacket.MINECRAFT_PACKAGE, "DataWatcherSerializer");
    public static final Class<?> DataWatcherObjectClass = getClass(IReflection.ServerPacket.MINECRAFT_PACKAGE, "DataWatcherObject");

    public static final Class<?> PacketPlayOutMountClass = getClass(IReflection.ServerPacket.MINECRAFT_PACKAGE, "PacketPlayOutMount");
    public static final Class<?> PacketPlayOutUpdateAttributesClass = getClass(IReflection.ServerPacket.MINECRAFT_PACKAGE, "PacketPlayOutUpdateAttributes");
    public static final Class<?> PacketPlayOutUpdateEntityNBTClass = getClass(IReflection.ServerPacket.MINECRAFT_PACKAGE, "PacketPlayOutUpdateEntityNBT");
    public static final Class<?> PacketPlayOutTileEntityDataClass = getClass(IReflection.ServerPacket.MINECRAFT_PACKAGE, "PacketPlayOutTileEntityData");
    public static final Class<?> PacketPlayOutAttachEntityClass = getClass(IReflection.ServerPacket.MINECRAFT_PACKAGE, "PacketPlayOutAttachEntity");
    public static final Class<?> PacketPlayOutNamedEntitySpawnClass = getClass(IReflection.ServerPacket.MINECRAFT_PACKAGE, "PacketPlayOutNamedEntitySpawn");
    public static final Class<?> PacketPlayOutEntityClass = getClass(IReflection.ServerPacket.MINECRAFT_PACKAGE, "PacketPlayOutEntity");
    public static final Class<?> PacketPlayOutEntityEquipmentClass = getClass(IReflection.ServerPacket.MINECRAFT_PACKAGE, "PacketPlayOutEntityEquipment");
    public static final Class<?> PacketPlayOutAnimationClass = getClass(IReflection.ServerPacket.MINECRAFT_PACKAGE, "PacketPlayOutAnimation");
    public static final Class<?> PacketPlayOutEntityStatusClass = getClass(IReflection.ServerPacket.MINECRAFT_PACKAGE, "PacketPlayOutEntityStatus");
    public static final Class<?> PacketPlayOutBedClass = getClass(IReflection.ServerPacket.MINECRAFT_PACKAGE, "PacketPlayOutBed");
    public static final Class<?> PacketPlayOutEntityDestroyClass = getClass(IReflection.ServerPacket.MINECRAFT_PACKAGE, "PacketPlayOutEntityDestroy");
    public static final Class<?> PacketPlayOutEntityEffectClass = getClass(IReflection.ServerPacket.MINECRAFT_PACKAGE, "PacketPlayOutEntityEffect");
    public static final Class<?> PacketPlayOutEntityTeleportClass = getClass(IReflection.ServerPacket.MINECRAFT_PACKAGE, "PacketPlayOutEntityTeleport");
    public static final Class<?> PacketPlayOutEntityVelocityClass = getClass(IReflection.ServerPacket.MINECRAFT_PACKAGE, "PacketPlayOutEntityVelocity");
    public static final Class<?> PacketPlayOutEntityLookClass = getClass(IReflection.ServerPacket.MINECRAFT_PACKAGE, "PacketPlayOutEntity$PacketPlayOutEntityLook");
    public static final Class<?> PacketPlayOutEntityHeadRotationClass = getClass(IReflection.ServerPacket.MINECRAFT_PACKAGE, "PacketPlayOutEntityHeadRotation");
    public static final Class<?> PacketPlayOutPlayerInfoClass = getClass(IReflection.ServerPacket.MINECRAFT_PACKAGE, "PacketPlayOutPlayerInfo");
    public static final Class<?> PacketPlayOutPlayerInfo$PlayerInfoData = getClass(IReflection.ServerPacket.MINECRAFT_PACKAGE, "PacketPlayOutPlayerInfo$PlayerInfoData");
    public static final Class<?> PacketPlayOutEntity$PacketPlayOutRelEntityMoveLookClass = getClass(IReflection.ServerPacket.MINECRAFT_PACKAGE, "PacketPlayOutEntity$PacketPlayOutRelEntityMoveLook");
    public static final Class<?> PacketPlayOutEntityMetadataClass = getClass(IReflection.ServerPacket.MINECRAFT_PACKAGE, "PacketPlayOutEntityMetadata");
    public static final Class<?> PacketPlayInSettingsClass = getClass(IReflection.ServerPacket.MINECRAFT_PACKAGE, "PacketPlayInSettings");
    public static final Class<?> PacketPlayOutSpawnEntityLivingClass = getClass(IReflection.ServerPacket.MINECRAFT_PACKAGE, "PacketPlayOutSpawnEntityLiving");
    public static final Class<?> PacketPlayOutSpawnEntityClass = getClass(IReflection.ServerPacket.MINECRAFT_PACKAGE, "PacketPlayOutSpawnEntity");
    public static final Class<?> PacketPlayOutBlockActionClass = getClass(IReflection.ServerPacket.MINECRAFT_PACKAGE, "PacketPlayOutBlockAction");
    public static final Class<?> PacketPlayInUpdateSignClass = getClass(IReflection.ServerPacket.MINECRAFT_PACKAGE, "PacketPlayInUpdateSign");
    public static final Class<?> PacketPlayOutOpenSignEditorClass = getClass(IReflection.ServerPacket.MINECRAFT_PACKAGE, "PacketPlayOutOpenSignEditor");

    public static final Class<?> EnumItemSlotClass = getClass(IReflection.ServerPacket.MINECRAFT_PACKAGE, "EnumItemSlot");
    public static final Class<?> EnumPlayerInfoActionClass = getClass(IReflection.ServerPacket.MINECRAFT_PACKAGE, "PacketPlayOutPlayerInfo$EnumPlayerInfoAction");
    public static final Class<?> EnumGamemodeClass = getClass(IReflection.ServerPacket.MINECRAFT_PACKAGE, (Version.getVersion().equals(Version.v1_8) || Version.getVersion().equals(Version.v1_9) ? "WorldSettings$" : "") + "EnumGamemode");
    public static final Class<?> EnumEntityUseActionClass = getClass(IReflection.ServerPacket.MINECRAFT_PACKAGE, "PacketPlayInUseEntity$EnumEntityUseAction");

    public static final Class<?> PlayerInteractManagerClass = getClass(IReflection.ServerPacket.MINECRAFT_PACKAGE, "PlayerInteractManager");

    public static final Class<?> ChatSerializerClass = getClass(IReflection.ServerPacket.MINECRAFT_PACKAGE, "IChatBaseComponent$ChatSerializer");
    public static final Class<?> IChatBaseComponentClass = getClass(IReflection.ServerPacket.MINECRAFT_PACKAGE, "IChatBaseComponent");
    public static final Class<?> ChatMessageClass = getClass(IReflection.ServerPacket.MINECRAFT_PACKAGE, "ChatMessage");
    public static final Class<?> ChatComponentTextClass = getClass(IReflection.ServerPacket.MINECRAFT_PACKAGE, "ChatComponentText");

    public static final IReflection.MethodAccessor getHandle = getMethod(CraftPlayerClass, "getHandle", EntityPlayerClass, new Class[] {});
    public static final IReflection.MethodAccessor getHandleCraftServer = getMethod(CraftServerClass, "getHandle", DedicatedPlayerListClass, new Class[] {});
    public static final IReflection.MethodAccessor moveToWorld = getMethod(PlayerListClass, "moveToWorld", EntityPlayerClass, new Class[] {EntityPlayerClass, int.class, boolean.class, Location.class, boolean.class});
    public static final IReflection.MethodAccessor moveToWorldV1_13 = getMethod(PlayerListClass, "moveToWorld", EntityPlayerClass, new Class[] {EntityPlayerClass, DimensionManagerClass, boolean.class, Location.class, boolean.class});
    public static final IReflection.MethodAccessor getHandleEntity = getMethod(CraftEntityClass, "getHandle", EntityClass, new Class[] {});
    public static final IReflection.MethodAccessor getBukkitEntity = getMethod(EntityClass, "getBukkitEntity", CraftEntityClass, new Class[] {});
    public static final IReflection.MethodAccessor getHandleOfCraftWorld = getMethod(CraftWorldClass, "getHandle", WorldServerClass, new Class[] {});
    public static final IReflection.MethodAccessor sendPacket = getMethod(PlayerConnectionClass, "sendPacket");
    public static final IReflection.MethodAccessor getEntityId = getMethod(CraftPlayerClass, "getEntityId", int.class, new Class[] {});
    public static final IReflection.MethodAccessor getServer = getMethod(CraftServerClass, "getServer", MinecraftServerClass, new Class[] {});
    public static final IReflection.MethodAccessor getTileEntity = getMethod(WorldClass, "getTileEntity", TileEntityClass, new Class[] {BlockPositionClass});

    public static final IReflection.FieldAccessor playerConnection = IReflection.getField(EntityPlayerClass, "playerConnection");

    public static class Blocks {
        public static Object findByName(String name) {
            IReflection.FieldAccessor field = IReflection.getField(BlocksClass, name);
            if(field == null) return null;

            return field.get(null);
        }
    }

    public static IReflection.MethodAccessor getMethod(Class<?> target, String methodName, Class<?>... parameterTypes) {
        return getMethod(target, methodName, null, parameterTypes);
    }

    public static IReflection.MethodAccessor getMethod(Class<?> target, String methodName, Class<?> returnType, Class<?>... parameterTypes) {
        try {
            return IReflection.getSaveMethod(target, methodName, returnType, parameterTypes);
        } catch(Exception ex) {
            return null;
        }
    }

    public static Class<?> getClass(IReflection.ServerPacket packet, String className) {
        try {
            return IReflection.getSaveClass(packet, className);
        } catch(Exception ex) {
            return null;
        }
    }

    public static void sendPacketToAll(Object packet) {
        Bukkit.getOnlinePlayers().forEach(p -> sendPacket(p, packet));
    }

    public static void sendPacket(Player target, Object packet) {
        if(!target.isOnline()) return;
        Object entityPlayer = getHandle.invoke(CraftPlayerClass.cast(target));
        sendPacket.invoke(playerConnection.get(entityPlayer), packet);
    }

    public static void sendPacket(Object packet, Player... target) {
        for(Player player : target) {
            if(player == null) continue;
            Object entityPlayer = getHandle.invoke(CraftPlayerClass.cast(player));
            sendPacket.invoke(playerConnection.get(entityPlayer), packet);
        }
    }

    public static Object getEntityPlayer(Player p) {
        if(p == null) return null;
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
        String jsonFormat = "{\"text\":\"" + text +"\"}";

        if(Version.getVersion().isBiggerThan(Version.v1_8)) {
            IReflection.MethodAccessor b = IReflection.getMethod(ChatSerializerClass, "a", IChatBaseComponentClass, new Class[] {String.class});
            return b.invoke(IChatBaseComponentClass, jsonFormat);
        } else {
            IReflection.MethodAccessor a = IReflection.getMethod(ChatSerializerClass, "a", IChatBaseComponentClass, new Class[] {String.class});
            return a.invoke(IChatBaseComponentClass, jsonFormat);
        }
    }

    public static Object getChatMessage(String text) {
        IReflection.ConstructorAccessor chatMessageCon = IReflection.getConstructor(ChatMessageClass, String.class, Object[].class);
        return chatMessageCon.newInstance(text, new Object[] {});
    }

    public static Object getChatComponentText(String text) {
        IReflection.ConstructorAccessor chatMessageCon = IReflection.getConstructor(ChatComponentTextClass, String.class);
        return chatMessageCon.newInstance(text);
    }

    public static Object getMinecraftServer() {
        return getServer.invoke(CraftServerClass.cast(Bukkit.getServer()));
    }

    public static Object getWorldServer() {
        return getWorldServer(Bukkit.getWorlds().get(0));
    }

    public static Object getWorldServer(World world) {
        return getHandleOfCraftWorld.invoke(CraftWorldClass.cast(world));
    }

    public static Object getPlayerInfoPacket(int enumId, Object entityPlayer) {
        IReflection.ConstructorAccessor tabCon = IReflection.getConstructor(PacketPlayOutPlayerInfoClass);

        IReflection.ConstructorAccessor dataCon = IReflection.getConstructor(PacketPlayOutPlayerInfo$PlayerInfoData, new Class[] {PacketPlayOutPlayerInfoClass, GameProfile.class, int.class, EnumGamemodeClass, IChatBaseComponentClass});

        if(dataCon == null) return null;

        IReflection.MethodAccessor getProfile = IReflection.getMethod(EntityPlayerClass, "getProfile", GameProfile.class, null);
        IReflection.FieldAccessor ping = IReflection.getField(EntityPlayerClass, "ping");
        IReflection.FieldAccessor playerInteractManager = IReflection.getField(EntityPlayerClass, "playerInteractManager");
        IReflection.MethodAccessor getGameMode = IReflection.getMethod(PlayerInteractManagerClass, "getGameMode", EnumGamemodeClass, null);
        IReflection.MethodAccessor getPlayerListName = IReflection.getMethod(EntityPlayerClass, "getPlayerListName", IChatBaseComponentClass, null);

        Object data = dataCon.newInstance(tabCon.newInstance(), getProfile.invoke(entityPlayer), ping.get(entityPlayer), getGameMode.invoke(playerInteractManager.get(entityPlayer)), getPlayerListName.invoke(entityPlayer));
        Object packet = tabCon.newInstance();

        IReflection.getField(packet.getClass(), "a").set(packet, PacketUtils.EnumPlayerInfoActionClass.getEnumConstants()[enumId]);
        ((List) IReflection.getField(packet.getClass(), "b").get(packet)).add(data);
        return packet;
    }

    public static Object getItemStack(ItemStack item) {
        IReflection.MethodAccessor asNMSCopy = IReflection.getMethod(CraftItemStackClass, "asNMSCopy", ItemStackClass, new Class[] {ItemStack.class});
        return asNMSCopy.invoke(CraftItemStackClass, item);
    }

    public static Object getBlockPosition(Location location) {
        IReflection.ConstructorAccessor con = IReflection.getConstructor(BlockPositionClass, Double.class, Double.class, Double.class);
        return con.newInstance(location.getX(), location.getY(), location.getZ());
    }

    public static Object getIBlockData(MaterialData data) {
        IReflection.MethodAccessor getByCombinedId = IReflection.getMethod(BlockClass, "getByCombinedId", IBlockDataClass, new Class[] {int.class});

        return getByCombinedId.invoke(null, getCombinedId(data.getItemType().getId(), data.getData()));
    }

    public static int getCombinedId(int id, byte data) {
        return id + (data << 12);
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
            Object packet = IReflection.getConstructor(PacketUtils.PacketPlayOutEntityDestroyClass, int[].class).newInstance(new int[] {getId(entity)});
            sendPacket(packet, players);
        }

        public static Object getTeleportPacket(Object entity, Location location) {
            IReflection.MethodAccessor getId = IReflection.getMethod(PacketUtils.EntityPlayerClass, "getId", int.class, null);
            int id = (int) getId.invoke(entity);

            Object packet = IReflection.getConstructor(PacketPlayOutEntityTeleportClass).newInstance();

            IReflection.FieldAccessor a = IReflection.getField(PacketPlayOutEntityTeleportClass, "a");
            IReflection.FieldAccessor b = IReflection.getField(PacketPlayOutEntityTeleportClass, "b");
            IReflection.FieldAccessor c = IReflection.getField(PacketPlayOutEntityTeleportClass, "c");
            IReflection.FieldAccessor d = IReflection.getField(PacketPlayOutEntityTeleportClass, "d");
            IReflection.FieldAccessor e = IReflection.getField(PacketPlayOutEntityTeleportClass, "e");
            IReflection.FieldAccessor f = IReflection.getField(PacketPlayOutEntityTeleportClass, "f");
            IReflection.FieldAccessor g = IReflection.getField(PacketPlayOutEntityTeleportClass, "g");

            a.set(packet, id);

            if(Version.getVersion().isBiggerThan(Version.v1_8)) {
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
            IReflection.MethodAccessor getId = IReflection.getMethod(PacketUtils.EntityPlayerClass, "getId", int.class, null);
            return (int) getId.invoke(entity);
        }

        public static Packet getVelocityPacket(Object entity, Vector vector) {
            int id = getId(entity);

            Packet packet = new Packet(PacketPlayOutEntityVelocityClass);
            packet.initialize(id, vector.getX(), vector.getY(), vector.getZ());

            return packet;
        }

        public static Packet getPassengerPacket(Object vehicle, Object passenger) {
            Packet packet = new Packet(PacketPlayOutAttachEntityClass);

            if(Version.getVersion().isBiggerThan(Version.v1_8)) {
                packet.initialize(new Class[] {EntityClass, EntityClass}, vehicle, passenger);
                return packet;
            } else {
                packet.initialize(new Class[] {int.class, EntityClass, EntityClass}, 0, passenger, vehicle);
                return packet;
            }
        }

        public static Packet getEjectPacket(Object vehicle) {
            Packet packet = new Packet(PacketPlayOutAttachEntityClass);

            if(Version.getVersion().isBiggerThan(Version.v1_8)) {
                packet.initialize(new Class[] {EntityClass, EntityClass}, vehicle, null);
                return packet;
            } else {
                packet.initialize(new Class[] {int.class, EntityClass, EntityClass}, 0, vehicle, null);
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
