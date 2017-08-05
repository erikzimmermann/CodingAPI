package de.CodingAir.v1_6.CodingAPI.Player.GUI.Anvil;

import de.CodingAir.v1_6.CodingAPI.API;
import de.CodingAir.v1_6.CodingAPI.Server.Reflections.IReflection;
import de.CodingAir.v1_6.CodingAPI.Server.Reflections.PacketUtils;
import de.CodingAir.v1_6.CodingAPI.Server.Version;
import de.CodingAir.v1_6.CodingAPI.Utils.Removable;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import java.util.HashMap;
import java.util.UUID;
import java.util.logging.Level;

/**
 * Removing of this disclaimer is forbidden.
 *
 * @author CodingAir
 * @verions: 1.0.0
 **/

public class AnvilGUI implements Removable {
	private UUID uniqueId = UUID.randomUUID();
	private Plugin plugin;
	private Player player;
	private AnvilListener listener;
	private HashMap<AnvilSlot, ItemStack> items = new HashMap<>();
	
	private Listener bukkitListener;
	private Inventory inv;
	
	public AnvilGUI(Plugin plugin, Player player, AnvilListener listener) {
		this.plugin = plugin;
		this.player = player;
		this.listener = listener;
		
		registerBukkitListener();
	}
	
	@Override
	public Class<? extends Removable> getAbstractClass() {
		return AnvilGUI.class;
	}
	
	@Override
	public Player getPlayer() {
		return this.player;
	}
	
	@Override
	public UUID getUniqueId() {
		return uniqueId;
	}
	
	@Override
	public void destroy() {
		this.player.closeInventory();
		remove();
	}
	
	private void registerBukkitListener() {
		this.bukkitListener = new Listener() {
			@EventHandler
			public void onInventoryClick(InventoryClickEvent e) {
				if(e.getWhoClicked() instanceof Player) {
					Player p = (Player) e.getWhoClicked();
					
					if(e.getInventory().equals(inv)) {
						e.setCancelled(true);
						
						ItemStack item = e.getCurrentItem();
						int slot = e.getRawSlot();
						
						AnvilClickEvent clickEvent = new AnvilClickEvent(p, AnvilSlot.bySlot(slot), item, AnvilGUI.this);
						
						if(listener != null) listener.onClick(clickEvent);
						Bukkit.getPluginManager().callEvent(clickEvent);
						
						e.setCancelled(clickEvent.isCancelled());
						
						if(clickEvent.getWillClose()) {
							
							AnvilCloseEvent anvilCloseEvent = new AnvilCloseEvent(player, AnvilGUI.this);
							
							Bukkit.getPluginManager().callEvent(anvilCloseEvent);
							if(listener != null) listener.onClose(anvilCloseEvent);
							
							if(!anvilCloseEvent.isCancelled()) {
								p.closeInventory();
								inv.clear();
								remove();
							}
							
							if(anvilCloseEvent.getPost() != null) anvilCloseEvent.getPost().run();
						}
						
						if(clickEvent.getWillDestroy()) {
							remove();
						}
					}
				}
			}
			
			@EventHandler
			public void onInventoryClose(InventoryCloseEvent e) {
				if(e.getPlayer() instanceof Player) {
					
					if(e.getInventory().equals(inv)) {
						
						AnvilCloseEvent anvilCloseEvent = new AnvilCloseEvent(player, AnvilGUI.this);
						
						Bukkit.getPluginManager().callEvent(anvilCloseEvent);
						if(listener != null) listener.onClose(anvilCloseEvent);
						
						inv.clear();
						remove();
						
						if(anvilCloseEvent.getPost() != null) {
							Bukkit.getScheduler().runTaskLaterAsynchronously(plugin, anvilCloseEvent.getPost(), 1L);
						}
					}
				}
			}
			
			@EventHandler
			public void onPlayerQuit(PlayerQuitEvent e) {
				if(!player.getName().equals(e.getPlayer().getName())) return;
				remove();
			}
		};
		
		Bukkit.getPluginManager().registerEvents(this.bukkitListener, this.plugin);
	}
	
