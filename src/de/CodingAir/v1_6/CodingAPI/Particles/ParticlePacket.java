package de.CodingAir.v1_6.CodingAPI.Particles;

import de.CodingAir.v1_6.CodingAPI.Server.Reflections.IReflection;
import de.CodingAir.v1_6.CodingAPI.Server.Reflections.PacketUtils;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.awt.*;
import java.lang.reflect.Constructor;

/**
 * Removing of this disclaimer is forbidden.
 *
 * @author CodingAir
 * @verions: 1.0.0
 **/

public class ParticlePacket {
	private Particle particle;
	private Object packet;
	private Color color = null;
	private boolean longDistance = false;
	
	public ParticlePacket(Particle particle) {
		this.particle = particle;
	}
	
	public ParticlePacket(Particle particle, Color color, boolean longDistance) {
		this.particle = particle;
		this.color = color;
		this.longDistance = longDistance;
	}
	
	public ParticlePacket initialize(Location loc) {
		if(!available()) return this;
		
		Class<?> enumParticle = IReflection.getClass(IReflection.ServerPacket.MINECRAFT_PACKAGE, "EnumParticle");
		Class<?> packetClass = IReflection.getClass(IReflection.ServerPacket.MINECRAFT_PACKAGE, "PacketPlayOutWorldParticles");
		Constructor packetConstructor = IReflection.getConstructor(packetClass).getConstructor();
		
		ParticleData data = null;
		
		if(particle.requiresData()) {
			Location below = loc.clone();
			below.setY(loc.getBlockY() - 1);
			
			//noinspection deprecation
			data = new ParticleData(below.getBlock().getType(), below.getBlock().getData());
		}
		
		if(particle.requiresWater() && !loc.getBlock().getType().equals(Material.WATER) && !loc.getBlock().getType().equals(Material.STATIONARY_WATER)) return this;
		
		float e = 0, f = 0, g = 0, h = 0;
		int i = 1;
		if(particle.isColorable() && this.color != null) {
			e = this.color.getRed() / 255;
			
			if(e == 0) e = 0.003921569F;
			
			f = this.color.getGreen() / 255;
			g = this.color.getBlue() / 255;
			h = 1F;
			i = 0;
		}
		
		try {
			packet = packetConstructor.newInstance();
			
			IReflection.setValue(packet, "a", enumParticle.getEnumConstants()[particle.getId()]);
			IReflection.setValue(packet, "j", this.longDistance);
			
			if (data != null) {
				int[] packetData = data.getPacketData();
				IReflection.setValue(packet, "k", particle == Particle.ITEM_CRACK ? packetData : new int[] { packetData[0] | (packetData[1] << 12) });
			}
			
			IReflection.setValue(packet, "b", (float) loc.getX());
			IReflection.setValue(packet, "c", (float) loc.getY());
			IReflection.setValue(packet, "d", (float) loc.getZ());
			IReflection.setValue(packet, "e", e);
			IReflection.setValue(packet, "f", f);
			IReflection.setValue(packet, "g", g);
			IReflection.setValue(packet, "h", h);
			IReflection.setValue(packet, "i", i);
		} catch(Exception exception) {
			exception.printStackTrace();
		}
		
		return this;
	}
	
	public boolean available() {
		Class<?> enumParticle = IReflection.getClass(IReflection.ServerPacket.MINECRAFT_PACKAGE, "EnumParticle");
		return enumParticle.getEnumConstants().length - 1 >= this.particle.getId();
	}
	
	public void send(Player... p) {
		if(packet == null) return;
		PacketUtils.sendPacket(packet, p);
	}
	
	public void send() {
		if(packet == null) return;
		PacketUtils.sendPacketToAll(packet);
	}
	
	public Particle getParticle() {
		return particle;
	}
	
	public Object getPacket() {
		return packet;
	}
	
	public Color getColor() {
		return color;
	}
	
	public void setColor(Color color) {
		this.color = color;
	}
	
	@Override
	public ParticlePacket clone() {
		ParticlePacket packet = new ParticlePacket(this.particle);
		packet.setColor(this.color);
		packet.setLongDistance(this.longDistance);
		return packet;
	}
	
	public boolean isLongDistance() {
		return longDistance;
	}
	
	public void setLongDistance(boolean longDistance) {
		this.longDistance = longDistance;
	}
}
