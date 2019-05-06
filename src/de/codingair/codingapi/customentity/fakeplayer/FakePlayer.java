package de.codingair.codingapi.customentity.fakeplayer;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import de.codingair.codingapi.API;
import de.codingair.codingapi.customentity.fakeplayer.extras.AnimationType;
import de.codingair.codingapi.customentity.fakeplayer.extras.EquipmentType;
import de.codingair.codingapi.customentity.fakeplayer.extras.FakePlayerDataWatcher;
import de.codingair.codingapi.customentity.fakeplayer.extras.FakePlayerListener;
import de.codingair.codingapi.customentity.fakeplayer.extras.modules.*;
import de.codingair.codingapi.customentity.fakeplayer.extras.modules.utils.Module;
import de.codingair.codingapi.customentity.fakeplayer.extras.modules.utils.Type;
import de.codingair.codingapi.customentity.fakeplayer.extras.motions.FakePlayerMotionLook;
import de.codingair.codingapi.customentity.fakeplayer.extras.motions.FakePlayerMotionPosition;
import de.codingair.codingapi.player.data.Skin;
import de.codingair.codingapi.player.data.gameprofile.GameProfileUtils;
import de.codingair.codingapi.server.Environment;
import de.codingair.codingapi.server.reflections.IReflection;
import de.codingair.codingapi.server.reflections.PacketUtils;
import de.codingair.codingapi.server.Version;
import de.codingair.codingapi.tools.Callback;
import de.codingair.codingapi.utils.Removable;
import de.codingair.codingapi.tools.OldItemBuilder;
import gnu.trove.map.TIntObjectMap;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

/**
 * Removing of this disclaimer is forbidden.
 *
 * @author codingair
 * @verions: 1.0.0
 **/

public class FakePlayer implements Removable {
    private static int ID = 0;

    private UUID uniqueId = UUID.randomUUID();
    private static final double MOVE_SPEED = 4.3D / 20;
    private static final double SPRINT_SPEED = 6.5D / 20;
    private static final double MOVE_TICKS = 0.429D;
    private static final double JUMP_TICKS = 3.0D;
    private static final long RETRY_TIME = 3L;
    private static final long TABLIST_REMOVE_TIME = 20L;
    private static final int VIEW_RADIUS = 160;

    private JavaPlugin plugin;

    private final int id = FakePlayer.ID++;

    private Object player;
    private Location location;
    private Location oldLocation;

    private GameProfile gameProfile;
    private boolean onTablist;

    private List<Player> visible = new ArrayList<>();

    private boolean isSprinting = false;
    private boolean isSleeping = false;
    private Object[] sleepingStorage = new Object[3];
    private FakePlayerDataWatcher dataWatcher = new FakePlayerDataWatcher();

    private int moveTicks = 0;
    private int jumpTicks = 0;

    private FakePlayerListener listener;
    private List<Module> modules = new ArrayList<>();

    private FakePlayerMotionLook motionLook = null;
    private FakePlayerMotionPosition motionPosition = null;

    public final String CHAT_TAG_NAME = "$name";
    public final String CHAT_TAG_MESSAGE = "$message";
    private String chatFormat = "<" + CHAT_TAG_NAME + "§r> " + CHAT_TAG_MESSAGE;

    private Runnable runnable = null;

    public FakePlayer(GameProfile gameProfile, Location location, JavaPlugin plugin) {
        this.gameProfile = gameProfile;
        this.setLocation(location);
        this.oldLocation = location.clone();

        this.plugin = plugin;
    }

    @Override
    public void destroy() {
        destroy(null);
    }

    @Override
    public Class<? extends Removable> getAbstractClass() {
        return FakePlayer.class;
    }

    @Override
    public UUID getUniqueId() {
        return uniqueId;
    }

    @Override
    public Player getPlayer() {
        return null;
    }

