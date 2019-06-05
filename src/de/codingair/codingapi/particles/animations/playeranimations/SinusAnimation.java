package de.codingair.codingapi.particles.animations.playeranimations;

import de.codingair.codingapi.particles.Particle;
import de.codingair.codingapi.particles.animations.Animation;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.List;

public class SinusAnimation extends CustomAnimation {
    private boolean minimize = true;
    private final double fixedHeight;
    private double degrees = 0;

    public SinusAnimation(Particle particle, Player player, Plugin plugin, boolean whileStanding, double radius, double height, int delay) {
        super(particle, player, plugin, whileStanding, radius, height, delay);

        this.fixedHeight = height;
    }

    @Override
    public List<Location> onDisplay() {
        double step = 0.1;
        double dif = 0.25;

        if(minimize) {
            setHeight(getHeight() - step);
            if(getHeight() < fixedHeight - dif) minimize = false;
        } else {
            setHeight(getHeight() + step);
            if(getHeight() > fixedHeight + dif) minimize = true;
        }

        Location circleLocation = getPlayer().getLocation().clone().add(
                getRadius() * Math.cos(this.degrees * Math.PI / 180),
                getHeight(),
                getRadius() * Math.sin(this.degrees * Math.PI / 180));

        degrees += (12 - getRadius() * 0.2);
        if(degrees >= 360) degrees -= 360;
        if(degrees < 0) degrees += 360;

        return new ArrayList<Location>() {{
            add(circleLocation);
        }};
    }
}
