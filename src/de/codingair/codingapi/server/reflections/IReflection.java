package de.codingair.codingapi.server.reflections;

import java.lang.reflect.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;

/**
 * -== IReflection ==-
 *
 * This library was created by @Ingrim4 and allows you to use the reflection-api implemented in java easily for spigot plugins
 * You are welcome to use it and redistribute it under the following conditions:
 *  * Don't claim this class as your own
 *  * Don't remove this disclaimer
 *
 * @author Ingrim4
 * @version 1.2.8
 */
public class IReflection {
	
	public static void setValue(Object instance, String fieldName, Object value) throws IllegalArgumentException, IllegalAccessException, NoSuchFieldException, SecurityException {
		FieldAccessor field = getField(instance.getClass(), fieldName);
		field.set(instance, value);
	}
	
	public interface ConstructorAccessor {
		
		public Object newInstance(Object... args);
		
		public Constructor<?> getConstructor();
		
	}
	
	public interface MethodAccessor {
		
		public Object invoke(Object target, Object... args);

		public Method getMethod();
		
	}
	
	public interface FieldAccessor<T> {

		public T get(Object target);

		public void set(Object target, Object value);
		
		public Field getField();
		
	}
	
	public static Class<?> getSaveClass(ServerPacket packet, String name) throws ClassNotFoundException{
		try {
			return Class.forName(packet + name);
		} catch (ClassNotFoundException e) {
			throw e;
		}
	}
	
