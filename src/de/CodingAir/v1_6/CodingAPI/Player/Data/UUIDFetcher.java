package de.CodingAir.v1_6.CodingAPI.Player.Data;

import de.CodingAir.v1_6.CodingAPI.Tools.Callback;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.net.URL;
import java.net.URLConnection;
import java.util.Scanner;
import java.util.UUID;

public class UUIDFetcher {
	
	/**
	 * Returns null if the uuid does not exists
	 *
	 * @param name : String
	 * @param callback : Callback
	 */
	public static void getUUID(String name, Callback<UUID> callback) {
		try{
			URL url = new URL("https://api.mojang.com/users/profiles/minecraft/" + name);
			URLConnection uc = url.openConnection();
			uc.setUseCaches(false);
			uc.setDefaultUseCaches(false);
			uc.addRequestProperty("User-Agent", "Mozilla/5.0");
			uc.addRequestProperty("Cache-Control", "no-cache, no-store, must-revalidate");
			uc.addRequestProperty("Pragma", "no-cache");
			
			String json = new Scanner(uc.getInputStream(), "UTF-8").useDelimiter("\\A").next();
			JSONObject data = (JSONObject) new JSONParser().parse(json);
			
			String id = (String) data.get("id");
			String uuid = id.substring(0, 8) + "-" + id.substring(8, 12) + "-" + id.substring(12, 16) + "-" + id.substring(16, 20) + "-" + id.substring(20, 32);
			
			callback.accept(UUID.fromString(uuid));
		} catch(Exception e) {
			callback.accept(null);
		}
	}
	
	public static UUID getUUIDFromId(String id) {
		String uuid = id.substring(0, 8) + "-" + id.substring(8, 12) + "-" + id.substring(12, 16) + "-" + id.substring(16, 20) + "-" + id.substring(20, 32);
		return UUID.fromString(uuid);
	}
	
}
