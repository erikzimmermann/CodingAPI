package de.CodingAir.v1_6.CodingAPI.CustomEntity.FakePlayer.Extras.Modules.Utils;

import de.CodingAir.v1_6.CodingAPI.CustomEntity.FakePlayer.FakePlayer;

/**
 * Removing of this disclaimer is forbidden.
 *
 * @author CodingAir
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
