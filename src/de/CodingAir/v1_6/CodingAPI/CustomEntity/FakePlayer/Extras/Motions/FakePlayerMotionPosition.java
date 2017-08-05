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

public class FakePlayerMotionPosition {
	private FakePlayer fakePlayer;
	private Location position;
	private boolean gravity;
	private Callback<Boolean> callback;
	
	public FakePlayerMotionPosition(FakePlayer fakePlayer, Location position, boolean gravity, Callback<Boolean> callback) {
		this.fakePlayer = fakePlayer;
		this.position = position;
		this.gravity = gravity;
		this.callback = callback;
	}
	
	public FakePlayer getFakePlayer() {
		return fakePlayer;
	}
	
	public Location getPosition() {
		return position;
	}
	
	public boolean hasGravity() {
		return gravity;
	}
	
	public Callback<Boolean> getCallback() {
		return callback;
	}
}
