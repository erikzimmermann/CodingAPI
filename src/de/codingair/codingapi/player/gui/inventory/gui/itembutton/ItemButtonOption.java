package de.codingair.codingapi.player.gui.inventory.gui.itembutton;

import de.codingair.codingapi.server.SoundData;
import org.bukkit.Sound;

/**
 * Removing of this disclaimer is forbidden.
 *
 * @author codingair
 * @verions: 1.0.0
 **/

public class ItemButtonOption {
	private boolean movable = false;
	private Sound clickSound = null;
	private SoundData clickSound2 = null;
	private boolean closeOnClick = false;
	private boolean onlyLeftClick = false;
	private boolean onlyRightClick = false;
	private boolean numberKey = false;
	private boolean doubleClick = false;

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
		option.setClickSound(this.clickSound2);
		option.setDoubleClick(this.doubleClick);
		option.setMovable(this.movable);
		option.setOnlyLeftClick(this.onlyLeftClick);
		option.setCloseOnClick(this.closeOnClick);
		option.setOnlyRightClick(this.onlyRightClick);
		option.setNumberKey(this.numberKey);
		return option;
	}

	public boolean isNumberKey() {
		return numberKey;
	}

	public void setNumberKey(boolean numberKey) {
		this.numberKey = numberKey;
	}

	public boolean isDoubleClick() {
		return doubleClick;
	}

	public void setDoubleClick(boolean doubleClick) {
		this.doubleClick = doubleClick;
	}

	public SoundData getClickSound2() {
		return clickSound2;
	}

	public void setClickSound(SoundData clickSound2) {
		this.clickSound2 = clickSound2;
	}
}
