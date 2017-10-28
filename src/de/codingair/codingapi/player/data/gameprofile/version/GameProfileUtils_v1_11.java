package de.codingair.codingapi.player.data.gameprofile.version;

import com.mojang.authlib.GameProfile;
import de.codingair.codingapi.player.data.Skin;
import net.minecraft.server.v1_11_R1.*;
import org.bukkit.craftbukkit.v1_11_R1.*;
import org.bukkit.craftbukkit.v1_11_R1.entity.*;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

public class GameProfileUtils_v1_11 {
	
	public static void updateGameProfile(Plugin plugin, Player p, Skin skin, String nickName) {
		CraftPlayer cp = (CraftPlayer) p;
		EntityPlayer enP = cp.getHandle();
		
		GameProfile profile = new GameProfile(p.getUniqueId(), nickName);
		MinecraftServer server = ((CraftServer) Bukkit.getServer()).getServer();
		WorldServer world = ((CraftWorld) Bukkit.getWorlds().get(0)).getHandle();
		
		if(skin != null && skin.isLoaded()){
			skin.modifyProfile(profile);
		} else {
			profile.getProperties().removeAll("textures");
			profile.getProperties().putAll("textures", cp.getProfile().getProperties().get("textures"));
		}
		
		EntityPlayer newPlayer = new EntityPlayer(server, world, profile, new PlayerInteractManager(world));
		
		double health = p.getHealth();
		int foodLevel = p.getFoodLevel();
		float exp = p.getExp();
		int level = p.getLevel();
		org.bukkit.inventory.ItemStack[] content = p.getInventory().getContents().clone();
		org.bukkit.inventory.ItemStack[] armor = p.getInventory().getArmorContents().clone();
		Location loc = p.getLocation().clone();
		
		cp.getInventory().clear();
		cp.setExp(0);
		cp.setLevel(0);
		
		PacketPlayOutEntityDestroy destroy = new PacketPlayOutEntityDestroy(cp.getEntityId());
		PacketPlayOutPlayerInfo tabRemove = new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.REMOVE_PLAYER, enP);
		PacketPlayOutPlayerInfo tabAdd = new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.ADD_PLAYER, newPlayer);
		
		cp.getHandle().playerConnection.sendPacket(destroy);
		cp.getHandle().playerConnection.sendPacket(tabRemove);
		cp.setHealth(0);
		
		Bukkit.getOnlinePlayers().forEach(all -> {
			if(!all.getName().equalsIgnoreCase(p.getName())){
				((CraftPlayer) all).getHandle().playerConnection.sendPacket(tabRemove);
			}
		});
		
		Bukkit.getScheduler().runTaskLater(plugin, new Runnable() {
			@Override
			public void run() {
				cp.spigot().respawn();
				
				cp.setHealth(health);
				cp.setFoodLevel(foodLevel);
				cp.setExp(exp);
				cp.setLevel(level);
				cp.getInventory().setContents(content);
				cp.getInventory().setArmorContents(armor);
				
				cp.teleport(loc);
				
				cp.getHandle().playerConnection.sendPacket(tabAdd);
				
				PacketPlayOutNamedEntitySpawn spawn = new PacketPlayOutNamedEntitySpawn(cp.getHandle());
				
				Bukkit.getOnlinePlayers().forEach(all -> {
					if(!all.getName().equalsIgnoreCase(p.getName())){
						((CraftPlayer) all).getHandle().playerConnection.sendPacket(destroy);
						((CraftPlayer) all).getHandle().playerConnection.sendPacket(spawn);
						((CraftPlayer) all).getHandle().playerConnection.sendPacket(tabAdd);
					}
				});
			}
		}, 2L);
	}
	
	public static void updateOtherGameProfile(Plugin plugin, Player p, Player other, Skin skin, String nickName) {
		CraftPlayer cp = (CraftPlayer) other;
		EntityPlayer enP = cp.getHandle();
		
		GameProfile profile = new GameProfile(other.getUniqueId(), nickName);
		MinecraftServer server = ((CraftServer) Bukkit.getServer()).getServer();
		WorldServer world = ((CraftWorld) Bukkit.getWorlds().get(0)).getHandle();
		
		if(skin != null && skin.isLoaded()){
			skin.modifyProfile(profile);
		} else {
			profile.getProperties().removeAll("textures");
			profile.getProperties().putAll("textures", cp.getProfile().getProperties().get("textures"));
		}
		
		EntityPlayer newPlayer = new EntityPlayer(server, world, profile, new PlayerInteractManager(world));
		
		PacketPlayOutEntityDestroy destroy = new PacketPlayOutEntityDestroy(cp.getEntityId());
		PacketPlayOutPlayerInfo tabRemove = new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.REMOVE_PLAYER, enP);
		PacketPlayOutPlayerInfo tabAdd = new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.ADD_PLAYER, newPlayer);
		PacketPlayOutNamedEntitySpawn spawn = new PacketPlayOutNamedEntitySpawn(cp.getHandle());
		
		((CraftPlayer) p).getHandle().playerConnection.sendPacket(destroy);
		((CraftPlayer) p).getHandle().playerConnection.sendPacket(tabRemove);
		
		Bukkit.getScheduler().runTaskLater(plugin, new Runnable() {
			@Override
			public void run() {
				((CraftPlayer) p).getHandle().playerConnection.sendPacket(spawn);
				((CraftPlayer) p).getHandle().playerConnection.sendPacket(tabAdd);
				
				Location loc = p.getLocation();
				Location temp = new Location(loc.getWorld(), 0, 100, 0);
				p.teleport(temp);
				
				new BukkitRunnable(){
					@Override
					public void run() {
						if(p.getLocation().equals(temp)) {
							p.teleport(loc);
							
							this.cancel();
						}
					}
				}.runTaskTimer(plugin, 2L, 2L);
			}
		}, 4L);
	}
}
