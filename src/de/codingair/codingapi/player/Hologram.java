package de.codingair.codingapi.player;

import de.codingair.codingapi.API;
import de.codingair.codingapi.player.data.PacketReader;
import de.codingair.codingapi.server.Version;
import de.codingair.codingapi.server.events.PlayerWalkEvent;
import de.codingair.codingapi.server.reflections.IReflection;
import de.codingair.codingapi.server.reflections.Packet;
import de.codingair.codingapi.server.reflections.PacketUtils;
import de.codingair.codingapi.utils.Removable;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.*;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;

public class Hologram implements Removable {
    private static final double DISTANCE_TO_SEE = 120;
    private static final double DISTANCE = 0.25;

    private final UUID uniqueId = UUID.randomUUID();
    private JavaPlugin plugin;

    private List<Object> entities = new ArrayList<>();
    private List<Player> watchList = new ArrayList<>();
    private HashMap<Player, Boolean> initedPlayers = new HashMap<>();

    private List<String> text;
    private Location source;
    private Location location;
    private boolean visible = false;

    public Hologram(Location location, JavaPlugin plugin, String... text) {
        if(!API.getInstance().isInitialized()) throw new IllegalStateException("API have to be initialized!");

        this.text = new ArrayList<>(Arrays.asList(text));
        if(location.getWorld() == null) throw new IllegalStateException("Could not initialize Hologram with a location without world!");
        this.source = location.clone();
        this.plugin = plugin;
    }

    public static Listener getListener() {
        return new Listener() {
            @EventHandler(priority = EventPriority.LOWEST)
            public void onSwitchWorld(PlayerChangedWorldEvent e) {
                List<Hologram> holograms = API.getRemovables(Hologram.class);

                for(Hologram hologram : holograms) {
                    if(!hologram.isWatching(e.getPlayer())) continue;

                    if(hologram.getLocation().getWorld() == e.getFrom()) {
                        hologram.remove(e.getPlayer());
                    } else if(hologram.getLocation().getWorld() == e.getPlayer().getWorld() &&
                            hologram.getLocation().distance(e.getPlayer().getLocation()) <= DISTANCE_TO_SEE) hologram.update(e.getPlayer());
                }

                holograms.clear();
            }

            @EventHandler(priority = EventPriority.LOWEST)
            public void onTeleport(PlayerTeleportEvent e) {
                if(e.getFrom().getWorld() != e.getTo().getWorld()) return;

                Bukkit.getScheduler().runTaskLater(API.getInstance().getMainPlugin(), () -> {
                    List<Hologram> holograms = API.getRemovables(Hologram.class);

                    for(Hologram hologram : holograms) {
                        if(!hologram.isWatching(e.getPlayer())) continue;

                        double to = e.getTo().getWorld() != hologram.getLocation().getWorld() ? -1 : hologram.getLocation().distance(e.getTo());
                        double from = e.getFrom().getWorld() != hologram.getLocation().getWorld() ? -1 : hologram.getLocation().distance(e.getFrom());

                        if((to != -1 && to <= DISTANCE_TO_SEE) && (from == -1 || from > DISTANCE_TO_SEE)) {
                            hologram.update(e.getPlayer());
                        } else if((to == -1 || to > DISTANCE_TO_SEE) && (from != -1 && from <= DISTANCE_TO_SEE)) {
                            hologram.remove(e.getPlayer());
                        }
                    }

                    holograms.clear();
                }, 2);
            }

            @EventHandler(priority = EventPriority.LOWEST)
            public void onJoin(PlayerJoinEvent e) {
                List<Hologram> holograms = API.getRemovables(Hologram.class);

                for(Hologram hologram : holograms) {
                    if(!hologram.isWatching(e.getPlayer())) continue;
                    if(hologram.getLocation().getWorld() != e.getPlayer().getWorld()) continue;

                    if(hologram.getLocation().getWorld() == e.getPlayer().getWorld() &&
                            hologram.getLocation().distance(e.getPlayer().getLocation()) <= DISTANCE_TO_SEE) hologram.update(e.getPlayer());
                }

                holograms.clear();
            }

            @EventHandler(priority = EventPriority.LOWEST)
            public void onQuit(PlayerQuitEvent e) {
                List<Hologram> holograms = API.getRemovables(Hologram.class);

                for(Hologram hologram : holograms) {
                    hologram.remove(e.getPlayer());
                    hologram.watchList.remove(e.getPlayer());
                }

                holograms.clear();
            }

            @EventHandler(priority = EventPriority.LOWEST)
            public void onWalk(PlayerWalkEvent e) {
                List<Hologram> holograms = API.getRemovables(Hologram.class);

                for(Hologram hologram : API.getRemovables(Hologram.class)) {
                    if(!hologram.isWatching(e.getPlayer())) continue;

                    if(e.getFrom().getWorld() != e.getTo().getWorld() || e.getTo().getWorld() != hologram.getLocation().getWorld()) continue;

                    double to = hologram.getLocation().distance(e.getTo());
                    double from = hologram.getLocation().distance(e.getFrom());

                    if(to <= DISTANCE_TO_SEE && from > DISTANCE_TO_SEE) {
                        hologram.update(e.getPlayer(), true);
                    } else if(to > DISTANCE_TO_SEE && from <= DISTANCE_TO_SEE) {
                        hologram.remove(e.getPlayer());
                    }
                }

                holograms.clear();
            }
        };
    }

