package de.codingair.codingapi.particles.animations.customanimations;

import de.codingair.codingapi.particles.Particle;
import de.codingair.codingapi.particles.animations.movables.MovableMid;
import org.bukkit.Location;

import java.util.ArrayList;
import java.util.List;

public class LineAnimation extends CustomAnimation {
    private static final List<List<Location>> CACHE = new ArrayList<>();
    private static final double STEP = 0.5D;

    public LineAnimation(Particle particle, MovableMid mid, double radius, double height, int delay) {
        super(particle, mid, radius, height, delay);
    }

    public LineAnimation(Particle particle, Location mid, double radius, double height, int delay) {
        super(particle, mid, radius, height, delay);
    }

    @Override
    public List<List<Location>> calculate(boolean cached) {
        double r = cached ? CALCULATE_RADIUS : getRadius();
        List<List<Location>> locations = cached ? CACHE : new ArrayList<>();
        clear(CACHE);

        List<Location> l = new ArrayList<>();
        int step = 0;
        double y = 0;
        while(y < r) {
            y = -r + STEP * step++;
            l.add(getZero().add(0, y, 0));
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
