package de.codingair.codingapi.player;

import de.codingair.codingapi.API;
import de.codingair.codingapi.server.Version;
import de.codingair.codingapi.server.events.PlayerWalkEvent;
import de.codingair.codingapi.server.reflections.IReflection;
import de.codingair.codingapi.server.reflections.Packet;
import de.codingair.codingapi.server.reflections.PacketUtils;
import de.codingair.codingapi.tools.Converter;
import de.codingair.codingapi.utils.Removable;
import net.minecraft.server.v1_9_R1.EntityArmorStand;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

import java.util.*;

/**
 * Removing of this disclaimer is forbidden.
 *
 * @author codingair
 * @verions: 1.0.0
 **/

public class Hologram implements Removable {
    private static final double DISTANCE = 0.25;

    private UUID uniqueId = UUID.randomUUID();
    private List<Object> entities = new ArrayList<>();
    private List<Player> players = new ArrayList<>();

    private List<String> text;
    private Location location;
    private boolean initialized = false;
    private boolean visible = false;

    public Hologram(Location location, String... text) {
        this.text = Converter.fromArrayToList(text);
        this.location = location.clone();
    }

    public Hologram(Location location, Player p, String... text) {
        this.text = Converter.fromArrayToList(text);
        this.location = location.clone();
        addPlayer(p);
    }

    public static Listener getListener() {
        return new Listener() {
            @EventHandler
            public void onSwitchWorld(PlayerChangedWorldEvent e) {
                new Timer().schedule(new TimerTask() {
                    @Override
                    public void run() {
                        for(Hologram hologram : API.getRemovables(Hologram.class)) {
                            hologram.update(e.getPlayer());
                        }
                    }
                }, 500);
            }

            @EventHandler
            public void onTeleport(PlayerTeleportEvent e) {
                new Timer().schedule(new TimerTask() {
                    @Override
                    public void run() {
                        for(Hologram hologram : API.getRemovables(Hologram.class)) {
                            if(hologram.getLocation().getWorld() == e.getTo().getWorld() && hologram.getLocation().distance(e.getTo()) < 50) hologram.update(e.getPlayer());
                        }
                    }
                }, 500);
            }

            @EventHandler
            public void onWalk(PlayerWalkEvent e) {
                new Timer().schedule(new TimerTask() {
                    @Override
                    public void run() {
                        for(Hologram hologram : API.getRemovables(Hologram.class)) {
                            if(hologram.getLocation().getWorld() == e.getFrom().getWorld() && hologram.getLocation().getWorld() == e.getTo().getWorld() &&
                                    hologram.getLocation().distance(e.getFrom()) >= 50.0 && hologram.getLocation().distance(e.getTo()) < 50.0) {
                                hologram.update(e.getPlayer());
                            }
                        }
                    }
                }, 500);
            }
        };
    }

    @Override
    public void destroy() {
        remove();
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
        return uniqueId;
    }

    private void updateText() {
        if(!checkVersion()) return;
        Class<?> entity = IReflection.getClass(IReflection.ServerPacket.MINECRAFT_PACKAGE, "EntityArmorStand");
        IReflection.MethodAccessor setCustomName = IReflection.getMethod(entity, "setCustomName", new Class[] {String.class});
        IReflection.MethodAccessor getDataWatcher = IReflection.getMethod(PacketUtils.EntityClass, "getDataWatcher", PacketUtils.DataWatcherClass, new Class[] {});

        if(this.text.size() == this.entities.size()) {
            for(int i = 0; i < this.text.size(); i++) {
                String line = this.text.get(i);
                Object armorstand = this.entities.get(i);

                setCustomName.invoke(armorstand, line);

                Packet packet = new Packet(PacketUtils.PacketPlayOutEntityMetadataClass, getPreparedPlayers());
                packet.initialize(new Class[] {int.class, PacketUtils.DataWatcherClass, boolean.class}, PacketUtils.EntityPackets.getId(armorstand), getDataWatcher.invoke(armorstand), true);
                packet.send();
            }
        } else {
            update();
        }
    }

