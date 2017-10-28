package de.codingair.codingapi.game.utils;

import de.codingair.codingapi.game.Game;
import de.codingair.codingapi.player.gui.inventory.gui.GUI;
import de.codingair.codingapi.player.gui.PlayerItem;
import de.codingair.codingapi.tools.Callback;
import de.codingair.codingapi.game.gui.TeamGUI;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

/**
 * Removing of this disclaimer is forbidden.
 *
 * @author CodingAir
 * @verions: 1.0.0
 **/

public class TeamItem extends PlayerItem {
	private Game game;
	private Callback<Team> callback;
	private String title;
	
	public TeamItem(Plugin plugin, Player player, Callback<Team> assign, String title, ItemStack item, Game game) {
		super(plugin, player, item);
		this.game = game;
		this.callback = assign;
		this.title = title;
	}
	
	@Override
	public void onInteract(PlayerInteractEvent e) {
		if(!GUI.usesGUI(getPlayer())) new TeamGUI(getPlayer(), this.game, this.callback, this.title, getPlugin()).open();
	}
}
