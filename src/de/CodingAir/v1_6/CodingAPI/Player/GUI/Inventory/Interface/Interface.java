package de.CodingAir.v1_6.CodingAPI.Player.GUI.Inventory.Interface;

import de.CodingAir.v1_6.CodingAPI.Player.GUI.GUIListener;
import de.CodingAir.v1_6.CodingAPI.Player.GUI.Inventory.Interface.ItemButton.ItemButton;
import de.CodingAir.v1_6.CodingAPI.Server.Reflections.IReflection;
import de.CodingAir.v1_6.CodingAPI.Server.Reflections.PacketUtils;
import de.CodingAir.v1_6.CodingAPI.Tools.OldItemBuilder;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;

/*
			| 00 | 01 | 02 | 03 | 04 | 05 | 06 | 07 | 08 |
			| 09 | 10 | 11 | 12 | 13 | 14 | 15 | 16 | 17 |
			| 18 | 19 | 20 | 21 | 22 | 23 | 24 | 25 | 26 |
			| 27 | 28 | 29 | 30 | 31 | 32 | 33 | 34 | 35 |
			| 36 | 37 | 38 | 39 | 40 | 41 | 42 | 43 | 44 |
			| 45 | 46 | 47 | 48 | 49 | 50 | 51 | 52 | 53 |
 */

/**
 * @author Erik Zimmermann (CodingAir) [05.10.2016]
 * @version 1.9
 */

@Deprecated
public class Interface {
	public static final List<Interface> interfaces = new ArrayList<>();

	private Inventory inventory;
	private List<InterfaceListener> listener = new ArrayList<>();
	private HashMap<Integer, ItemButton> buttons = new HashMap<>();
	private HashMap<Integer, InterfaceBackup> backups = new HashMap<>();
	private List<Player> currentPlayers = new ArrayList<>();
	private String wrappersName = null;
	private boolean editableItems = true;
	private String title;
	boolean oldUsage = true;

	/**
	 * @param owner  (InventoryHolder) (can be null)
	 * @param title  (String)          (title)
	 * @param size   (Integer)          (size)
	 * @param plugin (Plugin)         For separately listeners and buttons)
	 * @author Erik Zimmermann (CodingAir) [05.10.2016]
	 * @version 1.10.2
	 */
	@Deprecated
	public Interface(InventoryHolder owner, String title, int size, Plugin plugin) {
		this.inventory = Bukkit.createInventory(owner, size, title);
		if(plugin != null && !GUIListener.isRegistered()) GUIListener.register(plugin);

		this.title = title;
	}
	
	public void addListener(InterfaceListener listener) {
		this.listener.add(listener);
	}
	
	public List<InterfaceListener> getListener() {
		return listener;
	}
	
	public void clearListeners() {
		this.listener = new ArrayList<InterfaceListener>();
	}
	
	@Deprecated
	public void addButton(int slot, ItemButton button) {
		this.buttons.remove(slot);
		this.buttons.put(slot, button);
		button.setSlot(slot);
		button.setInterface(this);
		
		setItem(slot, button.getItem());
	}
	
	public void addButton(ItemButton button) {
		this.buttons.remove(button.getSlot());
		this.buttons.put(button.getSlot(), button);
		button.setInterface(this);
		
		setItem(button.getSlot(), button.getItem());
	}
	
	public ItemButton removeButton(int slot) {
		return this.buttons.remove(slot);
	}
	
	public ItemButton removeButton(ItemButton button) {
		return this.buttons.remove(button);
	}
	
	public void clearButtons() {
		this.buttons = new HashMap<Integer, ItemButton>();
	}
	
	public InterfaceBackup getBackup(int id) {
		return this.backups.get(id);
	}
	
	public InterfaceBackup removeBackup(int id) {
		return this.backups.remove(id);
	}
	
	public void backupContent(int id) {
		this.backups.remove(id);
		ItemStack[] backup = new ItemStack[getSize()];
		
		int i = 0;
		for(ItemStack item : getContents()) {
			if(item == null) backup[i] = OldItemBuilder.getItem(Material.AIR);
			else backup[i] = item.clone();
			i++;
		}
		
		this.backups.put(id, new InterfaceBackup(backup, getButtons(), this.wrappersName));
	}
	
	public boolean restoreBackup(int id) {
		InterfaceBackup backup = this.backups.get(id);
		
		if(backup == null) return false;
		
		setSize(backup.getContents().length);
		setContents(backup.getContents());
		this.buttons = backup.getButtons();
		this.wrappersName = backup.getWrappersName();
		
		return true;
	}
	
	public void clearBackups() {
		this.backups = new HashMap<Integer, InterfaceBackup>();
	}
	
