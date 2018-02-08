package de.codingair.codingapi.customentity.fakeplayer.extras;

import de.codingair.codingapi.server.Version;

/**
 * Removing of this disclaimer is forbidden.
 *
 * @author codingair
 * @verions: 1.0.0
 **/

public enum EquipmentType {
	MAINHAND(0, 0),
	OFFHAND(-1, 1),
	HELMET(1, 5),
	CHESTPLATE(2, 4),
	LEGGINGS(3, 3),
	BOOTS(4, 2),
	ALL(-999, -999);
	
	private int older;
	private int newer;
	
	EquipmentType(int older, int newer) {
		this.older = older;
		this.newer = newer;
	}
	
	public int getId() {
		if(Version.getVersion().equals(Version.v1_9) || Version.getVersion().equals(Version.v1_10) || Version.getVersion().equals(Version.v1_11)) return this.newer;
		else return older;
	}
}
