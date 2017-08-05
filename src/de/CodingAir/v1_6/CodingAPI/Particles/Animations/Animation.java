package de.CodingAir.v1_6.CodingAPI.Particles.Animations;

import de.CodingAir.v1_6.CodingAPI.Particles.Particle;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;

/**
 * Removing of this disclaimer is forbidden.
 *
 * @author CodingAir
 * @verions: 1.0.0
 **/

public abstract class Animation {
	private static List<Animation> animations = new ArrayList<>();
	public final static int standingTicks = 2;
	
	private boolean whileStanding = false;
	private int standing = 0;
	
	private Particle particle;
	private Player player;
	
	public Animation(Particle particle, Player player, Plugin plugin) {
		this.particle = particle;
		this.player = player;
		this.whileStanding = false;
		
		AnimationListener.register(plugin);
	}
	
	public Animation(Particle particle, Player player, Plugin plugin, boolean whileStanding) {
		this.particle = particle;
		this.player = player;
		this.whileStanding = whileStanding;
		
		AnimationListener.register(plugin);
	}
	
	public void onTick() {
		standing++;
		
		Location loc = null;
		if(!whileStanding) loc = onDisplay();
		else if(isStanding()) loc = onDisplay();
		
		if(loc != null) particle.send(loc);
	}
	
	public abstract Location onDisplay();
	
	public boolean isWhileStanding() {
		return whileStanding;
	}
	
	public Particle getParticle() {
		return particle;
	}
	
	public Player getPlayer() {
		return player;
	}
	
	public boolean isStanding() {
		return standing > standingTicks;
	}
	
	public static List<Animation> getAnimations() {
		return animations;
	}
	
	public boolean isRunning() {
		return animations.contains(this);
	}
	
	public void setRunning(boolean running) {
		if(running && !animations.contains(this)) animations.add(this);
		else if(!running && animations.contains(this)) animations.remove(this);
	}
	
	public void setStandingTicks(int standing) {
		this.standing = standing;
	}
}