	public ItemButton getButtonAt(int slot) {
		return this.buttons.get(slot);
	}
	
	public ItemButton getButton(ItemStack item) {
		for(ItemButton button : this.buttons.values()) {
			if(button.getItem().equals(item)) return button;
		}
		
		return null;
	}
	
	public void updateButton(int slot, ItemButton button) {
		for(int slots : this.buttons.keySet()) {
			if(this.buttons.get(slots).equals(button)) {
				this.buttons.remove(slots);
				this.buttons.put(slot, button);
				break;
			}
		}
	}
	
	public HashMap<Integer, ItemButton> getButtons() {
		return buttons;
	}
	
	/**
	 * In this function you can fill all slots without items with the ItemStack background.
	 *
	 * @param background (ItemStack)
	 */
	public void setBackground(ItemStack background) {
		for(int slot = 0; slot < getSize(); slot++) {
			if(getItem(slot) == null) setItem(slot, background.clone());
			else if(getItem(slot).getType().equals(Material.AIR)) setItem(slot, background.clone());
		}
	}
	
	public Interface setDisplayname(int slot, String name) {
		ItemStack item = getItem(slot).clone();
		ItemMeta meta = item.getItemMeta();
		meta.setDisplayName(name);
		item.setItemMeta(meta);
		
		clear(slot);
		setItem(slot, item);
		return this;
	}
	
	public Interface setLore(int slot, String... lore) {
		ItemStack item = getItem(slot).clone();
		ItemMeta meta = item.getItemMeta();
		List<String> finalLore = new ArrayList<String>();
		
		for(String line : lore) {
			if(!line.startsWith("§")) line = line + "§f";
			finalLore.add(line);
		}
		
		meta.setLore(finalLore);
		item.setItemMeta(meta);
		
		clear(slot);
		setItem(slot, item);
		return this;
	}
	
	public Interface setAmount(int slot, int amount) {
		ItemStack item = getItem(slot).clone();
		item.setAmount(amount);
		
		clear(slot);
		setItem(slot, item);
		return this;
	}
	
	public boolean setSize(int size) {
		double s = (double) size;
		double res = s / 9;
		res *= 100;
		String resS = res + "";
		
		if(!resS.endsWith("00") && !resS.endsWith("0") || size > 54) return false;
		
		ItemStack[] contents = getContents();
		ItemStack[] newContents = new ItemStack[size];
		
		int i = 0;
		for(ItemStack item : contents) {
			if(i >= size) break;
			
			newContents[i] = item;
			i++;
		}
		
		this.inventory = Bukkit.createInventory(getHolder(), size, getTitle());
		setContents(newContents);
		
		return true;
	}
	
	public void setTitle(String title) {
		this.title = title;
		
		this.currentPlayers.forEach(p -> {
			Class<?> packetPlayOutOpenWindowClass = IReflection.getClass(IReflection.ServerPacket.MINECRAFT_PACKAGE, "PacketPlayOutOpenWindow");
			Class<?> containerClass = IReflection.getClass(IReflection.ServerPacket.MINECRAFT_PACKAGE, "Container");
			
			IReflection.MethodAccessor updateInventory = IReflection.getMethod(PacketUtils.EntityPlayerClass, "updateInventory", new Class[]{containerClass});
			
			IReflection.FieldAccessor activeContainer = IReflection.getField(PacketUtils.EntityHumanClass, "activeContainer");
			IReflection.FieldAccessor windowId = IReflection.getField(containerClass, "windowId");
			
			IReflection.ConstructorAccessor con = IReflection.getConstructor(packetPlayOutOpenWindowClass, int.class, String.class, PacketUtils.IChatBaseComponentClass, int.class);
			
			Object ep = PacketUtils.getEntityPlayer(p);
			Object packet = con.newInstance(windowId.get(activeContainer.get(ep)), "minecraft:chest", PacketUtils.getChatMessage(title), p.getOpenInventory().getTopInventory().getSize());
			PacketUtils.sendPacket(p, packet);
			updateInventory.invoke(ep, activeContainer.get(ep));
		});
	}
	
	public int getItemAmount() {
		int i = 0;
		for(ItemStack item : getContents()) {
			if(item != null) {
				if(!item.getType().equals(Material.AIR)) i++;
			}
		}
		
		return i;
	}
	
	public boolean isFull() {
		return (getItemAmount() >= getSize());
	}
	
	public String getWrappersName() {
		return wrappersName;
	}
	
	public void setWrappersName(String wrappersName) {
		this.wrappersName = wrappersName;
	}
	
	public void clearContent() {
		this.setBackground(OldItemBuilder.getItem(Material.AIR));
	}
	
