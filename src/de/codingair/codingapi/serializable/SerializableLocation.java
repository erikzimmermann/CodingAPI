package de.codingair.codingapi.serializable;

import de.codingair.codingapi.tools.Location;

import java.io.Serializable;

public class SerializableLocation implements Serializable {
    String data;

    public SerializableLocation() {
    }

    public SerializableLocation(org.bukkit.Location location) {
        this.data = Location.getByLocation(location).toJSONString(4);
    }

    public String getData() {
        return data;
    }

    public org.bukkit.Location getLocation() {
        return Location.getByJSONString(this.data);
    }
}
