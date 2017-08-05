package de.CodingAir.v1_6.CodingAPI.Player.GUI.Inventory.Interface.ItemButton;

import org.bukkit.Sound;

/**
 * Removing of this disclaimer is forbidden.
 *
 * @author CodingAir
 * @verions: 1.0.0
 **/

public class ItemButtonOption {
	private boolean movable = false;
	private Sound clickSound = null;
	private boolean closeOnClick = false;
	private boolean onlyLeftClick = false;
	private boolean onlyRightClick = false;
	
	public boolean isMovable() {
		return movable;
	}
	
	public void setMovable(boolean movable) {
		this.movable = movable;
	}
	
	public Sound getClickSound() {
		return clickSound;
	}
	
	public void setClickSound(Sound clickSound) {
		this.clickSound = clickSound;
	}
	
	public boolean isCloseOnClick() {
		return closeOnClick;
	}
	
	public void setCloseOnClick(boolean closeOnClick) {
		this.closeOnClick = closeOnClick;
	}
	
	public boolean isOnlyLeftClick() {
		return onlyLeftClick;
	}
	
	public void setOnlyLeftClick(boolean onlyLeftClick) {
		this.onlyLeftClick = onlyLeftClick;
	}
	
	public boolean isOnlyRightClick() {
		return onlyRightClick;
	}
	
	public void setOnlyRightClick(boolean onlyRightClick) {
		this.onlyRightClick = onlyRightClick;
	}
	
	public ItemButtonOption clone() {
		ItemButtonOption option = new ItemButtonOption();
		option.setClickSound(this.clickSound);
		option.setMovable(this.movable);
		option.setOnlyLeftClick(this.onlyLeftClick);
		option.setCloseOnClick(this.closeOnClick);
		option.setOnlyRightClick(this.onlyRightClick);
		return option;
	}
}