	public static Class<?> getClass(ServerPacket packet, String name) {
		try {
			return Class.forName(packet + name);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		
		return null;
	}
	
	public static ConstructorAccessor getConstructor(Class<?> clazz, Class<?>... parameterTypes) {
		Class<?>[] p = DataType.convertToPrimitive(parameterTypes);
		for (Constructor<?> c : clazz.getDeclaredConstructors()){
			if (DataType.equalsArray(DataType.convertToPrimitive(c.getParameterTypes()), p)) {
				c.setAccessible(true);
				return new ConstructorAccessor() {
					
					@Override
					public Object newInstance(Object... args) {
						try {
							return c.newInstance(args);
						} catch (IllegalAccessException e) {
							throw new IllegalStateException("Cannot use reflection.", e);
						} catch (InvocationTargetException e) {
							throw new RuntimeException("An internal error occured.", e.getCause());
						} catch (InstantiationException e) {
							throw new RuntimeException("Cannot instantiate object.", e);
						} catch (IllegalArgumentException e) {
							throw e;
						}
					}
					
					@Override
					public Constructor<?> getConstructor() {
						return c;
					}
				};
			}
		}
		
		if (clazz.getSuperclass() != null)
			return IReflection.getConstructor(clazz.getSuperclass(), parameterTypes);
		
		return null;
	}
	
	public static ConstructorAccessor[] getConstructors(Class<?> clazz) {
		List<ConstructorAccessor> cons = new ArrayList<>();
		
		for (Constructor<?> c : clazz.getDeclaredConstructors()) {
			c.setAccessible(true);
			
			cons.add(new ConstructorAccessor() {
				
				@Override
				public Object newInstance(Object... args) {
					try {
						return c.newInstance(args);
					} catch (IllegalAccessException e) {
						throw new IllegalStateException("Cannot use reflection.", e);
					} catch (InvocationTargetException e) {
						throw new RuntimeException("An internal error occured.", e.getCause());
					} catch (InstantiationException e) {
						throw new RuntimeException("Cannot instantiate object.", e);
					} catch (IllegalArgumentException e) {
						throw e;
					}
				}
				
				@Override
				public Constructor<?> getConstructor() {
					return c;
				}
			});
		}
		
		return cons.toArray(new ConstructorAccessor[cons.size()]);
	}
	
	public static MethodAccessor getMethod(Class<?> target, String methodName, Class<?>... parameterTypes) {
		return IReflection.getMethod(target, methodName, null, parameterTypes);
	}
	
	public static MethodAccessor getMethod(Class<?> target, String methodName, Class<?> returnType, Class<?>... parameterTypes) {
		Class<?>[] primitiveParameter = DataType.convertToPrimitive(parameterTypes);
		for (Method method : target.getDeclaredMethods())
			if ((methodName == null || method.getName().equals(methodName)) && (returnType == null || method.getReturnType().equals(returnType)) && (primitiveParameter.length == 0 || DataType.equalsArray(DataType.convertToPrimitive(method.getParameterTypes()), primitiveParameter))) {
				method.setAccessible(true);
				return new MethodAccessor() {
					
					@Override
					public Object invoke(Object target, Object... args) {
						try {
							return method.invoke(target, args);
						} catch (IllegalAccessException e) {
							throw new IllegalStateException("Cannot use reflection.", e);
						} catch (InvocationTargetException e) {
							throw new RuntimeException("An internal error occured.", e.getCause());
						} catch (IllegalArgumentException e) {
							throw e;
						}
					}
					
					@Override
					public Method getMethod() {
						return method;
					}
				};
			}
		if (target.getSuperclass() != null)
			return IReflection.getMethod(target.getSuperclass(), methodName, returnType, parameterTypes);
		
		throw new IllegalStateException(String.format("Unable to find method %s (%s).", methodName, parameterTypes));
	}
	
	public static MethodAccessor getSaveMethod(Class<?> target, String methodName, Class<?> returnType, Class<?>... parameterTypes) throws IllegalStateException {
		Class<?>[] primitiveParameter = DataType.convertToPrimitive(parameterTypes);
		for (Method method : target.getDeclaredMethods())
			if ((methodName == null || method.getName().equals(methodName)) && (returnType == null || method.getReturnType().equals(returnType)) && (primitiveParameter.length == 0 || DataType.equalsArray(DataType.convertToPrimitive(method.getParameterTypes()), primitiveParameter))) {
				method.setAccessible(true);
				return new MethodAccessor() {
					
					@Override
					public Object invoke(Object target, Object... args) {
						try {
							return method.invoke(target, args);
						} catch (IllegalAccessException e) {
							throw new IllegalStateException("Cannot use reflection.", e);
						} catch (InvocationTargetException e) {
							throw new RuntimeException("An internal error occured.", e.getCause());
						} catch (IllegalArgumentException e) {
							throw e;
						}
					}
					
					@Override
					public Method getMethod() {
						return method;
					}
				};
			}
		if (target.getSuperclass() != null)
			return IReflection.getMethod(target.getSuperclass(), methodName, returnType, parameterTypes);
		
		throw new IllegalStateException(String.format("Unable to find method %s (%s).", methodName, parameterTypes));
	}
	
	public static <T> FieldAccessor<T> getField(Class<?> target, String fieldName) {
		return IReflection.getField(target, fieldName, null, 0);
	}
	
	public static <T> FieldAccessor<T> getField(Class<?> target, Class<T> fieldType, int index) {
		return IReflection.getField(target, null, fieldType, index);
	}
	
	private static <T> FieldAccessor<T> getField(Class<?> target, String fieldName, Class<T> fieldType, int index) {
		for (Field field : target.getDeclaredFields())
			if ((fieldName == null || fieldName.equals(field.getName())) && (fieldType == null || (fieldType.isAssignableFrom(field.getType()) && index-- <= 0))) {
				field.setAccessible(true);
				return new FieldAccessor<T>() {
					
					@Override
					@SuppressWarnings("unchecked")
					public T get(Object target) {
						try {
							return (T) field.get(target);
						} catch (IllegalAccessException e) {
							throw new IllegalStateException("Cannot use reflection.", e);
						} catch (IllegalArgumentException e) {
							throw e;
						}
					}
					
					@Override
					public void set(Object target, Object value) {
						try {
							field.set(target, value);
						} catch (IllegalAccessException e) {
							throw new IllegalStateException("Cannot use reflection.", e);
						} catch (IllegalArgumentException e) {
							throw e;
						}
					}
					
					@Override
					public Field getField() {
						return field;
					}
				};
			}
		
		if (target.getSuperclass() != null)
			return IReflection.getField(target.getSuperclass(), fieldName, fieldType, index);
		
		throw new IllegalStateException(String.format("Unable to find field %s (%s).", fieldName, fieldType));
	}
	
	public enum DataType {
		BYTE(byte.class, Byte.class), SHORT(short.class, Short.class), INTEGER(int.class,
				Integer.class), LONG(long.class, Long.class), CHARACTER(char.class, Character.class), FLOAT(float.class,
				Float.class), DOUBLE(double.class, Double.class), BOOLEAN(boolean.class, Boolean.class);
		
		private static final Map<Class<?>, DataType> CLASS_MAP = new HashMap<Class<?>, DataType>();
		private final Class<?> primitive;
		private final Class<?> reference;
		
		static {
			for (DataType t : DataType.values()) {
				DataType.CLASS_MAP.put(t.primitive, t);
				DataType.CLASS_MAP.put(t.reference, t);
			}
		}
		
		private DataType(Class<?> primitive, Class<?> reference) {
			this.primitive = primitive;
			this.reference = reference;
		}
		
		public Class<?> getPrimitive() {
			return this.primitive;
		}
		
		public Class<?> getReference() {
			return this.reference;
		}
		
		public static DataType fromClass(Class<?> c) {
			return DataType.CLASS_MAP.get(c);
		}
		
		public static Class<?> getPrimitive(Class<?> c) {
			DataType t = DataType.fromClass(c);
			return t == null ? c : t.getPrimitive();
		}
		
		public static Class<?> getReference(Class<?> c) {
			DataType t = DataType.fromClass(c);
			return t == null ? c : t.getReference();
		}
		
		public static Class<?>[] convertToPrimitive(Class<?>[] classes) {
			int length = classes == null ? 0 : classes.length;
			Class<?>[] types = new Class<?>[length];
			for (int i = 0; i < length; i++)
				types[i] = DataType.getPrimitive(classes[i]);
			return types;
		}
		
		public static boolean equalsArray(Class<?>[] a1, Class<?>[] a2) {
			if (a1 == null || a2 == null || a1.length != a2.length)
				return false;
			for (int i = 0; i < a1.length; i++)
				if (!a1[i].equals(a2[i]) && !a1[i].isAssignableFrom(a2[i]))
					return false;
			return true;
		}
	}
	
	public enum ServerPacket {
		
		MINECRAFT_PACKAGE("net.minecraft.server." + Bukkit.getServer().getClass().getPackage().getName().substring(23)),
		MOJANG_AUTHLIB("com.mojang.authlib"),
		CRAFTBUKKIT_PACKAGE(Bukkit.getServer().getClass().getPackage().getName()),
		BUKKIT_PACKET("org.bukkit");
		
		ServerPacket(String source) {
			this.name = source;
		}
		
		private final String name;
		
		@Override
		public String toString() {
			return this.name + ".";
		}
	}
}