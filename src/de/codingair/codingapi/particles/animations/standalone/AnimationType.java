package de.codingair.codingapi.particles.animations.standalone;

import de.codingair.codingapi.particles.animations.Animation;

public enum AnimationType {
    CIRCLE(CircleAnimation.class, de.codingair.codingapi.particles.animations.customanimations.AnimationType.CIRCLE),
    ROTATING_CIRCLE(RotatingCircleAnimation.class, de.codingair.codingapi.particles.animations.customanimations.AnimationType.ROTATING_CIRCLE),
    PULSING_CIRCLE(PulsingCircleAnimation.class, de.codingair.codingapi.particles.animations.customanimations.AnimationType.PULSING_CIRCLE),
    SINUS(SinusAnimation.class, de.codingair.codingapi.particles.animations.customanimations.AnimationType.SINUS),
    VECTOR(VectorAnimation.class, de.codingair.codingapi.particles.animations.customanimations.AnimationType.VECTOR);

    private Class<? extends Animation> clazz;
    private de.codingair.codingapi.particles.animations.customanimations.AnimationType custom;

    AnimationType(Class<? extends Animation> clazz, de.codingair.codingapi.particles.animations.customanimations.AnimationType custom) {
        this.clazz = clazz;
        this.custom = custom;
    }

    public Class<? extends Animation> getClazz() {
        return clazz;
    }

    public de.codingair.codingapi.particles.animations.customanimations.AnimationType getCustom() {
        return custom;
    }
}
