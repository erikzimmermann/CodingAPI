package de.codingair.codingapi.particles.animations.customanimations;

import de.codingair.codingapi.particles.Particle;
import de.codingair.codingapi.particles.animations.movables.MovableMid;
import org.bukkit.Location;

import java.util.ArrayList;
import java.util.List;

public class VectorAnimation extends CustomAnimation {
    private static final List<List<Location>> CACHE = new ArrayList<>();
    private static final double STEP = 0.5D;

    public VectorAnimation(Particle particle, MovableMid mid, double radius, double height, int delay) {
        super(particle, mid, radius, height, delay);
    }

    public VectorAnimation(Particle particle, Location mid, double radius, double height, int delay) {
        super(particle, mid, radius, height, delay);
    }

    @Override
    public List<List<Location>> calculate(boolean cached) {
        double r = cached ? CALCULATE_RADIUS : getRadius();
        List<List<Location>> locations = cached ? CACHE : new ArrayList<>();
        clear(CACHE);

        int step = 0;
        double y = -r + STEP * step++;
        while(y < r) {
            List<Location> l = new ArrayList<>();
            l.add(getZero().add(0, y, 0));
            y = -r + STEP * step++;
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
