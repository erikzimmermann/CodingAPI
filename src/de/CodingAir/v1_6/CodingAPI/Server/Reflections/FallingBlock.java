package de.CodingAir.v1_6.CodingAPI.Server.Reflections;

import de.CodingAir.v1_6.CodingAPI.API;
import de.CodingAir.v1_6.CodingAPI.Server.Environment;
import de.CodingAir.v1_6.CodingAPI.Tools.Converter;
import de.CodingAir.v1_6.CodingAPI.Utils.Removable;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.material.MaterialData;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Removing of this disclaimer is forbidden.
 *
 * @author CodingAir
 * @verions: 1.0.0
 **/

public class FallingBlock implements Removable {
	private UUID uniqueId = UUID.randomUUID();
	private Location location;
	private MaterialData data;
	private Object fallingBlock;
	
	public FallingBlock(Location location, MaterialData data) {
		this.location = location;
		this.data = data;
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
