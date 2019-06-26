package de.codingair.codingapi.player.gui.hotbar;

import de.codingair.codingapi.API;
import de.codingair.codingapi.player.gui.hotbar.components.ItemComponent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.*;
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
            if(System.currentTimeMillis() - gui.getLastClick() <= 50) {
                return;
            } else gui.setLastClick(System.currentTimeMillis());

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
                if(gui.getOnFinish() != null) gui.getOnFinish().run();
                return;
            }

            //LINK
            if(ic.getLink() != null) {
                gui.setWaiting(true);
                gui.setLastTriggeredSlot(e.getPlayer().getInventory().getHeldItemSlot());
                gui.close(false);
                ic.getLink().setBackup(gui.getBackup());
                ic.getLink().open(false);
                ic.getLink().setLastClick(System.currentTimeMillis());
                if(ic.getLink().getLastTriggeredSlot() != -1 && ic.getLink().getStartSlot() == -1) e.getPlayer().getInventory().setHeldItemSlot(ic.getLink().getLastTriggeredSlot());
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

    @EventHandler
    public void onInvClick(InventoryClickEvent e) {
        HotbarGUI h;
        if((h = API.getRemovable((Player) e.getWhoClicked(), HotbarGUI.class)) != null) {
            e.setCancelled(h.getItem(e.getSlot()) != null);
        }
    }

}
