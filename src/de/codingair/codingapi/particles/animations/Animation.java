package de.codingair.codingapi.particles.animations;

import de.codingair.codingapi.API;
import de.codingair.codingapi.particles.Particle;
import de.codingair.codingapi.utils.Ticker;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public abstract class Animation implements Ticker {
    private Particle particle;
    private double maxDistance = 30;

    public Animation(Particle particle) {
        this.particle = particle;
    }

    public boolean isRunning() {
        return API.getTicker(this) != null;
    }

    public void setRunning(boolean running) {
        if(running && !isRunning()) API.addTicker(this);
        else if(!running && isRunning()) API.removeTicker(this);
    }

    public final void sendParticle(Location location) {
        getParticle().send(location, maxDistance);
    }

    public Particle getParticle() {
        return particle;
    }

    @Override
    public void onSecond() { }

    @Override
    public Object getInstance() {
        return this;
    }

    public double getMaxDistance() {
        return maxDistance;
    }

    public void setMaxDistance(double maxDistance) {
        this.maxDistance = maxDistance;
    }
}
