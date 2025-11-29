package de.codingair.codingapi.player.data;

import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.mojang.authlib.properties.PropertyMap;
import de.codingair.codingapi.server.reflections.IReflection;
import de.codingair.codingapi.server.reflections.PacketUtils;
import de.codingair.codingapi.server.specification.Version;
import de.codingair.codingapi.tools.io.JSON.JSON;
import de.codingair.codingapi.tools.io.JSON.JSONParser;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.profile.PlayerProfile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder;

import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class GameProfileUtils {
    private static final String TEXTURE_URL = "http://textures.minecraft.net/texture/";
    private static IReflection.MethodAccessor craftMetaSkull$setProfile;
    private static IReflection.ConstructorAccessor craftPlayerProfileConstructor;

    static {
        try {
            Class<?> craftMetaSkullClass = IReflection.getClass(IReflection.ServerPacket.CRAFTBUKKIT_PACKAGE, "inventory.CraftMetaSkull");
            craftMetaSkull$setProfile = IReflection.getMethod(craftMetaSkullClass, (Class<?>) null, new Class[]{GameProfile.class});

            Class<?> craftPlayerProfileClass = IReflection.getClass(IReflection.ServerPacket.CRAFTBUKKIT_PACKAGE, "profile.CraftPlayerProfile");
            craftPlayerProfileConstructor = IReflection.getConstructor(craftPlayerProfileClass, GameProfile.class);
        } catch (Throwable ignored) {
            craftMetaSkull$setProfile = null;
        }
    }

    public static GameProfile getGameProfile(Player p) {
        IReflection.MethodAccessor getProfile = IReflection.getMethod(PacketUtils.EntityPlayerClass, GameProfile.class, new Class[0]);
        return (GameProfile) getProfile.invoke(PacketUtils.getEntityPlayer(p));
    }

    @Nullable
    public static String extractSkinId(@NotNull Player p) {
        return extractSkinId(getGameProfile(p));
    }

    @Nullable
    public static String extractSkinId(@NotNull ItemMeta meta) {
        if (!(meta instanceof SkullMeta)) return null;

        if (Version.before(21)) {
            try {
                IReflection.FieldAccessor<GameProfile> profile = IReflection.getField(meta.getClass(), "profile");
                return GameProfileUtils.extractSkinId(profile.get(meta));
            } catch (IllegalStateException ex) {
                // illegal ItemMeta class
                return null;
            }
        }

        org.bukkit.profile.PlayerProfile profile = ((SkullMeta) meta).getOwnerProfile();
        if (profile == null) return null;

        URL skinUrl = profile.getTextures().getSkin();
        if (skinUrl == null) return null;

        return skinUrl.toString().replace(TEXTURE_URL, "");
    }

    public static void applySkinIdToItem(@NotNull SkullMeta meta, @NotNull String skinId) {
        GameProfile gameProfile = createBySkinId(skinId);

        if (Version.atLeast(21)) {
            PlayerProfile profile = (PlayerProfile) craftPlayerProfileConstructor.newInstance(gameProfile);
            meta.setOwnerProfile(profile);
        } else {
            if (craftMetaSkull$setProfile != null) {
                // use setProfile for preventing warnings about `serializedProfile`
                craftMetaSkull$setProfile.invoke(meta, gameProfile);
            } else {
                // set profile for older versions
                try {
                    IReflection.FieldAccessor<GameProfile> profile = IReflection.getField(meta.getClass(), "profile");
                    profile.set(meta, gameProfile);
                } catch (Throwable ignored) {
                }
            }
        }
    }

    @Nullable
    public static String extractSkinId(@Nullable GameProfile gameProfile) {
        if (gameProfile == null) return null;

        Skin skin = new Skin(gameProfile, true) {
            @Override
            public void onLoad(Skin skin) {
            }

            @Override
            public void onFail(Skin skin) {
            }
        };

        String data = skin.getElement(Skin.SkinElement.SKIN);
        if (data == null) return null;

        return data.replace(TEXTURE_URL, "");
    }

    public static GameProfile createBySkinId(String skinId) {
        return getGameProfile(UUID.randomUUID(), "-", 0, "", TEXTURE_URL + skinId, null);
    }

    public static GameProfile gameProfileFromJSON(String code) {
        if (code == null) return null;

        try {
            JSON json = (JSON) new JSONParser().parse(code);

            UUID uniqueId = UUID.fromString(json.get("ID"));
            String name = json.get("Name");
            String pName = json.get("Property_Name");
            String pValue = json.get("Property_Value");
            String pSignature = json.get("Property_Signature");

            Multimap<String, Property> propertyMap = LinkedHashMultimap.create();
            propertyMap.put("textures", new Property(pName, pValue, pSignature));

            return GameProfileUtils.createGameProfile(uniqueId, name,
                    GameProfileUtils.createPropertyMap(propertyMap));

        } catch (Exception e) {
            return null;
        }
    }

    public static UUID getUUID(GameProfile gameProfile) {
        IReflection.MethodAccessor getId = IReflection.getMethod(GameProfile.class,
                Version.before(21.10) ? "getId" : "id");

        return (UUID) getId.invoke(gameProfile);
    }

    public static String getName(GameProfile gameProfile) {
        IReflection.MethodAccessor getName = IReflection.getMethod(GameProfile.class,
                Version.before(21.10) ? "getName" : "name");

        return (String) getName.invoke(gameProfile);
    }

    public static PropertyMap createPropertyMap(Multimap<String, Property> properties) {
        if(Version.before(21.10)) {
            PropertyMap map = new PropertyMap();
            map.putAll(properties);
            return map;
        }

        IReflection.ConstructorAccessor constructor = IReflection.getConstructor(PropertyMap.class, Multimap.class);
        return (PropertyMap) constructor.newInstance(properties);
    }

    public static PropertyMap getProperties(GameProfile gameProfile) {
        IReflection.MethodAccessor getProperties = IReflection.getMethod(GameProfile.class, PropertyMap.class, new Class[0]);
        return  (PropertyMap) getProperties.invoke(gameProfile);
    }

    public static GameProfile createGameProfile(UUID uuid, String name, PropertyMap properties) {
        IReflection.ConstructorAccessor[] constructors = IReflection.getConstructors(GameProfile.class);

        if(constructors[0].getConstructor().getParameterCount() == 2) {
            GameProfile profile = new GameProfile(uuid, name);
            profile.getProperties().putAll(properties);
            return profile;
        } else {
            return  (GameProfile) constructors[0].newInstance(uuid, name, properties);
        }
    }

    public static GameProfile getGameProfile(UUID uuid, String name, long timestamp, String signature, String skinUrl, String capeUrl) {
        boolean cape = capeUrl != null && !capeUrl.isEmpty();

        List<Object> args = new ArrayList<>();
        args.add(timestamp);
        args.add(uuid.toString());
        args.add(name);
        args.add(skinUrl);
        if (cape) args.add(capeUrl);

        Multimap<String, Property> properties = MultimapBuilder.linkedHashKeys().arrayListValues().build();
        properties.put("textures", new Property("textures", Base64Coder.encodeString(String.format(cape ? Skin.JSON_CAPE : Skin.JSON_SKIN, args.toArray(new Object[args.size()]))), signature));

        return createGameProfile(uuid, name, createPropertyMap(properties));
    }
}
