package de.codingair.codingapi.particles.animations.standalone;

import de.codingair.codingapi.particles.Particle;
import de.codingair.codingapi.particles.animations.Animation;
import org.bukkit.Location;
import org.bukkit.util.Vector;

public class VectorAnimation extends Animation {
    private Location start;
    private Vector direction;
    private int pos = 0;
    private int length;

    private int repetitions = -1;
    private int runs = 0;

    private int ticksBetweenParticles = 0;
    private int skippedTicks = 0;

    public VectorAnimation(Particle particle, Location start, Vector direction, int length) {
        super(particle);
        this.start = start;
        this.direction = direction;
        this.length = length;
    }

    public VectorAnimation(Particle particle, Location start, Vector direction, int length, int ticksBetweenParticles) {
        super(particle);
        this.start = start;
        this.direction = direction;
        this.length = length;
        this.ticksBetweenParticles = ticksBetweenParticles;
    }

    public VectorAnimation(Particle particle, Location start, Vector direction, int pos, int length, int repetitions, int ticksBetweenParticles) {
        super(particle);
        this.start = start;
        this.direction = direction;
        this.pos = pos;
        this.length = length;
        this.repetitions = repetitions;
        this.ticksBetweenParticles = ticksBetweenParticles;
    }

    @Override
    public void onTick() {
        if(ticksBetweenParticles > 0 && skippedTicks < ticksBetweenParticles) {
            skippedTicks++;
            return;
        } else if(ticksBetweenParticles > 0 && skippedTicks == ticksBetweenParticles) skippedTicks = 0;

        if(pos > length) {
            runs++;

            if(repetitions < 0 || runs < repetitions) pos = 0;
            else if(runs >= repetitions) {
                pos = 0;
                runs = 0;

                setRunning(false);
                return;
            }
        }

        Location loc = start.clone().add(this.direction.clone().multiply(this.pos));
        sendParticle(loc);

        pos++;
    }

    public Location getStart() {
        return start;
    }

    public Vector getDirection() {
        return direction;
    }

    public int getLength() {
        return length;
    }
}
