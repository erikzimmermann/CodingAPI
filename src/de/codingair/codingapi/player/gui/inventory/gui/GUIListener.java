package de.codingair.codingapi.player.gui.inventory.gui;

import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public interface GUIListener {
    void onInvClickEvent(InventoryClickEvent e);

    void onInvOpenEvent(InventoryOpenEvent e);

    void onInvCloseEvent(InventoryCloseEvent e);

    void onInvDragEvent(InventoryDragEvent e);

    void onMoveToTopInventory(ItemStack item, int oldRawSlot, List<Integer> newRawSlots);

    void onCollectToCursor(ItemStack item, List<Integer> oldRawSlots, int newRawSlot);
}