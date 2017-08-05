package de.CodingAir.v1_6.CodingAPI.Player.GUI.BossBar;

import de.CodingAir.v1_6.CodingAPI.API;
import de.CodingAir.v1_6.CodingAPI.CustomEntity.NetworkEntity.NetworkEntity;
import de.CodingAir.v1_6.CodingAPI.CustomEntity.NetworkEntity.NetworkEntityType;
import de.CodingAir.v1_6.CodingAPI.Server.Environment;
import de.CodingAir.v1_6.CodingAPI.Server.Reflections.IReflection;
import de.CodingAir.v1_6.CodingAPI.Server.Reflections.PacketUtils;
import de.CodingAir.v1_6.CodingAPI.Server.Version;
import de.CodingAir.v1_6.CodingAPI.Utils.Removable;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.Vector;

import java.util.UUID;

/**
 * Removing of this disclaimer is forbidden.
 *
 * @author CodingAir
 * @verions: 1.0.0
 **/

public class BossBar implements Removable {
	private UUID uniqueId;
	private float progress = 1F;
	private String message;
	private BarColor color;
	private BarStyle style = BarStyle.PROGRESS;
	private boolean darkSky = false;
	private boolean music = false;
	private boolean fog = false;
	private Player player;
	
	private Plugin plugin = null;
	private NetworkEntity[] entities = new NetworkEntity[6];
	
	public BossBar(Player player, String message, float progress, BarColor color, Plugin plugin) {
		this.player = player;
		this.uniqueId = UUID.randomUUID();
		this.message = message;
		this.progress = progress;
		this.color = color;
		this.plugin = plugin;
		
		checkProgress();
	}
	
	private void checkProgress() {
		if(progress <= 0) progress = 0.0001F;
	}
	
	@Override
	public UUID getUniqueId() {
		return uniqueId;
	}
	
	@Override
	public Class<? extends Removable> getAbstractClass() {
		return BossBar.class;
	}
	
	@Override
	public void destroy() {
		update(BossBarAction.REMOVE);
	}
	
	public void setUniqueId(UUID uniqueId) {
		this.uniqueId = uniqueId;
	}
	
	public float getProgress() {
		return progress;
	}
	
	public void setProgress(float progress) {
		this.progress = progress;
		
		checkProgress();
	}
	
	public String getMessage() {
		return message;
	}
	
	public void setMessage(String message) {
		this.message = message;
	}
	
	public BarColor getColor() {
		return color;
	}
	
	public void setColor(BarColor color) {
		this.color = color;
	}
	
	public BarStyle getStyle() {
		return style;
	}
	
	public void setStyle(BarStyle style) {
		this.style = style;
	}
	
	public boolean isDarkSky() {
		return darkSky;
	}
	
	public void setDarkSky(boolean darkSky) {
		this.darkSky = darkSky;
	}
	
	public boolean isMusic() {
		return music;
	}
	
	public void setMusic(boolean music) {
		this.music = music;
	}
	
	public boolean isFog() {
		return fog;
	}
	
	public void setFog(boolean fog) {
		this.fog = fog;
	}
	
	public void update(BossBarAction action) {
		if(action.equals(BossBarAction.ADD) && getBossBar(player) != null) return;
		if(!action.equals(BossBarAction.ADD) && getBossBar(player) == null) return;
		
		if(Version.getVersion().isBiggerThan(Version.v1_8)) {
			Class<?> packetClass = IReflection.getClass(IReflection.ServerPacket.MINECRAFT_PACKAGE, "PacketPlayOutBoss");
			Class<?> actionClass = IReflection.getClass(IReflection.ServerPacket.MINECRAFT_PACKAGE, "PacketPlayOutBoss$Action");
			Class<?> barColorClass = IReflection.getClass(IReflection.ServerPacket.MINECRAFT_PACKAGE, "BossBattle$BarColor");
			Class<?> barStyleClass = IReflection.getClass(IReflection.ServerPacket.MINECRAFT_PACKAGE, "BossBattle$BarStyle");
			IReflection.ConstructorAccessor constructorAccessor = IReflection.getConstructor(packetClass);
			
			IReflection.FieldAccessor setUuid = IReflection.getField(packetClass, "a");
			IReflection.FieldAccessor setAction = IReflection.getField(packetClass, "b");
			IReflection.FieldAccessor setMessage = IReflection.getField(packetClass, "c");
			IReflection.FieldAccessor setProgress = IReflection.getField(packetClass, "d");
			IReflection.FieldAccessor setColor = IReflection.getField(packetClass, "e");
			IReflection.FieldAccessor setStyle = IReflection.getField(packetClass, "f");
			IReflection.FieldAccessor setDarkenSky = IReflection.getField(packetClass, "g");
			IReflection.FieldAccessor setMusic = IReflection.getField(packetClass, "h");
			IReflection.FieldAccessor setCreateFog = IReflection.getField(packetClass, "i");
			
			Object packet = constructorAccessor.newInstance();
			
			setUuid.set(packet, this.getUniqueId());
			setAction.set(packet, actionClass.getEnumConstants()[action.getId()]);
			setMessage.set(packet, PacketUtils.getChatMessage(this.getMessage()));
			setProgress.set(packet, this.getProgress());
			setColor.set(packet, barColorClass.getEnumConstants()[this.getColor().getId()]);
			setStyle.set(packet, barStyleClass.getEnumConstants()[this.getStyle().getId()]);
			setDarkenSky.set(packet, this.isDarkSky());
			setMusic.set(packet, this.isMusic());
			setCreateFog.set(packet, this.isFog());
			
			PacketUtils.sendPacket(player, packet);
		} else {
			switch(action) {
				case ADD: {
					for(int i = 0; i < 6; i++) {
						NetworkEntity entity;
						this.entities[i] = entity = new NetworkEntity(NetworkEntityType.WITHER, getCardinalPoint(player, i), player);
						
						entity.setSteerable(false);
						entity.setDamageable(false);
						entity.setGravity(false);
						
						entity.spawn();
						entity.setHealth(this.progress * entity.getMaxHealth());
						entity.setCustomNameVisible(true);
						entity.setCustomName(this.message);
						entity.setInvisible(true);
					}
					
					break;
				}
				
				case REMOVE: {
					for(NetworkEntity entity : this.entities) {
						entity.destroy();
					}
					break;
				}
				
				default: {
					for(int i = 0; i < 6; i++) {
						NetworkEntity entity = this.entities[i];
						
						entity.setHealth(this.progress * entity.getMaxHealth());
						entity.setCustomName(this.message);
					}
					break;
				}
			}
		}
		
		if(action.equals(BossBarAction.ADD)) API.addRemovable(this);
		else if(action.equals(BossBarAction.REMOVE)) API.removeRemovable(this);
	}
	
