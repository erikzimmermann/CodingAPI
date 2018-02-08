package de.codingair.codingapi.particles.animations;

import de.codingair.codingapi.API;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.List;

/**
 * Removing of this disclaimer is forbidden.
 *
 * @author codingair
 * @verions: 1.0.0
 **/

public class AnimationListener implements Listener {
	private static boolean initialized = false;
	
	@EventHandler
	public void onMove(PlayerMoveEvent e) {
		Player p = e.getPlayer();
		
		double x = e.getFrom().getX() - e.getTo().getX();
		double y = e.getFrom().getY() - e.getTo().getY();
		double z = e.getFrom().getZ() - e.getTo().getZ();
		
		if(x < 0) x *= -1;
		if(y < 0) y *= -1;
		if(z < 0) z *= -1;
		
		double result = x + y + z;
		
		if(result > 0.05){
			API.getTickers(PlayerAnimation.class).forEach(anim -> {
				if(anim.getPlayer().getName().equals(p.getName())) anim.setStandingTicks(0);
			});
		}
	}
	
	@EventHandler
	public void onQuit(PlayerQuitEvent e) {
		Player p = e.getPlayer();

		List<PlayerAnimation> animations = new ArrayList<>(API.getTickers(PlayerAnimation.class));
		
		animations.forEach(anim -> {
			if(anim.getPlayer().getName().equals(p.getName())) anim.setRunning(false);
		});

		animations.clear();
	}
	
	public static void register(Plugin plugin) {
		if(initialized) return;
		
		Bukkit.getPluginManager().registerEvents(new AnimationListener(), plugin);
		initialized = true;
	}
	
}
