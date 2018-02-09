package de.codingair.codingapi.particles;

import org.bukkit.Material;

/**
 * Removing of this disclaimer is forbidden.
 *
 * @author codingair
 * @verions: 1.0.0
 **/

public class ParticleData {
	private Material material;
	private int data;
	private int[] packetData;
	
	public ParticleData(Material material, int data) {
		this.material = material;
		this.data = data;
		//noinspection deprecation
		this.packetData = new int[]{ material.getId(), data };
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
