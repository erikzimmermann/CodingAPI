package de.codingair.codingapi.particles.animations.customanimations;

import de.codingair.codingapi.particles.Particle;
import de.codingair.codingapi.particles.animations.movables.MovableMid;
import org.bukkit.Location;

import java.util.ArrayList;
import java.util.List;

import static java.lang.Math.cos;
import static java.lang.Math.sin;

/**
 * Removing of this disclaimer is forbidden.
 *
 * @author codingair
 * @verions: 1.0.0
 **/

public class CircleAnimation extends CustomAnimation {
    private static final List<List<Location>> CACHE = new ArrayList<>();

    public CircleAnimation(Particle particle, MovableMid mid, double radius, double height, int delay) {
        super(particle, mid, radius, height, delay);
    }

    public CircleAnimation(Particle particle, Location mid, double radius, double height, int delay) {
        super(particle, mid, radius, height, delay);
    }

    @Override
    public List<List<Location>> calculate(boolean cached) {
        double r = cached ? CALCULATE_RADIUS : getRadius();
        List<List<Location>> locations = cached ? CACHE : new ArrayList<>();
        clear(CACHE);

        double t = 0;
        while(t < 2 * Math.PI) {
            List<Location> l = new ArrayList<>();
            t += Math.PI / 8;

            double x = r * cos(t);
            double z = r * sin(t);

            l.add(getZero().add(x, 0, z));
            locations.add(l);
        }

        return locations;
    }

    @Override
    public boolean isMotionAnimation() {
        return true;
    }

    @Override
    List<List<Location>> getAnimCache() {
        return CACHE;
    }
}
