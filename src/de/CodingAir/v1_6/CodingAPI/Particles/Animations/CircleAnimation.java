package de.CodingAir.v1_6.CodingAPI.Particles.Animations;

import de.CodingAir.v1_6.CodingAPI.Particles.Particle;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import static java.lang.Math.cos;
import static java.lang.Math.sin;

/**
 * Removing of this disclaimer is forbidden.
 *
 * @author CodingAir
 * @verions: 1.0.0
 **/

public class CircleAnimation extends Animation {
	private double radius;
	private double t;
	private double height = 1;
	
	public CircleAnimation(Particle particle, Player player, Plugin plugin, double radius) {
		super(particle, player, plugin, true);
		this.radius = radius;
	}
	
	@Override
	public Location onDisplay() {
		Location loc = super.getPlayer().getLocation().clone();
		
		t += Math.PI / 8;
		
		double x = radius * cos(t);
		double y = height;
		double z = radius * sin(t);
		
		loc.add(x, y, z);
		
		return loc;
	}
	
	public double getRadius() {
		return radius;
	}
	
	public void setRadius(double radius) {
		this.radius = radius;
	}
	
	public double getHeight() {
		return height;
	}
	
	public void setHeight(double height) {
		this.height = height;
	}
}
