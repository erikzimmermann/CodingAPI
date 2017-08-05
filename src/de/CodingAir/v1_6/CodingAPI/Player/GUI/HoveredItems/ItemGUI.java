package de.CodingAir.v1_6.CodingAPI.Player.GUI.HoveredItems;

import de.CodingAir.v1_6.CodingAPI.Server.Sound;
import de.CodingAir.v1_6.CodingAPI.Tools.Converter;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.List;

/**
 * Removing of this disclaimer is forbidden.
 *
 * @author CodingAir
 * @verions: 1.0.0
 **/

public class ItemGUI {
	public static List<ItemGUI> ITEM_GUIS = new ArrayList<>();
	
	private Plugin plugin;
	
	private List<HoveredItem> hoveredItems = new ArrayList<>();
	private List<ItemGUIData> data = new ArrayList<>();
	private Player player;
	private ItemGUIListener listener = null;
	
	private int maxItems = 14;
	private double radius = 3.0D;
	private double height = 0.5D;
	private double moveHeight = 0.6D;
	
	private boolean initialized = false;
	private boolean visible = false;
	
	private boolean visibleOnSneak = false;
	
	public ItemGUI(Plugin plugin, Player player) {
		this.plugin = plugin;
		this.player = player;
		
		ITEM_GUIS.add(this);
	}
	
	public ItemGUI(Plugin plugin, Player player, ItemGUIListener listener) {
		this(plugin, player);
		this.listener = listener;
	}
	
	private void initialize() {
		if(initialized) remove(false);
		
		double current = (this.player.getLocation().getYaw() + 180.0) / 180.0 * Math.PI - 89.52;
		double distance = 2.0 / (double) maxItems * Math.PI;
		double d = current - (distance * ((double) data.size() - 1.0)) / 2.0;
		
		for(ItemGUIData datum : data) {
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
					p.playSound(p.getLocation(), Sound.CLICK.bukkitSound(), 1F, 1F);
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
					p.playSound(p.getLocation(), Sound.CHICKEN_EGG_POP.bukkitSound(), 0.05F, up ? 1.2F : 0.8F);
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
	
	public void remove(boolean destroy) {
		this.hoveredItems.forEach(HoveredItem::remove);
		this.hoveredItems.clear();
		
		if(destroy) {
			ITEM_GUIS = Converter.removeSafely(ITEM_GUIS, this);
			this.data.clear();
		}
		
		initialized = false;
	}
	
	public void spawn() {
		if(!initialized) initialize();
		
		this.hoveredItems.forEach(HoveredItem::spawn);
	}
	
	public void move(Location from, Location to) {
		if(!visible) return;
		
		double diffX = to.getX() - from.getX(), diffY = to.getY() - from.getY(), diffZ = to.getZ() - from.getZ();
		
		for(HoveredItem item : this.hoveredItems) {
			item.teleport(item.getTempLocation().add(diffX, diffY, diffZ), item.getTempLocation().getY() != item.getLocation().getY());
			item.setLocation(item.getLocation().add(diffX, diffY, diffZ));
		}
	}
	
	public void addData(ItemGUIData data) {
		this.data.add(data);
	}
	
	public void removeData(ItemGUIData data) {
		this.data.add(data);
	}
	
	public ItemGUIData getData(String name) {
		for(ItemGUIData datum : this.data) {
			if(datum.getName().equals(name)) return datum;
		}
		
		return null;
	}
	
	public void setText(HoveredItem item, String... text) {
		item.setText(text);
		ItemGUIData data = getData(item.getName());
		data.setText(text);
	}
	
	public void setText(HoveredItem item, List<String> text) {
		setText(item, text.toArray(new String[text.size()]));
	}
	
	public void setText(ItemGUIData data, String... text) {
		if(getItem(data.getName()) != null) getItem(data.getName()).setText(text);
		data.setText(text);
	}
	
	public void setText(ItemGUIData data, List<String> text) {
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
		
		if(visible) spawn();
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
	
	public Plugin getPlugin() {
		return plugin;
	}
	
	public List<ItemGUIData> getData() {
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
		for(ItemGUI gui : ITEM_GUIS) {
			if(gui.getPlayer().getName().equals(p.getName())) return gui;
		}
		
		return null;
	}
	
	public static boolean usesGUI(Player p) {
		return getGUI(p) != null;
	}
}
