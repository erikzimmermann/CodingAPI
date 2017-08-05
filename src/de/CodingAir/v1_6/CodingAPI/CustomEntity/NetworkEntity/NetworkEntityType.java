package de.CodingAir.v1_6.CodingAPI.CustomEntity.NetworkEntity;

import de.CodingAir.v1_6.CodingAPI.Server.Version;

/**
 * Removing of this disclaimer is forbidden.
 *
 * @author CodingAir
 * @verions: 1.0.0
 **/

public enum NetworkEntityType {
	DROPPED_ITEM("EntityItem", 2, false),
	AREA_AFFECT_CLOUD("EntityAreaEffectCloud", 3, false, Version.v1_9),
	ARMOR_STAND("EntityArmorStand", 78, false, Version.v1_8),
	ENDER_CRYSTAL("EntityEnderCrystal", 51, false),
	ELDER_GUARDIAN("EntityGuardianElder", 4, true, Version.v1_11),
	WITHER_SKELETON("EntitySkeletonWither", 5, true, Version.v1_11),
	STRAY("EntitySkeletonStray", 6, true, Version.v1_11),
	HUSK("EntityZombieHusk", 23, true, Version.v1_11),
	ZOMBIE_VILLAGER("EntityZombieVillager", 23, true, Version.v1_11),
	EVOKER("EntityEvoker", 34, true, Version.v1_11),
	VEX("EntityVex", 35, true, Version.v1_11),
	VINDICATOR("EntityVindicator", 36, true, Version.v1_11),
	ILLUSIONER("EntityIllagerIllusioner", 37, true, Version.v1_12),
	CREEPER("EntityCreeper", 50, true),
	SKELETON("EntitySkeleton", 51, true),
	SPIDER("EntitySpider", 52, true),
	GIANT("EntityGiantZombie", 53, true),
	ZOMBIE("EntityZombie", 54, true),
	SLIME("EntitySlime", 55, true),
	GHAST("EntityGhast", 56, true),
	ZOMBIE_PIGMAN("EntityPigZombie", 57, true),
	ENDERMAN("EntityEnderman", 58, true),
	CAVE_SPIDER("EntityCaveSpider", 59, true),
	SILVERFISH("EntitySilverfish", 60, true),
	BLAZE("EntityBlaze", 61, true),
	MAGMA_CUBE("EntityMagmaCube", 62, true),
	ENDER_DRAGON("EntityEnderDragon", 63, true),
	WITHER("EntityWither", 64, true),
	WITCH("EntityWitch", 66, true),
	ENDERMITE("EntityEndermite", 67, true),
	GUARDIAN("EntityGuardian", 68, true, Version.v1_8),
	SHULKER("EntityShulker", 69, true, Version.v1_9),
	SKELETON_HORSE("EntityHorseSkeleton", 28, true, Version.v1_11),
	ZOMBIE_HORSE("EntityHorseZombie", 29, true, Version.v1_11),
	DONKEY("EntityHorseDonkey", 31, true, Version.v1_11),
	MULE("EntityHorseMule", 32, true, Version.v1_11),
	BAT("EntityBat", 65, true),
	PIG("EntityPig", 90, true),
	SHEEP("EntitySheep", 91, true),
	COW("EntityCow", 92, true),
	CHICKEN("EntityChicken", 93, true),
	SQUID("EntitySquid", 94, true),
	WOLF("EntityWolf", 95, true),
	MOOSHROOM("EntityMushroomCow", 96, true),
	SNOW_GOLEM("EntityGolem", 97, true),
	OCELOT("EntityOcelot", 98, true),
	IRON_GOLEM("EntityIronGolem", 99, true),
	HORSE("EntityHorse", 100, true),
	RABBIT("EntityRabbit", 101, true, Version.v1_8),
	POLAR_BEAR("EntityPolarBear", 102, true, Version.v1_10),
	LLAMA("EntityLlama", 103, true, Version.v1_11),
	PARROT("EntityParrot", 105, true, Version.v1_12),
	VILLAGER("EntityVillager", 120, true);
	
	private String nmsClassName;
	private int id;
	private boolean isLivingEntity;
	private Version released;
	
	NetworkEntityType(String nmsClassName, int id, boolean isLivingEntity) {
		this.nmsClassName = nmsClassName;
		this.id = id;
		this.isLivingEntity = isLivingEntity;
		this.released = Version.v1_7;
	}
	
	NetworkEntityType(String nmsClassName, int id, boolean isLivingEntity, Version released) {
		this.nmsClassName = nmsClassName;
		this.id = id;
		this.isLivingEntity = isLivingEntity;
		this.released = released;
	}
	
	public String getNmsClassName() {
		return nmsClassName;
	}
	
	public int getId() {
		return id;
	}
	
	public boolean isLivingEntity() {
		return isLivingEntity;
	}
	
	public Version getReleased() {
		return released;
	}
}
