package de.codingair.codingapi.particles.animations.playeranimations;

import de.codingair.codingapi.particles.Particle;
import de.codingair.codingapi.particles.animations.PlayerAnimation;
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

public class CircleAnimation extends CustomAnimation {
    private double t;

    public CircleAnimation(Particle particle, Player player, Plugin plugin, double radius) {
        this(particle, player, plugin, true, radius, 1, 1);
    }

    public CircleAnimation(Particle particle, Player player, Plugin plugin, boolean whileStanding, double radius, double height, int delay) {
        super(particle, player, plugin, whileStanding, radius, height, delay);
    }

    @Override
    public List<Location> onDisplay() {
        Location loc = super.getPlayer().getLocation().clone();

        t += Math.PI / 8;

        double x = getRadius() * cos(t);
        double y = getHeight();
        double z = getRadius() * sin(t);

        loc.add(x, y, z);

        return new ArrayList<Location>() {{
            add(loc);
        }};
    }
}
