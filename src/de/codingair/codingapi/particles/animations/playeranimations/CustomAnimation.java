package de.codingair.codingapi.particles.animations.playeranimations;

import de.codingair.codingapi.particles.Particle;
import de.codingair.codingapi.particles.animations.PlayerAnimation;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public abstract class CustomAnimation extends PlayerAnimation {
    private double radius, height;

    public CustomAnimation(Particle particle, Player player, Plugin plugin, boolean whileStanding, double radius, double height, int delay) {
        super(particle, player, plugin, whileStanding, delay);
        this.radius = radius;
        this.height = height;
    }

    public double getRadius() {
        return radius;
    }

    public void setRadius(double radius) {
        this.radius = radius;
    }

    public double getHeight() {
        return height;
    }

    public void setHeight(double height) {
        this.height = height;
    }
}
