package de.codingair.codingapi.customentity.fakeplayer.extras.modules;

import de.codingair.codingapi.customentity.fakeplayer.extras.modules.utils.Module;
import de.codingair.codingapi.customentity.fakeplayer.extras.modules.utils.Type;
import de.codingair.codingapi.server.Environment;
import de.codingair.codingapi.customentity.fakeplayer.FakePlayer;
import org.bukkit.Location;
import org.bukkit.util.Vector;

/**
 * Removing of this disclaimer is forbidden.
 *
 * @author codingair
 * @verions: 1.0.0
 **/

public class JumpModule extends Module {
	public static final double JUMP_HEIGHT = 1.3D;
	public static final double DISTANCE_TO_BLOCK = 1.8D;
	public static final double DISTANCE_TO_BLOCK_SPRINTING = 3.0D;
	
	public JumpModule(FakePlayer player) {
		super(player, Type.JumpModule);
	}
	
	@Override
	public void onEvent() {
		if(!getPlayer().isOnGround()) return;
		
		Vector v = getJumpVector(getPlayer(), getPlayer().getLocation().clone());
		if(v != null && canJump(getPlayer())) {
			getPlayer().move(v.getX(), v.getY(), v.getZ(), true);
		}
	}
	
	public static Vector getJumpVector(FakePlayer player, Location to) {
		Location old = player.getOldLocation();
		
		double x = (to.getX() - old.getX()) * (player.isSprinting() ? DISTANCE_TO_BLOCK_SPRINTING : DISTANCE_TO_BLOCK);
		double z = (to.getZ() - old.getZ()) * (player.isSprinting() ? DISTANCE_TO_BLOCK_SPRINTING : DISTANCE_TO_BLOCK);
		
		to.add(x, 0, z);
		
		if(Environment.isBlock(to.getBlock()) && !Environment.isPassableDoor(to.getBlock())) {
			if(Environment.isSlab(to.getBlock())) {
				to.add(0, 0.5, 0);
				if(Environment.isBlock(to.getBlock())) {
					return new Vector(x, 0.5, z);
				}
			} else {
				return new Vector(x, JUMP_HEIGHT, z);
			}
		}
		
		return null;
	}
	
	public static boolean hasToJump(FakePlayer player, Location to) {
		Vector v = getJumpVector(player, to);
		return v != null && v.getY() == JUMP_HEIGHT;
	}
	
	public static boolean canJump(FakePlayer player) {
		Location loc = player.getLocation().clone();
		Location old = player.getOldLocation();
		
		double x = (loc.getX() - old.getX()) * (player.isSprinting() ? DISTANCE_TO_BLOCK_SPRINTING : DISTANCE_TO_BLOCK);
		double z = (loc.getZ() - old.getZ()) * (player.isSprinting() ? DISTANCE_TO_BLOCK_SPRINTING : DISTANCE_TO_BLOCK);
		
		loc.add(x, 0, z);
		
		if(Environment.isBlock(loc.getBlock())) {
			if(!Environment.isSlab(loc.getBlock())) {
				loc.add(0, JUMP_HEIGHT, 0);
				
				return !Environment.isBlock(loc.getBlock());
			}
		}
		
		return true;
	}
}
