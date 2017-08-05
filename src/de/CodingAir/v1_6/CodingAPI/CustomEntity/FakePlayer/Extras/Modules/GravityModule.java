package de.CodingAir.v1_6.CodingAPI.CustomEntity.FakePlayer.Extras.Modules;

import de.CodingAir.v1_6.CodingAPI.CustomEntity.FakePlayer.Extras.Modules.Utils.Module;
import de.CodingAir.v1_6.CodingAPI.CustomEntity.FakePlayer.Extras.Modules.Utils.Type;
import de.CodingAir.v1_6.CodingAPI.CustomEntity.FakePlayer.FakePlayer;
import de.CodingAir.v1_6.CodingAPI.Server.Environment;
import org.bukkit.Location;

/**
 * Removing of this disclaimer is forbidden.
 *
 * @author CodingAir
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
