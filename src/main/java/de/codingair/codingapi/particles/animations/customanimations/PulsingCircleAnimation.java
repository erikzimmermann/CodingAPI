package de.codingair.codingapi.particles.animations.customanimations;

import de.codingair.codingapi.particles.Particle;
import de.codingair.codingapi.particles.animations.movables.MovableMid;
import org.bukkit.Location;

import java.util.ArrayList;
import java.util.List;

import static java.lang.Math.cos;
import static java.lang.Math.sin;

public class PulsingCircleAnimation extends CustomAnimation {
    private static final List<List<Location>> CACHE = new ArrayList<>();

    public PulsingCircleAnimation(Particle particle, MovableMid mid, double radius, double height, int delay) {
        super(particle, mid, radius, height, delay);
    }

    public PulsingCircleAnimation(Particle particle, Location mid, double radius, double height, int delay) {
        super(particle, mid, radius, height, delay);
    }

    @Override
    public List<List<Location>> calculate(boolean cached) {
        double r = cached ? CALCULATE_RADIUS : getRadius();
        List<List<Location>> locations = cached ? CACHE : new ArrayList<>();
        clear(CACHE);

        List<Location> l = new ArrayList<>();
        double diff = (12 - r * 0.2);

        for(int i = 0; i < 360 / diff; i++) {
            double degrees = diff * i;

            if(degrees >= 360) degrees -= 360;
            if(degrees < 0) degrees += 360;

            double x = r * cos(degrees * Math.PI / 180);
            double z = r * sin(degrees * Math.PI / 180);

            l.add(getZero().add(x, 0, z));
        }

        locations.add(l);
        return locations;
    }

    @Override
    public boolean isMotionAnimation() {
        return false;
    }

    @Override
    List<List<Location>> getAnimCache() {
        return CACHE;
    }
}