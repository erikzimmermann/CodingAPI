package de.codingair.codingapi.server.reflections;

import org.bukkit.entity.Player;

import java.util.HashMap;

/**
 * Removing of this disclaimer is forbidden.
 *
 * @author codingair
 * @verions: 1.0.0
 **/

public class Packet {
	private Class<?> clazz;
	private Object packet;
	private Player[] players;
	private boolean initialized = false;
	private HashMap<String, Object> fields = new HashMap<>();
	
	public Packet(Class<?> packet) {
		this.clazz = packet;
		this.players = null;
	}
	
	public Packet(Class<?> packet, Player... players) {
		this.clazz = packet;
		this.players = players;
	}
	
	public void initialize(Object... parameters) {
		Class<?>[] clazzes = new Class<?>[parameters.length];
		
		for(int i = 0; i < parameters.length; i++) {
			clazzes[i] = parameters[i].getClass();
		}
		
		initialize(clazzes, parameters);
	}
	
	public void initialize(Class<?>[] parameterTypes, Object... parameters) {
		this.packet = IReflection.getConstructor(this.clazz, parameterTypes).newInstance(parameters);
		setFields();
		
		this.initialized = true;
	}
	
	private void setFields() {
		if(this.packet == null) return;
		
		for(String name : this.fields.keySet()) {
			Object value = this.fields.get(name);
			
			IReflection.FieldAccessor field = IReflection.getField(this.clazz, name);
			field.set(this.packet, value);
		}
		
		this.fields.clear();
	}
	
	public void editField(String name, Object value) {
		this.fields.put(name, value);
	}
	
	public Object getPacket() {
		return packet;
	}
	
	public Player[] getPlayers() {
		return players;
	}
	
	public Packet setPlayers(Player... players) {
		this.players = players;
		return this;
	}
	
	public boolean isInitialized() {
		return initialized;
	}
	
	public void send() {
		if(!this.isInitialized()) return;
		
		if(this.players == null || this.players.length == 0) PacketUtils.sendPacketToAll(this.packet);
		else PacketUtils.sendPacket(this.packet, players);
	}
}
