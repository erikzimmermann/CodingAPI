package de.codingair.codingapi.customentity.fakeplayer.extras.motions;

import de.codingair.codingapi.customentity.fakeplayer.FakePlayer;
import de.codingair.codingapi.tools.Callback;
import org.bukkit.Location;

/**
 * Removing of this disclaimer is forbidden.
 *
 * @author codingair
 * @verions: 1.0.0
 **/

public class FakePlayerMotionLook {
	private final FakePlayer fakePlayer;
	private final Location look;
	private final Callback<Boolean> callback;
	private final double divide;
	
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