    private void initialize() {
        if(isInitialized()) remove();

        this.location = source.clone();
        this.location.subtract(0, 2, 0);
        this.location.add(0, this.text.size() * DISTANCE, 0);

        for(String line : this.text) {
            Object armorStand = HologramPackets.createArmorStand(this.location);

            HologramPackets.setCustomName(armorStand, line);
            HologramPackets.setCustomNameVisible(armorStand, !line.isEmpty());
            HologramPackets.setInvisible(armorStand, true);
            HologramPackets.setInvulnerable(armorStand, true);
            HologramPackets.setGravity(armorStand, false);

            this.location.subtract(0, DISTANCE, 0);
            this.entities.add(armorStand);
        }

        API.addRemovable(this);
    }

    public void teleport(Location location) {
        this.source = location.clone();
        if(!isInitialized()) return;

        this.location = location.clone();
        this.location.subtract(0, 2, 0);
        this.location.add(0, this.text.size() * DISTANCE, 0);

        for(Object entity : this.entities) {
            HologramPackets.setLocation(entity, this.location);
            Object packet = PacketUtils.EntityPackets.getTeleportPacket(entity, this.location);
            PacketUtils.sendPacket(packet, this.initedPlayers.keySet().toArray(new Player[0]));
            this.location.subtract(0, DISTANCE, 0);
        }
    }

    public boolean isInitialized() {
        return API.getRemovable(getAbstractClass(), getUniqueId()) != null;
    }

