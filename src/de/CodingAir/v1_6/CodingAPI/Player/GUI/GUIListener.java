package de.CodingAir.v1_6.CodingAPI.Player.GUI;

import de.CodingAir.v1_6.CodingAPI.API;
import de.CodingAir.v1_6.CodingAPI.Player.GUI.HoveredItems.HoveredItem;
import de.CodingAir.v1_6.CodingAPI.Player.GUI.HoveredItems.ItemGUI;
import de.CodingAir.v1_6.CodingAPI.Player.GUI.Inventory.Interface.GUI;
import de.CodingAir.v1_6.CodingAPI.Player.GUI.Inventory.Interface.Interface;
import de.CodingAir.v1_6.CodingAPI.Player.GUI.Inventory.Interface.InterfaceListener;
import de.CodingAir.v1_6.CodingAPI.Player.GUI.Inventory.Interface.ItemButton.ItemButton;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import java.util.List;

/**
 * Removing of this disclaimer is forbidden.
 *
 * @author CodingAir
 * @verions: 1.0.0
 **/

public class GUIListener implements Listener {
	private static boolean registered = false;
	private Plugin plugin;

	public GUIListener(Plugin plugin) {
		this.plugin = plugin;
	}

	public static void register(Plugin plugin) {
		if(registered) return;
		Bukkit.getPluginManager().registerEvents(new GUIListener(plugin), plugin);
		registered = true;
	}
	
	public static boolean isRegistered() {
		return registered;
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
		if(e.getInventory() == null || (!GUI.usesGUI((Player) e.getWhoClicked()) && !GUI.usesOldGUI((Player) e.getWhoClicked()))) return;
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
				
				if((button.isOnlyLeftClick() && e.isLeftClick()) || (button.isOnlyRightClick() && e.isRightClick()) || (!button.isOnlyRightClick() && !button.isOnlyLeftClick())) {
					if(button.isCloseOnClick()) e.getWhoClicked().closeInventory();
					button.playSound((Player) e.getWhoClicked());
					Bukkit.getScheduler().runTaskLater(plugin, () -> button.onClick(e), 1L);
				}
			}
		}
	}
	
	@EventHandler
	public void onInvCloseEvent(InventoryCloseEvent e) {
		if(e.getInventory() == null || (!GUI.usesGUI((Player) e.getPlayer()) && !GUI.usesOldGUI((Player) e.getPlayer()))) return;
		Player p = (Player) e.getPlayer();

		Interface inv = GUI.usesGUI(p) ? GUI.getGUI(p) : GUI.getOldGUI(p);
		
		if(e.getInventory().getName().equals(inv.getInventory().getName()) && inv.isUsing((Player) e.getPlayer())) {
			inv.close((Player) e.getPlayer());
			for(InterfaceListener l : inv.getListener()) {
				l.onInvCloseEvent(e);
			}
		}
	}
	
	@EventHandler
	public void onInvOpenEvent(InventoryOpenEvent e) {
		if(e.getInventory() == null || (!GUI.usesGUI((Player) e.getPlayer()) && !GUI.usesOldGUI((Player) e.getPlayer()))) return;
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
		if(e.getInventory() == null || (!GUI.usesGUI((Player) e.getWhoClicked()) && !GUI.usesOldGUI((Player) e.getWhoClicked()))) return;
		Player p = (Player) e.getWhoClicked();

		Interface inv = GUI.usesGUI(p) ? GUI.getGUI(p) : GUI.getOldGUI(p);
		
		if(e.getInventory().getName().equals(inv.getInventory().getName()) && inv.isUsing((Player) e.getWhoClicked())) {
			e.setCancelled(!inv.isEditableItems());
			
			for(InterfaceListener l : inv.getListener()) {
				l.onInvDragEvent(e);
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
		
		Location from = e.getFrom().clone(), to = e.getTo().clone();
		double diffX = to.getX() - from.getX(), diffY = to.getY() - from.getY(), diffZ = to.getZ() - from.getZ();
		
		if(Math.abs(diffX) + Math.abs(diffY) + Math.abs(diffZ) == 0) return;
		
		gui.move(from, to);
	}
}
