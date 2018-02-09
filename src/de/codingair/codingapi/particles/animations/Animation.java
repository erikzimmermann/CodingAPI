package de.codingair.codingapi.particles.animations;

import de.codingair.codingapi.API;
import de.codingair.codingapi.particles.Particle;
import de.codingair.codingapi.utils.Ticker;

public abstract class Animation implements Ticker {
    private Particle particle;

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

    public Particle getParticle() {
        return particle;
    }

    @Override
    public void onSecond() { }

    @Override
    public Object getInstance() {
        return this;
    }
}
