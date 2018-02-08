package de.codingair.codingapi.customentity.fakeplayer.extras;

import de.codingair.codingapi.customentity.fakeplayer.FakePlayer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

/**
 * Removing of this disclaimer is forbidden.
 *
 * @author codingair
 * @verions: 1.0.0
 **/

public abstract class FakePlayerListener {
	private FakePlayer fakePlayer;
	private List<String> interacting = new ArrayList<>();
	
	public FakePlayerListener(FakePlayer fakePlayer) {
		this.fakePlayer = fakePlayer;
	}
	
	public void onInteract(Player p, InteractAction action) {
		if(interacting.contains(p.getName()) || !action.equals(InteractAction.INTERACT_AT)) return;
		
		interacting.add(p.getName());
		
		Bukkit.getScheduler().runTaskLater(fakePlayer.getPlugin(), new Runnable() {
			@Override
			public void run() {
				interacting.remove(p.getName());
			}
		}, 1L);
		
		onInteract(p);
	}
	
	public abstract void onInteract(Player p);
	
	public FakePlayer getFakePlayer() {
		return fakePlayer;
	}
	
	public enum InteractAction {
		ATTACK,
		INTERACT_AT,
		INTERACT;
	}
}
