package de.CodingAir.v1_6.CodingAPI.CustomEntity.FakePlayer.Extras.Motions;

import de.CodingAir.v1_6.CodingAPI.CustomEntity.FakePlayer.FakePlayer;
import de.CodingAir.v1_6.CodingAPI.Tools.Callback;
import org.bukkit.Location;

/**
 * Removing of this disclaimer is forbidden.
 *
 * @author CodingAir
 * @verions: 1.0.0
 **/

public class FakePlayerMotionLook {
	private FakePlayer fakePlayer;
	private Location look;
	private Callback<Boolean> callback;
	private double divide;
	
	public FakePlayerMotionLook(FakePlayer fakePlayer, Location look, Callback<Boolean> callback, double divide) {
		this.fakePlayer = fakePlayer;
		this.look = look;
		this.callback = callback;
		this.divide = divide;
	}
	
	public FakePlayer getFakePlayer() {
		return fakePlayer;
	}
	
	public Location getLook() {
		return look;
	}
	
	public Callback<Boolean> getCallback() {
		return callback;
	}
	
	public double getDivider() {
		return divide;
	}
}