	public Player getPlayer() {
		return player;
	}
	
	public Plugin getPlugin() {
		return plugin;
	}
	
	private void teleport(Location loc, int id) {
		this.entities[id].teleport(loc);
	}
	
	public static BossBar getBossBar(Player p) {
		return API.getRemovable(p, BossBar.class);
	}
	
	public static void onTick() {
		for(BossBar bar : API.getRemovables(BossBar.class)) {
			for(int i = 0; i < 6; i++) {
				Location loc = getCardinalPoint(bar.getPlayer(), i);
				bar.teleport(loc, i);
			}
		}
	}
	
	private static Location getCardinalPoint(Player player, int id) {
		Location location = player.getEyeLocation().clone();
		int distance = 90;
		int sub = 4;
		int maxBlockSuccession = 5;
		
		switch(id) {
			case 0: {
				int max = player.getLocation().getBlockY() + 150;
				Block top = Environment.getNextTopBlock(location.clone(), max, true);
				double y = top == null ? max : top.getY();
				
				if(y - location.getY() > distance) location.setY(location.getY() + distance);
				else location.setY(y + 5);
				
				break;
			}
			
			case 1: {
				Block bottom = Environment.getNextBottomBlock(location.clone(), true);
				double y = bottom == null ? 0 : bottom.getY();
				
				if(location.getY() - y > distance) location.setY(location.getY() - distance);
				else location.setY(y - 4);
				
				break;
			}
			
			case 2: {
				location.setX(player.getEyeLocation().getX() - distance);
				
				while(Environment.getBlocksBetween(player.getEyeLocation(), location, true).size() > maxBlockSuccession) {
					distance -= sub;
					location.setX(player.getEyeLocation().getX() - distance);
				}
				
				break;
			}
			
			case 3: {
				location.setX(player.getEyeLocation().getX() + distance);
				
				while(Environment.getBlocksBetween(player.getEyeLocation(), location, true).size() > maxBlockSuccession) {
					distance -= sub;
					location.setX(player.getEyeLocation().getX() + distance);
				}
				
				break;
			}
			
			case 4: {
				location.setZ(player.getEyeLocation().getZ() - distance);
				
				while(Environment.getBlocksBetween(player.getEyeLocation(), location, true).size() > maxBlockSuccession) {
					distance -= sub;
					location.setZ(player.getEyeLocation().getZ() - distance);
				}
				
				break;
			}
			
			case 5: {
				location.setZ(player.getEyeLocation().getZ() + distance);
				
				while(Environment.getBlocksBetween(player.getEyeLocation(), location, true).size() > maxBlockSuccession) {
					distance -= sub;
					location.setZ(player.getEyeLocation().getZ() + distance);
				}
				
				break;
			}
		}
		
		if(id >= 2 && id <= 5) {
			Block bottom = Environment.getNextBottomBlock(location.clone());
			location.setY(bottom == null ? 0 : bottom.getY() - 3.5);
			
			Vector v = player.getLocation().toVector().subtract(location.toVector());
			v.multiply(1 / (v.length() / sub));
			
			while(Environment.getBlocksBetween(player.getEyeLocation(), location, true).size() >= maxBlockSuccession) {
				location.add(0, 0.5, 0);
			}
		}
		
		double cD = location.distance(player.getLocation());
		if(cD > maxBlockSuccession) {
			double tooMuch = cD - maxBlockSuccession;
			
			Vector v = player.getLocation().toVector().subtract(location.toVector());
			double multiply = 1 - 1 / (v.length() / tooMuch);
			
			v.multiply(multiply);
			
			location.add(v);
		}
		
		if(id != 0) location.subtract(0, 4 + player.getEyeHeight(true), 0);
		
		return location;
	}
}
