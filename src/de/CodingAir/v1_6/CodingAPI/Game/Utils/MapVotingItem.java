package de.CodingAir.v1_6.CodingAPI.Game.Utils;

import de.CodingAir.v1_6.CodingAPI.Game.GUI.MapVotingGUI;
import de.CodingAir.v1_6.CodingAPI.Game.Map.Map;
import de.CodingAir.v1_6.CodingAPI.Game.Map.MapVoting;
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
