package de.CodingAir.v1_6.CodingAPI.Player.GUI.HoveredItems;

import org.bukkit.entity.Player;

/**
 * Removing of this disclaimer is forbidden.
 *
 * @author CodingAir
 * @verions: 1.0.0
 **/

public abstract class ItemGUIListener {
	public abstract void onClick(Player player, HoveredItem item);
	public abstract void onShow(Player player);
	public abstract void onHide(Player player);
	public abstract void onLookAt(Player player, HoveredItem item);
	public abstract void onUnlookAt(Player player, HoveredItem item);
}
