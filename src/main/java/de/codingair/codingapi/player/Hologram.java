package de.codingair.codingapi.player;

import com.github.Anon8281.universalScheduler.UniversalRunnable;
import com.github.Anon8281.universalScheduler.UniversalScheduler;
import de.codingair.codingapi.API;
import de.codingair.codingapi.nms.NmsLoader;
import de.codingair.codingapi.player.data.PacketReader;
import de.codingair.codingapi.server.events.PlayerWalkEvent;
import de.codingair.codingapi.server.reflections.IReflection;
import de.codingair.codingapi.server.reflections.Packet;
import de.codingair.codingapi.server.reflections.PacketUtils;
import de.codingair.codingapi.server.specification.Version;
import de.codingair.codingapi.utils.Removable;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.*;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;

public class Hologram implements Removable {
    private static final double DISTANCE_TO_SEE = 120;
    private static final double DISTANCE = 0.25;
    private static final double ARMOR_STAND_HEIGHT = 0.625;
    private static final Set<Object> DATA_WATCHER_OBJECTS = new HashSet<>();

    static {
        if (Version.atLeast(19.03)) {
            Class<?> c = HologramPackets.getArmorStandClass();
            DATA_WATCHER_OBJECTS.addAll(HologramPackets.getDataWatcherObjects(c));
        }
    }

    private final UUID uniqueId = UUID.randomUUID();
    private final JavaPlugin plugin;

    private final List<Object> entities = new ArrayList<>();
    private final List<Player> watchList = new ArrayList<>();
    private final HashMap<Player, Boolean> initiatedPlayers = new HashMap<>();

    private final List<String> text;
    private final long updateInterval;
    private Location source;
    private Location location;
    private boolean visible = false;
    private UniversalRunnable runnable;

    @NmsLoader
    private Hologram() {
        this.plugin = null;
        this.text = null;
        this.updateInterval = 0;
    }

    public Hologram(Location location, JavaPlugin plugin, String... text) {
        this(location, plugin, 0, text);
    }

    public Hologram(Location location, JavaPlugin plugin, long updateInterval, String... text) {
        if (!API.getInstance().isInitialized()) throw new IllegalStateException("API have to be initialized!");

        this.text = new ArrayList<>(Arrays.asList(text));
        if (location.getWorld() == null)
            throw new IllegalStateException("Could not initialize Hologram with a location without world!");
        this.source = location.clone();
        this.plugin = plugin;
        this.updateInterval = updateInterval / 50;
    }

