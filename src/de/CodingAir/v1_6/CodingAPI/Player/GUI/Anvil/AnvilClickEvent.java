package de.CodingAir.v1_6.CodingAPI.Player.GUI.Anvil;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;

/**
 * Removing of this disclaimer is forbidden.
 *
 * @author CodingAir
 * @verions: 1.0.0
 **/

public class AnvilClickEvent extends Event {
	private Player player;
	private AnvilSlot slot;
	
	private ItemStack item;
	
	private boolean close = true;
	private boolean destroy = true;
	private boolean cancelled = false;
	
	private AnvilGUI anvil;
	
	public static HandlerList handlers = new HandlerList();
	
	public AnvilClickEvent(Player player, AnvilSlot slot, ItemStack item, AnvilGUI anvil) {
		this.anvil = anvil;
		this.player = player;
		this.slot = slot;
		this.item = item;
	}
	
	@Override
	public HandlerList getHandlers() {
		return handlers;
	}
	
	public AnvilGUI getAnvil() {
		return anvil;
	}
	
	public static HandlerList getHandlerList() {
		return handlers;
	}
	
	public AnvilSlot getSlot() {
		return slot;
	}
	
	public boolean getWillClose() {
		return close;
	}
	
	public void setWillClose(boolean close) {
		this.close = close;
	}
	
	public boolean getWillDestroy() {
		return destroy;
	}
	
	public void setWillDestroy(boolean destroy) {
		this.destroy = destroy;
	}
	
	public void setClose(boolean close) {
		this.destroy = close;
		this.close = close;
	}
	
	public boolean willClose() {
		return this.close;
	}
	
	public boolean isCancelled() {
		return cancelled;
	}
	
	public void setCancelled(boolean cancelled) {
		this.cancelled = cancelled;
	}
	
	public ItemStack getItem() {
		return item;
	}
	
	public Player getPlayer() {
		return player;
	}
	
	public String getInput() {
		if(this.item == null || !this.item.hasItemMeta()) return null;
		return this.item.getItemMeta().getDisplayName();
	}
}
