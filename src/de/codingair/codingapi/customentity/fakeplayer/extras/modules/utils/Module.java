package de.codingair.codingapi.customentity.fakeplayer.extras.modules.utils;

import de.codingair.codingapi.customentity.fakeplayer.FakePlayer;

/**
 * Removing of this disclaimer is forbidden.
 *
 * @author codingair
 * @verions: 1.0.0
 **/

public abstract class Module {
	private FakePlayer player;
	private Type type;
	
	public Module(FakePlayer player, Type type) {
		this.player = player;
		this.type = type;
	}
	
	public abstract void onEvent();
	
	public FakePlayer getPlayer() {
		return player;
	}
	
	public Type getType() {
		return type;
	}
}
