package de.codingair.codingapi.particles.animations.standalone;

import de.codingair.codingapi.particles.Particle;
import de.codingair.codingapi.particles.animations.Animation;
import org.bukkit.Location;

public class SinusAnimation extends Animation {
    private Location location;

    private boolean minimize = true;
    private double radius;
    private final double fixedHeight;
    private double height;
    private double degrees = 0;
    private Location circleLocation;

    public SinusAnimation(Particle particle, Location location, double radius, double height) {
        super(particle);
        this.location = location;
        this.radius = radius;

        this.fixedHeight = height;
        this.height = fixedHeight;

        calculateStep();
    }

    private void calculateStep() {
        double dif = 0.25;
        double step = 0.1;

        if(minimize) {
            height -= step;
            if(height < fixedHeight - dif) minimize = false;
        } else {
            height += step;
            if(height > fixedHeight + dif) minimize = true;
        }

        this.circleLocation = this.location.clone().add(
                this.radius * Math.cos(this.degrees * Math.PI / 180),
                height,
                radius * Math.sin(this.degrees * Math.PI / 180));

        degrees += (12 - this.radius * 0.2);
        if(degrees >= 360) degrees -= 360;
        if(degrees < 0) degrees += 360;
    }

    @Override
    public void onTick() {
        sendParticle(this.circleLocation);
        calculateStep();
    }

    public Location getLocation() {
        return location;
    }

    public double getFixedHeight() {
        return fixedHeight;
    }

    public double getRadius() {
        return radius;
    }
}
