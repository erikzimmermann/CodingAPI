package de.codingair.codingapi.customentity.fakeplayer.extras.motions;

import de.codingair.codingapi.tools.Callback;
import de.codingair.codingapi.customentity.fakeplayer.FakePlayer;
import org.bukkit.Location;

/**
 * Removing of this disclaimer is forbidden.
 *
 * @author codingair
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
