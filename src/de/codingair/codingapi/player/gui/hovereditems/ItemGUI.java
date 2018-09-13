package de.codingair.codingapi.player.gui.hovereditems;

import de.codingair.codingapi.API;
import de.codingair.codingapi.server.Sound;
import de.codingair.codingapi.server.SoundData;
import de.codingair.codingapi.tools.Converter;
import de.codingair.codingapi.tools.OldItemBuilder;
import de.codingair.codingapi.utils.Removable;
import de.codingair.codingapi.utils.TextAlignment;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Removing of this disclaimer is forbidden.
 *
 * @author codingair
 * @verions: 1.0.0
 **/

public class ItemGUI implements Removable {
	private UUID uniqueId = UUID.randomUUID();
	
	private JavaPlugin plugin;
	
	private List<HoveredItem> hoveredItems = new ArrayList<>();
	private List<Item> data = new ArrayList<>();
	private Player player;
	private ItemGUIListener listener = null;
	
	private int maxItems = 14;
	private double radius = 3.0D;
	private double height = 0.1D;
	private double moveHeight = 0.6D;
	
	private boolean initialized = false;
	private boolean visible = false;
	
	private boolean visibleOnSneak = false;
	private boolean closeOnWalk = false;

	private SoundData interactSound = new SoundData(Sound.CLICK, 1F, 1F);
	private SoundData hoverSound = new SoundData(Sound.CLICK, 0.05F, 1.2F);
	private SoundData unhoverSound = new SoundData(Sound.CLICK, 0.05F, 0.8F);
	private SoundData closeSound = new SoundData(Sound.ITEM_BREAK, 1F, 1F);
	private SoundData openSound = new SoundData(Sound.LEVEL_UP, 1F, 1F);

	public ItemGUI(JavaPlugin plugin, Player player) {
		this.plugin = plugin;
		this.player = player;

		API.addRemovable(this);
	}
	
	public ItemGUI(JavaPlugin plugin, Player player, ItemGUIListener listener) {
		this(plugin, player);
		this.listener = listener;
	}

	@Override
	public void destroy() {
		remove(true);
	}

	@Override
	public Class<? extends Removable> getAbstractClass() {
		return ItemGUI.class;
	}

	@Override
	public UUID getUniqueId() {
		return uniqueId;
	}

	private void initialize() {
		if(initialized) remove(false);
		if(!usesGUI(this.player)) API.addRemovable(this);
		
		double current = (this.player.getLocation().getYaw() + 180.0) / 180.0 * Math.PI - 89.52;
		double distance = 2.0 / (double) maxItems * Math.PI;
		double d = current - (distance * ((double) data.size() - 1.0)) / 2.0;
		
		for(Item datum : data) {
			Location loc = this.player.getLocation().clone();
			
			double x = radius * Math.cos(d);
			double y = height;
			double z = radius * Math.sin(d);
			
			loc.add(x, y, z);
			
			hoveredItems.add(new HoveredItem(this.player, datum.getItem().clone(), loc, getPlugin(), datum.getName()) {
				private boolean top = false;
				
				@Override
				public void onInteract(Player p) {
					if(hasListener()) getListener().onClick(p, this);
                    if(ItemGUI.this.interactSound != null) ItemGUI.this.interactSound.play(p);
				}
				
				@Override
				public void onLookAt(Player p) {
					if(!isLookAt() && !top) {
						onMove(p, true);
						teleport(getTempLocation().add(0, moveHeight, 0), true);
						top = true;
						
						if(hasListener()) getListener().onLookAt(p, this);
					}
				}
				
				@Override
				public void onUnlookAt(Player p) {
					if(isLookAt() && top) {
						onMove(p, false);
						teleport(getTempLocation().subtract(0, moveHeight, 0), true);
						top = false;
						
						if(hasListener()) getListener().onUnlookAt(p, this);
					}
				}
				
				private void onMove(Player p, boolean up) {
                    if(up && ItemGUI.this.hoverSound != null) ItemGUI.this.hoverSound.play(p);
                    else if(ItemGUI.this.unhoverSound != null) ItemGUI.this.unhoverSound.play(p);
				}
			});
			
			d += distance;
			
			double temp = d * 180.0 / Math.PI;
			if(temp < 360.0) temp += 360.0;
			if(temp > 360.0) temp -= 360.0;
			
			d = temp / 180.0 * Math.PI;
		}
		
		initialized = true;
	}
	
	private void remove(boolean destroy) {
		boolean spawned = !hoveredItems.isEmpty();

		for(HoveredItem hoveredItem : this.hoveredItems) {
			if(!hoveredItem.isSpawned()) spawned = false;
		}

		this.hoveredItems.forEach(HoveredItem::remove);
		this.hoveredItems.clear();
		
		if(destroy) {
			this.data.clear();
            API.removeRemovable(this);
		}

		if(this.closeSound != null && spawned) this.closeSound.play(this.player);
		visible = false;
		
		initialized = false;
	}
	
	public void open() {
		if(!initialized) initialize();

		if(this.openSound != null) this.openSound.play(this.player);
		this.hoveredItems.forEach(HoveredItem::spawn);
		visible = true;
	}

	public void close() {
	    remove(true);
    }
	
