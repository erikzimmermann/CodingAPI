package de.CodingAir.v1_6.CodingAPI.Game.Utils;

import de.CodingAir.v1_6.CodingAPI.Server.Color;
import de.CodingAir.v1_6.CodingAPI.Tools.Location;
import org.bukkit.DyeColor;
import org.bukkit.entity.Player;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.util.ArrayList;
import java.util.List;

public class Team {
	private String name;
	private DyeColor color;
	private List<Player> members = new ArrayList<>();
	private Location spawn;
	
	private int kills = 0;
	private int deaths = 0;
	
	public Team(String name, Location spawn, DyeColor color) {
		this.name = name;
		this.color = color;
		this.spawn = spawn.clone();
	}
	
	public Team(String name, DyeColor color) {
		this.name = name;
		this.color = color;
		this.spawn = null;
	}
	
	public String getName() {
		return name;
	}
	
	public DyeColor getColor() {
		return color;
	}
	
	public List<Player> getMembers() {
		return members;
	}
	
	public Location getSpawn() {
		return spawn;
	}
	
	public void setSpawn(Location spawn) {
		this.spawn = spawn;
	}
	
	public String getChatColor(){
		return Color.dyeColorToChatColor(this.color);
	}
	
	public void addMember(Player p) {
		if(!this.isMember(p)) this.members.add(p);
	}
	
	public void removeMember(Player p){
		if(this.isMember(p)){
			this.members.remove(this.getMember(p.getName()));
		}
	}
	
	public boolean isMember(Player p){
		return getMember(p.getName()) != null;
	}
	
	public Player getMember(String name){
		for(Player member : this.members){
			if(member.getName().equals(name)) return member;
		}
		
		return null;
	}
	
	public void teleportAllToSpawn(){
		if(this.spawn == null) return;
		
		this.members.forEach(p -> p.teleport(this.spawn));
	}
	
	public int getKills() {
		return kills;
	}
	
	public void setKills(int kills) {
		this.kills = kills;
	}
	
	public int getDeaths() {
		return deaths;
	}
	
	public void setDeaths(int deaths) {
		this.deaths = deaths;
	}
	
	public double getKD(){
		return ((double) ((int) ((((double) this.kills) / ((double) this.deaths)) * 100))) / 100;
	}
	
	public String toJSONString() {
		JSONObject jsonObject = new JSONObject();
		
		jsonObject.put("Name", this.name);
		jsonObject.put("DyeColor", this.color.toString());
		jsonObject.put("Spawn", this.spawn.toJSONString());
		
		return jsonObject.toJSONString();
	}
	
	public static Team getFromJSONString(String code) {
		try {
			JSONObject jsonObject = (JSONObject) new JSONParser().parse(code);
			
			String name = (String) jsonObject.get("Name");
			DyeColor color = DyeColor.valueOf((String) jsonObject.get("DyeColor"));
			Location spawn = Location.getByJSONString((String) jsonObject.get("Spawn"));
			
			return new Team(name, spawn, color);
		} catch(ParseException e) {
			e.printStackTrace();
			return null;
		}
	}
}