    public static Listener getListener() {
        return new Listener() {
            @EventHandler(priority = EventPriority.LOWEST)
            public void onSwitchWorld(PlayerChangedWorldEvent e) {
                List<Hologram> l = API.getRemovables(null, Hologram.class);
                for (Hologram hologram : l) {
                    if (hologram.isNotWatching(e.getPlayer())) continue;

                    if (hologram.getLocation().getWorld() == e.getFrom()) {
                        hologram.remove(e.getPlayer());
                    } else if (hologram.getLocation().getWorld() == e.getPlayer().getWorld() &&
                            hologram.getLocation().distance(e.getPlayer().getLocation()) <= DISTANCE_TO_SEE)
                        hologram.update(e.getPlayer());
                }
                l.clear();
            }

            @EventHandler(priority = EventPriority.LOWEST)
            public void onTeleport(PlayerTeleportEvent e) {
                if (e.getTo() == null) return;
                if (e.getFrom().getWorld() != e.getTo().getWorld()) return;

                UniversalScheduler.getScheduler(API.getInstance().getMainPlugin()).runTaskLater(
                        () -> {
                            List<Hologram> l = API.getRemovables(null, Hologram.class);
                            for (Hologram hologram : l) {
                                if (hologram.isNotWatching(e.getPlayer())) continue;

                                double to = e.getTo().getWorld() != hologram.getLocation().getWorld() ? -1 : hologram.getLocation().distance(e.getTo());
                                double from = e.getFrom().getWorld() != hologram.getLocation().getWorld() ? -1 : hologram.getLocation().distance(e.getFrom());

                                if ((to != -1 && to <= DISTANCE_TO_SEE) && (from == -1 || from > DISTANCE_TO_SEE)) {
                                    hologram.update(e.getPlayer());
                                } else if ((to == -1 || to > DISTANCE_TO_SEE) && (from != -1 && from <= DISTANCE_TO_SEE)) {
                                    hologram.remove(e.getPlayer());
                                }
                            }
                            l.clear();
                        }, 2);
            }

            @EventHandler(priority = EventPriority.LOWEST)
            public void onJoin(PlayerJoinEvent e) {
                List<Hologram> l = API.getRemovables(null, Hologram.class);
                for (Hologram hologram : l) {
                    if (hologram.isNotWatching(e.getPlayer())) continue;
                    if (hologram.getLocation().getWorld() != e.getPlayer().getWorld()) continue;

                    if (hologram.getLocation().getWorld() == e.getPlayer().getWorld() &&
                            hologram.getLocation().distance(e.getPlayer().getLocation()) <= DISTANCE_TO_SEE)
                        hologram.update(e.getPlayer());
                }
                l.clear();
            }

            @EventHandler(priority = EventPriority.LOWEST)
            public void onQuit(PlayerQuitEvent e) {
                List<Hologram> l = API.getRemovables(null, Hologram.class);
                for (Hologram hologram : l) {
                    hologram.remove(e.getPlayer());
                    hologram.watchList.remove(e.getPlayer());
                }
                l.clear();
            }

            @EventHandler(priority = EventPriority.LOWEST)
            public void onWalk(PlayerWalkEvent e) {
                List<Hologram> l = API.getRemovables(null, Hologram.class);
                for (Hologram hologram : l) {
                    if (hologram.isNotWatching(e.getPlayer())) continue;

                    if (e.getFrom().getWorld() != e.getTo().getWorld() || e.getTo().getWorld() != hologram.getLocation().getWorld())
                        continue;

                    double to = hologram.getLocation().distance(e.getTo());
                    double from = hologram.getLocation().distance(e.getFrom());

                    if (to <= DISTANCE_TO_SEE && from > DISTANCE_TO_SEE) {
                        hologram.update(e.getPlayer(), true);
                    } else if (to > DISTANCE_TO_SEE && from <= DISTANCE_TO_SEE) {
                        hologram.remove(e.getPlayer());
                    }
                }
                l.clear();
            }
        };
    }

    private void initialize() {
        if (isInitialized()) remove();

        this.location = source.clone();
        this.location.subtract(0, ARMOR_STAND_HEIGHT, 0);
        this.location.add(0, this.text.size() * DISTANCE, 0);

        for (String line : this.text) {
            Object armorStand = HologramPackets.createArmorStand(this.location);

            HologramPackets.setCustomName(armorStand, line);
            HologramPackets.setCustomNameVisible(armorStand, !line.isEmpty());
            HologramPackets.setInvisible(armorStand, true);
            HologramPackets.setInvulnerable(armorStand, true);
            HologramPackets.setMarker(armorStand, true);
            HologramPackets.setGravity(armorStand, false);

            this.location.subtract(0, DISTANCE, 0);
            this.entities.add(armorStand);
        }

        initializeRunnable();
        API.addRemovable(this);
    }

    private void initializeRunnable() {
        if (updateInterval > 0) {
            this.runnable = new UniversalRunnable() {
                @Override
                public void run() {
                    List<Player> watchList = new ArrayList<>(Hologram.this.watchList);
                    for (Player player : watchList) {
                        initiatedPlayers.replace(player, true);
                    }
                    watchList.clear();

                    update(null);
                }
            };
            this.runnable.runTaskTimer(plugin, 0, updateInterval);
        }
    }

