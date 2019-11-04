package de.codingair.codingapi.tools;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

public class HitBox {
    private double lX, lY, lZ, hX, hY, hZ;

    public HitBox() {
    }

    public HitBox(HitBox box) {
        lX = box.lX;
        lY = box.lY;
        lZ = box.lZ;
        hX = box.hX;
        hY = box.hY;
        hZ = box.hZ;
    }

    public HitBox(Player player) {
        this(player.getLocation(), 0, player.getEyeHeight());
    }

    public HitBox(Location l, double width, double height) {

        this(l.getX() - width, l.getY(), l.getZ() - width, l.getX() + width, l.getY() + height, l.getZ() + width);
    }

    public HitBox(double x, double y, double z) {
        this(x, y, z, x, y, z);
    }

    public HitBox(double lX, double lY, double lZ, double hX, double hY, double hZ) {
        this.lX = lX;
        this.lY = lY;
        this.lZ = lZ;
        this.hX = hX;
        this.hY = hY;
        this.hZ = hZ;
    }

    public void addProperty(HitBox box) {
        lX = Math.min(lX, box.lX);
        lY = Math.min(lY, box.lY);
        lZ = Math.min(lZ, box.lZ);
        hX = Math.max(hX, box.hX);
        hY = Math.max(hY, box.hY);
        hZ = Math.max(hZ, box.hZ);
    }

    public void addProperty(double x, double y, double z) {
        lX = Math.min(lX, x);
        lY = Math.min(lY, y);
        lZ = Math.min(lZ, z);
        hX = Math.max(hX, x);
        hY = Math.max(hY, y);
        hZ = Math.max(hZ, z);
    }

    public boolean collides(HitBox box) {
        HitBox lower, higher;
        if(hX - lX >= box.hX - box.lX) {
            higher = this;
            lower = box;
        } else {
            higher = box;
            lower = this;
        }
        if((higher.lX <= lower.lX && higher.hX >= lower.lX) || (higher.lX <= lower.hX && higher.hX >= lower.hX) || (higher.lX > lower.lX && higher.hX < lower.hX)) {
            if(hY - lY >= box.hY - box.lY) {
                higher = this;
                lower = box;
            } else {
                higher = box;
                lower = this;
            }

            if((higher.lY <= lower.lY && higher.hY >= lower.lY) || (higher.lY <= lower.hY && higher.hY >= lower.hY) || (higher.lY > lower.lY && higher.hY < lower.hY)) {
                if(hZ - lZ >= box.hZ - box.lZ) {
                    higher = this;
                    lower = box;
                } else {
                    higher = box;
                    lower = this;
                }

                if((higher.lZ <= lower.lZ && higher.hZ >= lower.lZ) || (higher.lZ <= lower.hZ && higher.hZ >= lower.hZ) || (higher.lZ > lower.lZ && higher.hZ < lower.hZ)) {
                    return true;
                }
            }
        }

        return false;
    }

    public double getlX() {
        return lX;
    }

    public void setlX(double lX) {
        this.lX = lX;
    }

    public double getlY() {
        return lY;
    }

    public void setlY(double lY) {
        this.lY = lY;
    }

    public double getlZ() {
        return lZ;
    }

    public void setlZ(double lZ) {
        this.lZ = lZ;
    }

    public double gethX() {
        return hX;
    }

    public void sethX(double hX) {
        this.hX = hX;
    }

    public double gethY() {
        return hY;
    }

    public void sethY(double hY) {
        this.hY = hY;
    }

    public double gethZ() {
        return hZ;
    }

    public void sethZ(double hZ) {
        this.hZ = hZ;
    }
}
