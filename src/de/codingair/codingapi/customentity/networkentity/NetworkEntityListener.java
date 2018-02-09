package de.codingair.codingapi.customentity.networkentity;

import org.bukkit.entity.Player;

/**
 * Removing of this disclaimer is forbidden.
 *
 * @author codingair
 * @verions: 1.0.0
 **/

public abstract class NetworkEntityListener {
	public abstract void onHit(Player player);
	public abstract void onInteract(Player player);
}