    public void teleport(Location location) {
        this.source = location.clone();
        if (!isInitialized()) return;

        this.location = location.clone();
        this.location.subtract(0, ARMOR_STAND_HEIGHT, 0);
        this.location.add(0, this.text.size() * DISTANCE, 0);

        for (Object entity : this.entities) {
            HologramPackets.setLocation(entity, this.location);
            HologramPackets.teleport(entity, this.location, this.initiatedPlayers.keySet().toArray(new Player[0]));
            this.location.subtract(0, DISTANCE, 0);
        }
    }

    public boolean isInitialized() {
        return API.getRemovable(Hologram.class, getUniqueId()) != null;
    }

    public void update() {
        if (cannotUpdateText()) {
            List<String> backupText = new ArrayList<>(this.text);
            destroy();
            this.text.addAll(backupText);
            backupText.clear();

            initialize();
        } else {
            for (int i = 0; i < this.entities.size(); i++) {
                Object entity = this.entities.get(i);
                String text = this.text.get(i);
                HologramPackets.setCustomName(entity, text);
            }
        }

        update(null);
    }

    public void update(Player player) {
        update(player, false);
    }

    private void update(Player player, boolean force) {
        List<Player> handle = new ArrayList<>();
        if (player != null) {
            if (isNotWatching(player)) return;

            handle.add(player);
        } else handle.addAll(this.watchList);

        for (Player p : handle) {
            if (!isSpawnable(p.getLocation(), force)) continue;

            if (visible) {
                if (isSpawnedFor(p)) {
                    //update text
                    if (hasChanged(p)) {
                        updateText(p);
                        setChanged(p, false);
                    }
                } else {
                    //spawn
                    spawn(p);
                }
            } else {
                if (isSpawnedFor(p)) {
                    //destroy
                    destroy(p);
                } else {
                    //put on update list
                    this.initiatedPlayers.put(player, false);
                }
            }
        }

        handle.clear();
    }

    private void updateText(Player player) {
        if (cannotUpdateText() || !isSpawnedFor(player)) return;

        for (int i = 0; i < this.entities.size(); i++) {
            HologramPackets.update(player, this.entities.get(i), modifyText(player, this.text.get(i)));
        }
    }

    public String modifyText(Player player, String text) {
        return text;
    }

    private void injectPacketReader(Player player) {
        if (Version.atLeast(17)) return; //todo: not supported at the moment

        //Interact event handler
        new PacketReader(player, "CodingAPI-HologramReader", getPlugin()) {
            @Override
            public boolean readPacket(Object packet) {
                if (packet.getClass().getSimpleName().equals("PacketPlayInUseEntity")) {
                    IReflection.FieldAccessor<?> action = IReflection.getField(packet.getClass(), "action");
                    IReflection.FieldAccessor<Integer> fA = IReflection.getField(packet.getClass(), "a");
                    int clicked = fA.get(packet);

                    String aS = action.get(packet).toString();
                    Action a;
                    if (aS.equals("INTERACT_AT")) a = Action.RIGHT_CLICK_AIR;
                    else if (aS.equals("ATTACK")) a = Action.LEFT_CLICK_AIR;
                    else return false;

                    List<Object> armorStands = new ArrayList<>(entities);
                    for (Object entity : armorStands) {
                        int id = PacketUtils.EntityPackets.getId(entity);
                        if (id == clicked) {
                            UniversalScheduler.getScheduler(Hologram.this.getPlugin()).runTask(() ->
                                    Bukkit.getPluginManager().callEvent(
                                            new PlayerInteractEvent(player, a, player.getInventory().getItem(player.getInventory().getHeldItemSlot()), null, BlockFace.UP)
                                    )
                            );
                            break;
                        }
                    }
                    armorStands.clear();
                }

                return false;
            }

            @Override
            public boolean writePacket(Object packet) {
                return false;
            }
        }.inject();
    }

