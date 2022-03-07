package de.codingair.codingapi.player.gui.hovereditems;

import org.bukkit.entity.Player;

public abstract class ItemGUIListener {
	public abstract void onClick(Player player, HoveredItem item);
	public abstract void onShow(Player player);
	public abstract void onHide(Player player);
	public abstract void onLookAt(Player player, HoveredItem item);
	public abstract void onUnlookAt(Player player, HoveredItem item);
}
