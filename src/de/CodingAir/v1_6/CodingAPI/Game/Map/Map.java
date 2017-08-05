package de.CodingAir.v1_6.CodingAPI.Game.Map;


import de.CodingAir.v1_6.CodingAPI.Game.Utils.Team;
import de.CodingAir.v1_6.CodingAPI.Tools.Location;
import org.bukkit.DyeColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.util.ArrayList;
import java.util.List;

public class Map {
	private String name;
	private List<Team> teams = new ArrayList<>();
	private MapListener listener;
	private Location[] deathmatchSpawns;
	private Location spectatorSpawn;
	private boolean deathmatch = false;
	private boolean friendlyFire = false;
	private Plugin plugin;
	
	public Map(String name, List<Team> teams, Location[] deathmatchSpawns, Location spectatorSpawn, boolean friendlyFire, Plugin plugin) {
		this.name = name;
		this.teams = teams;
		this.deathmatchSpawns = deathmatchSpawns;
		this.spectatorSpawn = spectatorSpawn;
		this.friendlyFire = friendlyFire;
		this.plugin = plugin;
	}
	
	public Map(String name, List<Team> teams, Location[] deathmatch, Plugin plugin) {
		this.plugin = plugin;
		this.name = name;
		this.teams = teams;
		this.listener = new MapListener(this.plugin);
		this.deathmatchSpawns = deathmatch;
	}
	
	public Map(String name, Plugin plugin) {
		this.plugin = plugin;
		this.name = name;
		this.listener = new MapListener(this.plugin);
	}
	
	public String getName() {
		return name;
	}
	
	public void setTeams(List<Team> teams) {
		for(Team team : teams) {
			team.setSpawn(getTeam(team.getColor()).getSpawn());
		}
		
		this.teams = teams;
	}
	
	public List<Team> getTeams() {
		return teams;
	}
	
	public void reset() {
		this.listener.reset();
	}
	
	public Team getBalancedTeam() {
		if(this.teams.size() == 0) return null;
		
		Team team = null;
		
		for(Team t : this.teams) {
			if(team == null) team = t;
			else if(team.getMembers().size() > t.getMembers().size()) {
				team = t;
			}
		}
		
		return team;
	}
	
	public Team getRandomTeam() {
		if(this.teams.size() == 0) return null;
		
		return this.teams.get((int) (Math.random() * this.teams.size()));
	}
	
	public void assignToBalancedTeam(Player p) {
		Team team = this.getBalancedTeam();
		
		if(team == null) return;
		
		team.addMember(p);
	}
	
	public Team getTeam(Player p) {
		for(Team team : this.getTeams()) {
			if(team.isMember(p)) return team;
		}
		
		return null;
	}
	
	public Team getTeam(DyeColor color) {
		for(Team team : this.getTeams()) {
			if(team.getColor().equals(color)) return team;
		}
		
		return null;
	}
	
	public Team getTeam(String teamName) {
		for(Team team : this.getTeams()) {
			if(team.getName().equalsIgnoreCase(teamName)) return team;
		}
		
		return null;
	}
	
	public Team addTeam(Team team) {
		this.teams.add(team);
		return team;
	}
	
	public boolean isDeathmatch() {
		return deathmatch;
	}
	
	public Location[] getDeathmatchSpawns() {
		return deathmatchSpawns;
	}
	
	public void setDeathmatchSpawns(Location[] deathmatchSpawns) {
		this.deathmatchSpawns = deathmatchSpawns;
	}
	
	public Location getSpectatorSpawn() {
		return spectatorSpawn;
	}
	
	public void setSpectatorSpawn(Location spectatorSpawn) {
		this.spectatorSpawn = spectatorSpawn;
	}
	
	public boolean isFriendlyFire() {
		return friendlyFire;
	}
	
	public void setFriendlyFire(boolean friendlyFire) {
		this.friendlyFire = friendlyFire;
	}
	
	public Plugin getPlugin() {
		return plugin;
	}
	
	public MapListener getListener() {
		return listener;
	}
	
	public String toJSONString() {
		JSONObject json = new JSONObject();
		JSONArray teams = new JSONArray();
		
		for(Team team : this.teams) {
			teams.add(team.toJSONString());
		}
		
		json.put("Name", this.name);
		json.put("FriendlyFire", this.friendlyFire);
		json.put("Teams", teams.toJSONString());
		json.put("SpectatorSpawn", this.spectatorSpawn == null ? null : this.spectatorSpawn.toJSONString());
		
		return json.toJSONString();
	}
	
	public static Map getFromJSONString(String code, Plugin plugin) {
		try {
			JSONObject jsonObject = (JSONObject) new JSONParser().parse(code);
			
			String name = (String) jsonObject.get("Name");
			boolean friendlyFire = (boolean) jsonObject.get("FriendlyFire");
			
			List<Team> teams = new ArrayList<>();
			JSONArray jsonArray = (JSONArray) new JSONParser().parse((String) jsonObject.get("Teams"));
			for(Object o : jsonArray) {
				teams.add(Team.getFromJSONString((String) o));
			}
			
			Location specSpawn = jsonObject.get("SpectatorSpawn") == null ? null : Location.getByJSONString((String) jsonObject.get("SpectatorSpawn"));
			
			Map map = new Map(name, teams, null, plugin);
			map.setSpectatorSpawn(specSpawn);
			map.setFriendlyFire(friendlyFire);
			
			return map;
		} catch(ParseException e) {
			e.printStackTrace();
			return null;
		}
	}
}
