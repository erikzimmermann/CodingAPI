package de.codingair.codingapi.tools;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.util.Vector;
import de.codingair.codingapi.tools.JSON.JSONObject;
import org.json.simple.parser.JSONParser;

import java.text.DecimalFormat;

public class Location extends org.bukkit.Location {
    private String worldName;

    public Location(String worldName, double x, double y, double z, float yaw, float pitch) {
        super(Bukkit.getWorld(worldName), x, y, z, yaw, pitch);
        this.worldName = worldName;
    }

    public Location(org.bukkit.Location location) {
        super(location.getWorld(), location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch());
        this.worldName = location instanceof Location ? ((Location) location).getWorldName() : location.getWorld() == null ? null : location.getWorld().getName();
    }

    public Location(org.json.simple.JSONObject json) {
        super(json.get("World") == null ? null : Bukkit.getWorld((String) json.get("World")),
                Double.parseDouble(((String) json.get("X")).replace(",", ".")), Double.parseDouble(((String) json.get("Y")).replace(",", ".")), Double.parseDouble(((String) json.get("Z")).replace(",", ".")),
                json.get("Yaw") == null ? 0F : Float.parseFloat(((String) json.get("Yaw")).replace(",", ".")), json.get("Pitch") == null ? 0F : Float.parseFloat(((String) json.get("Pitch")).replace(",", ".")));
        this.worldName = json.get("World") == null ? null : (String) json.get("World");
    }

    public boolean hasOnlyCoords() {
        return getYaw() == 0 && getPitch() == 0;
    }

    public String toJSONString() {
        DecimalFormat format = new DecimalFormat("0.00");

        JSONObject json = new JSONObject();

        json.put("World", this.worldName);
        json.put("X", format.format(this.getX()).replace(",", "."));
        json.put("Y", format.format(this.getY()).replace(",", "."));
        json.put("Z", format.format(this.getZ()).replace(",", "."));

        if(!hasOnlyCoords()) {
            json.put("Yaw", format.format(this.getYaw()).replace(",", "."));
            json.put("Pitch", format.format(this.getPitch()).replace(",", "."));
        }

        return json.toJSONString();
    }

    public JSONObject toJSON(int decimalPlaces) {
        StringBuilder s = new StringBuilder("0" + (decimalPlaces > 0 ? "." : ""));
        for(int i = 0; i < decimalPlaces; i++) {
            s.append("0");
        }
        DecimalFormat format = new DecimalFormat(s.toString());

        JSONObject json = new JSONObject();

        if(decimalPlaces > 0) {
            json.put("World", this.worldName);
            json.put("X", format.format(this.getX()).replace(",", "."));
            json.put("Y", format.format(this.getY()).replace(",", "."));
            json.put("Z", format.format(this.getZ()).replace(",", "."));

            if(!hasOnlyCoords()) {
                json.put("Yaw", format.format(this.getYaw()).replace(",", "."));
                json.put("Pitch", format.format(this.getPitch()).replace(",", "."));
            }
        } else {
            json.put("World", this.worldName);
            json.put("X", (this.getX() + "").replace(",", "."));
            json.put("Y", (this.getY() + "").replace(",", "."));
            json.put("Z", (this.getZ() + "").replace(",", "."));

            if(!hasOnlyCoords()) {
                json.put("Yaw", (this.getYaw() + "").replace(",", "."));
                json.put("Pitch", (this.getPitch() + "").replace(",", "."));
            }
        }

        return json;
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

    public void trim(int decimalPlaces) {
        setX(trim(getX(), decimalPlaces));
        setY(trim(getY(), decimalPlaces));
        setZ(trim(getZ(), decimalPlaces));
        setYaw(trim(getYaw(), decimalPlaces));
        setPitch(trim(getPitch(), decimalPlaces));
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
        this.worldName = world == null ? null : world.getName();
    }

    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        } else if (!(obj instanceof org.bukkit.Location)) {
            return false;
        } else {
            org.bukkit.Location other = (org.bukkit.Location)obj;
            if (this.getWorld() != other.getWorld() && (this.getWorld() == null || !this.getWorld().equals(other.getWorld()))) {
                return false;
            } else if (Double.doubleToLongBits(this.getX()) != Double.doubleToLongBits(other.getX())) {
                return false;
            } else if (Double.doubleToLongBits(this.getY()) != Double.doubleToLongBits(other.getY())) {
                return false;
            } else if (Double.doubleToLongBits(this.getZ()) != Double.doubleToLongBits(other.getZ())) {
                return false;
            } else if (Float.floatToIntBits(this.getPitch()) != Float.floatToIntBits(other.getPitch())) {
                return false;
            } else {
                return Float.floatToIntBits(this.getYaw()) == Float.floatToIntBits(other.getYaw());
            }
        }
    }

    @Override
    public Location clone() {
        return new Location(this);
    }

    public static Location getByJSONString(String jsonString) {
        if(jsonString == null) return null;

        try {
            return new Location((org.json.simple.JSONObject) new JSONParser().parse(jsonString));
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
        return worldName;
    }
}
