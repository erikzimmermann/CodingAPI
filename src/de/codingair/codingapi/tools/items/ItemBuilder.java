package de.codingair.codingapi.tools.items;

import com.mojang.authlib.GameProfile;
import de.codingair.codingapi.player.data.gameprofile.GameProfileUtils;
import de.codingair.codingapi.player.gui.inventory.gui.Skull;
import de.codingair.codingapi.server.Version;
import de.codingair.codingapi.server.reflections.IReflection;
import de.codingair.codingapi.server.reflections.PotionData;
import de.codingair.codingapi.tools.OldItemBuilder;
import de.codingair.codingapi.utils.TextAlignment;
import org.bukkit.Color;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.material.MaterialData;
import org.bukkit.potion.Potion;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.util.*;

/**
 * Removing of this disclaimer is forbidden.
 *
 * @author codingair
 * @verions: 1.0.0
 **/

public class ItemBuilder {
    private String name = "";
    private Material type;
    private byte data = 0;
    private short durability = 0;
    private int amount = 1;
    private DyeColor color = null;
    private ItemMeta preMeta = null;
    private PotionData potionData = null;

    private GameProfile skullOwner = null;
    private List<String> lore = null;
    private HashMap<Enchantment, Integer> enchantments = null;
    private boolean hideStandardLore = false;
    private boolean hideEnchantments = false;
    private boolean hideName = false;

    public ItemBuilder() {
    }

    public ItemBuilder(XMaterial xMaterial) {
        this(xMaterial.parseItem());
    }

    public ItemBuilder(Material type) {
        this.type = type;
    }

    public ItemBuilder(ItemStack item) {
        this.type = item.getType();
        this.data = item.getData().getData();
        this.durability = item.getDurability();
        this.amount = item.getAmount();

        if(item.getEnchantments().size() > 0) {
            enchantments = new HashMap<>();
            enchantments.putAll(item.getEnchantments());
        }

        if(item.hasItemMeta()) {
            this.preMeta = item.getItemMeta();
            this.name = item.getItemMeta().getDisplayName();

            if(enchantments == null) enchantments = new HashMap<>();
            enchantments.putAll(item.getItemMeta().getEnchants());
            item.getItemMeta().getEnchants().forEach((ench, level) -> this.preMeta.removeEnchant(ench));

            try {
                LeatherArmorMeta meta = (LeatherArmorMeta) item.getItemMeta();
                this.color = DyeColor.getByColor(meta.getColor());
            } catch(Exception ignored) {
            }

            if(item.getItemMeta().hasLore()) {
                lore = new ArrayList<>();
                lore.addAll(item.getItemMeta().getLore());
            }
            this.hideEnchantments = item.getItemMeta().hasItemFlag(ItemFlag.HIDE_ENCHANTS);
            this.hideStandardLore = (item.getItemMeta().getItemFlags().size() == 1 && !item.getItemMeta().hasItemFlag(ItemFlag.HIDE_ENCHANTS)) || item.getItemMeta().getItemFlags().size() > 1;
            if(item.getItemMeta().getDisplayName() != null && item.getItemMeta().getDisplayName().equals("§0")) {
                this.hideName = true;
                this.name = null;
            }
        }

        if(item.getType().name().toUpperCase().contains("POTION")) {
            this.potionData = new PotionData(item);
        }

        try {
            ItemMeta meta = item.getItemMeta();
            IReflection.FieldAccessor profile = IReflection.getField(meta.getClass(), "profile");
            this.skullOwner = (GameProfile) profile.get(meta);
        } catch(Exception ignored) {
        }
    }

    public ItemBuilder(Material type, ItemMeta itemMeta) {
        this(type);
        this.preMeta = itemMeta;
    }

    public ItemBuilder(ItemStack item, ItemMeta itemMeta) {
        this(item);
        this.preMeta = itemMeta;
    }

    /**
     * This constructor create a head
     *
     * @param profile GameProfile
     */
    public ItemBuilder(GameProfile profile) {
        this(OldItemBuilder.getHead(profile));
    }

    /**
     * This constructor create a head
     *
     * @param player Player
     */
    public ItemBuilder(Player player) {
        this(GameProfileUtils.getGameProfile(player));
    }

    public ItemBuilder(Skull skull) {
        this(skull.getItemStack(), skull.getItemMeta());
    }

