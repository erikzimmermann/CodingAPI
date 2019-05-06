package de.codingair.codingapi.particles.animations.standalone;

import de.codingair.codingapi.particles.Particle;
import de.codingair.codingapi.particles.animations.Animation;
import org.bukkit.Location;

import static java.lang.Math.cos;
import static java.lang.Math.sin;

/**
 * Removing of this disclaimer is forbidden.
 *
 * @author codingair
 * @verions: 1.0.0
 **/

public class RotatingCircleAnimation extends Animation {
    private double radius;
    private double degrees;
    private double height;
    private Location location;

    private int ticksBetweenParticles = 0;
    private int skippedTicks = 0;

    public RotatingCircleAnimation(Particle particle, double radius, double height, Location location) {
        super(particle);
        this.radius = radius;
        this.height = height;
        this.location = location;
    }

    public RotatingCircleAnimation(Particle particle, double radius, double height, Location location, int ticksBetweenParticles) {
        super(particle);
        this.radius = radius;
        this.height = height;
        this.location = location;
        this.ticksBetweenParticles = ticksBetweenParticles;
    }

    @Override
    public void onTick() {
        if(ticksBetweenParticles > 0 && skippedTicks < ticksBetweenParticles) {
            skippedTicks++;
            return;
        } else if(ticksBetweenParticles > 0 && skippedTicks == ticksBetweenParticles) skippedTicks = 0;


        this.degrees += 2;
        double radius = this.radius;

        for(int i = 0; i < 360; i += 36) {
            double degrees = i + this.degrees;
            if(degrees >= 360) degrees -= 360;
            if(degrees < 0) degrees += 360;

            Location loc = this.location.clone();

            double x = radius * cos(degrees * Math.PI / 180);
            double y = height;
            double z = radius * sin(degrees * Math.PI / 180);

            loc.add(x, y, z);

            sendParticle(loc);
        }
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

    public Location getLocation() {
        return location;
    }

    public int getTicksBetweenParticles() {
        return ticksBetweenParticles;
    }
}
