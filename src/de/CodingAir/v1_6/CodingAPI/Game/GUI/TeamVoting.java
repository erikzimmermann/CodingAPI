package de.CodingAir.v1_6.CodingAPI.Game.GUI;

import de.CodingAir.v1_6.CodingAPI.Game.Game;
import de.CodingAir.v1_6.CodingAPI.Game.Utils.Team;
import de.CodingAir.v1_6.CodingAPI.Player.GUI.HoveredItems.HoveredItem;
import de.CodingAir.v1_6.CodingAPI.Player.GUI.HoveredItems.ItemGUI;
import de.CodingAir.v1_6.CodingAPI.Player.GUI.HoveredItems.ItemGUIData;
import de.CodingAir.v1_6.CodingAPI.Player.GUI.HoveredItems.ItemGUIListener;
import de.CodingAir.v1_6.CodingAPI.Tools.OldItemBuilder;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Removing of this disclaimer is forbidden.
 *
 * @author CodingAir
 * @verions: 1.0.0
 **/

public class TeamVoting {
	private Game game;
	private HashMap<String, GUI> guis = new HashMap<>();
	
	public TeamVoting(Game game) {
		this.game = game;
	}
	
	public void register() {
		for(Player p : game.getPlayers()) {
			register(p);
		}
	}
	
	public void register(Player p) {
		guis.put(p.getName(), new GUI(p));
	}
	
	public void unregister() {
		for(Player p : game.getPlayers()) {
			unregister(p);
		}
	}
	
	public void unregister(Player p) {
		if(!guis.containsKey(p.getName())) return;
		
		guis.get(p.getName()).remove(true);
		guis.remove(p.getName());
	}
	
	public class GUI extends ItemGUI {
		private boolean interacting = false;
		
		public GUI(Player p) {
			super(game.getPlugin(), p);
			setVisibleOnSneak(true);
			
			setListener(new ItemGUIListener() {
				@Override
				public void onClick(Player player, HoveredItem item) {
					if(interacting) {
						game.getGameListener().onTeamChangeCooldownInterruption(p);
						return;
					} else interacting = true;
					
					Team old = game.getTeam(p);
					Team team = game.getTeam(item.getName());
					
					if(old != null) {
						old.removeMember(p);
						
						if(old.equals(team)) {
							game.getGameListener().onTeamQuit(p, old);
						} else {
							game.getGameListener().onTeamChange(p, old, team);
							team.addMember(p);
						}
					}
					
					if(old == null) {
						game.getGameListener().onTeamJoin(p, team);
						team.addMember(p);
					}
					
					updateAll();
					
					new BukkitRunnable() {
						@Override
						public void run() {
							interacting = false;
						}
					}.runTaskLater(game.getPlugin(), 20L);
				}
				
				@Override
				public void onShow(Player player) {
				}
				
				@Override
				public void onHide(Player player) {
				}
				
				@Override
				public void onLookAt(Player player, HoveredItem item) {
					setText(item, getText(game.getTeam(item.getName()), false));
				}
				
				@Override
				public void onUnlookAt(Player player, HoveredItem item) {
					setText(item, getText(game.getTeam(item.getName()), true));
				}
			});
			
			for(Team team : game.getTeams()) {
				addData(new ItemGUIData(team.getName(), OldItemBuilder.getColored(Material.WOOL, "Wool", team.getColor()), getText(team, true)));
			}
		}
		
		public void update() {
			boolean edited = false;
			
			for(HoveredItem item : getHoveredItems()) {
				edited = true;
				setText(item, getText(game.getTeam(item.getName()), !item.isLookAt()));
			}
			
			if(edited) return;
			
			for(ItemGUIData datum : getData()) {
				setText(datum, getText(game.getTeam(datum.getName()), true));
			}
		}
		
		private List<String> getText(Team team, boolean background) {
			List<String> text = new ArrayList<>();
			text.add(team.getChatColor() + "§n" + team.getName());
			
			if(!team.getMembers().isEmpty()) text.add(HoveredItem.EMPTY);
			
			for(Player member : team.getMembers()) {
				if(background) text.add("§7...");
				else text.add("§7" + member.getName());
			}
			
			return text;
		}
	}
	
	public void updateAll() {
		this.guis.values().forEach(GUI::update);
	}
	
	public Game getGame() {
		return game;
	}
}
