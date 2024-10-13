package de.codingair.codingapi.particles;

import org.bukkit.Material;

public class ParticleData {
    private final Material material;
    private final int data;
    private final int[] packetData;

    public ParticleData(Material material, int data) {
        this.material = material;
        this.data = data;
        //noinspection deprecation
        this.packetData = new int[]{material.getId(), data};
    }

    public Material getMaterial() {
        return material;
    }

    public int getData() {
        return data;
    }

    public int[] getPacketData() {
        return packetData;
    }
}
