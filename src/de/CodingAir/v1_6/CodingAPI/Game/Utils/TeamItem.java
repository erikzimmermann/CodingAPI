package de.CodingAir.v1_6.CodingAPI.Game.Utils;

import de.CodingAir.v1_6.CodingAPI.Game.GUI.TeamGUI;
import de.CodingAir.v1_6.CodingAPI.Game.Game;
import de.CodingAir.v1_6.CodingAPI.Player.GUI.Inventory.Interface.GUI;
import de.CodingAir.v1_6.CodingAPI.Player.GUI.PlayerItem;
import de.CodingAir.v1_6.CodingAPI.Tools.Callback;
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
