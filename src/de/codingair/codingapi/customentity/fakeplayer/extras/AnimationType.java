package de.codingair.codingapi.customentity.fakeplayer.extras;

/**
 * Removing of this disclaimer is forbidden.
 *
 * @author codingair
 * @verions: 1.0.0
 **/

public enum AnimationType {
	SWING_MAIN_ARM(0),
	TAKE_DAMAGE(1),
	LEAVE_BED(2),
	SWING_OFFHAND(3),
	CRITICAL_EFFECT(4),
	MAGIC_CRITICAL_EFFECT(5),
	
	HURT(2, true),
	DEATH(3, true),
	SHIELD_BLOCK(29, true),
	SHIELD_BREAK(30, true),
	THORNS(33, true);
	
	private int id;
	private boolean sound = false;
	
	AnimationType(int id) {
		this.id = id;
	}
	
	AnimationType(int id, boolean sound) {
		this.id = id;
		this.sound = sound;
	}
	
	public int getId() {
		return id;
	}
	
	public boolean isSound() {
		return sound;
	}
}
