package de.codingair.codingapi.serializable;

import org.json.simple.JSONObject;

import java.text.DecimalFormat;

public class SerializableLocationHelper {
    public static SerializableLocation buildSerializableLocation(String world, double x, double y, double z, float yaw, float pitch) {
        DecimalFormat format = new DecimalFormat("0.0000");

        JSONObject json = new JSONObject();

        json.put("World", world);
        json.put("X", format.format(x).replace(",", "."));
        json.put("Y", format.format(y).replace(",", "."));
        json.put("Z", format.format(z).replace(",", "."));
        json.put("Yaw", format.format(yaw).replace(",", "."));
        json.put("Pitch", format.format(pitch).replace(",", "."));

        SerializableLocation loc = new SerializableLocation();
        loc.data = json.toJSONString();
        return loc;
    }

    public static SerializableLocation buildSerializableLocation(String data) {
        SerializableLocation loc = new SerializableLocation();
        loc.data = data;
        return loc;
    }
}
