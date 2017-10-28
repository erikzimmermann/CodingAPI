package de.codingair.codingapi.particles.Animations;

import de.codingair.codingapi.particles.Particle;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public class VectorAnimation extends Animation {
    public VectorAnimation(Particle particle, Player player, Plugin plugin) {
        super(particle, player, plugin);
    }

    public VectorAnimation(Particle particle, Player player, Plugin plugin, boolean whileStanding) {
        super(particle, player, plugin, whileStanding);
    }

    @Override
    public Location onDisplay() {
        return null;
    }
}
