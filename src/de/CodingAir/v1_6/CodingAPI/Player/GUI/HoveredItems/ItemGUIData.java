package de.CodingAir.v1_6.CodingAPI.Player.GUI.HoveredItems;

import de.CodingAir.v1_6.CodingAPI.Tools.Converter;
import de.CodingAir.v1_6.CodingAPI.Tools.OldItemBuilder;
import de.CodingAir.v1_6.CodingAPI.Utils.TextAlignment;
import org.bukkit.inventory.ItemStack;

import java.util.List;

/**
 * Removing of this disclaimer is forbidden.
 *
 * @author CodingAir
 * @verions: 1.0.0
 **/

public class ItemGUIData {
	private String name;
	private ItemStack item;
	private List<String> text;
	
	public ItemGUIData(String name, ItemStack item, List<String> text) {
		OldItemBuilder.setText(item, TextAlignment.LEFT, text);
		
		this.name = name;
		this.item = item.clone();
		this.text = text;
	}
	
	public ItemGUIData(String name, ItemStack item, String... text) {
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
