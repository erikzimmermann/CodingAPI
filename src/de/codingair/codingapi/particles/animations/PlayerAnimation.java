package de.codingair.codingapi.particles.animations;

import de.codingair.codingapi.particles.Particle;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.List;

/**
 * Removing of this disclaimer is forbidden.
 *
 * @author codingair
 * @verions: 1.0.0
 **/

public abstract class PlayerAnimation extends Animation {
    private final static int standingTicks = 2;
    public static final int MAX_SPEED = 10;
    public static final int MIN_SPEED = 1;
    private static final int MAX_TICKS = 10;
    private static final int MIN_TICKS = 1;

    private boolean whileStanding;
    private int standing = 0;

    private int skipped = 0;
    private int delay = 0;
    private int speed = 0;

    private Player player;

    public PlayerAnimation(Particle particle, Player player, Plugin plugin) {
        super(particle);
        this.player = player;
        this.whileStanding = false;

        AnimationListener.register(plugin);
    }

    public PlayerAnimation(Particle particle, Player player, Plugin plugin, boolean whileStanding, int speed) {
        super(particle);
        this.player = player;
        this.whileStanding = whileStanding;
        setSpeed(speed);
        skipped = delay;
        this.speed = speed;

        AnimationListener.register(plugin);
    }

    @Override
    public void onTick() {
        if(delay > 0) {
            if(skipped < delay) {
                skipped++;
                return;
            } else if(skipped == delay) skipped = 0;
        }

        standing++;

        List<Location> loc = null;
        if(!whileStanding) loc = onDisplay();
        else if(isStanding()) loc = onDisplay();

        if(loc != null) {
            for(Location location : loc) {
                getParticle().send(location, false, player);
            }

            loc.clear();
        }
    }

    public abstract List<Location> onDisplay();

    public int getDelay() {
        return delay;
    }

    public void setSpeed(int speed) {
        if(speed < MIN_SPEED) speed = MIN_SPEED;
        if(speed > MAX_SPEED) speed = MAX_SPEED;
        this.delay = MAX_TICKS - speed;
    }

    public int getSpeed() {
        return speed;
    }

    public boolean isWhileStanding() {
        return whileStanding;
    }

    public Player getPlayer() {
        return player;
    }

    public boolean isStanding() {
        return standing > standingTicks;
    }

    public void setStandingTicks(int standing) {
        this.standing = standing;
    }
}
