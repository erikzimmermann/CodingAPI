package de.codingair.codingapi.tools;

import java.util.*;

/**
 * Removing of this disclaimer is forbidden.
 *
 * @author codingair
 * @verions: 1.0.0
 **/

public class Converter {
	public static <T> List<T> fromArrayToList(T[] a) {
		return Arrays.asList(a);
	}
	
	public static <T> List<T> fromSetToList(Set<T> a) {
		List<T> l = new ArrayList<>();
		
		l.addAll(a);
		
		return l;
	}
	
	public static <T> List<T> removeSafely(List<T> l, T object) {
		List<T> newL = new ArrayList<>();
		
		for(T t : l) {
			if(!t.equals(object)) newL.add(t);
		}
		
		return newL;
	}
	
	public static <T> List<T> removeSafely(List<T> l, int index) {
		List<T> newL = new ArrayList<>();
		newL.addAll(l);
		
		newL.remove(index);
		
		return newL;
	}
	
	public static <K, V> HashMap<K, V> removeSafely(HashMap<K, V> h, K key) {
		HashMap<K, V> newH = new HashMap<>();
		newH.putAll(h);
		
		newH.remove(key);
		
		return newH;
	}
}
