package de.codingair.codingapi.game.gui;

import de.codingair.codingapi.game.Game;
import de.codingair.codingapi.game.utils.Team;
import de.codingair.codingapi.player.gui.hovereditems.HoveredItem;
import de.codingair.codingapi.player.gui.hovereditems.ItemGUI;
import de.codingair.codingapi.player.gui.hovereditems.ItemGUIListener;
import de.codingair.codingapi.tools.OldItemBuilder;
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
	private boolean enabled = true;
	private Game game;
	private HashMap<String, GUI> guis = new HashMap<>();
	private boolean visibleOnSneak = false;
	private boolean closeOnWalk = false;

	public TeamVoting(Game game) {
		this.game = game;
	}

	public boolean isVisible(Player p) {
		if(!enabled) return false;
		return guis.get(p.getName()).isVisible();
	}

	public void open(Player p) {
		if(!enabled) return;
		guis.get(p.getName()).setVisible(true);
	}

	public void close(Player p) {
		if(!enabled) return;
		guis.get(p.getName()).setVisible(false);
	}
	
	public void register() {
		if(!enabled) return;
		for(Player p : game.getPlayers()) {
			register(p);
		}
	}
	
	public void register(Player p) {
		if(!enabled) return;
		guis.put(p.getName(), new GUI(p));
	}
	
	public void unregister() {
		if(!enabled) return;
		for(Player p : game.getPlayers()) {
			unregister(p);
		}
	}
	
	public void unregister(Player p) {
		if(!enabled) return;
		if(!guis.containsKey(p.getName())) return;
		
		guis.get(p.getName()).close();
		guis.remove(p.getName());
	}

	public boolean isVisibleOnSneak() {
		return visibleOnSneak;
	}

	public void setVisibleOnSneak(boolean visibleOnSneak) {
		this.visibleOnSneak = visibleOnSneak;
	}

	public boolean isCloseOnWalk() {
		return closeOnWalk;
	}

	public void setCloseOnWalk(boolean closeOnWalk) {
		this.closeOnWalk = closeOnWalk;
	}

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public class GUI extends ItemGUI {
		private boolean interacting = false;
		
		public GUI(Player p) {
			super(game.getPlugin(), p);
			setVisibleOnSneak(TeamVoting.this.visibleOnSneak);
			setCloseOnWalk(TeamVoting.this.closeOnWalk);
			
			setListener(new ItemGUIListener() {
				@Override
				public void onClick(Player player, HoveredItem item) {
					if(interacting) {
						game.getGameListener().onTeamChangeCooldownInterruption(p);
						return;
					} else interacting = true;

					Team team = game.getTeam(item.getName());
					getGame().changeTeam(p, team);
					
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
				addData(new ItemGUI.Item(team.getName(), OldItemBuilder.getColored(Material.WOOL, "Wool", team.getColor()), getText(team, true)));
			}
		}
		
		public void update() {
			for(HoveredItem item : getHoveredItems()) {
				setText(item, getText(game.getTeam(item.getName()), !item.isLookAt()));
			}
		}
		
		private List<String> getText(Team team, boolean background) {
			List<String> text = new ArrayList<>();
			text.add(team.getChatColor() + "§n" + team.getName());
			
			if(!team.getMembers().isEmpty()) text.add(HoveredItem.EMPTY);
			else return text;

			if(background) text.add("§7...");
			else for(Player member : team.getMembers()) {
				text.add("§7" + member.getName());
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
