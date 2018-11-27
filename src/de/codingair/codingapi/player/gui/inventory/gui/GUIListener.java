package de.codingair.codingapi.player.gui.inventory.gui;

import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public abstract class GUIListener {
    public abstract void onInvClickEvent(InventoryClickEvent e);

    public abstract void onInvOpenEvent(InventoryOpenEvent e);

    public abstract void onInvCloseEvent(InventoryCloseEvent e);

    public abstract void onInvDragEvent(InventoryDragEvent e);

    @Deprecated
    public abstract void onMoveToTopInventory(ItemStack item, int oldRawSlot, List<Integer> newRawSlots);

    public boolean onMoveToTopInventory(int oldRawSlot, List<Integer> newRawSlots, ItemStack item) {
        return false;
    }

    public void onDropItem(InventoryClickEvent e) { }

    public void onClickBottomInventory(InventoryClickEvent e) {
    }

    public abstract void onCollectToCursor(ItemStack item, List<Integer> oldRawSlots, int newRawSlot);
}