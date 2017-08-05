package de.CodingAir.v1_6.CodingAPI.CustomEntity;

import org.bukkit.entity.EntityType;

public class CustomEntityData {
	private String name;
	private int id;
	private EntityType type;
	private Class<?> nmsClass;
	private Class<?> customClass;
	
	public CustomEntityData(String name, int id, EntityType type, Class<?> nmsClass, Class<?> customClass) {
		this.name = name;
		this.id = id;
		this.type = type;
		this.nmsClass = nmsClass;
		this.customClass = customClass;
	}
	
	public String getName() {
		return name;
	}
	
	public int getID() {
		return id;
	}
	
	public EntityType getType() {
		return type;
	}
	
	public Class<?> getNMSClass() {
		return nmsClass;
	}
	
	public Class<?> getCustomClass() {
		return customClass;
	}
}
