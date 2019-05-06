package de.codingair.codingapi.player.gui;

import de.codingair.codingapi.API;
import de.codingair.codingapi.utils.Removable;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;
import java.util.UUID;

public abstract class PlayerItem extends ItemStack implements Removable {
	private UUID uniqueId = UUID.randomUUID();
	private JavaPlugin plugin;
	private Player player;
	private boolean freezed = true;
	
	public PlayerItem(JavaPlugin plugin, Player player, ItemStack item) {
		this.setToItemStack(item);
		
		this.plugin = plugin;
		this.player = player;
		
		API.addRemovable(this);
		if(!GUIListener.isRegistered()) GUIListener.register(plugin);
		//remove();
	}
	
	@Override
	public UUID getUniqueId() {
		return uniqueId;
	}
	
	@Override
	public Class<? extends Removable> getAbstractClass() {
		return PlayerItem.class;
	}
	
	@Override
	public void destroy() {
		remove();
	}
	
	public JavaPlugin getPlugin() {
		return plugin;
	}
	
	@Override
	public Player getPlayer() {
		return player;
	}
	
	private void setToItemStack(ItemStack item) {
		this.setAmount(item.getAmount());
		this.setType(item.getType());
		this.setData(item.getData());
		this.setDurability(item.getDurability());
		this.setItemMeta(item.getItemMeta());
	}
	
	public void remove() {
		int slot = 0;
		for(ItemStack stack : player.getInventory().getContents()) {
			if(!this.equals(stack)) slot++;
			else break;
		}
		
		player.getInventory().setItem(slot, null);
		player.updateInventory();
		
		API.removeRemovable(this);
	}
	
	public abstract void onInteract(PlayerInteractEvent e);
	
	public boolean isFreezed() {
		return freezed;
	}
	
	public PlayerItem setFreezed(boolean freezed) {
		this.freezed = freezed;
		return this;
	}
	
	public static boolean isUsing(Player p) {
		return API.getRemovable(p, PlayerItem.class) != null;
	}
	
	public static List<PlayerItem> getPlayerItems(Player p) {
		return API.getRemovables(p, PlayerItem.class);
	}
}