	public void move(Location from, Location to) {
		if(!visible) return;
		
		double diffX = to.getX() - from.getX(), diffY = to.getY() - from.getY(), diffZ = to.getZ() - from.getZ();
		
		for(HoveredItem item : this.hoveredItems) {
			item.teleport(item.getTempLocation().add(diffX, diffY, diffZ), item.getTempLocation().getY() != item.getLocation().getY());
			item.setLocation(item.getLocation().add(diffX, diffY, diffZ));
		}
	}
	
	public void addData(Item data) {
		this.data.add(data);
	}
	
	public void removeData(Item data) {
		this.data.add(data);
	}
	
	public Item getData(String name) {
		for(Item datum : this.data) {
			if(datum.getName().equals(name)) return datum;
		}
		
		return null;
	}
	
	public void setText(HoveredItem item, String... text) {
		item.setText(text);
		Item data = getData(item.getName());
		data.setText(text);
	}
	
	public void setText(HoveredItem item, List<String> text) {
		setText(item, text.toArray(new String[text.size()]));
	}
	
	public void setText(Item data, String... text) {
		if(getItem(data.getName()) != null) getItem(data.getName()).setText(text);
		data.setText(text);
	}
	
	public void setText(Item data, List<String> text) {
		setText(data, text.toArray(new String[text.size()]));
	}
	
	public boolean hasListener() {
		return this.listener != null;
	}
	
	public List<HoveredItem> getHoveredItems() {
		return hoveredItems;
	}
	
	public HoveredItem getItem(int id) {
		for(HoveredItem item : hoveredItems) {
			if(item.getID() == id) return item;
		}
		
		return null;
	}
	
	public HoveredItem getItem(String name) {
		for(HoveredItem item : hoveredItems) {
			if(item.getName().equals(name)) return item;
		}
		
		return null;
	}
	
	public Player getPlayer() {
		return player;
	}
	
	public boolean isVisible() {
		return visible;
	}
	
	public void setVisible(boolean visible) {
		this.visible = visible;
		
		if(visible) open();
		else remove(false);
	}
	
	public boolean isInitialized() {
		return initialized;
	}
	
	public ItemGUIListener getListener() {
		return listener;
	}
	
	public void setListener(ItemGUIListener listener) {
		this.listener = listener;
	}
	
	public JavaPlugin getPlugin() {
		return plugin;
	}
	
	public List<Item> getData() {
		return data;
	}
	
	public int getMaxItems() {
		return maxItems;
	}
	
	public void setMaxItems(int maxItems) {
		this.maxItems = maxItems;
	}
	
	public double getRadius() {
		return radius;
	}
	
	public void setRadius(double radius) {
		this.radius = radius;
	}
	
	public double getHeight() {
		return height;
	}
	
	public void setHeight(double height) {
		this.height = height;
	}
	
	public double getMoveHeight() {
		return moveHeight;
	}
	
	public void setMoveHeight(double moveHeight) {
		this.moveHeight = moveHeight;
	}
	
	public boolean isVisibleOnSneak() {
		return visibleOnSneak;
	}
	
	public void setVisibleOnSneak(boolean visibleOnSneak) {
		this.visibleOnSneak = visibleOnSneak;
	}
	
	public static ItemGUI getGUI(Player p) {
		return API.getRemovable(p, ItemGUI.class);
	}
	
	public static boolean usesGUI(Player p) {
		return getGUI(p) != null;
	}

    public SoundData getInteractSound() {
        return interactSound;
    }

    public void setInteractSound(SoundData interactSound) {
        this.interactSound = interactSound;
    }

    public SoundData getHoverSound() {
        return hoverSound;
    }

    public void setHoverSound(SoundData hoverSound) {
        this.hoverSound = hoverSound;
    }

    public SoundData getUnhoverSound() {
        return unhoverSound;
    }

    public void setUnhoverSound(SoundData unhoverSound) {
        this.unhoverSound = unhoverSound;
    }

    public SoundData getCloseSound() {
        return closeSound;
    }

    public void setCloseSound(SoundData closeSound) {
        this.closeSound = closeSound;
    }

    public boolean isCloseOnWalk() {
        return closeOnWalk;
    }

    public void setCloseOnWalk(boolean closeOnWalk) {
        this.closeOnWalk = closeOnWalk;
    }

	public SoundData getOpenSound() {
		return openSound;
	}

	public void setOpenSound(SoundData openSound) {
		this.openSound = openSound;
	}

	public static class Item {
		private String name;
		private ItemStack item;
		private List<String> text;

		public Item(String name, ItemStack item, List<String> text) {
			OldItemBuilder.setText(item, TextAlignment.LEFT, text);

			this.name = name;
			this.item = item.clone();
			this.text = text;
		}

		public Item(String name, ItemStack item, String... text) {
			OldItemBuilder.setText(item, TextAlignment.LEFT, text);

			this.name = name;
			this.item = item.clone();
			this.text = Converter.fromArrayToList(text);
		}

		public String getName() {
			return name;
		}

		public ItemStack getItem() {
			return item;
		}

		public List<String> getText() {
			return text;
		}

		public void setText(List<String> text) {
			OldItemBuilder.setText(item, TextAlignment.LEFT, text);
			this.text = text;
		}

		public void setText(String... text) {
			OldItemBuilder.setText(item, TextAlignment.LEFT, text);
			this.text = Converter.fromArrayToList(text);
		}
	}
}
