package de.codingair.codingapi.particles.animations.standalone;

import de.codingair.codingapi.particles.animations.Animation;

public enum AnimationType {
    CIRCLE(CircleAnimation.class),
    ROTATING_CIRCLE(RotatingCircleAnimation.class),
    PULSING_CIRCLE(PulsingCircleAnimation.class),
    SINUS(SinusAnimation.class),
    VECTOR(VectorAnimation.class);

    private Class<? extends Animation> clazz;

    AnimationType(Class<? extends Animation> clazz) {
        this.clazz = clazz;
    }

    public Class<? extends Animation> getClazz() {
        return clazz;
    }
}