    public org.bukkit.inventory.ItemStack getItem() {
        org.bukkit.inventory.ItemStack item = new org.bukkit.inventory.ItemStack(this.type);

        if(this.type.name().contains("POTION")) {
            PotionMeta meta = (PotionMeta) item.getItemMeta();

            if(potionData != null) {
                if(Version.getVersion().isBiggerThan(Version.v1_8) && this.potionData.isCorrect()) {
                    meta = this.potionData.getMeta();
                } else if(!Version.getVersion().isBiggerThan(Version.v1_8) && this.potionData.isCorrect()) {
                    Potion potion = this.potionData.getPotion();
                    meta.setMainEffect(potion.getType().getEffectType());
                }
            }

            item.setItemMeta(meta);
        }

        if((this.type.name().contains("SKULL") || this.type.name().contains("HEAD")) && this.data == 3) {
            item = new ItemStack(this.type, 1, (short) 3);
        }

        item.setAmount(this.amount);

        ItemMeta meta = preMeta == null ? item.getItemMeta() : this.preMeta;
        if(meta != null) {
            meta.setDisplayName(this.name);
            meta.setLore(this.lore);

            if(isColorable() && this.color != null) {
                if(LeatherArmorMeta.class.isInstance(meta)) {
                    LeatherArmorMeta leatherArmorMeta = (LeatherArmorMeta) meta;
                    leatherArmorMeta.setColor(this.color.getColor());
                } else {
                    if(this.type.equals(Material.INK_SACK)) this.data = this.color.getDyeData();
                    else this.data = this.color.getWoolData();

                    item.setDurability((short) this.data);
                }
            } else {
                MaterialData data = this.data == 0 ? null : new MaterialData(this.type, this.data);
                item.setData(data);
                item.setDurability(this.durability);
            }

            if(hideName || this.name == null) meta.setDisplayName("§0");
            if(hideEnchantments) meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            if(hideStandardLore) {
                for(ItemFlag itemFlag : ItemFlag.values()) {
                    if(itemFlag.equals(ItemFlag.HIDE_ENCHANTS)) continue;

                    meta.addItemFlags(itemFlag);
                }
            }

            if(this.skullOwner != null) {
                IReflection.FieldAccessor profile = IReflection.getField(meta.getClass(), "profile");
                profile.set(meta, this.skullOwner);
            }

            item.setItemMeta(meta);
        }

        if(this.enchantments != null) item.addUnsafeEnchantments(this.enchantments);

        return item;
    }

    public String toJSONString() {
        JSONObject jsonObject = new JSONObject();
        JSONObject color = new JSONObject();
        JSONObject enchantments = new JSONObject();
        JSONArray lore = new JSONArray();

        if(this.enchantments != null) {
            for(Enchantment ench : this.enchantments.keySet()) {
                enchantments.put(ench.getName(), this.enchantments.get(ench));
            }
        }

        if(this.lore != null) {
            lore.addAll(this.lore);
        }

        if(this.color != null) {
            color.put("Red", this.color.getColor().getRed());
            color.put("Green", this.color.getColor().getGreen());
            color.put("Blue", this.color.getColor().getBlue());
        }

        if(this.name != null) jsonObject.put("Name", this.name.replace("§", "&"));
        if(this.type != null) jsonObject.put("Type", this.type.name());
        jsonObject.put("Data", this.data);
        jsonObject.put("Durability", this.durability);
        jsonObject.put("Amount", this.amount);
        if(this.lore != null) jsonObject.put("Lore", lore.isEmpty() ? null : lore.toJSONString());
        if(this.color != null) jsonObject.put("Color", color.isEmpty() ? null : color.toJSONString());
        if(this.enchantments != null)
            jsonObject.put("Enchantments", enchantments.isEmpty() ? null : enchantments.toJSONString());
        jsonObject.put("HideStandardLore", this.hideStandardLore);
        jsonObject.put("HideEnchantments", this.hideEnchantments);
        jsonObject.put("HideName", this.hideName);
        jsonObject.put("PotionData", this.potionData == null ? null : this.potionData.toJSONString());

        jsonObject.put("SkullOwner", this.skullOwner == null ? null : GameProfileUtils.gameProfileToString(this.skullOwner));

        return jsonObject.toJSONString();
    }

