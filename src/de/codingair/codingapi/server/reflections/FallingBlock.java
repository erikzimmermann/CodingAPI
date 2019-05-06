package de.codingair.codingapi.server.reflections;

import de.codingair.codingapi.API;
import de.codingair.codingapi.server.Environment;
import de.codingair.codingapi.utils.Removable;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.material.MaterialData;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.UUID;

/**
 * Removing of this disclaimer is forbidden.
 *
 * @author codingair
 * @verions: 1.0.0
 **/

public class FallingBlock implements Removable {
	private UUID uniqueId = UUID.randomUUID();
	private Location location;
	private MaterialData data;
	private Object fallingBlock;
	private JavaPlugin plugin;
	
	public FallingBlock(Location location, MaterialData data, JavaPlugin plugin) {
		this.location = location;
		this.data = data;
		this.plugin = plugin;
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
		return FallingBlock.class;
	}
	
	@Override
	public UUID getUniqueId() {
		return uniqueId;
	}

	@Override
	public JavaPlugin getPlugin() {
		return plugin;
	}

	public void spawn() {
		this.fallingBlock = Environment.spawnNonSolidFallingBlock(this.location, this.data);
		API.addRemovable(this);
	}
	
	public void remove() {
		if(!isSpawned()) return;
		
		PacketUtils.EntityPackets.destroyEntity(this.fallingBlock, Bukkit.getOnlinePlayers().toArray(new Player[Bukkit.getOnlinePlayers().size()]));
		this.fallingBlock = null;
		
		API.removeRemovable(this);
	}
	
	public boolean isSpawned() {
		return this.fallingBlock != null;
	}
	
	public Location getLocation() {
		return location;
	}
	
	public MaterialData getData() {
		return data;
	}
}
