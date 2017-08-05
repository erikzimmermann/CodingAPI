package de.CodingAir.v1_6.CodingAPI.Player.GUI.HoveredItems;

import de.CodingAir.v1_6.CodingAPI.API;
import de.CodingAir.v1_6.CodingAPI.Player.Data.PacketReader;
import de.CodingAir.v1_6.CodingAPI.Player.Hologram;
import de.CodingAir.v1_6.CodingAPI.Server.Reflections.IReflection;
import de.CodingAir.v1_6.CodingAPI.Server.Reflections.Packet;
import de.CodingAir.v1_6.CodingAPI.Server.Reflections.PacketUtils;
import de.CodingAir.v1_6.CodingAPI.Server.Version;
import de.CodingAir.v1_6.CodingAPI.Tools.OldItemBuilder;
import de.CodingAir.v1_6.CodingAPI.Utils.Removable;
import de.CodingAir.v1_6.CodingAPI.Utils.TextAlignment;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Removing of this disclaimer is forbidden.
 *
 * @author CodingAir
 * @verions: 1.0.0
 **/

public abstract class HoveredItem implements Removable {
	private UUID uniqueId = UUID.randomUUID();
	private static int ID_COUNTER = 0;
	private static final double ARMORSTAND_HEIGHT = 1.2D;
	private static final double HOLOGRAM_HEIGHT = 0.6D;
	
	public static final String EMPTY = "$EMPTY$";
	
	private int ID = ID_COUNTER++;
	private String name = null;
	
	private Plugin plugin;
	private Player player;
	
	private Object item;
	private Object armorStand;
	private ItemStack stack;
	private boolean lookAt = false;
	
	private Location location;
	private Location tempLocation;
	
	private Hologram hologram;
	
	private PacketReader packetReader;
	
	public HoveredItem(Player player, ItemStack item, Location location, Plugin plugin) {
		this.plugin = plugin;
		this.player = player;
		this.stack = item;
		this.location = location.clone();
		this.tempLocation = location.clone();
	}
	
	public HoveredItem(Player player, ItemStack item, Location location, Plugin plugin, String name) {
		this(player, item, location, plugin);
		this.name = name;
	}
	
	@Override
	public UUID getUniqueId() {
		return uniqueId;
	}
	
	@Override
	public void destroy() {
		remove();
	}
	
	@Override
	public Class<? extends Removable> getAbstractClass() {
		return HoveredItem.class;
	}
	
	public abstract void onInteract(Player p);
	
	public abstract void onLookAt(Player p);
	
	public abstract void onUnlookAt(Player p);
	
	public void update() {
		this.spawn();
	}
	
	public void spawn() {
		remove();
		
		Class<?> entityArmorStand = IReflection.getClass(IReflection.ServerPacket.MINECRAFT_PACKAGE, "EntityArmorStand");
		Class<?> entityItem = IReflection.getClass(IReflection.ServerPacket.MINECRAFT_PACKAGE, "EntityItem");
		
		IReflection.MethodAccessor setInvisible = IReflection.getMethod(entityArmorStand, "setInvisible", new Class[]{boolean.class});
		IReflection.FieldAccessor pickupDelay = IReflection.getField(entityItem, "pickupDelay");
		IReflection.FieldAccessor noDamageTicks = IReflection.getField(PacketUtils.EntityClass, "noDamageTicks");
		IReflection.FieldAccessor onGround = IReflection.getField(PacketUtils.EntityClass, "onGround");
		IReflection.MethodAccessor setGravity;
		if(Version.getVersion().isBiggerThan(Version.v1_8))
			setGravity = IReflection.getMethod(entityArmorStand, "setNoGravity", new Class[]{boolean.class});
		else setGravity = IReflection.getMethod(entityArmorStand, "setGravity", new Class[]{boolean.class});
		
		//Location for the armorstand
		Location temp = location.clone();
		temp.subtract(0, ARMORSTAND_HEIGHT, 0);
		
		/*
		 CREATE
		 */
		
		this.item = IReflection.getConstructor(entityItem, PacketUtils.WorldServerClass, double.class, double.class, double.class, PacketUtils.ItemStackClass).newInstance(PacketUtils.getWorldServer(this.location.getWorld()), this.location.getX(), this.location.getY(), this.location.getZ(), PacketUtils.getItemStack(stack));
		this.armorStand = IReflection.getConstructor(entityArmorStand, PacketUtils.WorldServerClass, double.class, double.class, double.class).newInstance(PacketUtils.getWorldServer(temp.getWorld()), temp.getX(), temp.getY(), temp.getZ());
		
		/*
		 MODIFIER
		 */
		
		pickupDelay.set(item, Integer.MAX_VALUE);
		noDamageTicks.set(item, Integer.MAX_VALUE);
		onGround.set(item, true);
		
		setInvisible.invoke(armorStand, true);
		setGravity.invoke(armorStand, false);
		noDamageTicks.set(armorStand, Integer.MAX_VALUE);
		
		/*
		 SPAWN
		 */
		
		Packet packet = new Packet(PacketUtils.PacketPlayOutSpawnEntityLivingClass, this.player);
		packet.initialize(new Class[]{PacketUtils.EntityLivingClass}, this.armorStand);
		packet.send();
		
		IReflection.MethodAccessor getDataWatcher = IReflection.getMethod(PacketUtils.EntityClass, "getDataWatcher", PacketUtils.DataWatcherClass, new Class[]{});
		PacketUtils.EntityPackets.spawnEntity(this.item, 2, this.player);
		packet = new Packet(PacketUtils.PacketPlayOutEntityMetadataClass, this.player);
		packet.initialize(new Class[]{int.class, PacketUtils.DataWatcherClass, boolean.class}, PacketUtils.EntityPackets.getId(this.item), getDataWatcher.invoke(this.item), true);
		packet.send();
		
		/*
		 SET PASSENGERS
		 */
		
		packet = PacketUtils.EntityPackets.getPassengerPacket(this.armorStand, this.item);
		if(packet != null) {
			packet.setPlayers(this.player);
			packet.send();
		}
		
		/*
		 HOLOGRAM
		 */
		
		List<String> text = new ArrayList<>();
		text.add(this.stack.getItemMeta().getDisplayName());
		if(this.stack.hasItemMeta() && this.stack.getItemMeta().hasLore())
			text.addAll(this.stack.getItemMeta().getLore());
		
		List<String> corrected = new ArrayList<>();
		
		for(String line : text) {
			if(line.contains(EMPTY)) corrected.add("");
			else corrected.add(line);
		}
		
		this.hologram = new Hologram(this.location.clone().add(0, HOLOGRAM_HEIGHT, 0), this.player, corrected.toArray(new String[corrected.size()]));
		this.hologram.update();
		
		this.packetReader = new PacketReader(this.player, "PacketReader_" + this.player.getName() + "_" + this.toString()) {
			@Override
			public boolean readPacket(Object packet) {
				if(packet.getClass().getSimpleName().equals("PacketPlayInUseEntity")) {
					if(isLookingAt(player)) {
						onInteract(player);
					}
				}
				
				return false;
			}
		};
		
		this.packetReader.inject();
		API.addRemovable(this);
	}
	