	public AnvilGUI open() {
		this.player.closeInventory();
		
		Class<?> containerAnvilClass = IReflection.getClass(IReflection.ServerPacket.MINECRAFT_PACKAGE, "ContainerAnvil");
		Class<?> playerInventoryClass = IReflection.getClass(IReflection.ServerPacket.MINECRAFT_PACKAGE, "PlayerInventory");
		Class<?> worldClass = IReflection.getClass(IReflection.ServerPacket.MINECRAFT_PACKAGE, "World");
		Class<?> blockPositionClass = IReflection.getClass(IReflection.ServerPacket.MINECRAFT_PACKAGE, "BlockPosition");
		Class<?> entityPlayerClass = IReflection.getClass(IReflection.ServerPacket.MINECRAFT_PACKAGE, "EntityPlayer");
		Class<?> craftInventoryViewClass = IReflection.getClass(IReflection.ServerPacket.CRAFTBUKKIT_PACKAGE, "inventory.CraftInventoryView");
		Class<?> packetPlayOutOpenWindowClass = IReflection.getClass(IReflection.ServerPacket.MINECRAFT_PACKAGE, "PacketPlayOutOpenWindow");
		Class<?> containerClass = IReflection.getClass(IReflection.ServerPacket.MINECRAFT_PACKAGE, "Container");
		Class<?> chatMessageClass = IReflection.getClass(IReflection.ServerPacket.MINECRAFT_PACKAGE, "ChatMessage");
		
		IReflection.ConstructorAccessor anvilContainerCon = IReflection.getConstructor(containerAnvilClass, playerInventoryClass, worldClass, blockPositionClass, entityPlayerClass);
		IReflection.ConstructorAccessor blockPositionCon = IReflection.getConstructor(blockPositionClass, Integer.class, Integer.class, Integer.class);
		IReflection.ConstructorAccessor chatMessageCon = IReflection.getConstructor(chatMessageClass, String.class, Object[].class);
		IReflection.ConstructorAccessor packetPlayOutOpenWindowCon = IReflection.getConstructor(packetPlayOutOpenWindowClass, Integer.class, String.class, chatMessageClass, int.class);
		
		IReflection.MethodAccessor getBukkitView = IReflection.getMethod(containerAnvilClass, "getBukkitView", craftInventoryViewClass, null);
		IReflection.MethodAccessor getTopInventory = IReflection.getMethod(craftInventoryViewClass, "getTopInventory", Inventory.class, null);
		IReflection.MethodAccessor nextContainerCounter = IReflection.getMethod(entityPlayerClass, "nextContainerCounter", int.class, null);
		IReflection.MethodAccessor addSlotListener = IReflection.getMethod(containerClass, "addSlotListener", new Class[]{entityPlayerClass});
		
		IReflection.FieldAccessor getInventory = IReflection.getField(entityPlayerClass, "inventory");
		IReflection.FieldAccessor getWorld = IReflection.getField(entityPlayerClass, "world");
		IReflection.FieldAccessor reachable = IReflection.getField(containerAnvilClass, "checkReachable");
		IReflection.FieldAccessor activeContainer = IReflection.getField(entityPlayerClass, "activeContainer");
		IReflection.FieldAccessor windowId = IReflection.getField(containerClass, "windowId");
		
		
		Object entityPlayer = PacketUtils.getEntityPlayer(this.player);
		Object inventory = getInventory.get(entityPlayer);
		Object world = getWorld.get(entityPlayer);
		Object blockPosition = blockPositionCon.newInstance(0, 0, 0);
		
		Object container = anvilContainerCon.newInstance(inventory, world, blockPosition, entityPlayer);
		reachable.set(container, false);
		
		inv = (Inventory) getTopInventory.invoke(getBukkitView.invoke(container));
		
		for(AnvilSlot slot : items.keySet()) {
			inv.setItem(slot.getSlot(), items.get(slot));
		}
		
		int c = (int) nextContainerCounter.invoke(entityPlayer);
		
		
		try {
			PacketUtils.sendPacket(this.player, packetPlayOutOpenWindowCon.newInstance(c, "minecraft:anvil", chatMessageCon.newInstance("AnvilGUI", new Object[]{}), 0));
		} catch(Exception e) {
			plugin.getLogger().log(Level.SEVERE, "Error: Cannot open the AnvilGUI in " + Version.getVersion().name() + "!");
		}
		
		
		activeContainer.set(entityPlayer, container);
		windowId.set(activeContainer.get(entityPlayer), c);
		addSlotListener.invoke(activeContainer.get(entityPlayer), entityPlayer);
		
		updateInventory();
		
		API.addRemovable(this);
		return this;
	}
	
	public void clearInventory() {
		items = new HashMap<>();
		this.updateInventory();
	}
	
	public void updateInventory() {
		Class<?> entityPlayerClass = IReflection.getClass(IReflection.ServerPacket.MINECRAFT_PACKAGE, "EntityPlayer");
		Class<?> containerAnvilClass = IReflection.getClass(IReflection.ServerPacket.MINECRAFT_PACKAGE, "ContainerAnvil");
		Class<?> craftInventoryViewClass = IReflection.getClass(IReflection.ServerPacket.CRAFTBUKKIT_PACKAGE, "inventory.CraftInventoryView");
		
		IReflection.FieldAccessor activeContainer = IReflection.getField(entityPlayerClass, "activeContainer");
		
		IReflection.MethodAccessor getBukkitView = IReflection.getMethod(containerAnvilClass, "getBukkitView", craftInventoryViewClass, null);
		IReflection.MethodAccessor getTopInventory = IReflection.getMethod(craftInventoryViewClass, "getTopInventory", Inventory.class, null);
		
		Object entityPlayer = PacketUtils.getEntityPlayer(this.player);
		Object container = activeContainer.get(entityPlayer);
		
		if(!container.toString().toLowerCase().contains("anvil")) return;
		
		inv = (Inventory) getTopInventory.invoke(getBukkitView.invoke(container));
		
		inv.clear();
		
		for(AnvilSlot slot : items.keySet()) {
			inv.setItem(slot.getSlot(), items.get(slot));
		}
		
		this.player.updateInventory();
	}
	
	public AnvilGUI setSlot(AnvilSlot slot, ItemStack item) {
		items.remove(slot);
		items.put(slot, item);
		return this;
	}
	
	public void remove() {
		this.clearInventory();
		this.listener = null;
		this.items = null;
		
		HandlerList.unregisterAll(this.bukkitListener);
		listener = null;
		
		API.removeRemovable(this);
	}
	
	public static AnvilGUI openAnvil(Plugin plugin, Player p, AnvilListener listener, ItemStack item) {
		return new AnvilGUI(plugin, p, listener).setSlot(AnvilSlot.INPUT_LEFT, item).open();
	}
}
