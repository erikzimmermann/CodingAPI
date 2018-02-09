package de.codingair.codingapi.customentity.fakeplayer.extras;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

/**
 * Removing of this disclaimer is forbidden.
 *
 * @author codingair
 * @verions: 1.0.0
 **/

public class FakePlayerDataWatcher {
	private boolean onFire = false;
	private boolean sneaking = false;
	private boolean sprinting = false;
	private boolean usingItem = false;
	private boolean invisible = false;
	
	/**
	 * Next attributes only for 1.9 and above
	 */
	private boolean glowing = false;
	private boolean usingElytra = false;
	
	public boolean isOnFire() {
		return onFire;
	}
	
	public FakePlayerDataWatcher setOnFire(boolean onFire) {
		this.onFire = onFire;
		return this;
	}
	
	public boolean isSneaking() {
		return sneaking;
	}
	
	public FakePlayerDataWatcher setSneaking(boolean sneaking) {
		this.sneaking = sneaking;
		return this;
	}
	
	public boolean isSprinting() {
		return sprinting;
	}
	
	public FakePlayerDataWatcher setSprinting(boolean sprinting) {
		this.sprinting = sprinting;
		return this;
	}
	
	public boolean isUsingItem() {
		return usingItem;
	}
	
	public FakePlayerDataWatcher setUsingItem(boolean usingItem) {
		this.usingItem = usingItem;
		return this;
	}
	
	public boolean isInvisible() {
		return invisible;
	}
	
	public FakePlayerDataWatcher setInvisible(boolean invisible) {
		this.invisible = invisible;
		return this;
	}
	
	public boolean isGlowing() {
		return glowing;
	}
	
	public FakePlayerDataWatcher setGlowing(boolean glowing) {
		this.glowing = glowing;
		return this;
	}
	
	public boolean isUsingElytra() {
		return usingElytra;
	}
	
	public FakePlayerDataWatcher setUsingElytra(boolean usingElytra) {
		this.usingElytra = usingElytra;
		return this;
	}
	
	public String toJSONString() {
		JSONObject json = new JSONObject();
		
		json.put("Fire", this.onFire);
		json.put("Sneaking", this.sneaking);
		json.put("Sprinting", this.sprinting);
		json.put("UsingItem", this.usingItem);
		json.put("Invisible", this.invisible);
		
		return json.toJSONString();
	}
	
	public static FakePlayerDataWatcher fromJSONString(String jsonCode) {
		try {
			JSONObject json = (JSONObject) new JSONParser().parse(jsonCode);
			
			FakePlayerDataWatcher dataWatcher = new FakePlayerDataWatcher();
			
			dataWatcher.setOnFire((boolean) json.get("Fire"));
			dataWatcher.setSneaking((boolean) json.get("Sneaking"));
			dataWatcher.setSprinting((boolean) json.get("Sprinting"));
			dataWatcher.setUsingItem((boolean) json.get("UsingItem"));
			dataWatcher.setUsingElytra((boolean) json.get("Invisible"));
			
			return dataWatcher;
		} catch(ParseException e) {
			e.printStackTrace();
			return null;
		}
	}
}
