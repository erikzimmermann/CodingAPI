package de.codingair.codingapi.player.gui.hotbar;

import de.codingair.codingapi.API;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

public class EventListener implements Listener {
    private static EventListener instance;
    private static boolean registered = false;

    public static void register(Plugin plugin) {
        if(registered) return;

        Bukkit.getPluginManager().registerEvents(instance = new EventListener(), plugin);
        registered = true;
    }

    public static EventListener getInstance() {
        return instance;
    }

    @EventHandler
    public void onSwitch(PlayerItemHeldEvent e) {
        HotbarGUI gui = API.getRemovable(e.getPlayer(), HotbarGUI.class);
        if(gui == null) return;

        ItemComponent oldSlot = gui.getItem(e.getPreviousSlot());
        ItemComponent newSlot = gui.getItem(e.getNewSlot());

        if(oldSlot != null && oldSlot.getAction() != null) oldSlot.getAction().onUnhover(gui, oldSlot, newSlot, e.getPlayer());
        if(newSlot != null && newSlot.getAction() != null) newSlot.getAction().onHover(gui, oldSlot, newSlot, e.getPlayer());
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent e) {
        HotbarGUI gui;
        if((gui = API.getRemovable(e.getPlayer(), HotbarGUI.class)) != null) {
            ItemComponent ic = gui.getMenu()[e.getPlayer().getInventory().getHeldItemSlot()];

            if(ic == null || ic.getItem() == null) return;
            e.setCancelled(true);

            //PLAY SOUND
            if(ic.getClickSound() != null && !ic.isSilent()) ic.getClickSound().play(e.getPlayer());
            else if(gui.getClickSound() != null && !ic.isSilent()) gui.getClickSound().play(e.getPlayer());

            //DO ACTION
            if(ic.getAction() != null) ic.getAction().onClick(gui, ic, e.getPlayer(), ClickType.getByAction(e.getAction(), e.getPlayer()));

            //CLOSE
            if(ic.isCloseOnClick() && ic.getLink() == null) {
                gui.close(true);
                return;
            }

            //LINK
            if(ic.getLink() != null) {
                gui.close(false);
                ic.getLink().open(false);
            }
        }
    }

    @EventHandler
    public void onDrop(PlayerDropItemEvent e) {
        if(API.getRemovable(e.getPlayer(), HotbarGUI.class) != null) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onPickup(PlayerPickupItemEvent e) {
        if(API.getRemovable(e.getPlayer(), HotbarGUI.class) != null) {
            e.setCancelled(true);
        }
    }

}
