package de.CodingAir.v1_6.CodingAPI.Game.GUI;

import de.CodingAir.v1_6.CodingAPI.Game.Map.Map;
import de.CodingAir.v1_6.CodingAPI.Game.Map.MapVoting;
import de.CodingAir.v1_6.CodingAPI.Player.GUI.Inventory.Interface.GUI;
import de.CodingAir.v1_6.CodingAPI.Player.GUI.Inventory.Interface.ItemButton.ItemButton;
import de.CodingAir.v1_6.CodingAPI.Player.GUI.Inventory.Interface.ItemButton.ItemButtonOption;
import de.CodingAir.v1_6.CodingAPI.Server.Sound;
import de.CodingAir.v1_6.CodingAPI.Tools.Callback;
import de.CodingAir.v1_6.CodingAPI.Tools.OldItemBuilder;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

/**
 * Removing of this disclaimer is forbidden.
 *
 * @author CodingAir
 * @verions: 1.0.0
 **/

public class MapVotingGUI extends GUI {
	private MapVoting mapVoting;
	private Callback<Map> callback;
	
	public MapVotingGUI(Player p, MapVoting mapVoting, Callback<Map> voted, String title, Plugin plugin) {
		super(p, title, 9, plugin, false);
		
		this.mapVoting = mapVoting;
		this.callback = voted;
		
		this.initialize(p);
	}
	
	@Override
	public void initialize(Player p) {
		Map[] maps = mapVoting.getMaps();
		
		ItemStack item = OldItemBuilder.setLore(OldItemBuilder.removeStandardLore(OldItemBuilder.getItem(Material.PAPER)), "§0");
		
		ItemStack map0 = null, map1 = null, map2 = null;
		
		if(maps[0] != null) map0 = OldItemBuilder.addLore(OldItemBuilder.setDisplayName(item.clone(), "§3§n" + maps[0].getName()), "§7Votes§8: §b" + mapVoting.getVotes(maps[0]));
		if(maps[1] != null) map1 = OldItemBuilder.addLore(OldItemBuilder.setDisplayName(item.clone(), "§3§n" + maps[1].getName()), "§7Votes§8: §b" + mapVoting.getVotes(maps[1]));
		if(maps[2] != null) map2 = OldItemBuilder.addLore(OldItemBuilder.setDisplayName(item.clone(), "§3§n" + maps[2].getName()), "§7Votes§8: §b" + mapVoting.getVotes(maps[2]));
		
		ItemButtonOption option = new ItemButtonOption();
		option.setOnlyLeftClick(true);
		option.setMovable(false);
		option.setCloseOnClick(true);
		option.setClickSound(Sound.CLICK.bukkitSound());
		
		this.addButton(new ItemButton(1, map0) {
			@Override
			public void onClick(InventoryClickEvent e) {
				if(mapVoting.vote(p, maps[0])) callback.accept(maps[0]);
				else callback.accept(null);
			}
		}.setOption(option));
		
		this.addButton(new ItemButton(4, map1) {
			@Override
			public void onClick(InventoryClickEvent e) {
				if(mapVoting.vote(p, maps[1])) callback.accept(maps[1]);
				else callback.accept(null);
			}
		}.setOption(option));
		
		this.addButton(new ItemButton(7, map2) {
			@Override
			public void onClick(InventoryClickEvent e) {
				if(mapVoting.vote(p, maps[2])) callback.accept(maps[2]);
				else callback.accept(null);
			}
		}.setOption(option));
	}
	
	public MapVoting getMapVoting() {
		return mapVoting;
	}
}
