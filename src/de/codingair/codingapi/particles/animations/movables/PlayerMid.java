package de.codingair.codingapi.particles.animations.movables;

import org.bukkit.Location;
import org.bukkit.entity.Player;

public class PlayerMid extends MovableMid {
    private Player player;

    public PlayerMid(Player player) {
        this.player = player;
    }

    @Override
    public Location getLocation() {
        return player.isOnline() ? player.getLocation() : null;
    }
}
