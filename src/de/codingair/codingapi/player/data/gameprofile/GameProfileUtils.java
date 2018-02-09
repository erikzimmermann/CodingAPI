package de.codingair.codingapi.player.data.gameprofile;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.mojang.util.UUIDTypeAdapter;
import de.codingair.codingapi.player.data.gameprofile.version.GameProfileUtils_v1_10;
import de.codingair.codingapi.player.data.gameprofile.version.GameProfileUtils_v1_11;
import de.codingair.codingapi.server.reflections.IReflection;
import de.codingair.codingapi.server.reflections.PacketUtils;
import de.codingair.codingapi.tools.Callback;
import de.codingair.codingapi.player.data.gameprofile.version.GameProfileUtils_v1_8;
import de.codingair.codingapi.player.data.gameprofile.version.GameProfileUtils_v1_9;
import de.codingair.codingapi.player.data.Skin;
import de.codingair.codingapi.server.Version;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.*;

public class GameProfileUtils {
	
	public static void updateGameProfile(Plugin plugin, Player p, Skin skin, String nickName) {
		if(Version.getVersion().equals(Version.v1_8))
			GameProfileUtils_v1_8.updateGameProfile(plugin, p, skin, nickName);
		else if(Version.getVersion().equals(Version.v1_9))
			GameProfileUtils_v1_9.updateGameProfile(plugin, p, skin, nickName);
		else if(Version.getVersion().equals(Version.v1_10))
			GameProfileUtils_v1_10.updateGameProfile(plugin, p, skin, nickName);
		else if(Version.getVersion().equals(Version.v1_11))
			GameProfileUtils_v1_11.updateGameProfile(plugin, p, skin, nickName);
	}
	
	public static void updateOtherGameProfile(Plugin plugin, Player p, Player other, Skin skin, String nickName) {
		if(Version.getVersion().equals(Version.v1_8))
			GameProfileUtils_v1_8.updateOtherGameProfile(plugin, p, other, skin, nickName);
		else if(Version.getVersion().equals(Version.v1_9))
			GameProfileUtils_v1_9.updateOtherGameProfile(plugin, p, other, skin, nickName);
		else if(Version.getVersion().equals(Version.v1_10))
			GameProfileUtils_v1_10.updateOtherGameProfile(plugin, p, other, skin, nickName);
		else if(Version.getVersion().equals(Version.v1_11))
			GameProfileUtils_v1_11.updateOtherGameProfile(plugin, p, other, skin, nickName);
	}
	
	public static GameProfile getGameProfile(Player p) {
		Class<?> entityPlayerClass = IReflection.getClass(IReflection.ServerPacket.MINECRAFT_PACKAGE, "EntityPlayer");
		IReflection.MethodAccessor getProfile = IReflection.getMethod(entityPlayerClass, "getProfile", GameProfile.class, new Class[]{});
		return (GameProfile) getProfile.invoke(PacketUtils.getEntityPlayer(p));
	}
	
	public static void loadGameProfile(UUID uniqueId, Callback<GameProfile> callback) {
		try {
			try {
				URL url = new URL("https://sessionserver.mojang.com/session/minecraft/profile/" + uniqueId.toString().replace("-", "") + "?unsigned=false");
				URLConnection uc = url.openConnection();
				uc.setUseCaches(false);
				uc.setDefaultUseCaches(false);
				uc.addRequestProperty("User-Agent", "Mozilla/5.0");
				uc.addRequestProperty("Cache-Control", "no-cache, no-store, must-revalidate");
				uc.addRequestProperty("Pragma", "no-cache");
				
				String json = new Scanner(uc.getInputStream(), "UTF-8").useDelimiter("\\A").next();
				JSONObject data = (JSONObject) new JSONParser().parse(json);
				JSONArray properties = (JSONArray) data.get("properties");
				String name = (String) data.get("name");
				
				for(int i = 0; i < properties.size(); i++) {
					try {
						JSONObject property = (JSONObject) properties.get(i);
						String propertyName = (String) property.get("name");
						String value = (String) property.get("value");
						String signature = property.containsKey("signature") ? (String) property.get("signature") : null;
						
						GameProfile gameProfile = new GameProfile(uniqueId, name);
						
						gameProfile.getProperties().removeAll("textures");
						gameProfile.getProperties().put("textures", new Property(propertyName, value, signature));
						
						callback.accept(gameProfile);
					} catch(Exception e) {
						callback.accept(null);
					}
				}
			} catch(IOException ex) {
				callback.accept(null);
			}
		} catch(Exception e) {
			callback.accept(null);
		}
	}
	
