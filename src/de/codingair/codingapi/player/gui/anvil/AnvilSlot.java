package de.codingair.codingapi.player.gui.anvil;

/**
 * Removing of this disclaimer is forbidden.
 *
 * @author codingair
 * @verions: 1.0.0
 **/

public enum AnvilSlot {
	INPUT_LEFT(0),
	INPUT_RIGHT(1),
	OUTPUT(2),
	NONE(-999);
	
	private int slot;
	
	private AnvilSlot(int slot) {
		this.slot = slot;
	}
	
	public int getSlot() {
		return slot;
	}
	
	public static AnvilSlot bySlot(int slot) {
		for(AnvilSlot anvilSlot : values()){
			if(anvilSlot.getSlot() == slot){
				return anvilSlot;
			}
		}
		
		return NONE;
	}
}