	public boolean isEditableItems() {
		return editableItems;
	}
	
	public void setEditableItems(boolean editableItems) {
		this.editableItems = editableItems;
	}
	
	public void open(Player p) {
		this.currentPlayers.add(p);
		setTitle(this.title);
		if(oldUsage) interfaces.add(this);

		p.openInventory(this.inventory);
	}
	
	public void close(Player p) {
		Player o = null;
		
		for(Player current : this.currentPlayers) {
			if(current.getName().equals(p.getName())) o = current;
		}
		
		if(o == null) return;
		List<Player> current = new ArrayList<>();
		current.addAll(this.currentPlayers);
		current.remove(o);
		
		this.currentPlayers = current;
		if(oldUsage) interfaces.remove(this);

		p.closeInventory();
	}
	
	public boolean isUsing(Player p) {
		for(Player current : this.currentPlayers) {
			if(current.getName().equals(p.getName())) return true;
		}
		
		return false;
	}
	
	/**
	 * Alignment: Center only without ItemButtons!
	 *
	 * @param alignment : ItemAlignment
	 */
	public void setAlignment(ItemAlignment alignment) {
		if(alignment.equals(ItemAlignment.LEFT)) {
			for(int i = 0; i < 6; i++) {
				int slot = 9 * i;
				
				for(ItemStack stack : getItemsFromRow(i)) {
					if(stack != null && !stack.getType().equals(Material.AIR)) {
						this.setItem(slot, stack);
						slot++;
					}
				}
			}
		} else if(alignment.equals(ItemAlignment.RIGHT)) {
			for(int i = 0; i < 6; i++) {
				List<ItemStack> items = getItemsFromRow(i);
				List<ItemStack> clone = new ArrayList<>();
				clone.addAll(items);
				
				clone.forEach(item -> {
					if(item == null || item.getType().equals(Material.AIR)) {
						items.remove(item);
					}
				});
				
				if(items.size() == 0) return;
				
				for(int slot = 8 + 8 * i; slot >= 9 * i; slot--) {
					ItemStack item = items.get(items.size() - 1);
					items.remove(item);
					this.setItem(slot, item);
				}
			}
		} else if(alignment.equals(ItemAlignment.CENTER)) {
			for(int i = 0; i < this.getSize() / 9; i++) {
				List<ItemButton> buttons = getButtonsFromRow(i);
				List<ItemStack> items = getItemsFromRow(i);
				
				int amount = 0;
				
				for(ItemStack item : items) {
					if(item != null && !item.getType().equals(Material.AIR)) amount++;
				}
				
				
				if(amount != 0) {
					int[] slots;
					switch(amount) {
						case 9:
							slots = new int[]{0, 1, 2, 3, 4, 5, 6, 7, 8};
							break;
						case 8:
							slots = new int[]{0, 1, 2, 3, 5, 6, 7, 8};
							break;
						case 7:
							slots = new int[]{1, 2, 3, 4, 5, 6, 7};
							break;
						case 6:
							slots = new int[]{1, 2, 3, 5, 6, 7};
							break;
						case 5:
							slots = new int[]{1, 2, 4, 6, 7};
							break;
						case 4:
							slots = new int[]{1, 3, 5, 7};
							break;
						case 3:
							slots = new int[]{2, 4, 6};
							break;
						case 2:
							slots = new int[]{2, 6};
							break;
						case 1:
							slots = new int[]{4};
							break;
						default:
							slots = new int[]{};
							break;
					}
					
					int slot = 0;
					
					clearRow(i);
					
					for(int id = 0; id < 9; id++) {
						ItemStack item = items.get(id);
						
						if(item != null) {
							ItemButton b = buttons.get(id);
							
							setItem(i * 9 + slots[slot], item);
							
							if(b != null) {
								b.setSlot(i * 9 + slots[slot]);
								b.updateInInterface();
							}
							slot++;
						}
					}
				}
			}
		}
	}
	
	public List<ItemStack> getItemsFromRow(int row) {
		List<ItemStack> items = new ArrayList<>();
		
		for(int i = 0; i < 6; i++) {
			if(row == i && this.getSize() >= 9 + 9 * i) {
				for(int slot = 9 * i; slot < 9 + 9 * i; slot++) {
					items.add(this.getItem(slot));
				}
			}
		}
		
		return items;
	}
	
	public List<ItemButton> getButtonsFromRow(int row) {
		List<ItemButton> items = new ArrayList<>();
		
		for(int i = 0; i < 6; i++) {
			if(row == i && this.getSize() >= 9 + 9 * i) {
				for(int slot = 9 * i; slot < 9 + 9 * i; slot++) {
					items.add(this.getButtonAt(slot));
				}
			}
		}
		
		return items;
	}
	