    private void uninjectPacketReader(Player player) {
        if (Version.atLeast(17)) return; //todo: not supported at the moment

        //Interact event handler
        List<PacketReader> l = API.getRemovables(player, PacketReader.class);
        for (PacketReader reader : l) {
            if (reader.getName().equals("CodingAPI-HologramReader")) {
                reader.unInject();
                break;
            }
        }
        l.clear();
    }

    private void spawn(Player player) {
        if (isSpawnedFor(player)) return;

        injectPacketReader(player);
        for (Object entity : this.entities) {
            HologramPackets.spawn(player, entity);
        }

        //put on spawned list
        this.initiatedPlayers.put(player, true);
        update(player);
    }

    private void destroy(Player player) {
        if (!isSpawnedFor(player)) return;

        for (Object entity : this.entities) {
            HologramPackets.destroy(player, entity);
        }
        uninjectPacketReader(player);

        //remove from spawned list
        this.initiatedPlayers.remove(player);
    }

    private void remove() {
        remove(null);
    }

    private void remove(Player player) {
        //destroy and remove from list
        if (this.initiatedPlayers.containsKey(player)) {
            this.initiatedPlayers.remove(player);
            destroy(player);
        }
    }

    @Override
    public void destroy() {
        //remove all initedPlayers, destroy all entities, clear all lists
        this.watchList.forEach(this::destroy);

        this.initiatedPlayers.clear();
        this.watchList.clear();
        this.text.clear();
        this.entities.clear();

        if (this.runnable != null) {
            this.runnable.cancel();
            this.runnable = null;
        }

        API.removeRemovable(this);
    }

    private void setChanged() {
        for (Player player : this.initiatedPlayers.keySet()) {
            setChanged(player, true);
        }
    }

    private void setChanged(Player player, boolean changed) {
        this.initiatedPlayers.replace(player, changed);
    }

    private boolean hasChanged(Player player) {
        Boolean changed = this.initiatedPlayers.get(player);
        return changed != null && changed;
    }

    public void addPlayer(Player player) {
        if (!this.watchList.contains(player)) {
            this.watchList.add(player);
            update(player);
        }
    }

