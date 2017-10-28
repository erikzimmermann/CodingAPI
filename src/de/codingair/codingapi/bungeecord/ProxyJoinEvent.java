package de.codingair.codingapi.bungeecord;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class ProxyJoinEvent extends Event{
	private static HandlerList handlerList = new HandlerList();
	private ProxiedPlayer player;
	private String server;
	
	public ProxyJoinEvent(ProxiedPlayer player, String server) {
		this.player = player;
		this.server = server;
	}
	
	@Override
	public HandlerList getHandlers() {
		return handlerList;
	}
	
	public static HandlerList getHandlerList(){
		return handlerList;
	}
	
	public ProxiedPlayer getPlayer() {
		return player;
	}
	
	public String getServer() {
		return server;
	}
}
