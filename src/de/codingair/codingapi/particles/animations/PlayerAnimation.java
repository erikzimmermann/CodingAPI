package de.codingair.codingapi.particles.animations;

import de.codingair.codingapi.particles.Particle;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

/**
 * Removing of this disclaimer is forbidden.
 *
 * @author codingair
 * @verions: 1.0.0
 **/

public abstract class PlayerAnimation extends Animation {
	private final static int standingTicks = 2;
	
	private boolean whileStanding;
	private int standing = 0;

	private Player player;
	
	public PlayerAnimation(Particle particle, Player player, Plugin plugin) {
		super(particle);
		this.player = player;
		this.whileStanding = false;
		
		AnimationListener.register(plugin);
	}
	
	public PlayerAnimation(Particle particle, Player player, Plugin plugin, boolean whileStanding) {
		super(particle);
		this.player = player;
		this.whileStanding = whileStanding;
		
		AnimationListener.register(plugin);
	}

	@Override
	public void onTick() {
		standing++;
		
		Location loc = null;
		if(!whileStanding) loc = onDisplay();
		else if(isStanding()) loc = onDisplay();
		
		if(loc != null) getParticle().send(loc, false);
	}
	
	public abstract Location onDisplay();
	
	public boolean isWhileStanding() {
		return whileStanding;
	}
	
	public Player getPlayer() {
		return player;
	}
	
	public boolean isStanding() {
		return standing > standingTicks;
	}
	
	public void setStandingTicks(int standing) {
		this.standing = standing;
	}
}
