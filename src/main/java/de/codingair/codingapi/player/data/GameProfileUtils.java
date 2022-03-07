package de.codingair.codingapi.player.data;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.mojang.util.UUIDTypeAdapter;
import de.codingair.codingapi.server.reflections.IReflection;
import de.codingair.codingapi.server.reflections.PacketUtils;
import de.codingair.codingapi.server.specification.Version;
import de.codingair.codingapi.tools.io.JSON.JSON;
import de.codingair.codingapi.tools.io.JSON.JSONParser;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class GameProfileUtils {
    public static GameProfile getGameProfile(Player p) {
        //Version.since(18, "getProfile", "fp"),
        IReflection.MethodAccessor getProfile = IReflection.getMethod(PacketUtils.EntityPlayerClass, GameProfile.class, new Class[] {});
        return (GameProfile) getProfile.invoke(PacketUtils.getEntityPlayer(p));
    }

    public static String extractSkinId(Player p) {
        return extractSkinId(getGameProfile(p));
    }

    public static String extractSkinId(GameProfile gameProfile) {
        if(gameProfile == null) return null;

        Skin skin = new Skin(gameProfile, true) {
            @Override
            public void onLoad(Skin skin) {
            }

            @Override
            public void onFail(Skin skin) {
            }
        };

        return ((String) skin.getElement(Skin.SkinElement.SKIN)).replace("http://textures.minecraft.net/texture/", "");
    }

    public static GameProfile createBySkinId(String skinId) {
        return getGameProfile(UUID.randomUUID(), "-", 0, "", "http://textures.minecraft.net/texture/" + skinId, null);
    }

    public static GameProfile gameProfileFromJSON(String code) {
        if(code == null) return null;

        try {
            JSON json = (JSON) new JSONParser().parse(code);

            UUID uniqueId = UUID.fromString(json.get("ID"));
            String name = json.get("Name");
            String pName = json.get("Property_Name");
            String pValue = json.get("Property_Value");
            String pSignature = json.get("Property_Signature");

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
     * @param p      PLayer
     * @param skin   Skin
     * @param plugin Plugin
     */
    public static void setData(Player p, Skin skin, String name, Plugin plugin) {
        Object entityPlayer = PacketUtils.getEntityPlayer(p);
        Object server = PacketUtils.getMinecraftServer();
        Object world = PacketUtils.getWorldServer();

        GameProfile profile = new GameProfile(p.getUniqueId(), name);
        profile.getProperties().removeAll("textures");
        profile.getProperties().put("textures", new Property("textures", skin.getValue(), skin.getSignature()));

        Class<?> PlayerInteractManagerClass = IReflection.getClass(IReflection.ServerPacket.SERVER_LEVEL, "PlayerInteractManager");
        IReflection.ConstructorAccessor entityPlayerCon = IReflection.getConstructor(PacketUtils.EntityPlayerClass, PacketUtils.MinecraftServerClass, PacketUtils.WorldServerClass, GameProfile.class, PlayerInteractManagerClass);
        IReflection.ConstructorAccessor destroyPacket = IReflection.getConstructor(PacketUtils.PacketPlayOutEntityDestroyClass, int[].class);
        IReflection.ConstructorAccessor packet = IReflection.getConstructor(PacketUtils.PacketPlayOutNamedEntitySpawnClass, PacketUtils.EntityHumanClass);
        IReflection.FieldAccessor playerInteractManager = IReflection.getField(PacketUtils.EntityPlayerClass, "playerInteractManager");

        Object newPlayer = entityPlayerCon.newInstance(server, world, profile, playerInteractManager.get(entityPlayer));
        Object spawn = packet.newInstance(entityPlayer);
        Object destroy = destroyPacket.newInstance(new int[] {PacketUtils.getEntityId(p)});
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
        if(cape) args.add(capeUrl);

        profile.getProperties().put("textures", new Property("textures", Base64Coder.encodeString(String.format(cape ? Skin.JSON_CAPE : Skin.JSON_SKIN, args.toArray(new Object[args.size()]))), signature));
        return profile;
    }
}