    public void update() {
        if(!canUpdateText()) {
            List<Player> backupPlayers = new ArrayList<>(this.initedPlayers.keySet());
            List<String> backupText = new ArrayList<>(this.text);

            destroy();

            for(Player backupPlayer : backupPlayers) {
                this.initedPlayers.put(backupPlayer, false);
            }
            this.text.addAll(backupText);

            backupPlayers.clear();
            backupText.clear();

            initialize();
        } else {
            for(int i = 0; i < this.entities.size(); i++) {
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
        if(player != null) {
            if(!isWatching(player)) return;

            handle.add(player);
        } else handle.addAll(this.watchList);

        for(Player p : handle) {
            if(!isSpawnable(p.getLocation(), force)) continue;

            if(visible) {
                if(isSpawnedFor(p)) {
                    //update text
                    if(hasChanged(p)) {
                        updateText(p);
                        setChanged(p, false);
                    }
                } else {
                    //spawn
                    spawn(p);
                }
            } else {
                if(isSpawnedFor(p)) {
                    //destroy
                    destroy(p);
                } else {
                    //put on update list
                    this.initedPlayers.put(player, false);
                }
            }
        }

        handle.clear();
    }

    private void updateText(Player player) {
        if(!canUpdateText() || !isSpawnedFor(player)) return;

        for(int i = 0; i < this.entities.size(); i++) {
            HologramPackets.update(player, this.entities.get(i), this.text.get(i));
        }
    }

    private void injectPacketReader(Player player) {
        //Interact event handler
        new PacketReader(player, "CodingAPI-HologramReader", getPlugin()) {
            @Override
            public boolean readPacket(Object packet) {
                if(packet.getClass().getSimpleName().equals("PacketPlayInUseEntity")) {
                    IReflection.FieldAccessor action = IReflection.getField(packet.getClass(), "action");
                    IReflection.FieldAccessor fA = IReflection.getField(packet.getClass(), "a");
                    int clicked = (int) fA.get(packet);

                    String aS = action.get(packet).toString();
                    Action a;
                    if(aS.equals("INTERACT_AT")) a = Action.RIGHT_CLICK_AIR;
                    else if(aS.equals("ATTACK")) a = Action.LEFT_CLICK_AIR;
                    else return false;

                    List<Object> armorStands = new ArrayList<>(entities);
                    for(Object entity : armorStands) {
                        int id = PacketUtils.EntityPackets.getId(entity);
                        if(id == clicked) {
                            Bukkit.getScheduler().runTask(Hologram.this.getPlugin(), () -> Bukkit.getPluginManager().callEvent(new PlayerInteractEvent(player, a, player.getInventory().getItem(player.getInventory().getHeldItemSlot()), null, null)));
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
        //Interact event handler
        List<PacketReader> l = API.getRemovables(player, PacketReader.class);

        for(PacketReader reader : l) {
            if(reader.getName().equals("CodingAPI-HologramReader")) {
                reader.unInject();
                break;
            }
        }

        l.clear();
    }

    private void spawn(Player player) {
        if(isSpawnedFor(player)) return;

        injectPacketReader(player);
        for(Object entity : this.entities) {
            HologramPackets.spawn(player, entity);
        }

        //put on spawned list
        this.initedPlayers.put(player, false);
    }

    private void destroy(Player player) {
        if(!isSpawnedFor(player)) return;

        for(Object entity : this.entities) {
            HologramPackets.destroy(player, entity);
        }
        uninjectPacketReader(player);

        //remove from spawned list
        this.initedPlayers.remove(player);
    }

    private void remove() {
        remove(null);
    }

    private void remove(Player player) {
        //destroy and remove from list
        if(this.initedPlayers.containsKey(player)) {
            this.initedPlayers.remove(player);
            destroy(player);
        }
    }

    @Override
    public void destroy() {
        //remove all initedPlayers, destroy all entities, clear all lists
        this.watchList.forEach(this::destroy);

        this.initedPlayers.clear();
        this.watchList.clear();
        this.text.clear();
        this.entities.clear();

        API.removeRemovable(this);
    }

    private void setChanged(boolean changed) {
        for(Player player : this.initedPlayers.keySet()) {
            setChanged(player, changed);
        }
    }

    private void setChanged(Player player, boolean changed) {
        this.initedPlayers.replace(player, changed);
    }

    private boolean hasChanged(Player player) {
        Boolean changed = this.initedPlayers.get(player);
        return changed == null ? false : changed;
    }

    public void addPlayer(Player player) {
        if(!this.watchList.contains(player)) {
            this.watchList.add(player);
            update(player);
        }
    }

    public void addAll() {
        for(Player player : Bukkit.getOnlinePlayers()) {
            addPlayer(player);
        }
    }

    public void removeAll() {
        List<Player> list = new ArrayList<>(this.watchList);

        for(Player player : list) {
            remove(player);
        }

        list.clear();
    }

    public void removePlayer(Player player) {
        this.watchList.remove(player);
        destroy(player);
    }

    public boolean isWatching(Player player) {
        return this.watchList.contains(player);
    }

    public void addText(String... text) {
        addText(new ArrayList<>(Arrays.asList(text)));
    }

    public void addText(List<String> text) {
        if(isSame(this.text, text)) return;

        setChanged(true);
        this.text.addAll(text);
        text.clear();
    }

    public void setText(String... text) {
        setText(new ArrayList<>(Arrays.asList(text)));
    }

    public void setText(List<String> text) {
        if(isSame(this.text, text)) return;

        setChanged(true);
        this.text.clear();
        this.text.addAll(text);

        text.clear();
    }

    public List<String> getText() {
        return Collections.unmodifiableList(text);
    }

    private boolean isSame(List<String> list0, List<String> list1) {
        if(list0 == list1) return true;
        if(list0 == null || list1 == null) {
            return list0 == list1;
        }

        if(list0.size() == list1.size()) {
            boolean same = true;

            for(int i = 0; i < list0.size(); i++) {
                if(list0.get(i) == null || list1.get(i) == null) {
                    if(list0.get(i) == list1.get(i)) continue;
                    same = false;
                    break;
                } else if(!list0.get(i).equals(list1.get(i))) {
                    same = false;
                    break;
                }
            }

            return same;
        } else return false;
    }

    private boolean isSpawnable(Location location) {
        return isSpawnable(location, false);
    }

    private boolean isSpawnable(Location location, boolean force) {
        return this.location != null && location != null && this.location.getWorld() == location.getWorld() && (force || this.location.distance(location) <= DISTANCE_TO_SEE);
    }

    private boolean canUpdateText() {
        return this.text != null && this.entities != null && this.text.size() == this.entities.size();
    }

    private boolean isSpawnedFor(Player player) {
        return this.initedPlayers.containsKey(player);
    }

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        if(this.visible == visible) return;

        setChanged(true);
        this.visible = visible;
    }

    @Override
    public Player getPlayer() {
        return null;
    }

    @Override
    public Class<? extends Removable> getAbstractClass() {
        return Hologram.class;
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

    private static class HologramPackets {
        public static void setGravity(Object armorStand, boolean gravity) {
            IReflection.MethodAccessor setGravity;

            if(Version.getVersion().isBiggerThan(Version.v1_9)) {
                setGravity = IReflection.getMethod(armorStand.getClass(), "setNoGravity", new Class[] {boolean.class});
            } else {
                setGravity = IReflection.getMethod(armorStand.getClass(), "setGravity", new Class[] {boolean.class});
            }

            if(Version.getVersion().isBiggerThan(Version.v1_9)) gravity = !gravity;
            setGravity.invoke(armorStand, gravity);
        }

        public static void setInvisible(Object armorStand, boolean invisible) {
            IReflection.MethodAccessor setInvisible = IReflection.getMethod(armorStand.getClass(), "setInvisible", new Class[] {boolean.class});
            setInvisible.invoke(armorStand, invisible);
        }

        public static void setInvulnerable(Object armorStand, boolean invulnerable) {
            if(Version.getVersion().isBiggerThan(Version.v1_9)) {
                IReflection.MethodAccessor setInvulnerable = IReflection.getMethod(armorStand.getClass(), "setInvulnerable", new Class[] {boolean.class});
                setInvulnerable.invoke(armorStand, invulnerable);
            }
        }

        public static void setCustomName(Object armorStand, String text) {
            Class<?> entity = IReflection.getClass(IReflection.ServerPacket.MINECRAFT_PACKAGE, "EntityArmorStand");
            IReflection.MethodAccessor setCustomName;

            if(Version.getVersion().isBiggerThan(Version.v1_12)) {
                setCustomName = IReflection.getMethod(entity, "setCustomName", new Class[] {PacketUtils.IChatBaseComponentClass});
            } else {
                setCustomName = IReflection.getMethod(entity, "setCustomName", new Class[] {String.class});
            }

            if(Version.getVersion().isBiggerThan(Version.v1_12)) {
                setCustomName.invoke(armorStand, PacketUtils.getIChatBaseComponent(text));
            } else {
                setCustomName.invoke(armorStand, text);
            }
        }

        public static void update(Player player, Object armorStand, String text) {
            IReflection.MethodAccessor getDataWatcher = IReflection.getMethod(PacketUtils.EntityClass, "getDataWatcher", PacketUtils.DataWatcherClass, new Class[] {});

            setCustomName(armorStand, text);

            Packet packet = player == null ? new Packet(PacketUtils.PacketPlayOutEntityMetadataClass, player) : new Packet(PacketUtils.PacketPlayOutEntityMetadataClass, player);
            packet.initialize(new Class[] {int.class, PacketUtils.DataWatcherClass, boolean.class}, PacketUtils.EntityPackets.getId(armorStand), getDataWatcher.invoke(armorStand), true);
            packet.send();
        }

        public static void setCustomNameVisible(Object armorStand, boolean visible) {
            IReflection.MethodAccessor setCustomNameVisible = IReflection.getMethod(armorStand.getClass(), "setCustomNameVisible", new Class[] {boolean.class});
            setCustomNameVisible.invoke(armorStand, visible);
        }

        public static void destroy(Player player, Object armorStand) {
            Object packet = IReflection.getConstructor(PacketUtils.PacketPlayOutEntityDestroyClass, int[].class).newInstance((Object) new int[] {PacketUtils.EntityPackets.getId(armorStand)});
            PacketUtils.sendPacket(packet, player);
        }

        public static void spawn(Player player, Object armorStand) {
            Object packet = IReflection.getConstructor(PacketUtils.PacketPlayOutSpawnEntityLivingClass, PacketUtils.EntityLivingClass).newInstance(armorStand);
            PacketUtils.sendPacket(packet, player);
        }

        public static void setLocation(Object armorStand, Location location) {
            IReflection.MethodAccessor setPosition = IReflection.getMethod(PacketUtils.EntityClass, "setPosition", new Class[] {double.class, double.class, double.class});
            IReflection.FieldAccessor world = IReflection.getField(PacketUtils.EntityClass, "world");

            world.set(armorStand, PacketUtils.getWorldServer(location.getWorld()));
            setPosition.invoke(armorStand, location.getX(), location.getY(), location.getZ());
        }

        public static Object createArmorStand(Location location) {
            Class<?> entity = IReflection.getClass(IReflection.ServerPacket.MINECRAFT_PACKAGE, "EntityArmorStand");
            return IReflection.getConstructor(entity, PacketUtils.WorldServerClass, double.class, double.class, double.class).newInstance(PacketUtils.getWorldServer(location.getWorld()), location.getX(), location.getY(), location.getZ());
        }
    }
}
