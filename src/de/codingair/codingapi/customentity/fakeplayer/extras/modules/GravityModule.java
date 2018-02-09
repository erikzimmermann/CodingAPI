package de.codingair.codingapi.customentity.fakeplayer.extras.modules;

import de.codingair.codingapi.customentity.fakeplayer.extras.modules.utils.Type;
import de.codingair.codingapi.server.Environment;
import de.codingair.codingapi.customentity.fakeplayer.extras.modules.utils.Module;
import de.codingair.codingapi.customentity.fakeplayer.FakePlayer;
import org.bukkit.Location;

/**
 * Removing of this disclaimer is forbidden.
 *
 * @author codingair
 * @verions: 1.0.0
 **/

public class GravityModule extends Module {
	public static final double FALLING_ACCELERATION = 0.08D;
	private double fallingAcceleration = 0;
	
	public GravityModule(FakePlayer player) {
		super(player, Type.GravityModule);
	}
	
	@Override
	public void onEvent() {
		if(getPlayer().isJumping()) return;
		
		if(!getPlayer().isOnGround()) fallingAcceleration += FALLING_ACCELERATION;
		else {
			fallingAcceleration = 0;
			return;
		}
		
		Location loc = getPlayer().getLocation().clone();
		while(!Environment.isBlock(loc.getBlock())) {
			loc.setY(loc.getBlockY() - 1);
		}
		
		loc.setY(loc.getBlockY() + Environment.getBlockHeight(loc.getBlock()));
		double distance = getPlayer().getLocation().getY() - loc.getY();
		
		if(distance < fallingAcceleration) {
			if(distance != 0) getPlayer().move(0, -Math.abs(distance), 0);
			fallingAcceleration = 0;
		} else {
			getPlayer().move(0, -fallingAcceleration, 0);
		}
	}
}