    public void updateText(Player player) {
        if(!checkVersion()) return;
        Class<?> entity = IReflection.getClass(IReflection.ServerPacket.MINECRAFT_PACKAGE, "EntityArmorStand");
        IReflection.MethodAccessor setCustomName = IReflection.getMethod(entity, "setCustomName", new Class[] {String.class});
        IReflection.MethodAccessor getDataWatcher = IReflection.getMethod(PacketUtils.EntityClass, "getDataWatcher", PacketUtils.DataWatcherClass, new Class[] {});

        if(this.text.size() == this.entities.size()) {
            for(int i = 0; i < this.text.size(); i++) {
                String line = this.text.get(i);
                Object armorstand = this.entities.get(i);

                setCustomName.invoke(armorstand, line);

                Packet packet = new Packet(PacketUtils.PacketPlayOutEntityMetadataClass, player);
                packet.initialize(new Class[] {int.class, PacketUtils.DataWatcherClass, boolean.class}, PacketUtils.EntityPackets.getId(armorstand), getDataWatcher.invoke(armorstand), true);
                packet.send();
            }
        } else {
            update();
        }
    }

    public void update() {
        if(!checkVersion()) return;
        if(initialized) {
            this.location.add(0, 2, 0);

            hide();
            if(text.size() != this.entities.size()) remove();
        }

        initialize();
        show();
    }

    public void show() {
        if(!checkVersion()) return;
        if(!initialized) initialize();

        for(Object armorStand : this.entities) {
            Object packet = IReflection.getConstructor(PacketUtils.PacketPlayOutSpawnEntityLivingClass, PacketUtils.EntityLivingClass).newInstance(armorStand);
            PacketUtils.sendPacket(packet, getPreparedPlayers());
        }

        visible = true;
    }

