package de.codingair.codingapi.player.data;

import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.mojang.authlib.properties.PropertyMap;
import de.codingair.codingapi.server.reflections.IReflection;
import de.codingair.codingapi.tools.io.lib.JSONArray;
import de.codingair.codingapi.tools.io.lib.JSONObject;
import de.codingair.codingapi.tools.io.lib.JSONParser;
import de.codingair.codingapi.tools.io.lib.ParseException;
import de.codingair.codingapi.utils.PropertyUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Base64;
import java.util.Scanner;
import java.util.UUID;

public abstract class Skin {
    public static final String JSON_SKIN = "{\"timestamp\":%d,\"profileId\":\"%s\",\"profileName\":\"%s\",\"textures\":{\"SKIN\":{\"url\":\"%s\"}}}";
    public static final String JSON_CAPE = "{\"timestamp\":%d,\"profileId\":\"%s\",\"profileName\":\"%s\",\"textures\":{\"SKIN\":{\"url\":\"%s\"},\"CAPE\":{\"url\":\"%s\"}}}";
    private final UUID uuid;
    private final String name;
    private String signature;
    private String value;
    private boolean loaded = false;
    private boolean unsigned = false;
    @Deprecated
    public Skin(UUID uuid, String name) {
        this.uuid = uuid;
        this.name = name;

        this.load();
    }

    @Deprecated
    public Skin(GameProfile profile) {
        this.uuid = profile.getId();
        this.name = profile.getName();

        PropertyMap properties = GameProfileUtils.getProperties(profile);

        properties.get("textures").forEach(property -> {
            this.value = PropertyUtils.getValue(property);
            this.signature = PropertyUtils.getSignature(property);
        });

        loaded = true;
    }

    public Skin(UUID uuid, String name, boolean unsigned) {
        this.uuid = uuid;
        this.name = name;
        this.unsigned = unsigned;

        this.load();
    }

    public Skin(GameProfile profile, boolean unsigned) {
        this.uuid = profile.getId();
        this.name = profile.getName();
        this.unsigned = unsigned;

        GameProfileUtils.getProperties(profile).get("textures").forEach(property -> {
            this.value = PropertyUtils.getValue(property);
            this.signature = PropertyUtils.getSignature(property);
        });

        loaded = true;
    }

    @Deprecated
    public static Skin getSkin(UUID uuid) {
        return new Skin(uuid, "none") {
            @Override
            public void onLoad(Skin skin) {

            }

            @Override
            public void onFail(Skin skin) {

            }
        };
    }

    public static Skin getSkin(UUID uuid, String name) {
        return new Skin(uuid, name) {
            @Override
            public void onLoad(Skin skin) {

            }

            @Override
            public void onFail(Skin skin) {

            }
        };
    }

    public static Skin getSkin(GameProfile profile) {
        return new Skin(profile) {
            @Override
            public void onLoad(Skin skin) {

            }

            @Override
            public void onFail(Skin skin) {

            }
        };
    }

    private void load() {
        try {
            URL url = new URL("https://sessionserver.mojang.com/session/minecraft/profile/" + this.uuid.toString().replace("-", "") + "?unsigned=" + unsigned);
            HttpURLConnection uc = (HttpURLConnection) url.openConnection();
            uc.setUseCaches(false);
            uc.setDefaultUseCaches(false);
            uc.addRequestProperty("User-Agent", "Mozilla/5.0");
            uc.addRequestProperty("Cache-Control", "no-cache, no-store, must-revalidate");
            uc.addRequestProperty("Pragma", "no-cache");
            uc.setRequestMethod("GET");
            uc.connect();

            if (uc.getResponseCode() == 204) {
                // Skin for profile UUID not found - do not continue loading
                return;
            } else if (uc.getResponseCode() != 200) {
                // another error; do not continue loading, but log the error
                throw new RuntimeException("Error loading skin data: " + uc.getResponseCode());
            }

            String json = new Scanner(uc.getInputStream(), "UTF-8").useDelimiter("\\A").next();
            JSONObject data = (JSONObject) new JSONParser().parse(json);
            JSONArray properties = (JSONArray) data.get("properties");

            for (int i = 0; i < properties.size(); i++) {
                try {
                    JSONObject property = (JSONObject) properties.get(i);
                    String value = (String) property.get("value");
                    String signature = property.containsKey("signature") ? (String) property.get("signature") : null;

                    this.value = value;
                    this.signature = signature;

                    this.loaded = true;
                    onLoad(this);
                } catch (Exception e) {
                    onFail(this);
                }
            }
        } catch (Exception e) {
            onFail(this);
        }
    }

