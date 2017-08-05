package de.CodingAir.v1_6.CodingAPI.Particles;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.List;

/**
 * Removing of this disclaimer is forbidden.
 *
 * @author CodingAir
 * @verions: 1.0.0
 **/

public enum Particle {
	EXPLOSION_NORMAL("explode", 0),
	EXPLOSION_LARGE("largeexplode", 1),
	EXPLOSION_HUGE("hugeexplosion", 2),
	FIREWORKS_SPARK("fireworksSpark", 3),
	WATER_BUBBLE("bubble", 4, ParticleProperty.REQUIRES_WATER),
	WATER_SPLASH("splash", 5),
	WATER_WAKE("wake", 6),
	SUSPENDED("suspended", 7, ParticleProperty.REQUIRES_WATER),
	SUSPENDED_DEPTH("depthsuspend", 8),
	CRIT("crit", 9),
	CRIT_MAGIC("magicCrit", 10),
	SMOKE_NORMAL("smoke", 11),
	SMOKE_LARGE("largesmoke", 12),
	SPELL("spell", 13),
	SPELL_INSTANT("instantSpell", 14),
	SPELL_MOB("mobSpell", 15, ParticleProperty.COLORABLE),
	SPELL_MOB_AMBIENT("mobSpellAmbient", 16, ParticleProperty.COLORABLE),
	SPELL_WITCH("witchMagic", 17),
	DRIP_WATER("dripWater", 18),
	DRIP_LAVA("dripLava", 19),
	VILLAGER_ANGRY("angryVillager", 20),
	VILLAGER_HAPPY("happyVillager", 21),
	TOWN_AURA("townaura", 22),
	NOTE("note", 23, ParticleProperty.COLORABLE),
	PORTAL("portal", 24),
	ENCHANTMENT_TABLE("enchantmenttable", 25),
	FLAME("flame", 26),
	LAVA("lava", 27),
	FOOTSTEP("footstep", 28),
	CLOUD("cloud", 29),
	REDSTONE("reddust", 30, ParticleProperty.COLORABLE),
	SNOWBALL("snowballpoof", 31),
	SNOW_SHOVEL("snowshovel", 32),
	SLIME("slime", 33),
	HEART("heart", 34),
	BARRIER("barrier", 35),
	ITEM_CRACK("iconcrack", 36, ParticleProperty.REQUIRES_DATA),
	BLOCK_CRACK("blockcrack", 37, ParticleProperty.REQUIRES_DATA),
	BLOCK_DUST("blockdust", 38, ParticleProperty.REQUIRES_DATA),
	WATER_DROP("droplet", 39),
	ITEM_TAKE("take", 40),
	MOB_APPEARANCE("mobappearance", 41),
	DRAGON_BREATH("dragonbreath", 42),
	END_ROD("endRod", 43),
	DAMAGE_INDICATOR("damageIndicator", 44),
	SWEEP_ATTACK("sweepAttack", 45),
	FALLING_DUST("fallingdust", 46);
	
	private String name;
	private int id;
	private List<ParticleProperty> properties = null;
	
	Particle(String name, int id) {
		this.name = name;
		this.id = id;
	}
	
	Particle(String name, int id, ParticleProperty... property) {
		this.name = name;
		this.id = id;
		properties = Arrays.asList(property);
	}
	
	public boolean isColorable() {
		if(properties == null) return false;
		return properties.contains(ParticleProperty.COLORABLE);
	}
	
	public boolean requiresData() {
		if(properties == null) return false;
		return properties.contains(ParticleProperty.REQUIRES_DATA);
	}
	
	public boolean requiresWater() {
		if(properties == null) return false;
		return properties.contains(ParticleProperty.REQUIRES_WATER);
	}
	
	public String getName() {
		return name;
	}
	
	public int getId() {
		return id;
	}
	
	public List<ParticleProperty> getProperties() {
		return properties;
	}
	
	public void send(Location loc) {
		ParticlePacket packet = new ParticlePacket(this);
		if(packet.available()) {
			packet.initialize(loc);
			packet.send();
		}
	}
	
	public void send(Location loc, Player... players) {
		ParticlePacket packet = new ParticlePacket(this);
		if(packet.available()) {
			packet.initialize(loc);
			
			for(Player player : players) {
				packet.send(player);
			}
		}
	}
	
	public ParticlePacket getParticlePacket(Location loc) {
		ParticlePacket packet = new ParticlePacket(this);
		packet.initialize(loc);
		return packet;
	}
	
	public static Particle getById(int id) {
		for(Particle particle : Particle.values()) {
			if(particle.getId() == id) return particle;
		}
		
		return null;
	}
}