    private void initialize() {
        if(this.player != null) return;

        Class<?> PlayerInteractManagerClass = IReflection.getClass(IReflection.ServerPacket.MINECRAFT_PACKAGE, "PlayerInteractManager");
        IReflection.ConstructorAccessor interactCon = IReflection.getConstructor(PlayerInteractManagerClass, PacketUtils.WorldServerClass);
        IReflection.ConstructorAccessor entityPlayerCon = IReflection.getConstructor(PacketUtils.EntityPlayerClass, PacketUtils.MinecraftServerClass, PacketUtils.WorldServerClass, GameProfile.class, PlayerInteractManagerClass);
        IReflection.ConstructorAccessor dataWatcherCon = IReflection.getConstructor(PacketUtils.DataWatcherClass, PacketUtils.EntityClass);

        Object worldServer = PacketUtils.getWorldServer(location.getWorld());
        Object player = entityPlayerCon.newInstance(PacketUtils.getMinecraftServer(), worldServer, this.gameProfile, interactCon.newInstance(worldServer));

        IReflection.MethodAccessor getDataWatcher = IReflection.getMethod(PacketUtils.EntityClass, "getDataWatcher", PacketUtils.DataWatcherClass, new Class[] {});
        IReflection.FieldAccessor setDataWatcher = IReflection.getField(PacketUtils.EntityClass, "datawatcher");

        IReflection.MethodAccessor setLocation = IReflection.getMethod(PacketUtils.EntityPlayerClass, "setLocation", null, new Class[] {double.class, double.class, double.class, float.class, float.class});
        setLocation.invoke(player, this.location.getX(), this.location.getY(), this.location.getZ(), this.location.getYaw(), this.location.getPitch());

        Object dataWatcher;
        if(Version.getVersion().isBiggerThan(Version.v1_8)) {
            //1.9 and bigger
            dataWatcher = getDataWatcher.invoke(player);

            IReflection.MethodAccessor set = IReflection.getMethod(PacketUtils.DataWatcherClass, "set", new Class[] {PacketUtils.DataWatcherObjectClass, Object.class});
            IReflection.FieldAccessor a = IReflection.getField(PacketUtils.DataWatcherRegistryClass, "a");
            IReflection.MethodAccessor A = IReflection.getMethod(PacketUtils.DataWatcherSerializerClass, "a", PacketUtils.DataWatcherObjectClass, new Class[] {int.class});

            set.invoke(dataWatcher, A.invoke(a.get(PacketUtils.DataWatcherRegistryClass), 13), (byte) 127);
        } else {
            //1.8 and lower
            Object nullObj = null;
            dataWatcher = dataWatcherCon.newInstance(nullObj);

            IReflection.MethodAccessor a = IReflection.getMethod(PacketUtils.DataWatcherClass, "a", null, new Class[] {int.class, Object.class});
            a.invoke(dataWatcher, 10, (byte) 127);
        }
        setDataWatcher.set(player, dataWatcher);

        this.player = player;
        API.addRemovable(this);
    }

    public void onTick() {
        if(hasModule(Type.TargetModule)) getModule(Type.TargetModule).onEvent();
        if(hasModule(Type.FollowModule)) getModule(Type.FollowModule).onEvent();
        if(hasModule(Type.ParticleModule)) getModule(Type.ParticleModule).onEvent();
        if(hasModule(Type.GravityModule)) getModule(Type.GravityModule).onEvent();

        if(moveTicks > 0) moveTicks--;
        if(jumpTicks > 0) jumpTicks--;

        if(this.motionLook != null) {
            float yaw = this.motionLook.getLook().getYaw() - this.location.getYaw();
            float pitch = this.motionLook.getLook().getPitch() - this.location.getPitch();

            if(yaw > 180) yaw -= 360;
            else if(yaw < -180) yaw += 360;

            yaw /= this.motionLook.getDivider();
            pitch /= this.motionLook.getDivider();

            this.updateHeadRotation(this.location.getYaw() + yaw, this.location.getPitch() + pitch);

            if(Math.abs(yaw) < 0.5 && Math.abs(pitch) < 0.5) {
                this.motionLook.getCallback().accept(true);
                this.motionLook = null;
            }
        }

        if(this.motionPosition != null) {
            if(this.motionPosition.hasGravity()) {
                this.addModule(Type.GravityModule);
                this.addModule(Type.JumpModule);
            }

            float[] calculated = this.calculateLook(this.motionPosition.getPosition().getX(), this.location.getY() + this.getEyeHeight(), this.motionPosition.getPosition().getZ());
            this.updateHeadRotation(calculated[0], calculated[1]);
            this.moveForward();

            this.motionPosition.getPosition().setY(this.location.getY());

            if(this.location.distance(this.motionPosition.getPosition()) < 0.2) {
                this.motionPosition.getCallback().accept(true);
                this.motionPosition = null;
            }
        }

        if(this.runnable != null) {
            Runnable runnable = this.runnable;
            this.runnable = null;
            runnable.run();
        }
    }

