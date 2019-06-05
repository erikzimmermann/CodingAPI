package de.codingair.codingapi.particles.animations.playeranimations;

import de.codingair.codingapi.particles.Particle;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.List;

import static java.lang.Math.cos;
import static java.lang.Math.sin;

public class PulsingCircleAnimation extends CustomAnimation {
    public PulsingCircleAnimation(Particle particle, Player player, Plugin plugin, boolean whileStanding, double radius, double height, int delay) {
        super(particle, player, plugin, whileStanding, radius, height, delay);
    }

    @Override
    public List<Location> onDisplay() {
        List<Location> locations = new ArrayList<>();

        double diff = (12 - getRadius() * 0.2);

        for(int i = 0; i < 360 / diff; i++) {
            double degrees = diff * i;

            if(degrees >= 360) degrees -= 360;
            if(degrees < 0) degrees += 360;

            double x = getRadius() * cos(degrees * Math.PI / 180);
            double y = getHeight();
            double z = getRadius() * sin(degrees * Math.PI / 180);

            Location loc = getPlayer().getLocation().clone();
            loc.add(x, y, z);
            locations.add(loc);
        }

        return locations;
    }
}