package de.CodingAir.v1_6.CodingAPI.Game.Map;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;

public class MapListener implements Listener {
	private Plugin plugin;
	private List<BlockBackup> backups = new ArrayList<>();
	
	public MapListener(Plugin plugin) {
		this.plugin = plugin;
		
		Bukkit.getPluginManager().registerEvents(this, this.plugin);
	}
	
	public void reset() {
		this.backups.forEach(BlockBackup::reset);
		this.backups = new ArrayList<>();
	}
	
	@EventHandler
	public void onBlockPlace(BlockPlaceEvent e) {
		Block b = e.getBlock();
		this.backups.add(new BlockBackup(b.getLocation(), e.getBlockReplacedState()));
	}
	
	@EventHandler
	public void onBlockBreak(BlockBreakEvent e) {
		Player p = e.getPlayer();
		Block b = e.getBlock();
		this.backups.add(new BlockBackup(b.getLocation(), b.getState()));
		
		
	}
	
	public Vector getRandomVector() {
		boolean xMinus = ((int) (Math.random() * 2)) == 0;
		boolean ZMinus = ((int) (Math.random() * 2)) == 0;
		
		double x = Math.random() * 0.2;
		double z = Math.random() * 0.2;
		
		if(xMinus) x *= -1;
		if(ZMinus) z *= -1;
		
		return new Vector(x, 0.3, z);
	}
	
	public boolean canPickupItem(Player p, ItemStack item) {
		int available = 0;
		
		for(ItemStack items : p.getInventory().getContents()) {
			if(items == null) return true;
			if(items.equals(item)) {
				available += 64 - items.getAmount();
			}
		}
		
		if(available >= item.getAmount()) return true;
		
		for(ItemStack items : p.getInventory().getContents()) {
			if(items == null || items.getType().equals(Material.AIR)) {
				return true;
			}
		}
		
		return false;
	}
}