    public void teleport(Location location) {
        this.setLocation(location);

        IReflection.ConstructorAccessor con = IReflection.getConstructor(PacketUtils.PacketPlayOutEntityTeleportClass);

        Object packet = con.newInstance();

        IReflection.FieldAccessor a = IReflection.getField(PacketUtils.PacketPlayOutEntityTeleportClass, "a");
        IReflection.FieldAccessor b = IReflection.getField(PacketUtils.PacketPlayOutEntityTeleportClass, "b");
        IReflection.FieldAccessor c = IReflection.getField(PacketUtils.PacketPlayOutEntityTeleportClass, "c");
        IReflection.FieldAccessor d = IReflection.getField(PacketUtils.PacketPlayOutEntityTeleportClass, "d");
        IReflection.FieldAccessor e = IReflection.getField(PacketUtils.PacketPlayOutEntityTeleportClass, "e");
        IReflection.FieldAccessor f = IReflection.getField(PacketUtils.PacketPlayOutEntityTeleportClass, "f");
        IReflection.FieldAccessor g = IReflection.getField(PacketUtils.PacketPlayOutEntityTeleportClass, "g");

        a.set(packet, this.getEntityId());

        if(Version.getVersion().isBiggerThan(Version.v1_8)) {
            //new
            b.set(packet, location.getX());
            c.set(packet, location.getY());
            d.set(packet, location.getZ());
        } else {
            //old
            b.set(packet, this.toFixedPointNumber(location.getX()));
            c.set(packet, this.toFixedPointNumber(location.getY()));
            d.set(packet, this.toFixedPointNumber(location.getZ()));
        }

        e.set(packet, this.toAngle(location.getYaw()));
        f.set(packet, this.toAngle(location.getPitch()));
        g.set(packet, false);

        this.sendPacket(packet);
    }

    public void moveForward() {
        float yaw = (float) Math.toRadians(this.location.getYaw());

        double x = -Math.sin(yaw);
        double z = Math.cos(yaw);

        this.move(x, 0, z);
    }

    public void move(double x, double y, double z) {
        move(x, y, z, false);
    }

    public void move(double x, double y, double z, boolean jump) {
        double preX = this.location.getX();
        double preY = this.location.getY();
        double preZ = this.location.getZ();

        this.oldLocation.setX(preX);
        this.oldLocation.setY(preY);
        this.oldLocation.setZ(preZ);

        if(this.isSprinting) {
            x *= SPRINT_SPEED;
            z *= SPRINT_SPEED;
        } else {
            x *= MOVE_SPEED;
            z *= MOVE_SPEED;
        }

        boolean old = true;
        IReflection.ConstructorAccessor con = IReflection.getConstructor(PacketUtils.PacketPlayOutEntity$PacketPlayOutRelEntityMoveLookClass, new Class[] {int.class, byte.class, byte.class, byte.class, byte.class, byte.class, boolean.class});

        if(con == null) {
            con = IReflection.getConstructor(PacketUtils.PacketPlayOutEntity$PacketPlayOutRelEntityMoveLookClass, new Class[] {int.class, long.class, long.class, long.class, byte.class, byte.class, boolean.class});
            old = false;
        }

        Object packet;

        double changeX, changeY, changeZ;

        if(old) {
            packet = con.newInstance(getEntityId(), (byte) toFixedPointNumber(x), (byte) toFixedPointNumber(y), (byte) toFixedPointNumber(z), toAngle(this.location.getYaw()), toAngle(this.location.getPitch()), true);

            changeX = toFixedPointNumber(x) / 32D;
            changeY = toFixedPointNumber(y) / 32D;
            changeZ = toFixedPointNumber(z) / 32D;
        } else {
            packet = con.newInstance(getEntityId(), toFixedPointNumber(preX + x, preX), toFixedPointNumber(preY + y, preY), toFixedPointNumber(preZ + z, preZ), toAngle(this.location.getYaw()), toAngle(this.location.getPitch()), true);

            changeX = toFixedPointNumber(preX + x, preX) / 4096D;
            changeY = toFixedPointNumber(preY + y, preY) / 4096D;
            changeZ = toFixedPointNumber(preZ + z, preZ) / 4096D;
        }

        this.location.add(changeX, changeY, changeZ);

        if(JumpModule.hasToJump(this, location.clone()) && !JumpModule.canJump(this)) {
            this.location.add(-changeX, -changeY, -changeZ);
            return;
        }

        sendPacket(packet);

        int moveTicks = Math.round((float) ((Math.abs(x) + Math.abs(y) + Math.abs(z)) / MOVE_TICKS));
        if(moveTicks == 0) moveTicks = 1;

        int jumpTicks = Math.round((float) (y * JUMP_TICKS));
        if(jumpTicks == 0) jumpTicks = 1;

        if(y > 0) this.jumpTicks = jumpTicks;
        this.moveTicks = moveTicks;

        if(!jump) if(hasModule(Type.JumpModule)) getModule(Type.JumpModule).onEvent();
    }

