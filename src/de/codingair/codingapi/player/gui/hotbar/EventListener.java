package de.codingair.codingapi.player.gui.hotbar;

import de.codingair.codingapi.API;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
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
    public void onInteract(PlayerInteractEvent e) {
        ItemStack item = e.getItem();

        if(item == null || item.getType().equals(Material.AIR)) return;

        HotbarGUI gui;
        if((gui = API.getRemovable(e.getPlayer(), HotbarGUI.class)) != null) {
            ItemComponent ic = gui.getMenu()[e.getPlayer().getInventory().getHeldItemSlot()];

            if(ic == null || ic.getItem() == null) return;

            if(ic.getItem().isSimilar(item)) {
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