	public void clearRow(int row) {
		for(int i = 0; i < 6; i++) {
			if(row == i && this.getSize() >= 9 + 9 * i) {
				for(int slot = 9 * i; slot < 9 + 9 * i; slot++) {
					setItem(slot, OldItemBuilder.getItem(Material.AIR));
				}
			}
		}
	}
	
	public Inventory getInventory() {
		return inventory;
	}
	
	//STANDARD
	
	//1.8
	public HashMap<Integer, ItemStack> addItem(ItemStack... arg0) throws IllegalArgumentException {
		return this.inventory.addItem(arg0);
	}
	
	@SuppressWarnings("deprecation")
	public HashMap<Integer, ? extends ItemStack> all(int arg0) {
		return this.inventory.all(arg0);
	}
	
	public HashMap<Integer, ? extends ItemStack> all(Material arg0)
			throws IllegalArgumentException {
		return this.inventory.all(arg0);
	}
	
	public HashMap<Integer, ? extends ItemStack> all(ItemStack arg0) {
		return this.inventory.all(arg0);
	}
	
	public void clear() {
		this.inventory.clear();
		this.buttons.clear();
	}
	
	public void clear(int arg0) {
		this.inventory.clear(arg0);
	}
	
	@SuppressWarnings("deprecation")
	public boolean contains(int arg0) {
		return this.inventory.contains(arg0);
	}
	
	public boolean contains(Material arg0) throws IllegalArgumentException {
		return this.inventory.contains(arg0);
	}
	
	public boolean contains(ItemStack arg0) {
		return this.inventory.contains(arg0);
	}
	
	@SuppressWarnings("deprecation")
	public boolean contains(int arg0, int arg1) {
		return this.inventory.contains(arg0, arg1);
	}
	
	public boolean contains(Material arg0, int arg1) {
		return this.inventory.contains(arg0, arg1);
	}
	
	public boolean contains(ItemStack arg0, int arg1) {
		return this.inventory.contains(arg0, arg1);
	}
	
	public boolean containsAtLeast(ItemStack arg0, int arg1) {
		return this.inventory.containsAtLeast(arg0, arg1);
	}
	
	@SuppressWarnings("deprecation")
	public int first(int arg0) {
		return this.inventory.first(arg0);
	}
	
	public int first(Material arg0) throws IllegalArgumentException {
		return this.inventory.first(arg0);
	}
	
	public int first(ItemStack arg0) {
		return this.inventory.first(arg0);
	}
	
	public int firstEmpty() {
		return this.inventory.firstEmpty();
	}
	
	public ItemStack[] getContents() {
		return this.inventory.getContents();
	}
	
	public InventoryHolder getHolder() {
		return this.inventory.getHolder();
	}
	
	public ItemStack getItem(int arg0) {
		return this.inventory.getItem(arg0);
	}
	
	public int getMaxStackSize() {
		return this.inventory.getMaxStackSize();
	}
	
	public String getName() {
		return this.inventory.getName();
	}
	
	public InventoryType getType() {
		return this.inventory.getType();
	}
	
	public List<HumanEntity> getViewers() {
		return this.inventory.getViewers();
	}
	
	public ListIterator<ItemStack> iterator() {
		return this.inventory.iterator();
	}
	
	public ListIterator<ItemStack> iterator(int arg0) {
		return this.inventory.iterator(arg0);
	}
	
	@SuppressWarnings("deprecation")
	public void remove(int arg0) {
		this.inventory.remove(arg0);
	}
	
	public void remove(Material arg0) throws IllegalArgumentException {
		this.inventory.remove(arg0);
	}
	
	public void remove(ItemStack arg0) {
		this.inventory.remove(arg0);
	}
	
	public HashMap<Integer, ItemStack> removeItem(ItemStack... arg0) throws IllegalArgumentException {
		return this.inventory.removeItem(arg0);
	}
	
	public void setContents(ItemStack[] arg0) throws IllegalArgumentException {
		this.inventory.setContents(arg0);
		
	}

	public void setItem(int arg0, ItemStack arg1) {
		this.inventory.setItem(arg0, arg1);
	}

	public void setItem(int x, int y, ItemStack arg1) {
		int slot = x + 9 * y;

		this.inventory.setItem(slot, arg1);
	}
	
	public void setMaxStackSize(int arg0) {
		this.inventory.setMaxStackSize(arg0);
		
	}
	
	public int getSize() {
		return this.inventory.getSize();
	}
	
	public String getTitle() {
		return this.inventory.getTitle();
	}
}