    public static ItemBuilder getFromJSON(String code) {
        if(code == null) return new ItemBuilder(Material.AIR);

        try {
            ItemBuilder item = new ItemBuilder();
            JSONParser parser = new JSONParser();
            JSONObject jsonObject = (JSONObject) parser.parse(code);

            for(Object key : jsonObject.keySet()) {
                String keyName = (String) key;

                switch(keyName) {
                    case "Lore": {
                        Object obj = jsonObject.get("Lore");
                        if(obj == null) break;

                        String loreCode = (String) jsonObject.get("Lore");
                        JSONArray jsonLore = (JSONArray) parser.parse(loreCode);
                        List<String> lore = new ArrayList<>();

                        for(Object value : jsonLore) {
                            String v = (String) value;
                            lore.add(v);
                        }

                        item.setLore(lore);
                        break;
                    }

                    case "Color": {
                        Object obj = jsonObject.get("Color");
                        if(obj == null) break;

                        String loreCode = (String) jsonObject.get("Color");
                        JSONObject jsonColor = (JSONObject) parser.parse(loreCode);
                        int red = Integer.parseInt(jsonColor.get("Red") + "");
                        int green = Integer.parseInt(jsonColor.get("Green") + "");
                        int blue = Integer.parseInt(jsonColor.get("Blue") + "");

                        item.setColor(DyeColor.getByColor(Color.fromRGB(red, green, blue)));
                        break;
                    }

                    case "Enchantments": {
                        Object obj = jsonObject.get("Enchantments");
                        if(obj == null) break;

                        String enchantmentCode = (String) obj;
                        JSONObject jsonEnchantments = (JSONObject) parser.parse(enchantmentCode);

                        for(Object keySet : jsonEnchantments.keySet()) {
                            String name = (String) keySet;
                            Enchantment enchantment = Enchantment.getByName(name);
                            int level = Integer.parseInt(jsonEnchantments.get(name) + "");

                            item.addEnchantment(enchantment, level);
                        }
                        break;
                    }

                    case "Name": {
                        Object obj = jsonObject.get("Name");
                        if(obj == null) break;

                        item.setName(((String) jsonObject.get("Name")).replace("&", "§"));
                        break;
                    }

                    case "PotionData": {
                        Object obj = jsonObject.get("PotionData");
                        if(obj == null) break;

                        PotionData data = PotionData.fromJSONString((String) obj);
                        item.setPotionData(data);
                        break;
                    }

                    case "Type": {
                        Object obj = jsonObject.get("Type");
                        if(obj == null) break;

                        String name = (String) jsonObject.get("Type");
                        Material material = null;

                        try {
                            material = Material.valueOf(name);
                        } catch(IllegalArgumentException ex) {
                            if(Version.getVersion().isBiggerThan(Version.v1_12)) {
                                obj = jsonObject.get("Data");
                                byte data = 0;
                                if(obj != null) data = Byte.parseByte(jsonObject.get("Data") + "");

                                XMaterial mat = XMaterial.requestXMaterial(name, data);

                                if(mat == null && name.equalsIgnoreCase("SKULL_ITEM")) {
                                    mat = XMaterial.PLAYER_HEAD;
                                } else if(mat == null) {
                                    throw new IllegalAccessException("Couldn't find material (" + name + ", " + data + ")!");
                                }

                                if(mat != null) material = mat.parseMaterial();
                            } else {
                                XMaterial xType = XMaterial.valueOf(name);
                                material = xType.parseMaterial();
                                item.setData((byte) xType.data);
                            }

                            try {
                                material = Material.valueOf(name);
                            } catch(IllegalArgumentException ex2) {
                                if(name.toUpperCase().equals("SPLASH_POTION")) {
                                    material = Material.POTION;
                                }
                            }
                        }

                        item.setType(material);
                        break;
                    }

                    case "Data": {
                        Object obj = jsonObject.get("Data");
                        if(obj == null || item.getData() == 0) break;

                        item.setData(Byte.parseByte(jsonObject.get("Data") + ""));
                        break;
                    }

                    case "Durability": {
                        Object obj = jsonObject.get("Durability");
                        if(obj == null) break;

                        item.setDurability(Short.parseShort(jsonObject.get("Durability") + ""));
                        break;
                    }

                    case "Amount": {
                        Object obj = jsonObject.get("Amount");
                        if(obj == null) break;

                        item.setAmount(Integer.parseInt(jsonObject.get("Amount") + ""));
                        break;
                    }

                    case "HideStandardLore": {
                        Object obj = jsonObject.get("HideStandardLore");
                        if(obj == null) break;

                        item.setHideStandardLore((boolean) jsonObject.get("HideStandardLore"));
                        break;
                    }

                    case "HideEnchantments": {
                        Object obj = jsonObject.get("HideEnchantments");
                        if(obj == null) break;

                        item.setHideEnchantments((boolean) jsonObject.get("HideEnchantments"));
                        break;
                    }

                    case "HideName": {
                        Object obj = jsonObject.get("HideName");
                        if(obj == null) break;

                        item.setHideName((boolean) jsonObject.get("HideName"));
                        break;
                    }

                    case "SkullOwner": {
                        Object obj = jsonObject.get("SkullOwner");
                        if(obj == null) break;

                        GameProfile pf = GameProfileUtils.gameProfileFromJSON((String) obj);
                        item.setSkullOwner(pf);
                        break;
                    }
                }
            }

            return item;
        } catch(Exception ex) {
            ex.printStackTrace();
            return new ItemBuilder(Material.AIR);
        }
    }

