package de.codingair.codingapi.bungeecord;

import java.util.UUID;

public abstract class ProxiedPlayer {
	private String server;
	private UUID uniqueId;
	private String name;
	
	public ProxiedPlayer(String server, UUID uniqueId, String name) {
		this.server = server;
		this.uniqueId = uniqueId;
		this.name = name;
	}
	
	public UUID getUniqueId() {
		return uniqueId;
	}
	
	public String getName() {
		return name;
	}
	
	public String getServer() {
		return server;
	}
	
	public void setServer(String server) {
		this.server = server;
	}
	
	public abstract void sendMessage(String message);
}
