package de.CodingAir.v1_6.CodingAPI.CustomEntity.FakePlayer.Extras.Modules;

import de.CodingAir.v1_6.CodingAPI.CustomEntity.FakePlayer.Extras.Modules.Utils.Module;
import de.CodingAir.v1_6.CodingAPI.CustomEntity.FakePlayer.Extras.Modules.Utils.Type;
import de.CodingAir.v1_6.CodingAPI.CustomEntity.FakePlayer.FakePlayer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

/**
 * Removing of this disclaimer is forbidden.
 *
 * @author CodingAir
 * @verions: 1.0.0
 **/

public class TargetModule extends Module {
	private double radius;
	
	public TargetModule(FakePlayer fakePlayer, double radius) {
		super(fakePlayer, Type.TargetModule);
		
		this.radius = radius;
	}
	
	@Override
	public void onEvent() {
		Player p = null;
		double distance = -1;
		
		for(Player player : Bukkit.getOnlinePlayers()) {
			if(p == null) {
				p = player;
				distance = getPlayer().getLocation().distance(player.getLocation());
				continue;
			}
			
			double d = getPlayer().getLocation().distance(player.getLocation());
			
			if(distance > d){
				p = player;
				distance = d;
			}
		}
		
		if(p == null || distance > this.radius) return;
		getPlayer().lookAt(p);
	}
	
	public double getRadius() {
		return radius;
	}
}