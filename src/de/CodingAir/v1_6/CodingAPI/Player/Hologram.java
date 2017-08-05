package de.CodingAir.v1_6.CodingAPI.Player;

import de.CodingAir.v1_6.CodingAPI.API;
import de.CodingAir.v1_6.CodingAPI.Server.Reflections.IReflection;
import de.CodingAir.v1_6.CodingAPI.Server.Reflections.Packet;
import de.CodingAir.v1_6.CodingAPI.Server.Reflections.PacketUtils;
import de.CodingAir.v1_6.CodingAPI.Server.Version;
import de.CodingAir.v1_6.CodingAPI.Tools.Converter;
import de.CodingAir.v1_6.CodingAPI.Utils.Removable;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Removing of this disclaimer is forbidden.
 *
 * @author CodingAir
 * @verions: 1.0.0
 **/

public class Hologram implements Removable {
	private UUID uniqueId = UUID.randomUUID();
	private static final double DISTANCE = 0.25;
	private List<Object> entities = new ArrayList<>();
	private List<Player> players = new ArrayList<>();
	
	private List<String> text;
	private Location location;
	private boolean initialized = false;
	
	public Hologram(Location location, String... text) {
		this.text = Converter.fromArrayToList(text);
		this.location = location.clone();
	}
	
	public Hologram(Location location, Player p, String... text) {
		this.text = Converter.fromArrayToList(text);
		this.location = location.clone();
		addPlayer(p);
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
	
	public void updateText() {
		Class<?> entity = IReflection.getClass(IReflection.ServerPacket.MINECRAFT_PACKAGE, "EntityArmorStand");
		IReflection.MethodAccessor setCustomName = IReflection.getMethod(entity, "setCustomName", new Class[]{String.class});
		IReflection.MethodAccessor getDataWatcher = IReflection.getMethod(PacketUtils.EntityClass, "getDataWatcher", PacketUtils.DataWatcherClass, new Class[]{});
		
		if(this.text.size() == this.entities.size()) {
			for(int i = 0; i < this.text.size(); i++) {
				String line = this.text.get(i);
				Object armorstand = this.entities.get(i);
				
				setCustomName.invoke(armorstand, line);
				
				Packet packet = new Packet(PacketUtils.PacketPlayOutEntityMetadataClass, this.players.toArray(new Player[this.players.size()]));
				packet.initialize(new Class[]{int.class, PacketUtils.DataWatcherClass, boolean.class}, PacketUtils.EntityPackets.getId(armorstand), getDataWatcher.invoke(armorstand), true);
				packet.send();
			}
		} else {
			update();
		}
	}
	
	public void update() {
		if(initialized) {
			this.location.add(0, 2, 0);
			
			hide();
			if(text.size() != this.entities.size()) remove();
		}
		
		initialize();
		show();
	}
	
	public void show() {
		if(!initialized) initialize();
		
		for(Object armorStand : this.entities) {
			Object packet = IReflection.getConstructor(PacketUtils.PacketPlayOutSpawnEntityLivingClass, PacketUtils.EntityLivingClass).newInstance(armorStand);
			PacketUtils.sendPacket(packet, this.players.toArray(new Player[this.players.size()]));
		}
	}
	
	public void hide() {
		if(!initialized) return;
		
		Class<?> entity = IReflection.getClass(IReflection.ServerPacket.MINECRAFT_PACKAGE, "EntityArmorStand");
		IReflection.MethodAccessor getId = IReflection.getMethod(entity, "getId", int.class, new Class[]{});
		
		for(Object armorStand : this.entities) {
			if(armorStand != null) {
				Object packet = IReflection.getConstructor(PacketUtils.PacketPlayOutEntityDestroyClass, int[].class).newInstance(new int[]{(int) getId.invoke(armorStand)});
				PacketUtils.sendPacket(packet, this.players.toArray(new Player[this.players.size()]));
			}
		}
	}
	
	private void initialize() {
		if(initialized) remove();
		
		Class<?> entity = IReflection.getClass(IReflection.ServerPacket.MINECRAFT_PACKAGE, "EntityArmorStand");
		IReflection.MethodAccessor setCustomName = IReflection.getMethod(entity, "setCustomName", new Class[]{String.class});
		IReflection.MethodAccessor setCustomNameVisible = IReflection.getMethod(entity, "setCustomNameVisible", new Class[]{boolean.class});
		IReflection.MethodAccessor setInvisible = IReflection.getMethod(entity, "setInvisible", new Class[]{boolean.class});
		IReflection.MethodAccessor setInvulnerable = null;
		IReflection.MethodAccessor setGravity;
		
		if(Version.getVersion().isBiggerThan(Version.v1_8)) {
			setInvulnerable = IReflection.getMethod(entity, "setInvulnerable", new Class[]{boolean.class});
			setGravity = IReflection.getMethod(entity, "setNoGravity", new Class[]{boolean.class});
		} else {
			setGravity = IReflection.getMethod(entity, "setGravity", new Class[]{boolean.class});
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
	}
	
	private Object create() {
		API.addRemovable(this);
		
		Class<?> entity = IReflection.getClass(IReflection.ServerPacket.MINECRAFT_PACKAGE, "EntityArmorStand");
		Object o = IReflection.getConstructor(entity, PacketUtils.WorldServerClass, double.class, double.class, double.class).newInstance(PacketUtils.getWorldServer(this.location.getWorld()), this.location.getX(), this.location.getY(), this.location.getZ());
		return o;
	}
	
	public void remove() {
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
		if(!this.initialized) return;
		this.location = location.clone();
		this.location.subtract(0, 2, 0);
		this.location.add(0, this.text.size() * DISTANCE, 0);
		
		for(Object entity : this.entities) {
			Object packet = PacketUtils.EntityPackets.getTeleportPacket(entity, this.location);
			PacketUtils.sendPacket(packet, this.players.toArray(new Player[this.players.size()]));
			this.location.subtract(0, DISTANCE, 0);
		}
	}
	
	public Location getLocation() {
		return location.clone();
	}
}
