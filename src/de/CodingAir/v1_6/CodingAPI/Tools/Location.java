package de.CodingAir.v1_6.CodingAPI.Tools;

import org.bukkit.Bukkit;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.text.DecimalFormat;

public class Location extends org.bukkit.Location{
	
	public Location (org.bukkit.Location location){
		super(location.getWorld(), location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch());
	}
	
	public Location (JSONObject json){
		super(Bukkit.getWorld((String) json.get("World")),
				Double.parseDouble(((String) json.get("X")).replace(",", ".")), Double.parseDouble(((String) json.get("Y")).replace(",", ".")), Double.parseDouble(((String) json.get("Z")).replace(",", ".")),
				json.get("Yaw") == null ? 0F : Float.parseFloat(((String) json.get("Yaw")).replace(",", ".")), json.get("Pitch") == null ? 0F : Float.parseFloat(((String) json.get("Pitch")).replace(",", ".")));
	}
	
	public boolean hasOnlyCoords() {
		return getYaw() == 0 && getPitch() == 0; 
	}
	
	public String toJSONString(){
		DecimalFormat format = new DecimalFormat("0.00");
		
		JSONObject json = new JSONObject();
		
		json.put("World", this.getWorld().getName());
		json.put("X", format.format(this.getX()).replace(",", "."));
		json.put("Y", format.format(this.getY()).replace(",", "."));
		json.put("Z", format.format(this.getZ()).replace(",", "."));
		
		if(!hasOnlyCoords()) {
			json.put("Yaw", format.format(this.getYaw()).replace(",", "."));
			json.put("Pitch", format.format(this.getPitch()).replace(",", "."));
		}
		
		return json.toJSONString();
	}
	
	public String toJSONString(int decimalPlaces){
		String s = "0.";
		for(int i = 0; i < decimalPlaces; i++) {
			s += "0";
		}
		DecimalFormat format = new DecimalFormat(s);
		
		JSONObject json = new JSONObject();
		
		if(decimalPlaces > 0) {
			json.put("World", this.getWorld().getName());
			json.put("X", format.format(this.getX()).replace(",", "."));
			json.put("Y", format.format(this.getY()).replace(",", "."));
			json.put("Z", format.format(this.getZ()).replace(",", "."));
			
			if(!hasOnlyCoords()) {
				json.put("Yaw", format.format(this.getYaw()).replace(",", "."));
				json.put("Pitch", format.format(this.getPitch()).replace(",", "."));
			}
		} else {
			json.put("World", this.getWorld().getName());
			json.put("X", (this.getX()+"").replace(",", "."));
			json.put("Y", (this.getY()+"").replace(",", "."));
			json.put("Z", (this.getZ()+"").replace(",", "."));
			
			if(!hasOnlyCoords()) {
				json.put("Yaw", (this.getYaw()+"").replace(",", "."));
				json.put("Pitch", (this.getPitch()+"").replace(",", "."));
			}
		}
		
		return json.toJSONString();
	}
	
	@Override
	public Location clone(){
		return new Location(this);
	}
	
	public static Location getByJSONString(String jsonString){
		if(jsonString == null) return null;
		
		try{
			return new Location((JSONObject) new JSONParser().parse(jsonString));
		} catch(Exception e) {
			return null;
		}
	}
	
	public static Location getByLocation(org.bukkit.Location location){
		if(location == null) return null;
		
		return new Location(location);
	}
}