    public void update(Player player) {
        if(!checkVersion()) return;
        if(!initialized) return;

        if(player.getWorld() != this.location.getWorld() || (!this.players.isEmpty() && !this.players.contains(player))) return;

        //Remove for new spawn
        Class<?> entity = IReflection.getClass(IReflection.ServerPacket.MINECRAFT_PACKAGE, "EntityArmorStand");
        IReflection.MethodAccessor getId = IReflection.getMethod(entity, "getId", int.class, new Class[] {});

        for(Object armorStand : this.entities) {
            if(armorStand != null) {
                Object packet = IReflection.getConstructor(PacketUtils.PacketPlayOutEntityDestroyClass, int[].class).newInstance((Object) new int[] {(int) getId.invoke(armorStand)});
                PacketUtils.sendPacket(packet, player);
            }
        }

        if(visible) {
            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    for(Object armorStand : entities) {
                        Object packet = IReflection.getConstructor(PacketUtils.PacketPlayOutSpawnEntityLivingClass, PacketUtils.EntityLivingClass).newInstance(armorStand);
                        PacketUtils.sendPacket(packet, player);
                    }
                }
            }, 100);
        }
    }

    public void hide() {
        if(!checkVersion()) return;
        if(!initialized) return;

        Class<?> entity = IReflection.getClass(IReflection.ServerPacket.MINECRAFT_PACKAGE, "EntityArmorStand");
        IReflection.MethodAccessor getId = IReflection.getMethod(entity, "getId", int.class, new Class[] {});

        for(Object armorStand : this.entities) {
            if(armorStand != null) {
                Object packet = IReflection.getConstructor(PacketUtils.PacketPlayOutEntityDestroyClass, int[].class).newInstance((Object) new int[] {(int) getId.invoke(armorStand)});
                PacketUtils.sendPacket(packet, getPreparedPlayers());
            }
        }

        visible = false;
    }

    private boolean checkVersion() {
        return !Version.getVersion().equals(Version.v1_9);
    }

    private void initialize() {
        if(!checkVersion()) {
            System.out.println("[CodingAPI] Holograms are not supported in 1.9!");
            return;
        }

        if(initialized) remove();

        Class<?> entity = IReflection.getClass(IReflection.ServerPacket.MINECRAFT_PACKAGE, "EntityArmorStand");
        IReflection.MethodAccessor setCustomName = IReflection.getMethod(entity, "setCustomName", new Class[] {String.class});
        IReflection.MethodAccessor setCustomNameVisible = IReflection.getMethod(entity, "setCustomNameVisible", new Class[] {boolean.class});
        IReflection.MethodAccessor setInvisible = IReflection.getMethod(entity, "setInvisible", new Class[] {boolean.class});
        IReflection.MethodAccessor setInvulnerable = null;
        IReflection.MethodAccessor setGravity;

        if(Version.getVersion().isBiggerThan(Version.v1_8)) {
            setInvulnerable = IReflection.getMethod(entity, "setInvulnerable", new Class[] {boolean.class});
            setGravity = IReflection.getMethod(entity, "setNoGravity", new Class[] {boolean.class});
        } else {
            setGravity = IReflection.getMethod(entity, "setGravity", new Class[] {boolean.class});
        }

        this.location.subtract(0, 2, 0);
        this.location.add(0, this.text.size() * DISTANCE, 0);

        for(String line : this.text) {
            Object armorStand = create();

            setCustomName.invoke(armorStand, line);
            setCustomNameVisible.invoke(armorStand, !line.isEmpty());
            setInvisible.invoke(armorStand, true);
            if(setInvulnerable != null) setInvulnerable.invoke(armorStand, true);

            boolean gravity = false;
            if(Version.getVersion().isBiggerThan(Version.v1_8)) gravity = !gravity;
            setGravity.invoke(armorStand, gravity);

            this.location.subtract(0, DISTANCE, 0);
            this.entities.add(armorStand);
        }

        this.initialized = true;
        API.addRemovable(this);
    }

    private Player[] getPreparedPlayers() {
        if(!checkVersion()) return null;
        if(this.players.isEmpty()) {
            List<Player> players = new ArrayList<>();

            for(Player player : Bukkit.getOnlinePlayers()) {
                if(player.getWorld() == this.location.getWorld()) players.add(player);
            }

            return players.toArray(new Player[players.size()]);
        } else {
            List<Player> players = new ArrayList<>();

            for(Player player : this.players) {
                if(player.getWorld() == this.location.getWorld()) players.add(player);
            }

            return players.toArray(new Player[players.size()]);
        }
    }

    private Object create() {
        if(!checkVersion()) return null;
        Class<?> entity = IReflection.getClass(IReflection.ServerPacket.MINECRAFT_PACKAGE, "EntityArmorStand");
        return IReflection.getConstructor(entity, PacketUtils.WorldServerClass, double.class, double.class, double.class).newInstance(PacketUtils.getWorldServer(this.location.getWorld()), this.location.getX(), this.location.getY(), this.location.getZ());
    }

    public void remove() {
        if(!checkVersion()) return;
        hide();
        this.entities = new ArrayList<>();

        this.initialized = false;

        API.removeRemovable(this);
    }

    public void addPlayer(Player p) {
        if(!this.players.contains(p)) this.players.add(p);
    }

    public void removePlayer(Player p) {
        if(this.players.contains(p)) this.players.remove(p);
    }

    public List<String> getText() {
        return text;
    }

    public void setText(List<String> text) {
        this.text = text;
        this.updateText();
    }

    public void setText(String... text) {
        this.text = Converter.fromArrayToList(text);
        this.updateText();
    }

    public void addText(List<String> text) {
        this.text.addAll(text);
        this.update();
    }

    public void addText(String... text) {
        this.text.addAll(Converter.fromArrayToList(text));
        this.update();
    }

    public void teleport(Location location) {
        if(!checkVersion()) return;
        if(!this.initialized) return;
        this.location = location.clone();
        this.location.subtract(0, 2, 0);
        this.location.add(0, this.text.size() * DISTANCE, 0);

        for(Object entity : this.entities) {
            Object packet = PacketUtils.EntityPackets.getTeleportPacket(entity, this.location);
            PacketUtils.sendPacket(packet, getPreparedPlayers());
            this.location.subtract(0, DISTANCE, 0);
        }
    }

    public Location getLocation() {
        return location.clone();
    }

    public boolean isVisible() {
        return visible;
    }
}
