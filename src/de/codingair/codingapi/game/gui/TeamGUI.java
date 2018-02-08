package de.codingair.codingapi.game.gui;

import de.codingair.codingapi.game.Game;
import de.codingair.codingapi.game.utils.Team;
import de.codingair.codingapi.player.gui.inventory.gui.GUI;
import de.codingair.codingapi.player.gui.inventory.gui.itembutton.ItemButton;
import de.codingair.codingapi.player.gui.inventory.gui.itembutton.ItemButtonOption;
import de.codingair.codingapi.tools.Callback;
import de.codingair.codingapi.player.gui.inventory.gui.ItemAlignment;
import de.codingair.codingapi.server.Sound;
import de.codingair.codingapi.tools.OldItemBuilder;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.List;

/**
 * Removing of this disclaimer is forbidden.
 *
 * @author codingair
 * @verions: 1.0.0
 **/

public class TeamGUI extends GUI {
	private Game game;
	private Callback<Team> callback;
	
	public TeamGUI(Player p, Game game, Callback<Team> assign, String title, Plugin plugin) {
		super(p, title, game.getTeams().size() > 7 ? 18 : 9, plugin, false);
		
		this.game = game;
		this.callback = assign;
		
		this.initialize(p);
	}
	
	@Override
	public void initialize(Player p) {
		ItemButtonOption option = new ItemButtonOption();
		option.setOnlyLeftClick(true);
		option.setMovable(false);
		option.setCloseOnClick(true);
		option.setClickSound(Sound.CLICK.bukkitSound());
		
		int line = 0;
		int slot = 0;
		
		for(Team team : game.getTeams()) {
			ItemStack teamItem = OldItemBuilder.setLore(OldItemBuilder.removeStandardLore(OldItemBuilder.getColored(Material.WOOL, team.getChatColor() + "§n" + team.getName(), team.getColor())), "§0");
			
			List<String> lore = new ArrayList<>();
			for(Player player : team.getMembers()) {
				lore.add("§7- §b" + player.getName());
			}
			
			OldItemBuilder.addLore(teamItem, lore.toArray(new String[lore.size()]));
			
			teamItem.setAmount(game.getTeamSize() - team.getMembers().size());
			
			addButton(new ItemButton(slot + line * 9, teamItem) {
				@Override
				public void onClick(InventoryClickEvent e) {
					if(team.equals(game.getTeam(p))) {
						if(game.hasTeam(p)) game.getTeam(p).removeMember(p);
						callback.accept(null);
					} else {
						if(game.hasTeam(p)) game.getTeam(p).removeMember(p);
						team.addMember(p);
						callback.accept(team);
					}
				}
			}.setOption(option));
			
			slot++;
			
			if(game.getTeams().size() > 7) {
				int half = Math.round(game.getTeams().size() / 2);
				
				if(slot >= half && game.getTeams().size() - half == slot) {
					line++;
					slot = 0;
				}
			}
			
			if(slot + line * 9 == getSize()) break;
		}
		
		setAlignment(ItemAlignment.CENTER);
		
		ItemStack placeHolder = OldItemBuilder.getColored(Material.STAINED_GLASS_PANE, "§0", DyeColor.BLACK);
		setItem(0, placeHolder);
		setItem(8, placeHolder);
		
		if(super.getSize() > 9) {
			setItem(9, placeHolder);
			setItem(17, placeHolder);
		}
	}
	
	public Game getGame() {
		return game;
	}
}
