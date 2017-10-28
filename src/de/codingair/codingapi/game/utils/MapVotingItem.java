package de.codingair.codingapi.game.utils;

import de.codingair.codingapi.game.map.MapVoting;
import de.codingair.codingapi.player.gui.inventory.gui.GUI;
import de.codingair.codingapi.player.gui.PlayerItem;
import de.codingair.codingapi.tools.Callback;
import de.codingair.codingapi.game.gui.MapVotingGUI;
import de.codingair.codingapi.game.map.Map;
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

public class MapVotingItem extends PlayerItem {
	private MapVoting mapVoting;
	private Callback<Map> callback;
	private String title;
	
	public MapVotingItem(Plugin plugin, Player player, ItemStack item, MapVoting mapVoting, Callback<Map> voted, String title) {
		super(plugin, player, item);
		this.mapVoting = mapVoting;
		this.callback = voted;
		this.title = title;
	}
	
	@Override
	public void onInteract(PlayerInteractEvent e) {
		if(!GUI.usesGUI(getPlayer())) new MapVotingGUI(getPlayer(), this.mapVoting, this.callback, this.title, getPlugin()).open();
	}
}
