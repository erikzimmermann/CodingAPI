package de.CodingAir.v1_6.CodingAPI.Server.PlayerData;

import de.CodingAir.v1_6.CodingAPI.Files.TempFile;
import org.bukkit.entity.Player;

/**
 * Removing of this disclaimer is forbidden.
 *
 * @author CodingAir
 * @verions: 1.0.0
 **/

public class PlayerData extends TempFile<PlayerData> {
	private String name = null;
	private boolean loadedSpawnChunk = false;
	private int viewDistance = -999;
	
	public PlayerData(Player name) {
		super(PlayerData.class, new PlayerDataTypeAdapter(), true);
		super.setSavingFile(this);
		this.name = name.getName();
	}
	
	PlayerData(PlayerDataTypeAdapter adapter) {
		super(PlayerData.class, adapter, true);
		super.setSavingFile(this);
	}
	
	public String getName() {
		return name;
	}
	
	void setName(String name) {
		this.name = name;
	}
	
	public boolean loadedSpawnChunk() {
		return loadedSpawnChunk;
	}
	
	public void setLoadedSpawnChunk(boolean loadedSpawnChunk) {
		this.loadedSpawnChunk = loadedSpawnChunk;
	}
	
	public int getViewDistance() {
		return viewDistance;
	}
	
	public void setViewDistance(int viewDistance) {
		this.viewDistance = viewDistance;
	}
}
