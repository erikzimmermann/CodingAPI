package de.CodingAir.v1_6.CodingAPI.Player.GUI.Inventory.Interface;

import de.CodingAir.v1_6.CodingAPI.API;
import de.CodingAir.v1_6.CodingAPI.Utils.Removable;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.plugin.Plugin;

import java.util.UUID;

/**
 * Removing of this disclaimer is forbidden.
 *
 * @author CodingAir
 * @verions: 1.0.0
 **/

public abstract class GUI extends Interface implements Removable {
	private UUID uniqueId = UUID.randomUUID();
	private Plugin plugin;
	private Player player;
	
	public GUI(Player p, String title, int size, Plugin plugin) {
		this(p, title, size, plugin, true);
	}
	
	public GUI(Player p, String title, int size, Plugin plugin, boolean preInitialize) {
		super(p, title, size, plugin);
		oldUsage = false;

		super.setEditableItems(false);

		this.plugin = plugin;
		this.player = p;
		
		super.addListener(new InterfaceListener() {
			@Override
			public void onInvClickEvent(InventoryClickEvent e) {
			}
			
			@Override
			public void onInvOpenEvent(InventoryOpenEvent e) {
				if(e.getPlayer().equals(player)) {
					onOpen(player);
				}
			}
			
			@Override
			public void onInvCloseEvent(InventoryCloseEvent e) {
				if(e.getPlayer().equals(player)) {
					onClose(player);
				}
			}
			
			@Override
			public void onInvDragEvent(InventoryDragEvent e) {
			
			}
		});
		
		this.setEditableItems(false);
		
		if(preInitialize) initialize(p);
	}
	
	@Override
	public UUID getUniqueId() {
		return uniqueId;
	}
	
	@Override
	public Class<? extends Removable> getAbstractClass() {
		return GUI.class;
	}
	
	@Override
	public void destroy() {
		close();
	}
	
	public abstract void initialize(Player p);

	public void reinitialize() {
		clear();
		initialize(this.player);
		setTitle(getTitle());
	}
	
	public void onClose(Player p) { }
	public void onOpen(Player p) { }
	
	@Override
	public void open(Player p) {
		if(GUI.usesGUI(p)) GUI.getGUI(p).close();
		if(GUI.usesOldGUI(p)) GUI.getOldGUI(p).close(p);

		super.open(p);
		API.addRemovable(this);
	}
	
	public void open() {
		if(GUI.usesGUI(this.player)) GUI.getGUI(this.player).close();
		if(GUI.usesOldGUI(this.player)) GUI.getOldGUI(this.player).close(this.player);

		super.open(this.player);
		API.addRemovable(this);
	}
	
	@Override
	public void close(Player p) {
		super.close(p);
		API.removeRemovable(this);
	}
	
	public void close() {
		super.close(this.player);
		API.removeRemovable(this);
	}
	
	@Override
	public Player getPlayer() {
		return player;
	}
	
	@Override
	public boolean isUsing(Player player) {
		return this.player.getName().equals(player.getName());
	}
	
	public Plugin getPlugin() {
		return plugin;
	}

	public static GUI getGUI(Player p) {
		return API.getRemovable(p, GUI.class);
	}

	public static Interface getOldGUI(Player p) {
		for (Interface i : Interface.interfaces) {
			if(i.isUsing(p)) return i;
		}

		return null;
	}

	public static boolean usesGUI(Player p) {
		return getGUI(p) != null;
	}

	public static boolean usesOldGUI(Player p) {
		return getOldGUI(p) != null;
	}
}