    public String toBase64String() {
        String code = toJSONString();
        if(code == null) return null;

        return Base64.getEncoder().encodeToString(code.getBytes());
    }

    public static ItemBuilder getFromBase64String(String code) {
        if(code == null) return null;
        return getFromJSON(new String(Base64.getDecoder().decode(code.getBytes())));
    }

    public ItemBuilder reset(boolean onlyAppearance) {
        if(onlyAppearance) {
            setHideName(false);
            setHideEnchantments(false);
            setHideStandardLore(false);
        } else {
            this.name = null;
            this.data = 0;
            this.amount = 1;
            this.color = null;
            this.lore = null;
            this.enchantments = null;
            this.hideStandardLore = true;
            this.hideEnchantments = true;
            this.hideName = true;
        }

        return this;
    }

    public boolean isColorable() {
        switch(this.type.name()) {
            case "INK_SACK":
            case "CARPET":
            case "WOOL":
            case "STAINED_GLASS":
            case "STAINED_CLAY":
            case "STAINED_GLASS_PANE":
            case "LEATHER_HELMET":
            case "LEATHER_CHESTPLATE":
            case "LEATHER_LEGGINGS":
            case "LEATHER_BOOTS":
                return true;
            default:
                return false;
        }
    }

    public DyeColor getColor() {
        return color;
    }

    public ItemBuilder setColor(DyeColor color) {
        this.color = color;
        return this;
    }

    public String getName() {
        return name;
    }

    public ItemBuilder setName(String name) {
        this.name = name;
        return this;
    }

    /**
     * Moves the lore at the top of the Item text, if the given name is null.
     *
     * @return ItemBuilder instance
     */
    public ItemBuilder checkFirstLine() {
        if(this.name == null && this.lore != null && !this.lore.isEmpty()) {
            setName(this.lore.remove(0));
        }

        setHideName(this.name == null);
        return this;
    }

    public ItemBuilder setText(String text, int wordWrap) {
        return setText(text, TextAlignment.LEFT, wordWrap);
    }

    public ItemBuilder setText(String text, TextAlignment alignment, int wordWrap) {
        List<String> list = TextAlignment.lineBreak(text, wordWrap);
        if(list.isEmpty()) return this;

        if(alignment != null) list = alignment.apply(list);

        setName(list.remove(0));
        setLore(list);
        return this;
    }

    public ItemBuilder setText(String... text) {
        return this.setText(new ArrayList<>(Arrays.asList(text)));
    }

    public ItemBuilder setText(List<String> text) {
        setName(null);
        removeLore();

        if(text.isEmpty()) return this;
        setName(text.remove(0));
        if(!text.isEmpty()) setLore(text);
        return this;
    }

    public ItemBuilder addText(String... text) {
        return this.addText(Arrays.asList(text));
    }

    public ItemBuilder addText(String text, int wordWrap) {
        List<String> list = TextAlignment.lineBreak(text, wordWrap);
        if(list.isEmpty()) return this;

        if(this.name == null || this.name.isEmpty()) {
            setName(list.remove(0));
            if(this.name != null && !this.name.isEmpty()) setHideName(false);
        }

        addLore(list);
        return this;
    }

    public ItemBuilder addText(List<String> text) {
        if(text.isEmpty()) return this;
        text = new ArrayList<>(text);

        if(this.name == null || this.name.isEmpty()) {
            this.name = text.remove(0);
            if(this.name != null && !this.name.isEmpty()) setHideName(false);
        }

        addLore(text);
        return this;
    }

    public ItemBuilder removeText(List<String> text) {
        if(text.isEmpty()) return this;

        for(String s : text) {
            if(this.name != null && this.name.equals(s)) this.name = null;
            this.lore.removeAll(text);
        }

        return this;
    }

    public Material getType() {
        return type;
    }

