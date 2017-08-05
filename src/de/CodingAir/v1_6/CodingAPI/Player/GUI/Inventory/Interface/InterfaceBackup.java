package de.CodingAir.v1_6.CodingAPI.Player.GUI.Inventory.Interface;

import de.CodingAir.v1_6.CodingAPI.Player.GUI.Inventory.Interface.ItemButton.ItemButton;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;

public class InterfaceBackup {
	private ItemStack[] contents;
	private HashMap<Integer, ItemButton> buttons;
	private String wrappersName;
	
	public InterfaceBackup(ItemStack[] contents, HashMap<Integer, ItemButton> buttons, String wrappersName) {
		this.contents = contents;
		this.buttons = buttons;
		this.wrappersName = wrappersName;
	}
	
	public ItemStack[] getContents() {
		return contents;
	}
	
	public HashMap<Integer, ItemButton> getButtons() {
		return buttons;
	}
	
	public String getWrappersName() {
		return wrappersName;
	}
}
