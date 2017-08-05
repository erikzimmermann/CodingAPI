package de.CodingAir.v1_6.CodingAPI.Game.Lobby;

import de.CodingAir.v1_6.CodingAPI.Game.Map.Map;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

/**
 * Removing of this disclaimer is forbidden.
 *
 * @author CodingAir
 * @verions: 1.0.0
 **/

public class LobbyListener implements Listener {
	private Lobby lobby;
	
	public LobbyListener(Lobby lobby) {
		this.lobby = lobby;
	}
	
	public Lobby getLobby() {
		return lobby;
	}
	
	public void onInitialize(Player p) {
	}
	
	public void onEndCountdown() {
	}
	
	public void onTick(int timeLeft) {
	}
	
	public void onStartMapVoting() {
	}
	
	public void onEndMapVoting(Map map) {
	}
	
	public void onNeededPlayers(int amount) {
	}
}
