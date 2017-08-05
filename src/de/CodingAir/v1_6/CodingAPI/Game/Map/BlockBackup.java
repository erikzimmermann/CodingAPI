package de.CodingAir.v1_6.CodingAPI.Game.Map;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.material.MaterialData;

public class BlockBackup {
	private Location loc;
	private Material type;
	private byte data;
	
	public BlockBackup(Block block) {
		this(block.getLocation(), block.getState());
	}
	
	public BlockBackup(Location loc, BlockState oldState) {
		MaterialData data = oldState.getData().clone();
		
		this.loc = loc.clone();
		this.type = data.getItemType();
		this.data = data.getData();
	}
	
	public void reset() {
		BlockState state = this.loc.getBlock().getState();
		
		MaterialData data = new MaterialData(this.type);
		data.setData(this.data);
		
		state.setType(this.type);
		state.setData(data);
		
		state.update(true);
	}
	
	public Location getLoc() {
		return loc;
	}
	
	public Material getType() {
		return type;
	}
	
	public byte getData() {
		return data;
	}
}
