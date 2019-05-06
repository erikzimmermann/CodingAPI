package de.codingair.codingapi.particles.animations.standalone;

import de.codingair.codingapi.particles.Particle;
import de.codingair.codingapi.particles.animations.Animation;
import org.bukkit.Location;

import java.util.ArrayList;
import java.util.List;

import static java.lang.Math.cos;
import static java.lang.Math.sin;

public class PulsingCircleAnimation extends Animation {
    private double radius;
    private double height;
    private Location location;

    private int ticksBetweenParticles = 0;
    private int skippedTicks = 0;

    public PulsingCircleAnimation(Particle particle, double radius, double height, Location location) {
        super(particle);
        this.radius = radius;
        this.height = height;
        this.location = location;
    }

    public PulsingCircleAnimation(Particle particle, double radius, double height, Location location, int ticksBetweenParticles) {
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

        List<Location> locations = new ArrayList<>();

        double diff = (12 - this.radius * 0.2);

        for(int i = 0; i < 360 / diff; i++) {
            double degrees = diff * i;

            if(degrees >= 360) degrees -= 360;
            if(degrees < 0) degrees += 360;

            double x = radius * cos(degrees * Math.PI / 180);
            double y = height;
            double z = radius * sin(degrees * Math.PI / 180);

            Location loc = this.location.clone();
            loc.add(x, y, z);
            locations.add(loc);
        }

        for(Location loc : locations) {
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