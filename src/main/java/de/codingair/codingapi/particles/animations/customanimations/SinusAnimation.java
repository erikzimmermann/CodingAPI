package de.codingair.codingapi.particles.animations.customanimations;

import de.codingair.codingapi.particles.Particle;
import de.codingair.codingapi.particles.animations.movables.MovableMid;
import org.bukkit.Location;

import java.util.ArrayList;
import java.util.List;

public class SinusAnimation extends CustomAnimation {
    private static final List<List<Location>> CACHE = new ArrayList<>();

    public SinusAnimation(Particle particle, MovableMid mid, double radius, double height, int delay) {
        super(particle, mid, radius, height, delay);
    }

    public SinusAnimation(Particle particle, Location mid, double radius, double height, int delay) {
        super(particle, mid, radius, height, delay);
    }

    @Override
    public List<List<Location>> calculate(boolean cached) {
        double r = cached ? CALCULATE_RADIUS : getRadius();
        List<List<Location>> locations = cached ? CACHE : new ArrayList<>();
        clear(CACHE);

        double dif = 0.25;
        double step = (8 * (2 * dif) / (360 / (12 - r * 0.2)));

        boolean minimize = false;
        double degrees = 0;
        double height = -dif;

        while(degrees < 360) {
            if(minimize) {
                height -= step;
                if(height < -dif) minimize = false;
            } else {
                height += step;
                if(height > dif) minimize = true;
            }

            Location circleLocation = getZero().add(
                    r * Math.cos(degrees * Math.PI / 180),
                    height,
                    r * Math.sin(degrees * Math.PI / 180));

            degrees += (12 - r * 0.2);

            locations.add(new ArrayList<Location>(){{add(circleLocation);}});
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
