package de.codingair.codingapi.player.gui.inventory.gui;

import de.codingair.codingapi.API;
import de.codingair.codingapi.server.Sound;
import de.codingair.codingapi.server.SoundData;
import de.codingair.codingapi.utils.Removable;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.Inventory;
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
	private SoundData openSound = null;
	private SoundData cancelSound = null;
	private boolean closingByButton = false;
	
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

	public void reinitialize(String title) {
		clear();
		initialize(this.player);
		setTitle(title);
	}
	
	public void onClose(Player p) { }
	public void onOpen(Player p) { }
	
	@Override
	public void open(Player p) {
		if(GUI.usesGUI(p)) GUI.getGUI(p).close();
		if(GUI.usesOldGUI(p)) GUI.getOldGUI(p).close(p);

		if(this.openSound != null) this.openSound.play(p);
		super.open(p);
		API.addRemovable(this);
	}
	
	public void open() {
		this.open(this.player);
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

	public SoundData getCancelSound() {
		return cancelSound;
	}

	public GUI setCancelSound(Sound sound) {
		this.cancelSound = new SoundData(sound, 1, 1);
		return this;
	}

	public GUI setCancelSound(SoundData cancelSound) {
		this.cancelSound = cancelSound;
		return this;
	}

	public boolean isClosingByButton() {
		return closingByButton;
	}

	public void setClosingByButton(boolean closingByButton) {
		this.closingByButton = closingByButton;
	}

	public void changeGUI(GUI newGui) {
		setSize(newGui.getSize());
        newGui.setInventory(getInventory());
		newGui.reinitialize();
	}

	protected void setInventory(Inventory inv) {
		this.inventory = inv;
	}

	public static GUI getGUI(Player p) {
		return API.getRemovable(p, GUI.class);
	}

	public static Interface getOldGUI(Player p) {
		for (Interface i : interfaces) {
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

	public SoundData getOpenSound() {
		return openSound;
	}

	public GUI setOpenSound(SoundData openSound) {
		this.openSound = openSound;
		return this;
	}
}
