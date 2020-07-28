package de.codingair.codingapi.particles.animations.movables;

import org.bukkit.Location;

public abstract class MovableMid {
    private final static int standingTicks = 2;
    private Location last = null;

    private int standing = 0;

    public abstract Location getLocation();

    private void update() {
        Location l = getLocation();
        if(l == null) return;
        if(last == null) {
            last = l;
            return;
        }

        double x = last.getX() - l.getX();
        double y = last.getY() - l.getY();
        double z = last.getZ() - l.getZ();

        if(Math.abs(x) + Math.abs(y) + Math.abs(z) > 0.05) {
            last = l;
            standing = 0;
        }

    }

    public void onTick() {
        standing++;
        update();
    }

    public boolean isStanding() {
        return standing >= standingTicks;
    }

    public int getStanding() {
        return standing;
    }

    public void setStanding(int standing) {
        this.standing = standing;
    }
}
