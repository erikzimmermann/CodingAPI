package de.codingair.codingapi.particles.animations.movables;

import org.bukkit.Location;

public class LocationMid extends MovableMid {
    private Location location;

    public LocationMid(Location location) {
        this.location = location;
    }

    @Override
    public Location getLocation() {
        return location.clone();
    }

    @Override
    public void onTick() {
    }

    @Override
    public boolean isStanding() {
        return true;
    }
}
