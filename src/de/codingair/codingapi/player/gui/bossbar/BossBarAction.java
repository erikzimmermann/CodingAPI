package de.codingair.codingapi.player.gui.bossbar;

/**
 * Removing of this disclaimer is forbidden.
 *
 * @author codingair
 * @verions: 1.0.0
 **/

public enum BossBarAction {
	ADD(0),
	REMOVE(1),
	UPDATE_PCT(2),
	UPDATE_NAME(3),
	UPDATE_STYLE(4),
	UPDATE_PROPERTIES(5);
	
	private int id;
	BossBarAction(int id) {
		this.id = id;
	}
	
	public int getId() {
		return id;
	}
}
