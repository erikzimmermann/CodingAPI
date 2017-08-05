package de.CodingAir.v1_6.CodingAPI.Game;

import de.CodingAir.v1_6.CodingAPI.Game.Map.Map;
import de.CodingAir.v1_6.CodingAPI.Game.Utils.Team;
import de.CodingAir.v1_6.CodingAPI.Time.TimeFetcher;
import org.bukkit.Bukkit;
import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.plugin.Plugin;

import java.util.List;

/**
 * Removing of this disclaimer is forbidden.
 *
 * @author CodingAir
 * @verions: 1.0.0
 **/

public class GameExample extends Game {
	public GameExample(Location lobbySpawn, Plugin plugin) {
		super("OneVsOne", "§8[§61vs1§8]", 1, 2, 2, 2, TimeFetcher.Time.MINUTES, 15, TimeFetcher.Time.SECONDS, lobbySpawn, 47, TimeFetcher.Time.SECONDS, 45, 10, plugin);
	}
	
	@Override
	public List<Team> initializeTeams(List<Team> teams) {
		
		for(DyeColor color : DyeColor.values()) {
			teams.add(new Team(color.name(), de.CodingAir.v1_6.CodingAPI.Tools.Location.getByLocation(Bukkit.getOnlinePlayers().iterator().next().getLocation().clone()), color));
			if(teams.size() == 13) break;
		}
		
		return teams;
	}
	
	@Override
	public List<Map> initializeMaps(List<Map> maps) {
		
		for(int i = 0; i < 3; i++) {
			String name = "TestMap_" + i;
			
			maps.add(new Map(name, this.getPlugin()));
		}
		
		return maps;
	}
}
