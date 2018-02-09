package de.codingair.codingapi.player.gui;

import de.codingair.codingapi.API;
import de.codingair.codingapi.player.gui.hovereditems.HoveredItem;
import de.codingair.codingapi.player.gui.inventory.gui.GUI;
import de.codingair.codingapi.player.gui.inventory.gui.Interface;
import de.codingair.codingapi.player.gui.inventory.gui.itembutton.ItemButton;
import de.codingair.codingapi.player.gui.hovereditems.ItemGUI;
import de.codingair.codingapi.player.gui.inventory.gui.InterfaceListener;
import de.codingair.codingapi.server.Sound;
import de.codingair.codingapi.server.SoundData;
import de.codingair.codingapi.server.events.PlayerWalkEvent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.*;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredListener;

import java.util.List;

/**
 * Removing of this disclaimer is forbidden.
 *
 * @author CodingAir
 * @verions: 1.0.0
 **/

public class GUIListener implements Listener {
    private static GUIListener instance = null;
    private Plugin plugin;

    public GUIListener(Plugin plugin) {
        if(instance != null) HandlerList.unregisterAll(instance);

        this.plugin = plugin;
        instance = this;
    }

    public static void register(Plugin plugin) {
        if(isRegistered()) return;
        Bukkit.getPluginManager().registerEvents(new GUIListener(plugin), plugin);
    }

    public static boolean isRegistered() {
        if(instance == null) return false;

        for(RegisteredListener registeredListener : HandlerList.getRegisteredListeners(instance.plugin)) {
            if(registeredListener.getListener() instanceof GUIListener) return true;
        }

        return false;
    }

    public static void onTick() {
        for(HoveredItem item : API.getRemovables(HoveredItem.class)) {
            boolean lookingAt = item.isLookingAt(item.getPlayer());

            if(lookingAt && !item.isLookAt()) {
                item.onLookAt(item.getPlayer());
                item.setLookAt(true);
            } else if(!lookingAt && item.isLookAt()) {
                item.onUnlookAt(item.getPlayer());
                item.setLookAt(false);
            }
        }
    }


	/*
     * PlayerItem
	 */