    public ItemBuilder setType(Material type) {
        this.type = type;
        return this;
    }

    public byte getData() {
        return data;
    }

    public ItemBuilder setData(byte data) {
        this.data = data;
        if(getType() != null && getType().equals(Material.POTION)) setDurability(getData());
        return this;
    }

    public short getDurability() {
        return durability;
    }

    public ItemBuilder setDurability(short durability) {
        this.durability = durability;
        return this;
    }

    public List<String> getLore() {
        return lore;
    }

    public ItemBuilder setLore(List<String> lore) {
        if(this.lore != null) this.lore.clear();
        else this.lore = new ArrayList<>();

        if(lore == null) return this;

        for(String s : lore) {
            if(s != null) this.lore.add(s);
        }
        return this;
    }

    public ItemBuilder setLore(String... lore) {
        return setLore(Arrays.asList(lore));
    }

    public ItemBuilder addLore(List<String> lore) {
        if(this.lore == null) this.lore = new ArrayList<>();

        if(lore == null) return this;

        for(String s : lore) {
            if(s != null) this.lore.add(s);
        }
        return this;
    }

    public ItemBuilder addLore(int index, List<String> lore) {
        if(this.lore == null) this.lore = new ArrayList<>();

        if(lore == null) return this;

        for(String s : lore) {
            if(s != null) this.lore.add(index++, s);
        }
        return this;
    }

    public ItemBuilder addLore(String... lore) {
        return addLore(Arrays.asList(lore));
    }

    public ItemBuilder addLore(int index, String... lore) {
        return addLore(index, Arrays.asList(lore));
    }

    public ItemBuilder removeLore(List<String> lore) {
        if(this.lore == null) return this;
        this.lore.removeAll(lore);
        return this;
    }

    public HashMap<Enchantment, Integer> getEnchantments() {
        return enchantments;
    }

    public ItemBuilder setEnchantments(HashMap<Enchantment, Integer> enchantments) {
        if(this.enchantments != null) this.enchantments.clear();
        this.enchantments = enchantments;
        return this;
    }

    public ItemBuilder addEnchantment(Enchantment enchantment, int level) {
        if(this.enchantments == null) this.enchantments = new HashMap<>();
        if(this.enchantments.containsKey(enchantment)) this.enchantments.remove(enchantment);
        this.enchantments.put(enchantment, level);
        return this;
    }

    public boolean isHideStandardLore() {
        return hideStandardLore;
    }

    public ItemBuilder setHideStandardLore(boolean hideStandardLore) {
        this.hideStandardLore = hideStandardLore;
        return this;
    }

    public boolean isHideEnchantments() {
        return hideEnchantments;
    }

    public ItemBuilder setHideEnchantments(boolean hideEnchantments) {
        this.hideEnchantments = hideEnchantments;
        return this;
    }

    public boolean isHideName() {
        return hideName;
    }

    public ItemBuilder setHideName(boolean hideName) {
        this.hideName = hideName;
        return this;
    }

    public int getAmount() {
        return amount;
    }

    public ItemBuilder setAmount(int amount) {
        this.amount = amount;
        return this;
    }

    public ItemBuilder removeLore() {
        this.lore = null;
        return this;
    }

    public ItemBuilder removeLore(int includingStart, int excludingEnd) {
        for(int i = excludingEnd - 1; i >= includingStart; i--) {
            this.lore.remove(i);
        }

        return this;
    }

    public ItemBuilder removeEnchantments() {
        this.enchantments = null;
        return this;
    }

    public ItemMeta getPreMeta() {
        return preMeta;
    }

    public PotionData getPotionData() {
        return potionData;
    }

    public void setPotionData(PotionData potionData) {
        this.potionData = potionData;
    }

    public ItemBuilder clone() {
        return new ItemBuilder(getItem(), getItem().getItemMeta());
    }

    public GameProfile getSkullOwner() {
        return skullOwner;
    }

    public void setSkullOwner(GameProfile skullOwner) {
        this.skullOwner = skullOwner;
    }

    public static ItemStack getHead(GameProfile gameProfile) {
        ItemStack item = new ItemStack(XMaterial.PLAYER_HEAD.parseMaterial(true), 1, (short) 3);
        if(gameProfile == null) return item;

        SkullMeta meta = (SkullMeta) item.getItemMeta();

        IReflection.FieldAccessor profile = IReflection.getField(meta.getClass(), "profile");
        profile.set(meta, gameProfile);

        item.setItemMeta(meta);

        return item;
    }
}
