package de.CodingAir.v1_6.CodingAPI.Game.Utils;

import de.CodingAir.v1_6.CodingAPI.Player.Data.UUIDFetcher;
import de.CodingAir.v1_6.CodingAPI.Server.Color;
import de.CodingAir.v1_6.CodingAPI.Tools.Callback;
import org.bukkit.DyeColor;
import org.bukkit.entity.Player;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class OfflineTeam {
	private String name;
	private DyeColor color;
	/* String := UUID */
	private List<String> members = new ArrayList<>();
	
	public OfflineTeam(String name, DyeColor color) {
		this.name = name;
		this.color = color;
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public DyeColor getColor() {
		return color;
	}
	
	public String getChatColor() {
		return Color.dyeColorToChatColor(this.color);
	}
	
	public List<String> getMembers() {
		return members;
	}
	
	public void add(String name) {
		UUIDFetcher.getUUID(name, new Callback<UUID>() {
			@Override
			public void accept(UUID uuid) {
				OfflineTeam.this.members.add(uuid.toString());
			}
		});
	}
	
	public void add(UUID uuid) {
		this.members.add(uuid.toString());
	}
	
	public void add(Player p) {
		this.members.add(p.getUniqueId().toString());
	}
	
	public void remove(Player p) {
		this.members.remove(p.getUniqueId().toString());
	}
	
	public void remove(UUID uuid) {
		this.members.remove(uuid.toString());
	}
	
	public boolean isMember(Player p) {
		return this.members.contains(p.getUniqueId().toString());
	}
	
	public boolean isMember(UUID uuid) {
		return this.members.contains(uuid.toString());
	}
	
	public void isMember(String name, Callback<Boolean> callback){
		UUIDFetcher.getUUID(name, new Callback<UUID>() {
			@Override
			public void accept(UUID uuid) {
				callback.accept(members.contains(uuid.toString()));
			}
		});
	}
	
	@Override
	public String toString() {
		JSONObject json = new JSONObject();
		json.put("Name", this.name);
		json.put("Color", this.color.name());
		
		JSONArray members = new JSONArray();
		members.addAll(this.members);
		
		json.put("Members", members.toJSONString());
		
		return json.toJSONString();
	}
	
	public static OfflineTeam byJSONString(String jsonString) {
		try{
			JSONObject json = (JSONObject) new JSONParser().parse(jsonString);
			
			String name = (String) json.get("Name");
			DyeColor color = DyeColor.valueOf((String) json.get("Color"));
			JSONArray members = (JSONArray) new JSONParser().parse((String) json.get("Members"));
			List<String> uuids = new ArrayList<>();
			uuids.addAll(members);
			
			OfflineTeam team = new OfflineTeam(name, color);
			uuids.forEach(uuid -> team.add(UUID.fromString(uuid)));
			
			return team;
		} catch(ParseException e) {
			return null;
		}
	}
	
	public static boolean correctJSONString(String jsonString) {
		return byJSONString(jsonString) != null;
	}
}
