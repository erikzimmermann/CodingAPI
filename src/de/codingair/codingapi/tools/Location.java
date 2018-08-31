package de.codingair.codingapi.tools;

import org.bukkit.Bukkit;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.text.DecimalFormat;

public class Location extends org.bukkit.Location {

    public Location(org.bukkit.Location location) {
        super(location.getWorld(), location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch());
    }

    public Location(JSONObject json) {
        super(Bukkit.getWorld((String) json.get("World")),
                Double.parseDouble(((String) json.get("X")).replace(",", ".")), Double.parseDouble(((String) json.get("Y")).replace(",", ".")), Double.parseDouble(((String) json.get("Z")).replace(",", ".")),
                json.get("Yaw") == null ? 0F : Float.parseFloat(((String) json.get("Yaw")).replace(",", ".")), json.get("Pitch") == null ? 0F : Float.parseFloat(((String) json.get("Pitch")).replace(",", ".")));
    }

    public boolean hasOnlyCoords() {
        return getYaw() == 0 && getPitch() == 0;
    }

    public String toJSONString() {
        DecimalFormat format = new DecimalFormat("0.00");

        JSONObject json = new JSONObject();

        json.put("World", this.getWorld().getName());
        json.put("X", format.format(this.getX()).replace(",", "."));
        json.put("Y", format.format(this.getY()).replace(",", "."));
        json.put("Z", format.format(this.getZ()).replace(",", "."));

        if(!hasOnlyCoords()) {
            json.put("Yaw", format.format(this.getYaw()).replace(",", "."));
            json.put("Pitch", format.format(this.getPitch()).replace(",", "."));
        }

        return json.toJSONString();
    }

    public String toJSONString(int decimalPlaces) {
        if(getWorld() == null) return null;
        StringBuilder s = new StringBuilder("0" + (decimalPlaces > 0 ? "." : ""));
        for(int i = 0; i < decimalPlaces; i++) {
            s.append("0");
        }
        DecimalFormat format = new DecimalFormat(s.toString());

        JSONObject json = new JSONObject();

        if(decimalPlaces > 0) {
            json.put("World", this.getWorld().getName());
            json.put("X", format.format(this.getX()).replace(",", "."));
            json.put("Y", format.format(this.getY()).replace(",", "."));
            json.put("Z", format.format(this.getZ()).replace(",", "."));

            if(!hasOnlyCoords()) {
                json.put("Yaw", format.format(this.getYaw()).replace(",", "."));
                json.put("Pitch", format.format(this.getPitch()).replace(",", "."));
            }
        } else {
            json.put("World", this.getWorld().getName());
            json.put("X", (this.getX() + "").replace(",", "."));
            json.put("Y", (this.getY() + "").replace(",", "."));
            json.put("Z", (this.getZ() + "").replace(",", "."));

            if(!hasOnlyCoords()) {
                json.put("Yaw", (this.getYaw() + "").replace(",", "."));
                json.put("Pitch", (this.getPitch() + "").replace(",", "."));
            }
        }

        return json.toJSONString();
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
            return new Location((JSONObject) new JSONParser().parse(jsonString));
        } catch(Exception e) {
            return null;
        }
    }

    public static Location getByLocation(org.bukkit.Location location) {
        if(location == null) return null;

        return new Location(location);
    }
}
