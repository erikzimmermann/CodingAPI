package de.CodingAir.v1_6.CodingAPI.CustomEntity.NetworkEntity;

import org.bukkit.entity.Player;

/**
 * Removing of this disclaimer is forbidden.
 *
 * @author CodingAir
 * @verions: 1.0.0
 **/

public abstract class NetworkEntityListener {
	public abstract void onHit(Player player);
	public abstract void onInteract(Player player);
}