	public static String gameProfileToString(GameProfile gameProfile) {
		if(gameProfile == null) return null;
		
		UUID uniqueId = gameProfile.getId();
		String name = gameProfile.getName();
		
		Collection<Property> properties = gameProfile.getProperties().get("textures");
		Property property = properties.toArray().length == 0 ? null : (Property) properties.toArray()[0];
		
		String pName = property == null ? null : property.getName();
		String pValue = property == null ? null : property.getValue();
		String pSignature = property == null ? null : property.getSignature();
		
		JSONObject json = new JSONObject();
		json.put("ID", uniqueId.toString());
		json.put("Name", name);
		json.put("Property_Name", pName);
		json.put("Property_Value", pValue);
		json.put("Property_Signature", pSignature);
		
		return json.toJSONString();
	}
	
	public static GameProfile gameProfileFromJSON(String code) {
		if(code == null) return null;
		
		try {
			JSONObject json = (JSONObject) new JSONParser().parse(code);
			
			UUID uniqueId = UUID.fromString((String) json.get("ID"));
			String name = (String) json.get("Name");
			String pName = (String) json.get("Property_Name");
			String pValue = (String) json.get("Property_Value");
			String pSignature = (String) json.get("Property_Signature");
			
			GameProfile gameProfile = new GameProfile(uniqueId, name);
			gameProfile.getProperties().put("textures", new Property(pName, pValue, pSignature));
			
			return gameProfile;
			
		} catch(Exception e) {
			return null;
		}
	}
	
	public static void setName(Player p, String customName, Plugin plugin) {
		setData(p, Skin.getSkin(getGameProfile(p)), customName, plugin);
	}
	
	/**
	 * Only other players see the new skin!
	 *
	 * @param p PLayer
	 * @param skin Skin
	 * @param plugin Plugin
	 */
	public static void setData(Player p, Skin skin, String name, Plugin plugin) {
		Object entityPlayer = PacketUtils.getEntityPlayer(p);
		Object server = PacketUtils.getMinecraftServer();
		Object world = PacketUtils.getWorldServer();
		
		GameProfile profile = new GameProfile(p.getUniqueId(), name);
		profile.getProperties().removeAll("textures");
		profile.getProperties().put("textures", new Property("textures", skin.getValue(), skin.getSignature()));
		
		Class<?> PlayerInteractManagerClass = IReflection.getClass(IReflection.ServerPacket.MINECRAFT_PACKAGE, "PlayerInteractManager");
		IReflection.ConstructorAccessor entityPlayerCon = IReflection.getConstructor(PacketUtils.EntityPlayerClass, PacketUtils.MinecraftServerClass, PacketUtils.WorldServerClass, GameProfile.class, PlayerInteractManagerClass);
		IReflection.ConstructorAccessor destroyPacket = IReflection.getConstructor(PacketUtils.PacketPlayOutEntityDestroyClass, new Class[]{int[].class});
		IReflection.ConstructorAccessor packet = IReflection.getConstructor(PacketUtils.PacketPlayOutNamedEntitySpawnClass, PacketUtils.EntityHumanClass);
		IReflection.FieldAccessor playerInteractManager = IReflection.getField(PacketUtils.EntityPlayerClass, "playerInteractManager");
		
		Object newPlayer = entityPlayerCon.newInstance(server, world, profile, playerInteractManager.get(entityPlayer));
		Object spawn = packet.newInstance(entityPlayer);
		Object destroy = destroyPacket.newInstance(new int[]{PacketUtils.getEntityId(p)});
		Object tabRemove = PacketUtils.getPlayerInfoPacket(4, entityPlayer);
		Object tabAdd = PacketUtils.getPlayerInfoPacket(0, newPlayer);
		
		PacketUtils.sendPacket(p, destroy);
		PacketUtils.sendPacketToAll(tabRemove);
		
		Bukkit.getScheduler().runTaskLater(plugin, () -> {
			PacketUtils.sendPacket(p, tabAdd);
			
			Bukkit.getOnlinePlayers().forEach(all -> {
				if(!all.getName().equalsIgnoreCase(p.getName())) {
					PacketUtils.sendPacket(all, destroy);
					PacketUtils.sendPacket(all, tabAdd);
					PacketUtils.sendPacket(all, spawn);
				}
			});
			
			p.teleport(p);
		}, 5L);
	}
	
	public static GameProfile getGameProfile(UUID uuid, String name, long timestamp, String signature, String skinUrl, String capeUrl) {
		GameProfile profile = new GameProfile(uuid, name);
		boolean cape = capeUrl != null && !capeUrl.isEmpty();
		
		List<Object> args = new ArrayList<>();
		args.add(timestamp);
		args.add(UUIDTypeAdapter.fromUUID(uuid));
		args.add(name);
		args.add(skinUrl);
		if (cape) args.add(capeUrl);
		
		profile.getProperties().put("textures", new Property("textures", Base64Coder.encodeString(String.format(cape ? Skin.JSON_CAPE : Skin.JSON_SKIN, args.toArray(new Object[args.size()]))), signature));
		return profile;
	}
}