    public void changeSkin(Skin skin) {
        this.gameProfile.getProperties().removeAll("textures");

        if(skin != null && skin.isLoaded() && skin.getValue() != null && !skin.getValue().isEmpty()) {
            skin.modifyProfile(this.gameProfile);
        }

        if(this.isSpawned()) respawn();
    }

    public void setName(String name) {
        GameProfile gameProfile = new GameProfile(this.gameProfile.getId(), name);

        gameProfile.getProperties().removeAll("textures");
        gameProfile.getProperties().putAll("textures", this.gameProfile.getProperties().get("textures"));

        this.gameProfile = gameProfile;
        if(this.isSpawned()) respawn();
    }

    public void setUUID(UUID uniqueId) {
        GameProfile gameProfile = new GameProfile(uniqueId, this.gameProfile.getName());

        gameProfile.getProperties().removeAll("textures");
        gameProfile.getProperties().putAll("textures", this.gameProfile.getProperties().get("textures"));

        this.gameProfile = gameProfile;
        if(this.isSpawned()) respawn();
    }

    public void updateHeadRotation(float yaw, float pitch) {
        if(!this.isSpawned()) return;

        Object packet = IReflection.getConstructor(PacketUtils.PacketPlayOutEntityLookClass, int.class, byte.class, byte.class, boolean.class).newInstance(this.getEntityId(), toAngle(yaw), toAngle(pitch), false);
        Object packetHead = IReflection.getConstructor(PacketUtils.PacketPlayOutEntityHeadRotationClass, PacketUtils.EntityClass, byte.class).newInstance(this.player, toAngle(yaw));

        sendPacket(packet);
        sendPacket(packetHead);

        this.location.setYaw(yaw);
        this.location.setPitch(pitch);

        IReflection.MethodAccessor setLocation = IReflection.getMethod(PacketUtils.EntityPlayerClass, "setLocation", null, new Class[] {double.class, double.class, double.class, float.class, float.class});
        setLocation.invoke(player, this.location.getX(), this.location.getY(), this.location.getZ(), this.location.getYaw(), this.location.getPitch());
    }

    public void updateHeadRotation(Player player) {
        if(!this.isSpawned()) return;

        float yaw = this.location.getYaw();
        float pitch = this.location.getPitch();

        Object packet = IReflection.getConstructor(PacketUtils.PacketPlayOutEntityLookClass, int.class, byte.class, byte.class, boolean.class).newInstance(this.getEntityId(), toAngle(yaw), toAngle(pitch), false);
        Object packetHead = IReflection.getConstructor(PacketUtils.PacketPlayOutEntityHeadRotationClass, PacketUtils.EntityClass, byte.class).newInstance(this.player, toAngle(yaw));

        sendPacket(player, packet);
        sendPacket(player, packetHead);
    }

    public void spawn(Location location, boolean tablist) {
        this.setLocation(location);
        this.spawn(tablist);
    }

    public void spawn(boolean tablist) {
        this.spawn(tablist, null);
    }

