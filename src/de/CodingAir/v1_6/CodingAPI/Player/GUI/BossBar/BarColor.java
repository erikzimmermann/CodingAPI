package de.CodingAir.v1_6.CodingAPI.Player.GUI.BossBar;

/**
 * Removing of this disclaimer is forbidden.
 *
 * @author CodingAir
 * @verions: 1.0.0
 **/

public enum BarColor {
	PINK(1),
	BLUE(2),
	RED(3),
	GREEN(4),
	YELLOW(5),
	PURPLE(6),
	WHITE(7);
	
	private int id;
	
	BarColor(int id) {
		this.id = id;
	}
	
	public int getId() {
		return id;
	}
}
