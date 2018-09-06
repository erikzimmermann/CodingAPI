package de.codingair.codingapi.player.gui.inventory;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 * Removing of this disclaimer is forbidden.
 *
 * @author codingair
 * @verions: 1.0.0
 **/

public class PlayerInventory {
	private Player player;
	private ItemStack[] content;
	private ItemStack[] armor;
	
	public PlayerInventory(Player player) {
		this.player = player;

		content = new ItemStack[36];

		for(int i = 0; i < 36; i++) {
			content[i] = player.getInventory().getContents()[i] == null ? null : player.getInventory().getContents()[i].clone();
		}

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