	public void remove() {
		if(this.item == null) return;
		
		PacketUtils.EntityPackets.destroyEntity(this.item, this.player);
		PacketUtils.EntityPackets.destroyEntity(this.armorStand, this.player);
		this.hologram.remove();
		
		this.item = null;
		this.armorStand = null;
		this.hologram = null;
		
		this.packetReader.unInject();
		API.removeRemovable(this);
	}
	
	public Plugin getPlugin() {
		return plugin;
	}
	
	public Player getPlayer() {
		return player;
	}
	
	public ItemStack getStack() {
		return stack;
	}
	
	public boolean isLookingAt(Player p) {
		double diffX = this.location.getX() - p.getLocation().getX();
		double diffY = (this.location.getY() + 0.4) - (p.getLocation().getY() + p.getEyeHeight());
		double diffZ = this.location.getZ() - p.getLocation().getZ();
		
		double hypoXZ = Math.sqrt(diffX * diffX + diffZ * diffZ);
		float yaw = (float) (Math.atan2(diffZ, diffX) * 180.0D / Math.PI) - 90.0F;
		float pitch = (float) -(Math.atan2(diffY, hypoXZ) * 180.0F / Math.PI);
		
		double result = Math.abs(yaw - p.getLocation().getYaw());
		if(result > 180) result = Math.abs(result - 360);
		
		if(result < 5) {
			if(Math.abs(pitch - p.getLocation().getPitch()) < 5)
				return true;
		}
		
		diffY = (this.location.getY() + 0.7) - (p.getLocation().getY() + p.getEyeHeight());
		pitch = (float) -(Math.atan2(diffY, hypoXZ) * 180.0F / Math.PI);
		
		if(result < 10) {
			if(Math.abs(pitch - p.getLocation().getPitch()) < 20)
				return true;
		}
		
		return false;
	}
	
	public void teleport(Location location) {
		teleport(location, false);
	}
	
	public void teleport(Location location, boolean temporarily) {
		if(this.item == null) return;
		
		if(!temporarily) {
			this.location = location.clone();
		}
		
		this.tempLocation = location.clone();
		
		Location temp = location.clone();
		temp.subtract(0, ARMORSTAND_HEIGHT, 0);
		
		PacketUtils.EntityPackets.getEjectPacket(this.armorStand).setPlayers(this.player).send();
		PacketUtils.sendPacket(this.player, PacketUtils.EntityPackets.getTeleportPacket(this.armorStand, temp));
		PacketUtils.EntityPackets.getPassengerPacket(this.armorStand, this.item).setPlayers(this.player).send();
		
		this.hologram.teleport(location.add(0, HOLOGRAM_HEIGHT, 0));
	}
	
	public Location getLocation() {
		return location.clone();
	}
	
	public void setLocation(Location location) {
		this.location = location.clone();
	}
	
	public Location getTempLocation() {
		return tempLocation.clone();
	}
	
	public boolean isLookAt() {
		return lookAt;
	}
	
	public void setLookAt(boolean lookAt) {
		this.lookAt = lookAt;
	}
	
	public void setText(String... text) {
		this.stack = OldItemBuilder.setText(this.stack, TextAlignment.LEFT, text);
		
		List<String> corrected = new ArrayList<>();
		
		for(String line : text) {
			if(line.contains(EMPTY)) corrected.add("");
			else corrected.add(line);
		}
		
		this.hologram.setText(corrected);
	}
	
	public void setText(List<String> text) {
		setText(text.toArray(new String[text.size()]));
	}
	
	public List<String> getText() {
		return this.hologram.getText();
	}
	
	public int getID() {
		return ID;
	}
	
	public String getName() {
		return name;
	}
	
	public static boolean usesItem(Player p) {
		return !getItems(p).isEmpty();
	}
	
	public static List<HoveredItem> getItems(Player p) {
		List<HoveredItem> hoveredItems = new ArrayList<>();
		
		for(HoveredItem hoveredItem : API.getRemovables(p, HoveredItem.class)) {
			if(hoveredItem.getPlayer().getName().equalsIgnoreCase(p.getName())) hoveredItems.add(hoveredItem);
		}
		
		return hoveredItems;
	}
}
