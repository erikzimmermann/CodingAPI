package de.CodingAir.v1_6.CodingAPI.CustomEntity.FakePlayer.Extras.Modules;

import de.CodingAir.v1_6.CodingAPI.CustomEntity.FakePlayer.Extras.Modules.Utils.Module;
import de.CodingAir.v1_6.CodingAPI.CustomEntity.FakePlayer.Extras.Modules.Utils.Type;
import de.CodingAir.v1_6.CodingAPI.CustomEntity.FakePlayer.FakePlayer;
import org.bukkit.entity.Player;

/**
 * Removing of this disclaimer is forbidden.
 *
 * @author CodingAir
 * @verions: 1.0.0
 **/

public class FollowModule extends Module {
	private Player followed = null;
	
	public FollowModule(FakePlayer player) {
		super(player, Type.FollowModule);
	}
	
	@Override
	public void onEvent() {
		if(this.followed == null) return;
		
		double diffX = followed.getLocation().getX() - getPlayer().getLocation().getX();
		double diffZ = followed.getLocation().getZ() - getPlayer().getLocation().getZ();
		
		getPlayer().lookAt(followed);
		
		if(Math.abs(diffX) > 3D || Math.abs(diffZ) > 3D) {
			getPlayer().moveForward();
		}
	}
	
	public Player getFollowed() {
		return followed;
	}
	
	public void setFollowed(Player followed) {
		this.followed = followed;
	}
}