    public void spawn(boolean tablist, Player update) {
        if(this.isSpawned() && update == null) return;

        IReflection.ConstructorAccessor packet = IReflection.getConstructor(PacketUtils.PacketPlayOutNamedEntitySpawnClass, PacketUtils.EntityHumanClass);

        if(this.player == null) {
            this.onTablist = tablist;
            this.initialize();
        }

        Object spawn = packet.newInstance(player);
        Object tabAdd = PacketUtils.getPlayerInfoPacket(0, player);
        Object tabRemove = PacketUtils.getPlayerInfoPacket(4, player);

        if(update == null) {
            List<Player> trying = this.getPreparedPlayers();

            for(Player player : trying) {
                if(!isReady(player)) {
                    spawn(tablist, player);
                    return;
                } else {
                    sendPacket(player, tabAdd);
                    sendPacket(player, spawn);
                }
            }

            if(!tablist) {
                Bukkit.getScheduler().runTaskLater(plugin, new Runnable() {
                    boolean running = false;

                    @Override
                    public void run() {
                        for(Player trying : trying) {
                            if(!isReady(trying)) {
                                if(!running) {
                                    Bukkit.getScheduler().runTaskLater(plugin, this, RETRY_TIME);
                                    running = true;
                                }
                                return;
                            } else {
                                sendPacket(trying, tabRemove);
                            }
                        }

                        running = false;
                    }
                }, TABLIST_REMOVE_TIME);
            }
        } else {
            Runnable runnable = () -> {
                sendPacket(update, tabAdd);
                sendPacket(update, spawn);

                if(!tablist) {
                    Bukkit.getScheduler().runTaskLater(plugin, () -> {
                        sendPacket(update, tabRemove);
                        updateHeadRotation(update);
                    }, TABLIST_REMOVE_TIME);
                }
            };

            if(!isReady(update)) {
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        if(Bukkit.getPlayer(update.getUniqueId()) == null) {
                            this.cancel();
                        }

                        if(!isReady(update)) {
                            Bukkit.getScheduler().runTaskLater(plugin, this, RETRY_TIME);
                        } else {
                            runnable.run();
                        }
                    }
                }.runTaskLater(this.plugin, RETRY_TIME);
            } else {
                runnable.run();
            }

        }

        this.updateHeadRotation(this.location.getYaw(), this.location.getPitch());

        if(this.dataWatcher != null) setDataWatcher(this.dataWatcher);
    }

    public void setDataWatcher(FakePlayerDataWatcher dataWatcher) {
        if(dataWatcher == null) dataWatcher = new FakePlayerDataWatcher();
        this.dataWatcher = dataWatcher;

        if(!this.isSpawned()) return;

        byte status = 0;

        status = changeMask(status, 0, dataWatcher.isOnFire()); //FIRE
        status = changeMask(status, 1, dataWatcher.isSneaking()); //SNEAK
        status = changeMask(status, 2, false); //NOT SET
        status = changeMask(status, 3, dataWatcher.isSprinting()); //SPRINT
        status = changeMask(status, 4, dataWatcher.isUsingItem()); //USE_ITEM
        status = changeMask(status, 5, dataWatcher.isInvisible()); //INVISIBLE

        if(Version.getVersion().isBiggerThan(Version.v1_8)) {
            status = changeMask(status, 6, dataWatcher.isGlowing()); //GLOWING
            status = changeMask(status, 7, dataWatcher.isUsingElytra()); //USING ELYTRA
        }

        editDataWatcherByte(0, status);

        IReflection.ConstructorAccessor packetCon = IReflection.getConstructor(PacketUtils.PacketPlayOutEntityMetadataClass, int.class, PacketUtils.DataWatcherClass, boolean.class);
        Object packet = packetCon.newInstance(getEntityId(), getDataWatcher(), false);
        sendPacket(packet);
    }

    public FakePlayerDataWatcher getFakePlayerDataWatcher() {
        return this.dataWatcher;
    }

    private void editDataWatcherByte(int key, byte value) {
        editDataWatcherByte(key, value, getDataWatcher());
    }

    private void editDataWatcherByte(int key, byte value, Object dataWatcher) {
        if(dataWatcher == null) dataWatcher = getDataWatcher();
        if(dataWatcher == null) throw new NullPointerException("No DataWatcher found!");

        if(Version.getVersion().isBiggerThan(Version.v1_8)) {
            //1.9 and bigger
            IReflection.MethodAccessor set = IReflection.getMethod(PacketUtils.DataWatcherClass, "set", new Class[] {PacketUtils.DataWatcherObjectClass, Object.class});
            IReflection.FieldAccessor a = IReflection.getField(PacketUtils.DataWatcherRegistryClass, "a");
            IReflection.MethodAccessor A = IReflection.getMethod(PacketUtils.DataWatcherSerializerClass, "a", PacketUtils.DataWatcherObjectClass, new Class[] {int.class});

            set.invoke(dataWatcher, A.invoke(a.get(PacketUtils.DataWatcherRegistryClass), key), value);
        } else {
            //1.8 and lower
            IReflection.FieldAccessor getDataValues = IReflection.getField(PacketUtils.DataWatcherClass, "dataValues");
            TIntObjectMap dataValues = (TIntObjectMap) getDataValues.get(dataWatcher);
            if(dataValues.containsKey(0)) dataValues.remove(0);

            IReflection.MethodAccessor a = IReflection.getMethod(PacketUtils.DataWatcherClass, "a", null, new Class[] {int.class, Object.class});
            a.invoke(dataWatcher, key, value);
        }
    }

    private Object getDataWatcher() {
        IReflection.MethodAccessor getDataWatcher = IReflection.getMethod(PacketUtils.EntityClass, "getDataWatcher", PacketUtils.DataWatcherClass, new Class[] {});
        return getDataWatcher.invoke(this.player);
    }

    public void destroy(Player update) {
        if(!this.isSpawned()) return;

        IReflection.ConstructorAccessor destroyPacket = IReflection.getConstructor(PacketUtils.PacketPlayOutEntityDestroyClass, new Class[] {int[].class});
        Object destroy = destroyPacket.newInstance(new int[] {getEntityId()});
        Object tabRemove = PacketUtils.getPlayerInfoPacket(4, player);

        if(update == null) {
            sendPacket(destroy);
            sendPacket(tabRemove);

            unregister();

            API.removeRemovable(this);

            this.player = null;
        } else {
            sendPacket(update, destroy);
            sendPacket(update, tabRemove);
        }
    }

    public void playAnimation(AnimationType type) {
        if(type.isSound()) {
            if(type.equals(AnimationType.DEATH)) {
                setEquipment(EquipmentType.ALL, OldItemBuilder.getItem(Material.AIR));
            }

            Object packet = IReflection.getConstructor(PacketUtils.PacketPlayOutEntityStatusClass, new Class[] {PacketUtils.EntityClass, byte.class}).newInstance(this.player, (byte) type.getId());
            sendPacket(packet);
        } else {
            Object packet = IReflection.getConstructor(PacketUtils.PacketPlayOutAnimationClass, new Class[] {PacketUtils.EntityClass, int.class}).newInstance(this.player, type.getId());
            sendPacket(packet);
        }
    }

    public void setEquipment(EquipmentType type, ItemStack item) {
        if(type.equals(EquipmentType.ALL)) {
            for(EquipmentType equipmentType : EquipmentType.values()) {
                if(!equipmentType.equals(EquipmentType.ALL)) setEquipment(equipmentType, item);
            }

            return;
        }

        boolean old = true;
        IReflection.ConstructorAccessor con = IReflection.getConstructor(PacketUtils.PacketPlayOutEntityEquipmentClass, new Class[] {int.class, int.class, PacketUtils.ItemStackClass});

        if(con == null) {
            con = IReflection.getConstructor(PacketUtils.PacketPlayOutEntityEquipmentClass, new Class[] {int.class, PacketUtils.EnumItemSlotClass, PacketUtils.ItemStackClass});
            old = false;
        }

        Object packet;
        if(old) {
            packet = con.newInstance(getEntityId(), type.getId(), PacketUtils.getItemStack(item));
        } else {
            packet = con.newInstance(getEntityId(), PacketUtils.EnumItemSlotClass.getEnumConstants()[type.getId()], PacketUtils.getItemStack(item));
        }

        sendPacket(packet);
    }

    public void setSleeping(boolean isSleeping) {
        if(!Version.getVersion().isBiggerThan(Version.v1_7)) {
            throw new IllegalStateException("This version does not support Fakeplayers in Sleep-Mode! (Rquires 1.8 or bigger!)");
        }

        if(this.isSleeping == isSleeping) return;

        if(isSleeping) {
            IReflection.FieldAccessor a = IReflection.getField(PacketUtils.PacketPlayOutBedClass, "a");
            IReflection.FieldAccessor b = IReflection.getField(PacketUtils.PacketPlayOutBedClass, "b");

            Object packet = IReflection.getConstructor(PacketUtils.PacketPlayOutBedClass, null).newInstance();
            Location bedLoc = this.location.clone().subtract(0, 159, 0);
            if(bedLoc.getY() < 1) bedLoc.setY(1);

            sleepingStorage[0] = bedLoc.getBlock().getLocation().clone();
            sleepingStorage[1] = bedLoc.getBlock().getType();
            sleepingStorage[2] = bedLoc.getBlock().getData();

            a.set(packet, this.getEntityId());
            b.set(packet, PacketUtils.getBlockPosition(bedLoc));

            sendBlockChange(bedLoc, Material.BED_BLOCK, (byte) 0);
            sendPacket(packet);
            teleport(this.location.clone().add(0, 0.1, 0));
        } else {
            playAnimation(AnimationType.LEAVE_BED);
            teleport(this.location.clone().subtract(0, 0.1, 0));

            sendBlockChange((Location) sleepingStorage[0], (Material) sleepingStorage[1], (byte) sleepingStorage[2]);
            sleepingStorage = new Object[3];
        }

        this.isSleeping = isSleeping;
    }

    public void lookAt(LivingEntity entity) {
        if(isSleeping) return;

        double diffX = entity.getLocation().getX() - this.location.getX();
        double diffY = (entity.getLocation().getY() + entity.getEyeHeight() * 0.95) - (this.location.getY() + (this.isSneaking() ? 1.24F : 1.62F));
        double diffZ = entity.getLocation().getZ() - this.location.getZ();

        double hypoXZ = Math.sqrt(diffX * diffX + diffZ * diffZ);
        float yaw = (float) (Math.atan2(diffZ, diffX) * 180.0D / Math.PI) - 90.0F;
        float pitch = (float) -(Math.atan2(diffY, hypoXZ) * 180.0F / Math.PI);

        this.updateHeadRotation(prepareYaw(yaw), pitch);
    }

    public void lookTo(float yaw, float pitch, double divide, Callback<Boolean> callback) {
        if(isSleeping) {
            callback.accept(false);
            return;
        }

        Location look = this.location.clone();
        look.setYaw(prepareYaw(yaw));
        look.setPitch(pitch);

        this.motionLook = new FakePlayerMotionLook(this, look, callback, divide);
    }

    public void moveTo(double x, double z, boolean gravity, Callback<Boolean> callback) {
        if(isSleeping) {
            callback.accept(false);
            return;
        }

        Location position = this.location.clone();
        position.setX(x);
        position.setZ(z);

        this.motionPosition = new FakePlayerMotionPosition(this, position, gravity, callback);
    }

    public float[] calculateLook(double x, double y, double z) {
        double diffX = x - this.location.getX();
        double diffY = y - (this.location.getY() + this.getEyeHeight());
        double diffZ = z - this.location.getZ();

        double hypoXZ = Math.sqrt(diffX * diffX + diffZ * diffZ);
        float yaw = (float) (Math.atan2(diffZ, diffX) * 180.0D / Math.PI) - 90.0F;
        float pitch = (float) -(Math.atan2(diffY, hypoXZ) * 180.0F / Math.PI);

        return new float[] {prepareYaw(yaw), pitch};
    }

    public void respawn() {
        if(this.isSpawned()) this.destroy(null);
        this.spawn(this.onTablist);

        if(this.visible.isEmpty()) {
            updateModules(Bukkit.getOnlinePlayers().toArray(new Player[Bukkit.getOnlinePlayers().size()]));
        } else {
            updateModules(this.visible.toArray(new Player[this.visible.size()]));
        }
    }

    public int getEntityId() {
        if(!this.isSpawned()) return -1;

        IReflection.MethodAccessor id = IReflection.getMethod(PacketUtils.EntityPlayerClass, "getId", int.class, null);
        return (int) id.invoke(this.player);
    }

    public void addModule(Type type) {
        if(this.hasModule(type)) return;

        switch(type) {
            case TargetModule:
                this.modules.add(new TargetModule(this, 5));
                break;

            case InteractModule:
                this.modules.add(new InteractModule(this));
                ((InteractModule) getModule(Type.InteractModule)).register();
                break;

            case FollowModule:
                this.modules.add(new FollowModule(this));
                break;

            case ParticleModule:
                this.modules.add(new ParticleModule(this));
                break;

            case GravityModule:
                this.modules.add(new GravityModule(this));
                break;

            case JumpModule:
                this.modules.add(new JumpModule(this));
                break;
        }
    }

    public void removeModule(Type type) {
        if(!this.hasModule(type)) return;
        this.modules.remove(this.getModule(type));
    }

    public Module getModule(Type type) {
        for(Module module : this.modules) {
            if(module.getType().equals(type)) return module;
        }

        return null;
    }

    public boolean hasModule(Type type) {
        return this.getModule(type) != null;
    }

    public void unregister() {
        if(hasModule(Type.InteractModule)) ((InteractModule) getModule(Type.InteractModule)).unRegister();
    }

    public void updateModules(Player... player) {
        if(hasModule(Type.InteractModule)) {
            for(Player p : player) {
                ((InteractModule) getModule(Type.InteractModule)).register(p);
            }
        }
    }

    public boolean isOnGround() {
        Location loc = this.location.clone();
        loc.setY(loc.getY() - 0.0001);
        Block b = loc.getBlock();

        double height = Environment.getBlockHeight(loc.getBlock());
        loc.setY(loc.getY() - height);

        return Environment.isBlock(b) && !b.equals(loc.getBlock());
    }

    public void setVisible(boolean visible, Player... players) {
        for(Player player : players) {
            if(visible && !this.visible.contains(player)) this.visible.add(player);
            else this.visible.remove(player);
        }
    }

    public void setVisible(boolean visible) {
        Bukkit.getOnlinePlayers().forEach(player -> {
            if(visible && !this.visible.contains(player)) this.visible.add(player);
            else this.visible.remove(player);
        });
    }

    public void sendPacket(Object packet) {
        if(!this.visible.isEmpty())
            PacketUtils.sendPacket(packet, this.visible.toArray(new Player[this.visible.size()]));
        else PacketUtils.sendPacketToAll(packet);
    }

    private List<Player> getPreparedPlayers() {
        List<Player> prepared = new ArrayList<>();

        if(!this.visible.isEmpty()) prepared.addAll(this.visible);
        else prepared.addAll(Bukkit.getOnlinePlayers());

        return prepared;
    }

    public void sendPacket(Player p, Object packet) {
        PacketUtils.sendPacket(p, packet);
    }

    @SuppressWarnings("deprecation")
    private void sendBlockChange(Location location, Material material, byte data) {
        if(!this.visible.isEmpty())
            this.visible.forEach(p -> p.sendBlockChange(location, material, data));
        else Bukkit.getOnlinePlayers().forEach(p -> p.sendBlockChange(location, material, data));
    }

    private byte changeMask(byte bitMask, int bit, boolean state) {
        if(state)
            return bitMask |= 1 << bit;
        else
            return bitMask &= ~(1 << bit);
    }

    public void chat(String message) {
        String finalMsg = this.chatFormat.replace(CHAT_TAG_NAME, this.gameProfile.getName()).replace(CHAT_TAG_MESSAGE, message);

        if(this.visible.isEmpty()) {
            for(Player p : Bukkit.getOnlinePlayers()) {
                p.sendMessage(finalMsg);
            }
        } else {
            for(Player p : this.visible) {
                p.sendMessage(finalMsg);
            }
        }
    }

    public String getChatFormat() {
        return chatFormat;
    }

    public void setChatFormat(String chatFormat) {
        if(chatFormat.contains(CHAT_TAG_NAME) && chatFormat.contains(CHAT_TAG_MESSAGE)) {
            this.chatFormat = chatFormat;
        } else throw new IllegalStateException("The ChatFormat of the FakePlayer has to contains all ChatTags!");
    }

    private void setLocation(Location location) {
        location = location.clone();
        location.setYaw(prepareYaw(location.getYaw()));
        this.location = location;

        if(this.isSpawned()) {
            IReflection.MethodAccessor setLocation = IReflection.getMethod(PacketUtils.EntityPlayerClass, "setLocation", null, new Class[] {double.class, double.class, double.class, float.class, float.class});
            setLocation.invoke(player, this.location.getX(), this.location.getY(), this.location.getZ(), this.location.getYaw(), this.location.getPitch());
        }
    }

    private float prepareYaw(float yaw) {
        while(yaw > 360) yaw -= 360;
        while(yaw < -360) yaw += 360;
        return yaw;
    }

    public void updatePlayer(Player player) {
        this.destroy(player);
        this.spawn(this.onTablist, player);

        if(hasModule(Type.InteractModule)) {
            InteractModule module = (InteractModule) getModule(Type.InteractModule);
            module.register(player);
        }
    }

    private boolean isReady(Player p) {
        IReflection.FieldAccessor joining = IReflection.getField(PacketUtils.EntityPlayerClass, "joining");
        return !(boolean) joining.get(PacketUtils.getEntityPlayer(p))/* || API.getInstance().getPlayerData(p).loadedSpawnChunk()*/;
//        return true;
    }

    public boolean isInRange(Location location) {
        if(!location.getWorld().getName().equals(this.location.getWorld().getName())) return false;
        return location.distance(this.location) < FakePlayer.VIEW_RADIUS;
    }

    private byte toAngle(float value) {
        return (byte) ((int) (value * 256.0F / 360.0F));
    }

    private int toFixedPointNumber(double value) {
        return (int) Math.floor(value * 32D);
    }

    private long toFixedPointNumber(double current, double pre) {
        return (long) (((current * 32D) - (pre * 32D)) * 128D);
    }

    public boolean isOnTablist() {
        return onTablist;
    }

    public JavaPlugin getPlugin() {
        return plugin;
    }

    public FakePlayerListener getListener() {
        return listener;
    }

    public void setListener(FakePlayerListener listener) {
        this.listener = listener;
    }

    public boolean hasListener() {
        return this.listener != null;
    }

    public List<Module> getModules() {
        return modules;
    }

    public GameProfile getGameProfile() {
        return gameProfile;
    }

    public Location getLocation() {
        return location.clone();
    }

    public Object getFakePlayerObject() {
        return player;
    }

    public boolean isSpawned() {
        return player != null;
    }

    public List<Player> getVisible() {
        return visible;
    }

    public boolean isMoving() {
        return this.moveTicks != 0;
    }

    public boolean isJumping() {
        return this.jumpTicks != 0;
    }

    public Location getOldLocation() {
        return oldLocation;
    }

    public boolean isSprinting() {
        return isSprinting;
    }

    public void setSprinting(boolean sprinting) {
        isSprinting = sprinting;
    }

    private boolean isSneaking() {
        return getFakePlayerDataWatcher().isSneaking();
    }

    public FakePlayerMotionLook getMotionLook() {
        return motionLook;
    }

    public double getEyeHeight() {
        return 1.62;
    }

    public Runnable getRunnable() {
        return runnable;
    }

    public void setRunnable(Runnable runnable) {
        this.runnable = runnable;
    }

    public boolean isSleeping() {
        return isSleeping;
    }

    public int getId() {
        return id;
    }
}
