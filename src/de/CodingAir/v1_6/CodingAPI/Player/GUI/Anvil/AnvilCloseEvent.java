package de.CodingAir.v1_6.CodingAPI.Player.GUI.Anvil;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Removing of this disclaimer is forbidden.
 *
 * @author CodingAir
 * @verions: 1.0.0
 **/

public class AnvilCloseEvent extends Event {
	private Player player;
	public static HandlerList handlers = new HandlerList();
	private boolean cancelled = false;
	private Runnable post = null;
	
	private AnvilGUI anvil;
	
	public AnvilCloseEvent(Player player, AnvilGUI anvil) {
		this.player = player;
		this.anvil = anvil;
	}
	
	@Override
	public HandlerList getHandlers() {
		return handlers;
	}
	
	public static HandlerList getHandlerList() {
		return handlers;
	}
	
	public AnvilGUI getAnvil() {
		return anvil;
	}
	
	public boolean isCancelled() {
		return cancelled;
	}
	
	public void setCancelled(boolean cancelled) {
		this.cancelled = cancelled;
	}
	
	public Player getPlayer() {
		return player;
	}
	
	public Runnable getPost() {
		return post;
	}
	
	public void setPost(Runnable post) {
		this.post = post;
	}
}
