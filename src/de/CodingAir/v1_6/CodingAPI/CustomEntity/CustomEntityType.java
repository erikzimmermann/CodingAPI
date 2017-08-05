package de.CodingAir.v1_6.CodingAPI.CustomEntity;

import de.CodingAir.v1_6.CodingAPI.Server.Version;
import net.minecraft.server.v1_11_R1.Entity;
import net.minecraft.server.v1_11_R1.EntityTypes;
import net.minecraft.server.v1_11_R1.MinecraftKey;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;

public class CustomEntityType {
	private static List<CustomEntityData> data = new ArrayList<>();
	
	public static void addData(CustomEntityData data){
		if(!CustomEntityType.data.contains(data)) CustomEntityType.data.add(data);
	}
	
	public static void registerEntities() {
		for(CustomEntityData type : data){
			
			String name = type.getName();
			int id = type.getID();
			Class<?> nmsClass = type.getNMSClass();
			Class<?> customClass = type.getCustomClass();
			
			try{
				
				List<Map<?, ?>> dataMap = new ArrayList<>();
				for(Field f : Class.forName("net.minecraft.server." + Version.getVersion().getVersionName() + ".EntityTypes").getDeclaredFields()){
					if(f.getType().getSimpleName().equals(Map.class.getSimpleName())){
						f.setAccessible(true);
						dataMap.add((Map<?, ?>) f.get(null));
					}
				}
				
				if(dataMap.get(2).containsKey(id)){
					dataMap.get(0).remove(name);
					dataMap.get(2).remove(id);
				}
				
				Method method = Class.forName("net.minecraft.server." + Version.getVersion().getVersionName() + ".EntityTypes").getDeclaredMethod("a", Class.class, String.class, int.class);
				method.setAccessible(true);
				method.invoke(null, customClass, name, id);
				
			} catch(Exception e) {
				
				try {
					EntityTypes.b.a(id, new MinecraftKey(name), (Class<? extends Entity>) nmsClass);
				} catch(Exception ex){
					ex.printStackTrace();
				}
				
			}
		}
	}
	
	public static void unregisterEntities() {
		for(CustomEntityData entity : data){
			try{
				((Map) getPrivateStatic(Class.forName("net.minecraft.server." + Version.getVersion().getVersionName() + ".EntityTypes"), "d")).remove(entity.getCustomClass());
			} catch(Exception e) {
				try{
					((HashSet) getPrivateStatic(Class.forName("net.minecraft.server." + Version.getVersion().getVersionName() + ".EntityTypes"), "d")).remove(entity.getCustomClass());
				} catch(Exception ex) {
					ex.printStackTrace();
				}
			}
			
			try{
				((Map) getPrivateStatic(Class.forName("net.minecraft.server." + Version.getVersion().getVersionName() + ".EntityTypes"), "f")).remove(entity.getCustomClass());
			} catch(Exception e) {
				try{
					((HashSet) getPrivateStatic(Class.forName("net.minecraft.server." + Version.getVersion().getVersionName() + ".EntityTypes"), "d")).remove(entity.getCustomClass());
				} catch(Exception ex) {
					ex.printStackTrace();
				}
			}
		}
		
		for(CustomEntityData entity : data){
			try{
				a(entity.getNMSClass(), entity.getName(), entity.getID());
			} catch(Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * A convenience method.
	 *
	 * @param clazz The class.
	 * @param f     The string representation of the private static field.
	 * @return The object found
	 * @throws Exception if unable to get the object.
	 */
	private static Object getPrivateStatic(Class clazz, String f) throws Exception {
		Field field = clazz.getDeclaredField(f);
		field.setAccessible(true);
		return field.get(null);
	}
	
	private static void a(Class paramClass, String paramString, int paramInt) {
		try{
			((Map) getPrivateStatic(Class.forName("net.minecraft.server." + Version.getVersion().getVersionName() + ".EntityTypes"), "c")).put(paramString, paramClass);
			((Map) getPrivateStatic(Class.forName("net.minecraft.server." + Version.getVersion().getVersionName() + ".EntityTypes"), "d")).put(paramClass, paramString);
			((Map) getPrivateStatic(Class.forName("net.minecraft.server." + Version.getVersion().getVersionName() + ".EntityTypes"), "e")).put(Integer.valueOf(paramInt), paramClass);
			((Map) getPrivateStatic(Class.forName("net.minecraft.server." + Version.getVersion().getVersionName() + ".EntityTypes"), "f")).put(paramClass, Integer.valueOf(paramInt));
			((Map) getPrivateStatic(Class.forName("net.minecraft.server." + Version.getVersion().getVersionName() + ".EntityTypes"), "g")).put(paramString, Integer.valueOf(paramInt));
		} catch(Exception exc) {
		}
	}
}