    public UUID getUUID() {
        return uuid;
    }

    public String getSignature() {
        return signature;
    }

    public String getName() {
        return name;
    }

    public String getValue() {
        return value;
    }

    public String getDecodedValue() {
        if (this.value == null) return null;
        return new String(Base64.getDecoder().decode(this.value));
    }

    public void encodeValue(String value) {
        this.value = Base64.getEncoder().encodeToString(value.getBytes());
    }

    public boolean isLoaded() {
        return loaded;
    }

    public abstract void onLoad(Skin skin);

    public abstract void onFail(Skin skin);

    public GameProfile modifyProfile(GameProfile profile) {
        if (!this.isLoaded()) return profile;

        PropertyMap properties = GameProfileUtils.getProperties(profile);

        Multimap<String, Property> copy = LinkedHashMultimap.create(properties);

        copy.removeAll("textures");
        copy.put("textures", new Property("textures", this.value, this.signature));

        IReflection.MethodAccessor getId = IReflection.getMethod(GameProfile.class, UUID.class, new Class[0]);
        IReflection.MethodAccessor getName = IReflection.getMethod(GameProfile.class, String.class, new Class[0]);

        UUID uuid = (UUID)getId.invoke(profile);
        String name = (String) getName.invoke(profile);

        return GameProfileUtils.createGameProfile(uuid, name, GameProfileUtils.createPropertyMap(copy));
    }

    @Nullable
    public <T> T getElement(@NotNull SkinElement element) {
        if (!this.isLoaded()) return null;

        String code = getDecodedValue();
        if (code == null) return null;

        //fix malformed JSON
        code = code.replace("{textures:", "{\"textures\":")
                .replace("{SKIN:", "{\"SKIN\":")
                .replace("{url:", "{\"url\":");

        if (code.isEmpty()) return null;

        try {
            JSONObject jsonObject = (JSONObject) new JSONParser().parse(code);

            if (element.equals(SkinElement.TIMESTAMP) || element.equals(SkinElement.PROFILE_ID) || element.equals(SkinElement.PROFILE_NAME))
                return (T) jsonObject.get(element.getName());

            JSONObject texturesJSON = (JSONObject) jsonObject.get("textures");

            if (element.equals(SkinElement.SKIN)) {
                JSONObject skinJSON = (JSONObject) texturesJSON.get("SKIN");

                if (skinJSON == null) return null;

                return (T) skinJSON.get("url");
            } else if (element.equals(SkinElement.CAPE)) {
                JSONObject capeJSON = (JSONObject) texturesJSON.get("CAPE");

                if (capeJSON == null) return null;

                return (T) capeJSON.get("url");
            }

            return null;
        } catch (ParseException e) {
            new IllegalArgumentException("Cannot resolve '" + element.name() + "'-element. Data='" + code + "'", e).printStackTrace();
            return null;
        }
    }

    public void setElement(String src, SkinElement element) {
        String skin = getElement(SkinElement.SKIN);
        String cape = getElement(SkinElement.CAPE);
        long timestamp = getElement(SkinElement.TIMESTAMP);

        if (element.equals(SkinElement.SKIN)) {
            GameProfile gameProfile = GameProfileUtils.getGameProfile(this.uuid, this.name, timestamp, this.signature, src, cape);
            Skin temp = getSkin(gameProfile);
            this.value = temp.getValue();
        } else if (element.equals(SkinElement.CAPE)) {
            GameProfile gameProfile = GameProfileUtils.getGameProfile(this.uuid, this.name, timestamp, this.signature, skin, src);
            Skin temp = getSkin(gameProfile);
            this.value = temp.getValue();
        }
    }

    public void setPublic(boolean isPublic) {

    }

    public enum SkinElement {
        SKIN("SKIN", String.class),
        CAPE("CAPE", String.class),
        TIMESTAMP("timestamp", long.class),
        PROFILE_ID("profileId", String.class),
        PROFILE_NAME("profileName", String.class);

        private final String name;
        private final Class<?> clazz;

        SkinElement(String name, Class<?> clazz) {
            this.name = name;
            this.clazz = clazz;
        }

        public String getName() {
            return name;
        }

        public Class<?> getClazz() {
            return clazz;
        }
    }
}