    public void addAll() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            addPlayer(player);
        }
    }

    public void removePlayer(Player player) {
        this.watchList.remove(player);
        destroy(player);
    }

    public boolean isNotWatching(Player player) {
        return !this.watchList.contains(player);
    }

    public void addText(String... text) {
        addText(new ArrayList<>(Arrays.asList(text)));
    }

    public void addText(List<String> text) {
        if (isSame(this.text, text)) return;

        setChanged();
        this.text.addAll(text);
        text.clear();
    }

    public List<String> getText() {
        return Collections.unmodifiableList(text);
    }

    public void setText(String... text) {
        setText(new ArrayList<>(Arrays.asList(text)));
    }

    public void setText(List<String> text) {
        if (isSame(this.text, text)) return;

        setChanged();
        this.text.clear();
        for (String s : text) {
            this.text.addAll(Arrays.asList(s.split("\n", -1)));
        }

        text.clear();
    }

    private boolean isSame(List<String> list0, List<String> list1) {
        if (list0 == list1) return true;
        if (list0 == null || list1 == null) return false;

        if (list0.size() == list1.size()) {
            boolean same = true;

            for (int i = 0; i < list0.size(); i++) {
                if (list0.get(i) == null || list1.get(i) == null) {
                    if (Objects.equals(list0.get(i), list1.get(i))) continue;
                    same = false;
                    break;
                } else if (!list0.get(i).equals(list1.get(i))) {
                    same = false;
                    break;
                }
            }

            return same;
        } else return false;
    }

    private boolean isSpawnable(Location location, boolean force) {
        return this.location != null && location != null && this.location.getWorld() == location.getWorld() && (force || this.location.distance(location) <= DISTANCE_TO_SEE);
    }

    private boolean cannotUpdateText() {
        return this.text == null || this.text.size() != this.entities.size();
    }

    private boolean isSpawnedFor(Player player) {
        return this.initiatedPlayers.containsKey(player);
    }

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        if (this.visible == visible) return;

        setChanged();
        this.visible = visible;
    }

    @Override
    public Player getPlayer() {
        return null;
    }

    @Override
    public UUID getUniqueId() {
        return this.uniqueId;
    }

    @Override
    public JavaPlugin getPlugin() {
        return this.plugin;
    }

    public Location getSource() {
        return source.clone();
    }

    public Location getLocation() {
        return location.clone();
    }

    public static class HologramPackets {
        private final static IReflection.MethodAccessor setInvisible = IReflection.getSaveMethod(ArmorStand.class, "setInvisible", null, boolean.class);
        private final static IReflection.MethodAccessor setVisible = IReflection.getSaveMethod(ArmorStand.class, "setVisible", null, boolean.class);
        private final static IReflection.FieldAccessor<Boolean> invulnerableField;
        private final static Class<?> dataWatcherItemClass;
        private final static Class<?> dataWatcherDataValueClass;
        private final static IReflection.MethodAccessor getDataWatcherItem;
        private final static IReflection.MethodAccessor serializeItem;
        private final static IReflection.MethodAccessor getDataWatcher;
        private final static IReflection.MethodAccessor setPosition;
        private final static IReflection.FieldAccessor<?> world = IReflection.getField(PacketUtils.EntityClass, PacketUtils.WorldClass, 0);
        private final static IReflection.ConstructorAccessor SPAWN_PACKET_CONSTRUCTOR;
        private final static IReflection.ConstructorAccessor DESTROY_PACKET_CONSTRUCTOR;
        private final static IReflection.ConstructorAccessor TELEPORT_PACKET_CONSTRUCTOR;
        private final static IReflection.ConstructorAccessor ARMOR_STAND_CONSTRUCTOR;

        static {
            if (Version.before(9)) {
                invulnerableField = IReflection.getField(PacketUtils.EntityClass, "invulnerable");
            } else invulnerableField = null;

            if (Version.atLeast(19.03)) {
                dataWatcherItemClass = IReflection.getClass(IReflection.ServerPacket.MINECRAFT_PACKAGE("net.minecraft.network.syncher"), Version.choose("SynchedEntityData$DataItem", "DataWatcher$Item"));
                dataWatcherDataValueClass = IReflection.getClass(IReflection.ServerPacket.MINECRAFT_PACKAGE("net.minecraft.network.syncher"), Version.choose("SynchedEntityData$DataValue", "DataWatcher$b", 20.05, "DataWatcher$c"));
                getDataWatcherItem = IReflection.getMethod(PacketUtils.DataWatcherClass, dataWatcherItemClass, new Class[]{PacketUtils.DataWatcherObjectClass});
                serializeItem = IReflection.getMethod(dataWatcherItemClass, dataWatcherDataValueClass, new Class[0]);
            } else {
                dataWatcherItemClass = null;
                dataWatcherDataValueClass = null;
                getDataWatcherItem = null;
                serializeItem = null;
            }

            if (Version.atLeast(21)) {
                setPosition = IReflection.getMethod(PacketUtils.EntityClass, null, new Class[]{double.class, double.class, double.class}, 0, m -> Modifier.isPublic(m) && Modifier.isFinal(m));
            } else {
                setPosition = IReflection.getMethod(PacketUtils.EntityClass, Version.choose("setPos", "setPosition", 18, "c", 19.03, "p"), new Class[]{double.class, double.class, double.class});
            }

            getDataWatcher = IReflection.getMethod(PacketUtils.EntityClass, PacketUtils.DataWatcherClass, new Class[]{});

            if (Version.atLeast(21)) {
                SPAWN_PACKET_CONSTRUCTOR = IReflection.getConstructor(PacketUtils.PacketPlayOutSpawnEntityClass, PacketUtils.EntityLivingClass, int.class, PacketUtils.BlockPositionClass);
            } else if (Version.atLeast(19)) {
                SPAWN_PACKET_CONSTRUCTOR = IReflection.getConstructor(PacketUtils.PacketPlayOutSpawnEntityClass, PacketUtils.EntityLivingClass);
            } else
                SPAWN_PACKET_CONSTRUCTOR = IReflection.getConstructor(PacketUtils.PacketPlayOutSpawnEntityLivingClass, PacketUtils.EntityLivingClass);
            if (SPAWN_PACKET_CONSTRUCTOR == null) throw new NullPointerException("Constructor could not be found.");

            if (Version.equals(17))
                DESTROY_PACKET_CONSTRUCTOR = IReflection.getConstructor(PacketUtils.PacketPlayOutEntityDestroyClass, int.class);
            else
                DESTROY_PACKET_CONSTRUCTOR = IReflection.getConstructor(PacketUtils.PacketPlayOutEntityDestroyClass, int[].class);
            if (DESTROY_PACKET_CONSTRUCTOR == null) throw new NullPointerException("Constructor could not be found.");

            if (Version.atLeast(17) && Version.before(21.02)) {
                TELEPORT_PACKET_CONSTRUCTOR = IReflection.getConstructor(PacketUtils.PacketPlayOutEntityTeleportClass, PacketUtils.EntityClass);
                if (TELEPORT_PACKET_CONSTRUCTOR == null)
                    throw new NullPointerException("Constructor could not be found.");
            } else TELEPORT_PACKET_CONSTRUCTOR = null;

            Class<?> entity = getArmorStandClass();
            ARMOR_STAND_CONSTRUCTOR = IReflection.getConstructor(entity, PacketUtils.WorldServerClass, double.class, double.class, double.class);
            if (ARMOR_STAND_CONSTRUCTOR == null) throw new NullPointerException("Constructor could not be found.");
        }

        @NmsLoader
        private HologramPackets() {
        }

        @NotNull
        private static ArmorStand bukkit(@NotNull Object nms) {
            return (ArmorStand) PacketUtils.getBukkitEntity(nms);
        }

        public static void setGravity(Object armorStand, boolean gravity) {
            bukkit(armorStand).setGravity(gravity);
        }

        public static void setInvisible(Object armorStand, boolean invisible) {
            ArmorStand as = bukkit(armorStand);

            if (setInvisible != null) setInvisible.invoke(as, invisible);
            else if (setVisible != null) setVisible.invoke(as, !invisible);
            else
                throw new IllegalStateException("Cannot find setInvisible nor setVisible for ArmorStands! Please contact the plugin author.");
        }

        public static void setInvulnerable(Object armorStand, boolean invulnerable) {
            if (Version.atLeast(9)) bukkit(armorStand).setInvulnerable(invulnerable);
            else {
                invulnerableField.set(armorStand, invulnerable);
            }
        }

        public static void setMarker(Object armorStand, boolean invulnerable) {
            bukkit(armorStand).setMarker(invulnerable);
        }

        public static void setCustomName(Object armorStand, String text) {
            bukkit(armorStand).setCustomName(text);
        }

        public static void setCustomNameVisible(Object armorStand, boolean visible) {
            bukkit(armorStand).setCustomNameVisible(visible);
        }

        public static void update(Player player, Object armorStand, String text) {
            setCustomName(armorStand, text);
            sendDataWatcher(player, armorStand);
        }

        public static void sendDataWatcher(Player player, Object armorStand) {
            if (Version.atLeast(19.03)) {
                assert PacketUtils.DataWatcherObjectClass != null;

                Object watcher = getDataWatcher.invoke(armorStand);

                // serialize all items manually
                List<Object> items = new ArrayList<>();
                for (Object dataWatcherObject : DATA_WATCHER_OBJECTS) {
                    Object item = getDataWatcherItem.invoke(watcher, dataWatcherObject);
                    if (item != null) items.add(serializeItem.invoke(item));
                }

                Packet packet = new Packet(PacketUtils.PacketPlayOutEntityMetadataClass, player);
                packet.initialize(new Class[]{int.class, List.class}, PacketUtils.EntityPackets.getId(armorStand), items);
                packet.send();
            } else {
                Packet packet = new Packet(PacketUtils.PacketPlayOutEntityMetadataClass, player);
                packet.initialize(new Class[]{int.class, PacketUtils.DataWatcherClass, boolean.class}, PacketUtils.EntityPackets.getId(armorStand), getDataWatcher.invoke(armorStand), true);
                packet.send();
            }
        }

        public static void destroy(Player player, Object armorStand) {
            Object packet;
            if (Version.equals(17)) {
                packet = DESTROY_PACKET_CONSTRUCTOR.newInstance(PacketUtils.EntityPackets.getId(armorStand));
            } else {
                packet = DESTROY_PACKET_CONSTRUCTOR.newInstance(new int[]{PacketUtils.EntityPackets.getId(armorStand)});
            }

            PacketUtils.sendPacket(packet, player);
        }

        public static void spawn(Player player, Object armorStand) {
            Object packet;
            if (Version.atLeast(21)) {
                Location l = bukkit(armorStand).getLocation();
                packet = SPAWN_PACKET_CONSTRUCTOR.newInstance(armorStand, 0, PacketUtils.getBlockPosition(l));

                // overwrite x, y, z coordinates (we use a simpler constructor to avoid using a lot of nms methods)
                IReflection.getNonStaticField(packet.getClass(), double.class, 0).set(packet, l.getX());
                IReflection.getNonStaticField(packet.getClass(), double.class, 1).set(packet, l.getY());
                IReflection.getNonStaticField(packet.getClass(), double.class, 2).set(packet, l.getZ());
            } else packet = SPAWN_PACKET_CONSTRUCTOR.newInstance(armorStand);
            PacketUtils.sendPacket(packet, player);

            if (Version.atLeast(15)) sendDataWatcher(player, armorStand);
        }

        public static void setLocation(Object armorStand, Location location) {
            world.set(armorStand, PacketUtils.getWorldServer(location.getWorld()));
            setPosition.invoke(armorStand, location.getX(), location.getY(), location.getZ());
        }

        public static void teleport(Object armorStand, Location location, Player... players) {
            if(Version.atLeast(21.02)) {
                Object packet = PacketUtils.EntityPackets.getTeleportPacket(armorStand, location);
                PacketUtils.sendPacket(packet, players);
            } else if (Version.atLeast(17)) {
                //set location before sending this packet
                Object packet = TELEPORT_PACKET_CONSTRUCTOR.newInstance(armorStand);
                PacketUtils.sendPacket(packet, players);
            } else {
                Object packet = PacketUtils.EntityPackets.getTeleportPacket(armorStand, location);
                PacketUtils.sendPacket(packet, players);
            }
        }

        public static Object createArmorStand(Location location) {
            return ARMOR_STAND_CONSTRUCTOR.newInstance(PacketUtils.getWorldServer(location.getWorld()), location.getX(), location.getY(), location.getZ());
        }

        @NotNull
        public static Class<?> getArmorStandClass() {
            return IReflection.getClass(IReflection.ServerPacket.MINECRAFT_PACKAGE("net.minecraft.world.entity.decoration"), Version.choose("EntityArmorStand", 21.11, "ArmorStand"));
        }

        public static Set<Object> getDataWatcherObjects(Class<?> c) {
            Set<Object> objects = new HashSet<>();
            for (Field declaredField : c.getDeclaredFields()) {
                if (PacketUtils.DataWatcherObjectClass == declaredField.getType()) {
                    declaredField.setAccessible(true);
                    try {
                        objects.add(declaredField.get(null));
                    } catch (IllegalAccessException e) {
                        throw new RuntimeException(e);
                    }
                }
            }

            if (c.getSuperclass() != null) objects.addAll(getDataWatcherObjects(c.getSuperclass()));
            return objects;
        }
    }
}
