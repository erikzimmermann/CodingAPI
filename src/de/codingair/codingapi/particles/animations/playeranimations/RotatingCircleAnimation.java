package de.codingair.codingapi.particles.animations.playeranimations;

import de.codingair.codingapi.particles.Particle;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

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

public class RotatingCircleAnimation extends CustomAnimation {
    private double degrees = 0;

    public RotatingCircleAnimation(Particle particle, Player player, Plugin plugin, boolean whileStanding, double radius, double height, int delay) {
        super(particle, player, plugin, whileStanding, radius, height, delay);
    }

    @Override
    public List<Location> onDisplay() {
        this.degrees += 2;
        double radius = getRadius();

        List<Location> locations = new ArrayList<>();
        for(int i = 0; i < 360; i += 36) {
            double degrees = i + this.degrees;
            if(degrees >= 360) degrees -= 360;
            if(degrees < 0) degrees += 360;

            Location loc = getPlayer().getLocation().clone();

            double x = radius * cos(degrees * Math.PI / 180);
            double y = getHeight();
            double z = radius * sin(degrees * Math.PI / 180);

            loc.add(x, y, z);
            locations.add(loc);
        }

        return locations;
    }
}
