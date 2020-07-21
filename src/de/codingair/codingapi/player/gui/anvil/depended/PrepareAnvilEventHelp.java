package de.codingair.codingapi.player.gui.anvil.depended;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class PrepareAnvilEventHelp implements Listener {
    private Inventory inv;
    private ItemStack result = null;

    public void setInv(Inventory inv) {
        this.inv = inv;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onAnvilPrepareLow(PrepareAnvilEvent e) {
        if(e.getInventory().equals(inv)) {
            result = e.getResult();
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onAnvilPrepareHigh(PrepareAnvilEvent e) {
        if(e.getInventory().equals(inv)) {
            e.setResult(result);
        }
    }

    public void unregister() {
        HandlerList.unregisterAll(this);
        inv = null;
        result = null;
    }
}
