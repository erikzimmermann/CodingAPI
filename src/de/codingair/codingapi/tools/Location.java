package de.codingair.codingapi.tools;

import de.codingair.codingapi.tools.io.JSON.JSON;
import de.codingair.codingapi.tools.io.JSON.JSONParser;
import de.codingair.codingapi.tools.io.utils.DataWriter;
import de.codingair.codingapi.tools.io.utils.Serializable;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.util.Vector;

public class Location extends org.bukkit.Location implements Serializable {
    private String worldName;

    public Location(String worldName, double x, double y, double z, float yaw, float pitch) {
        super(worldName == null ? null : Bukkit.getWorld(worldName), x, y, z, yaw, pitch);
        this.worldName = worldName;
    }

    public Location(String worldName, double x, double y, double z) {
        super(worldName == null ? null : Bukkit.getWorld(worldName), x, y, z, 0, 0);
        this.worldName = worldName;
    }

    public Location(World world, double x, double y, double z, float yaw, float pitch) {
        super(world, x, y, z, yaw, pitch);
        this.worldName = world == null ? null : world.getName();
    }

    public Location(World world, double x, double y, double z) {
        super(world, x, y, z, 0, 0);
        this.worldName = world == null ? null : world.getName();
    }

    public Location(Location location) {
        this();
        apply(location);
    }

    public Location(org.bukkit.Location location) {
        super(location.getWorld(), location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch());
        this.worldName = location instanceof Location ? ((Location) location).getWorldName() : location.getWorld() == null ? null : location.getWorld().getName();
    }

    public Location(JSON json) {
        super(json.get("World") == null ? null : Bukkit.getWorld((String) json.get("World")),
                json.getDouble("X"), json.getDouble("Y"), json.getDouble("Z"),
                json.getFloat("Yaw"), json.getFloat("Pitch"));
        this.worldName = json.getString("World");
    }

    public Location() {
        super(null, 0, 0, 0);
    }

    public boolean hasOnlyCoords() {
        return getYaw() == 0 && getPitch() == 0;
    }

    public String toJSONString() {
        return toJSON(2).toJSONString();
    }

    public JSON toJSON(int decimalPlaces) {
        JSON json = new JSON();

        json.put("World", this.worldName);
        json.put("X", trim(getX(), decimalPlaces));
        json.put("Y", trim(getY(), decimalPlaces));
        json.put("Z", trim(getZ(), decimalPlaces));
        json.put("Yaw", trim(getYaw(), decimalPlaces));
        json.put("Pitch", trim(getPitch(), decimalPlaces));

        return json;
    }

    @Override
    public boolean read(DataWriter d) throws Exception {
        this.worldName = d.getString("World");
        setWorld(this.worldName == null ? null : Bukkit.getWorld(this.worldName));
        setX(d.getDouble("X"));
        setY(d.getDouble("Y"));
        setZ(d.getDouble("Z"));
        setYaw(d.getFloat("Yaw"));
        setPitch(d.getFloat("Pitch"));

        return true;
    }

    @Override
    public void write(DataWriter json) {
        try {
            json.put("World", getWorld() == null ? this.worldName : getWorld().getName());
        } catch(Throwable t) {
            json.put("World", this.worldName);
        }
        json.put("X", trim(getX(), 4));
        json.put("Y", trim(getY(), 4));
        json.put("Z", trim(getZ(), 4));
        json.put("Yaw", trim(getYaw(), 4));
        json.put("Pitch", trim(getPitch(), 4));
    }

    @Override
    public void destroy() {
    }

    @Override
    public Location add(org.bukkit.Location vec) {
        return (Location) super.add(vec);
    }

    @Override
    public Location add(Vector vec) {
        return (Location) super.add(vec);
    }

    @Override
    public Location add(double x, double y, double z) {
        return (Location) super.add(x, y, z);
    }

    @Override
    public Location subtract(org.bukkit.Location vec) {
        return (Location) super.subtract(vec);
    }

    @Override
    public Location subtract(Vector vec) {
        return (Location) super.subtract(vec);
    }

    @Override
    public Location subtract(double x, double y, double z) {
        return (Location) super.subtract(x, y, z);
    }

    public Location trim(int decimalPlaces) {
        setX(trim(getX(), decimalPlaces));
        setY(trim(getY(), decimalPlaces));
        setZ(trim(getZ(), decimalPlaces));
        setYaw(trim(getYaw(), decimalPlaces));
        setPitch(trim(getPitch(), decimalPlaces));
        return this;
    }

    public void apply(org.bukkit.Location l) {
        setWorld(l.getWorld());
        if(l instanceof Location) setWorldName(((Location) l).getWorldName());
        setX(l.getX());
        setY(l.getY());
        setZ(l.getZ());
        setYaw(l.getYaw());
        setPitch(l.getPitch());
    }

    private double trim(double d, int decimalPlaces) {
        return ((double) (int) (d * Math.pow(10, decimalPlaces))) / Math.pow(10, decimalPlaces);
    }

    private float trim(float d, int decimalPlaces) {
        return (float) (((double) (int) (d * Math.pow(10, decimalPlaces))) / Math.pow(10, decimalPlaces));
    }

    public String toJSONString(int decimalPlaces) {
        return toJSON(decimalPlaces).toJSONString();
    }

    @Override
    public void setWorld(World world) {
        super.setWorld(world);
        if(world != null) this.worldName = world.getName();
    }

    public boolean equals(Object obj) {
        if(obj == null) {
            return false;
        } else if(!(obj instanceof org.bukkit.Location)) {
            return false;
        } else {
            org.bukkit.Location other = (org.bukkit.Location) obj;
            if(this.getWorld() != other.getWorld() && (this.getWorld() == null || !this.getWorld().equals(other.getWorld()))) {
                return false;
            } else if(Double.doubleToLongBits(this.getX()) != Double.doubleToLongBits(other.getX())) {
                return false;
            } else if(Double.doubleToLongBits(this.getY()) != Double.doubleToLongBits(other.getY())) {
                return false;
            } else if(Double.doubleToLongBits(this.getZ()) != Double.doubleToLongBits(other.getZ())) {
                return false;
            } else if(Float.floatToIntBits(this.getPitch()) != Float.floatToIntBits(other.getPitch())) {
                return false;
            } else {
                return Float.floatToIntBits(this.getYaw()) == Float.floatToIntBits(other.getYaw());
            }
        }
    }

    public boolean isEmpty() {
        return worldName == null && getWorld() == null && getX() == 0 && getY() == 0 && getZ() == 0 && getYaw() == 0 && getPitch() == 0;
    }

    @Override
    public Location clone() {
        return new Location(this);
    }

    public static Location getByJSONString(String jsonString) {
        if(jsonString == null) return null;

        try {
            return new Location((JSON) new JSONParser().parse(jsonString));
        } catch(Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static Location getByLocation(org.bukkit.Location location) {
        if(location == null) return null;

        return new Location(location);
    }

    public String getWorldName() {
        if(worldName == null && getWorld() != null) worldName = getWorld().getName();
        return worldName;
    }

    public void setWorldName(String worldName) {
        this.worldName = worldName;
        if(worldName != null) setWorld(Bukkit.getWorld(worldName));
    }
}
