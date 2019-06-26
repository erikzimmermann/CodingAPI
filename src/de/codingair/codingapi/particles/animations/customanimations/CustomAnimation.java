package de.codingair.codingapi.particles.animations.customanimations;

import de.codingair.codingapi.particles.Particle;
import de.codingair.codingapi.particles.animations.Animation;
import de.codingair.codingapi.particles.animations.movables.LocationMid;
import de.codingair.codingapi.particles.animations.movables.MovableMid;
import de.codingair.codingapi.particles.utils.Color;
import de.codingair.codingapi.tools.HitBox;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public abstract class CustomAnimation extends Animation {
    public static final int MAX_SPEED = 10;
    public static final int MIN_SPEED = 1;
    private static final int MAX_TICKS = 10;
    private static final int MIN_TICKS = 1;

    static final double CALCULATE_RADIUS = 2;
    static final double MAX_STANDARD_RADIUS = 3;

    private List<List<Location>> CACHE = new ArrayList<>();
    private int playId = 0;

    private double radius, height;
    private int xRotation = 0, yRotation = 0, zRotation = 0;
    private double sinX, sinY, sinZ, cosX, cosY, cosZ;
    private boolean calculateSinCos = true;

    private int skipped = 0;
    private int delay;
    private int speed = 0;

    private Color color;
    private int rainbow = 0;

    private MovableMid mid;
    private Player viewer;
    private Location zero;

    public CustomAnimation(Particle particle, MovableMid mid, double radius, double height, int speed) {
        super(particle);
        this.mid = mid;
        this.radius = radius;
        this.height = height;
        setSpeed(speed);

        this.zero = mid.getLocation();
        this.zero.setX(0);
        this.zero.setY(0);
        this.zero.setZ(0);
    }

    public CustomAnimation(Particle particle, Location mid, double radius, double height, int speed) {
        super(particle);
        this.mid = new LocationMid(mid);
        this.radius = radius;
        this.height = height;
        setSpeed(speed);

        this.zero = mid.clone();
        this.zero.setX(0);
        this.zero.setY(0);
        this.zero.setZ(0);
    }

    private void cache() {
        if(!useOwnCache()) return;
        if(!CACHE.isEmpty()) CACHE.clear();

        List<List<Location>> calculated = calculate(false);
        CACHE.addAll(calculated);
        calculated.clear();
    }

    private boolean useOwnCache() {
        return radius > MAX_STANDARD_RADIUS;
    }

    private void rotate(Location l) {
        if(calculateSinCos) {
            double rX = ((double) xRotation) * Math.PI / 180D;
            double rY = ((double) yRotation) * Math.PI / 180D;
            double rZ = ((double) zRotation) * Math.PI / 180D;

            sinX = Math.sin(rX);
            sinY = Math.sin(rY);
            sinZ = Math.sin(rZ);
            cosX = Math.cos(rX);
            cosY = Math.cos(rY);
            cosZ = Math.cos(rZ);

            calculateSinCos = false;
        }

        double x, y, z;

        //um x achse
        y = l.getY();
        z = l.getZ();
        l.setY(y * cosX - z * sinX);
        l.setZ(z * cosX + y * sinX);

        //um z Achse
        x = l.getX();
        y = l.getY();
        l.setX(x * cosZ - y * sinZ);
        l.setY(y * cosZ + x * sinZ);

        //um y achse
        x = l.getX();
        z = l.getZ();
        l.setX(x * cosY - z * sinY);
        l.setZ(z * cosY + x * sinY);
    }

    private void adjustLocations(List<Location> locations) {
        boolean rotation = xRotation != 0 || yRotation != 0 || zRotation != 0;
        Location mid = this.mid.getLocation();
        if(mid == null) {
            setRunning(false);
            return;
        }

        for(Location location : locations) {
            if(rotation) rotate(location);

            if(!useOwnCache()) location.multiply(getRadius() / CALCULATE_RADIUS);
            location.add(mid.getX(), mid.getY() + getHeight(), mid.getZ());
        }
    }

    @Override
    public void onTick() {
        if(delay > 0) {
            if(skipped < delay) {
                skipped++;

                this.mid.onTick();
                return;
            } else if(skipped == delay) skipped = 0;
        }

        this.mid.onTick();
        if(!this.mid.isStanding()) return;

        List<Location> locations = getCache().size() <= playId ? null : (get(isMotionAnimation() ? playId++ : 0));
        if(locations == null) {
            playId = 0;
            locations = get(playId++);
        }
        if(locations == null) {
            setRunning(false);
            throw new NullPointerException("No particle locations available!");
        }
        locations = copy(locations);

        adjustLocations(locations);

        if(locations != null) {
            if(viewer == null) {
                for(Location location : locations) {
                    getParticle().send(location, color == null ? null : (color == Color.RAINBOW ? Color.values()[rainbow++].getColor() : color.getColor()), false);
                    if(rainbow == Color.RAINBOW_COLOR_LENGTH) rainbow = 0;
                }
            } else {
                for(Location location : locations) {
                    getParticle().send(location, color == null ? null : (color == Color.RAINBOW ? Color.values()[rainbow++].getColor() : color.getColor()), false, viewer);
                    if(rainbow == Color.RAINBOW_COLOR_LENGTH) rainbow = 0;
                }
            }

            locations.clear();
        }
    }

    @Override
    public void setRunning(boolean running) {
        if(isRunning() == running) return;
        if(!running) CACHE.clear();
        else cache();
        super.setRunning(running);
    }

    public abstract List<List<Location>> calculate(boolean cached);

    public abstract boolean isMotionAnimation();

    private List<Location> get(int playId) {
        return getCache().size() <= playId ? null : getCache().get(playId);
    }

    abstract List<List<Location>> getAnimCache();

    private List<List<Location>> getCache() {
        if(useOwnCache()) {
            return CACHE;
        } else if(getAnimCache().isEmpty()) {
            calculate(true);
        }

        return getAnimCache();
    }

    public HitBox getHitBox() {
        List<List<Location>> cache = getCache();
        HitBox box = null;

        for(List<Location> locations : cache) {
            List<Location> copy = copy(locations);
            adjustLocations(copy);

            for(Location l : copy) {
                if(box == null) box = new HitBox(l.getX(), l.getY(), l.getZ());
                else box.addProperty(l.getX(), l.getY(), l.getZ());
            }

            copy.clear();
        }

        return box;
    }

    public double getRadius() {
        return radius;
    }

    public double getHeight() {
        return height;
    }

    public int getXRotation() {
        return xRotation;
    }

    public CustomAnimation setXRotation(int xRotation) {
        this.xRotation = xRotation;
        calculateSinCos = true;
        return this;
    }

    public int getYRotation() {
        return yRotation;
    }

    public CustomAnimation setYRotation(int yRotation) {
        this.yRotation = yRotation;
        calculateSinCos = true;
        return this;
    }

    public int getZRotation() {
        return zRotation;
    }

    public CustomAnimation setZRotation(int zRotation) {
        this.zRotation = zRotation;
        calculateSinCos = true;
        return this;
    }

    public int getDelay() {
        return delay;
    }

    public void setSpeed(int speed) {
        if(speed < MIN_SPEED) speed = MIN_SPEED;
        if(speed > MAX_SPEED) speed = MAX_SPEED;
        this.delay = MAX_TICKS - speed;
        this.speed = speed;
    }

    public int getSpeed() {
        return speed;
    }

    public Color getColor() {
        return color;
    }

    public <C extends CustomAnimation> C setColor(Color color) {
        this.color = color;
        return (C) this;
    }

    public Player getViewer() {
        return viewer;
    }

    public CustomAnimation setViewer(Player viewer) {
        this.viewer = viewer;
        return this;
    }

    public Location getZero() {
        return this.zero.clone();
    }

    public List<Location> copy(List<Location> cache) {
        List<Location> copy = new ArrayList<>();

        for(Location l : cache) {
            copy.add(l.clone());
        }

        return copy;
    }

    public void clear(List<List<Location>> cache) {
        if(cache == null) return;

        for(List<Location> locations : cache) {
            locations.clear();
        }

        cache.clear();
    }
}
