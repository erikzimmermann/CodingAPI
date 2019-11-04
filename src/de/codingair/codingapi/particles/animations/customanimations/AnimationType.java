package de.codingair.codingapi.particles.animations.customanimations;

import de.codingair.codingapi.particles.Particle;
import de.codingair.codingapi.particles.animations.movables.MovableMid;
import de.codingair.codingapi.particles.animations.movables.PlayerMid;
import de.codingair.codingapi.server.reflections.IReflection;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

public enum AnimationType {
    CIRCLE(0, CircleAnimation.class, "Circle"),
    PULSING_CIRCLE(1, PulsingCircleAnimation.class, "Pulsing Circle"),
    ROTATING_CIRCLE(2, RotatingCircleAnimation.class, "Rotating Circle"),
    SINUS(3, SinusAnimation.class, "Sinus"),
    VECTOR(4, VectorAnimation.class, "Vector"),
    LINE(5, LineAnimation.class, "Line"),
    ;

    private int id;
    private Class<? extends CustomAnimation> clazz;
    private String displayName;

    AnimationType(int id, Class<? extends CustomAnimation> clazz, String displayName) {
        this.id = id;
        this.clazz = clazz;
        this.displayName = displayName;
    }

    public CustomAnimation build(Particle particle, Player player, MovableMid mid, double radius, double height, int speed) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        return clazz.getConstructor(Particle.class, MovableMid.class, double.class, double.class, int.class)
                .newInstance(particle, mid, radius, height, speed).setViewer(player);
    }

    public int getId() {
        return id;
    }

    public Class<? extends CustomAnimation> getClazz() {
        return clazz;
    }

    public String getDisplayName() {
        return displayName;
    }

    public AnimationType next() {
        return next(id);
    }

    public AnimationType previous() {
        return previous(id);
    }

    @Override
    public String toString() {
        return displayName;
    }

    public static AnimationType getById(int id) {
        for(AnimationType value : values()) {
            if(value.getId() == id) return value;
        }

        throw new IllegalArgumentException("Couldn't found AnimationType with id=" + id);
    }

    public static AnimationType next(int id) {
        for(int i = 0; i < values().length; i++) {
            if(values()[i].getId() == id) return i + 1 == values().length ? values()[0] : values()[i + 1];
        }

        throw new IllegalArgumentException("Couldn't found AnimationType with id=" + id);
    }

    public static AnimationType previous(int id) {
        for(int i = 0; i < values().length; i++) {
            if(values()[i].getId() == id) {
                return i - 1 < 0 ? values()[values().length - 1] : values()[i - 1];
            }
        }

        throw new IllegalArgumentException("Couldn't found AnimationType with id=" + id);
    }

    public static void clearCache() {
        for(AnimationType value : values()) {
            IReflection.FieldAccessor cache = IReflection.getField(value.getClazz(), "CACHE");
            List<List<Location>> CACHE = ((List<List<Location>>) cache.get(null));

            for(List<Location> l : CACHE) {
                l.clear();
            }

            CACHE.clear();
        }
    }
}
