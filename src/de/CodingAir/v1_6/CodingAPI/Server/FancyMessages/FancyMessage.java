package de.CodingAir.v1_6.CodingAPI.Server.FancyMessages;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Removing of this disclaimer is forbidden.
 *
 * @author BubbleEgg
 * @verions: 1.0.0
 **/

public class FancyMessage {
	private Player player;
	private List<String> messages = new ArrayList<>();
	private MessageTypes type;
	private boolean autoSize = true;
	
	public FancyMessage() {
	
	}
	
	public FancyMessage(Player p, MessageTypes type, boolean autoSize, String... messages) {
		this.player = p;
		this.type = type;
		this.autoSize = autoSize;
		this.messages.addAll(Arrays.asList(messages));
	}
	
	public FancyMessage(MessageTypes type, boolean autoSize, String... messages) {
		this.type = type;
		this.autoSize = autoSize;
		this.messages.addAll(Arrays.asList(messages));
	}
	
	public void send() {
		send(this.player);
	}
	
	public void send(Player p) {
		switch(this.type) {
			case INFO_MESSAGE: {
				sendInfoMessage((this.player = p) == null);
				break;
			}
		}
	}
	
	public void setMessages(String... messages) {
		this.messages.addAll(Arrays.asList(messages));
	}
	
	public List<String> getMessages() {
		return this.messages;
	}
	
	public void setType(MessageTypes type) {
		this.type = type;
	}
	
	public MessageTypes getType() {
		return this.type;
	}
	
	public void setAutoSize(boolean autoSize) {
		this.autoSize = autoSize;
	}
	
	private void sendInfoMessage(boolean broadcast) {
		if(broadcast) {
			Bukkit.broadcastMessage("§7§m§l↔↔↔↔↔↔↔↔↔↔↔↔↔↔↔↔↔↔↔↔↔↔↔↔↔↔↔↔↔↔↔↔↔↔↔↔↔↔↔↔↔↔↔↔↔↔↔↔↔↔↔↔↔");
			
			if(this.messages.size() <= 7) {
				Bukkit.broadcastMessage("");
			}
			
			for(String message : this.messages) {
				Bukkit.broadcastMessage(message);
			}
			
			if(autoSize) {
				for(int i = 0; i < 6 - messages.size(); i++) {
					Bukkit.broadcastMessage("");
				}
			}
			
			if(this.messages.size() <= 6) {
				Bukkit.broadcastMessage("");
			}
			
			Bukkit.broadcastMessage("§7§m§l↔↔↔↔↔↔↔↔↔↔↔↔↔↔↔↔↔↔↔↔↔↔↔↔↔↔↔↔↔↔↔↔↔↔↔↔↔↔↔↔↔↔↔↔↔↔↔↔↔↔↔↔↔");
		} else {
			this.player.sendMessage("§7§m§l↔↔↔↔↔↔↔↔↔↔↔↔↔↔↔↔↔↔↔↔↔↔↔↔↔↔↔↔↔↔↔↔↔↔↔↔↔↔↔↔↔↔↔↔↔↔↔↔↔↔↔↔↔");
			
			if(this.messages.size() <= 7) {
				this.player.sendMessage("");
			}
			
			for(String message : this.messages) {
				this.player.sendMessage(message);
			}
			
			if(autoSize) {
				for(int i = 0; i < 6 - messages.size(); i++) {
					this.player.sendMessage("");
				}
			}
			
			if(this.messages.size() <= 6) {
				this.player.sendMessage("");
			}
			
			this.player.sendMessage("§7§m§l↔↔↔↔↔↔↔↔↔↔↔↔↔↔↔↔↔↔↔↔↔↔↔↔↔↔↔↔↔↔↔↔↔↔↔↔↔↔↔↔↔↔↔↔↔↔↔↔↔↔↔↔↔");
		}
	}
}
