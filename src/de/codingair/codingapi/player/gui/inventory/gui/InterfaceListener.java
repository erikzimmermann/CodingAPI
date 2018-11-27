package de.codingair.codingapi.player.gui.inventory.gui;

import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;

public interface InterfaceListener {
	void onInvClickEvent(InventoryClickEvent e);
	
	void onInvOpenEvent(InventoryOpenEvent e);
	
	void onInvCloseEvent(InventoryCloseEvent e);
	
	void onInvDragEvent(InventoryDragEvent e);
	
	default void addTo(Interface i) {
		i.addListener(this);
	}

	default void onClickBottomInventory(InventoryClickEvent e){}

	default void onDropItem(InventoryClickEvent e){}
}
