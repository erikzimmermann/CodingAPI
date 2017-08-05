package de.CodingAir.v1_6.CodingAPI.Player.GUI.Inventory;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 * Removing of this disclaimer is forbidden.
 *
 * @author CodingAir
 * @verions: 1.0.0
 **/

public class PlayerInventory {
	private Player player;
	private ItemStack[] content;
	private ItemStack[] armor;
	
	public PlayerInventory(Player player) {
		this.player = player;
		
		content = player.getInventory().getContents().clone();
		armor = player.getInventory().getArmorContents().clone();
	}
	
	public Player getPlayer() {
		return player;
	}
	
	public void restore() {
		this.player.getInventory().clear();
		this.player.getInventory().setContents(this.content.clone());
		this.player.getInventory().setArmorContents(this.armor.clone());
		this.player.updateInventory();
	}
	
	public ItemStack[] getContent() {
		return content;
	}
	
	public ItemStack[] getArmor() {
		return armor;
	}
}