    @EventHandler
    public void onInteractEvent(PlayerInteractEvent e) {
        if(!PlayerItem.isUsing(e.getPlayer())) return;

        List<PlayerItem> items = PlayerItem.getPlayerItems(e.getPlayer());
        Player p = e.getPlayer();
        ItemStack item = p.getInventory().getItemInHand();

        for(PlayerItem pItem : items) {
            if(item != null && pItem.equals(item)) pItem.onInteract(e);
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {
        if(!PlayerItem.isUsing((Player) e.getWhoClicked())) return;

        List<PlayerItem> items = PlayerItem.getPlayerItems((Player) e.getWhoClicked());
        ItemStack current = e.getCurrentItem();

        for(PlayerItem pItem : items) {
            if(pItem.equals(current) && pItem.isFreezed()) e.setCancelled(true);
        }
    }

    @EventHandler
    public void onDrop(PlayerDropItemEvent e) {
        if(!PlayerItem.isUsing(e.getPlayer())) return;

        List<PlayerItem> items = PlayerItem.getPlayerItems(e.getPlayer());
        ItemStack current = e.getItemDrop().getItemStack();

        for(PlayerItem pItem : items) {
            if(pItem.equals(current) && pItem.isFreezed()) e.setCancelled(true);
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        if(!PlayerItem.isUsing(e.getPlayer())) return;
        List<PlayerItem> items = PlayerItem.getPlayerItems(e.getPlayer());

        for(PlayerItem pItem : items) {
            pItem.remove();
        }
    }

	/*
	 * Interface
	 */

    @EventHandler
    public void onInvClickEvent(InventoryClickEvent e) {
        if(e.getInventory() == null || (!GUI.usesGUI((Player) e.getWhoClicked()) && !GUI.usesOldGUI((Player) e.getWhoClicked()))) {
            return;
        }
        Player p = (Player) e.getWhoClicked();

        Interface inv = GUI.usesGUI(p) ? GUI.getGUI(p) : GUI.getOldGUI(p);

        if(e.getInventory().getName().equals(inv.getInventory().getName())) {
            e.setCancelled(!inv.isEditableItems());

            if(e.getClickedInventory() == null) return;

            if(e.getClickedInventory().getName().equals(inv.getInventory().getName())) {
                for(InterfaceListener l : inv.getListener()) {
                    l.onInvClickEvent(e);
                }

                ItemStack item = e.getCurrentItem();
                int slot = e.getSlot();

                if(item == null || slot == -1) return;

                ItemButton button = inv.getButtonAt(slot);

                if(button == null) return;

                e.setCancelled(!button.isMovable());

                if((button.isOnlyLeftClick() && e.isLeftClick()) || (button.isOnlyRightClick() && e.isRightClick()) || (!button.isOnlyRightClick() && !button.isOnlyLeftClick()) || (button.getOption().isNumberKey() && e.getClick().equals(ClickType.NUMBER_KEY))) {
                    if(button.isCloseOnClick()) {
                        if(inv instanceof GUI) ((GUI) inv).setClosingByButton(true);
                        e.getWhoClicked().closeInventory();
                    }
                    button.playSound((Player) e.getWhoClicked());
                    Bukkit.getScheduler().runTaskLater(plugin, () -> button.onClick(e), 1L);
                }
            } else {
                if(inv instanceof GUI) {
                    GUI gui = (GUI) inv;

                    if(gui.isMoveOwnItems()) {
                        if(e.getClickedInventory().equals(e.getView().getBottomInventory())) {
                            switch(e.getAction()) {
                                case MOVE_TO_OTHER_INVENTORY:
                                    e.setCancelled(!inv.isEditableItems());
                                    break;
                                default:
                                    e.setCancelled(false);
                            }
                        }
                    }
                }
            }
        }
    }

    @EventHandler
    public void onInvCloseEvent(InventoryCloseEvent e) {
        if(e.getInventory() == null || (!GUI.usesGUI((Player) e.getPlayer()) && !GUI.usesOldGUI((Player) e.getPlayer())))
            return;
        Player p = (Player) e.getPlayer();

        Interface inv = GUI.usesGUI(p) ? GUI.getGUI(p) : GUI.getOldGUI(p);

        if(e.getInventory().getName().equals(inv.getInventory().getName()) && inv.isUsing((Player) e.getPlayer())) {
            if(inv instanceof GUI && !((GUI) inv).isClosingByButton()) {
                SoundData sound = ((GUI) inv).getCancelSound();
                if(sound != null) sound.play(p);
            }

            inv.close((Player) e.getPlayer());
            for(InterfaceListener l : inv.getListener()) {
                l.onInvCloseEvent(e);
            }
        }
    }

    @EventHandler
    public void onInvOpenEvent(InventoryOpenEvent e) {
        if(e.getInventory() == null || (!GUI.usesGUI((Player) e.getPlayer()) && !GUI.usesOldGUI((Player) e.getPlayer())))
            return;
        Player p = (Player) e.getPlayer();

        Interface inv = GUI.usesGUI(p) ? GUI.getGUI(p) : GUI.getOldGUI(p);

        if(e.getInventory().getName().equals(inv.getInventory().getName()) && inv.isUsing((Player) e.getPlayer())) {
            for(InterfaceListener l : inv.getListener()) {
                l.onInvOpenEvent(e);
            }
        }
    }

    @EventHandler
    public void onInvDragEvent(InventoryDragEvent e) {
        if(e.getInventory() == null || (!GUI.usesGUI((Player) e.getWhoClicked()) && !GUI.usesOldGUI((Player) e.getWhoClicked())))
            return;
        Player p = (Player) e.getWhoClicked();

        Interface inv = GUI.usesGUI(p) ? GUI.getGUI(p) : GUI.getOldGUI(p);

        if(e.getInventory().getName().equals(inv.getInventory().getName())) {
            e.setCancelled(!inv.isEditableItems());

            for(InterfaceListener l : inv.getListener()) {
                l.onInvDragEvent(e);
            }

            if(inv instanceof GUI) {
                GUI gui = (GUI) inv;

                if(gui.isMoveOwnItems()) {
                    boolean topInv = false;

                    for(Integer slot : e.getRawSlots()) {
                        if(slot < gui.getSize()) topInv = true;
                    }

                    if(!topInv) e.setCancelled(false);
                }
            }
        }
    }
	
	/*
	 * ItemGUI
	 */

    @EventHandler
    public void onSneak(PlayerToggleSneakEvent e) {
        Player p = e.getPlayer();

        if(!ItemGUI.usesGUI(p)) return;
        ItemGUI gui = ItemGUI.getGUI(p);

        if(gui.isVisibleOnSneak()) gui.setVisible(e.isSneaking());
    }

    @EventHandler
    public void onMove(PlayerMoveEvent e) {
        Player p = e.getPlayer();

        if(!ItemGUI.usesGUI(p)) return;
        ItemGUI gui = ItemGUI.getGUI(p);
        if(gui.isCloseOnWalk()) return;

        Location from = e.getFrom().clone(), to = e.getTo().clone();
        double diffX = to.getX() - from.getX(), diffY = to.getY() - from.getY(), diffZ = to.getZ() - from.getZ();

        if(Math.abs(diffX) + Math.abs(diffY) + Math.abs(diffZ) == 0) return;

        gui.move(from, to);
    }

    @EventHandler
    public void onWalk(PlayerWalkEvent e) {
        Player p = e.getPlayer();

        if(!ItemGUI.usesGUI(p)) return;
        ItemGUI gui = ItemGUI.getGUI(p);
        if(gui.isVisible() && gui.isCloseOnWalk()) gui.setVisible(false);
    }
}
