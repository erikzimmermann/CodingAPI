package de.codingair.codingapi.player.data;

import de.codingair.codingapi.tools.Callback;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.net.URL;
import java.net.URLConnection;
import java.util.Scanner;
import java.util.UUID;

public class NameFetcher {
	
	/**
	 * Returns null if the uuid does not exists
	 *
	 * @param uuid : UUID
	 * @param callback : Callback
	 */
	public static void getName(UUID uuid, Callback<String> callback) {
		try{
			URL url = new URL("https://sessionserver.mojang.com/session/minecraft/profile/" + uuid.toString().replace("-", ""));
			URLConnection uc = url.openConnection();
			uc.setUseCaches(false);
			uc.setDefaultUseCaches(false);
			uc.addRequestProperty("User-Agent", "Mozilla/5.0");
			uc.addRequestProperty("Cache-Control", "no-cache, no-store, must-revalidate");
			uc.addRequestProperty("Pragma", "no-cache");
			
			String json = new Scanner(uc.getInputStream(), "UTF-8").useDelimiter("\\A").next();
			JSONObject data = (JSONObject) new JSONParser().parse(json);
			
			String name = (String) data.get("name");
			
			callback.accept(name);
		} catch(Exception e) {
			callback.accept(null);
		}
	}
	
}
