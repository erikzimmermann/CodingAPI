package de.codingair.codingapi.tools.items;

import com.mojang.authlib.GameProfile;
import de.codingair.codingapi.player.data.gameprofile.GameProfileUtils;
import de.codingair.codingapi.player.gui.inventory.gui.Skull;
import de.codingair.codingapi.server.Version;
import de.codingair.codingapi.server.reflections.IReflection;
import de.codingair.codingapi.server.reflections.PacketUtils;
import de.codingair.codingapi.server.reflections.PotionData;
import de.codingair.codingapi.tools.OldItemBuilder;
import de.codingair.codingapi.tools.io.lib.JSONObject;
import de.codingair.codingapi.tools.io.utils.DataWriter;
import de.codingair.codingapi.tools.io.utils.Serializable;
import de.codingair.codingapi.tools.io.JSON.JSON;
import de.codingair.codingapi.utils.ChatColor;
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
import de.codingair.codingapi.tools.io.lib.JSONArray;
import de.codingair.codingapi.tools.io.lib.JSONParser;
import de.codingair.codingapi.tools.io.lib.ParseException;

import java.util.*;

/**
 * Removing of this disclaimer is forbidden.
 *
 * @author codingair
 * @verions: 1.0.0
 **/

public class ItemBuilder implements Serializable {
    private String name = "";
    private Material type;
    private byte data = 0;
    private short durability = 0;
    private int amount = 1;
    private DyeColor color = null;
    private ItemMeta preMeta = null;
    private PotionData potionData = null;

    private NBTTagCompound nbt = null;
    private int customModel = 0;

    private String skullId = null;
    private List<String> lore = null;
    private HashMap<Enchantment, Integer> enchantments = null;
    private boolean hideStandardLore = false;
    private boolean hideEnchantments = false;
    private boolean hideName = false;
    private boolean unbreakable = false;

    public ItemBuilder() {
    }

    public ItemBuilder(String skullId) {
        this(XMaterial.PLAYER_HEAD);
        setSkullId(skullId);
    }

    public ItemBuilder(XMaterial xMaterial) {
        this(xMaterial.parseItem());
    }

    public ItemBuilder(Material type) {
        this.type = type;
    }

    public ItemBuilder(ItemStack item) {
        this.nbt = new NBTTagCompound(item);
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

            if(Version.getVersion().isBiggerThan(Version.v1_11)) this.unbreakable = preMeta.isUnbreakable();
            if(Version.getVersion().isBiggerThan(Version.v1_13) && (boolean) PacketUtils.hasCustomModelData.invoke(preMeta)) {
                this.customModel = (int) PacketUtils.getCustomModelData.invoke(preMeta);
            }

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
            IReflection.FieldAccessor<?> profile = IReflection.getField(meta.getClass(), "profile");
            this.skullId = GameProfileUtils.extractSkinId((GameProfile) profile.get(meta));
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
                if(meta instanceof LeatherArmorMeta) {
                    LeatherArmorMeta leatherArmorMeta = (LeatherArmorMeta) meta;
                    leatherArmorMeta.setColor(this.color.getColor());
                } else {
                    if(this.type.name().equals("INK_SACK")) this.data = this.color.getDyeData();
                    else this.data = this.color.getWoolData();

                    item.setDurability(this.data);
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

            if(this.skullId != null) {
                try {
                    IReflection.FieldAccessor<GameProfile> profile = IReflection.getField(meta.getClass(), "profile");
                    profile.set(meta, GameProfileUtils.createBySkinId(skullId));
                } catch(IllegalStateException ignored) {
                }
            }

            if(Version.getVersion().isBiggerThan(Version.v1_11)) meta.setUnbreakable(this.unbreakable);
            if(Version.getVersion().isBiggerThan(Version.v1_13)) PacketUtils.setCustomModelData.invoke(meta, this.customModel);

            item.setItemMeta(meta);
        }

        if(this.enchantments != null) item.addUnsafeEnchantments(this.enchantments);

        if(nbt != null) return new NBTTagCompound(item).setNBT(nbt).getItem();
        else return item;
    }

    @Override
    public boolean read(DataWriter d) throws Exception {
        try {
            for(Object key : d.keySet()) {
                String keyName = (String) key;

                switch(keyName) {
                    case "Lore": {
                        JSONArray jsonLore = d.getList("Lore");
                        if(jsonLore == null) break;

                        List<String> lore = new ArrayList<>();

                        for(Object value : jsonLore) {
                            String v = (String) value;
                            lore.add(v == null ? null : ChatColor.translateAlternateColorCodes('&', v));
                        }

                        setLore(lore);
                        break;
                    }

                    case "Color": {
                        JSON jsonColor = d.get("Color");
                        if(jsonColor == null) break;

                        int red = Integer.parseInt(jsonColor.get("Red") + "");
                        int green = Integer.parseInt(jsonColor.get("Green") + "");
                        int blue = Integer.parseInt(jsonColor.get("Blue") + "");

                        setColor(DyeColor.getByColor(Color.fromRGB(red, green, blue)));
                        break;
                    }

                    case "Enchantments": {
                        JSON jsonEnchantments = d.get("Enchantments");
                        if(jsonEnchantments == null) break;

                        for(Object keySet : jsonEnchantments.keySet()) {
                            String name = (String) keySet;
                            Enchantment enchantment = Enchantment.getByName(name);
                            int level = Integer.parseInt(jsonEnchantments.get(name) + "");

                            addEnchantment(enchantment, level);
                        }
                        break;
                    }

                    case "Name": {
                        String name = d.get("Name");
                        if(name == null) break;

                        setName(ChatColor.translateAlternateColorCodes('&', name));
                        break;
                    }

                    case "PotionData": {
                        Object obj = d.getRaw("PotionData");
                        if(obj == null) break;

                        PotionData data = PotionData.fromJSONString((String) obj);
                        setPotionData(data);
                        break;
                    }

                    case "Type": {
                        Object obj = d.get("Type");
                        if(obj == null) break;

                        String name = d.get("Type");
                        Material material = null;

                        try {
                            material = Material.valueOf(name);
                        } catch(IllegalArgumentException ex) {
                            if(Version.getVersion().isBiggerThan(Version.v1_12)) {
                                obj = d.get("Data");
                                byte data = 0;
                                if(obj != null) data = Byte.parseByte(d.get("Data") + "");

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
                                setData((byte) xType.data);
                            }

                            try {
                                material = Material.valueOf(name);
                            } catch(IllegalArgumentException ex2) {
                                if(name.toUpperCase().equals("SPLASH_POTION")) {
                                    material = Material.POTION;
                                }
                            }
                        }

                        setType(material);
                        break;
                    }

                    case "Data": {
                        Object obj = d.get("Data");
                        if(obj == null || getData() == 0) break;

                        setData(Byte.parseByte(d.get("Data") + ""));
                        break;
                    }

                    case "Durability": {
                        Object obj = d.get("Durability");
                        if(obj == null) break;

                        setDurability(Short.parseShort(d.get("Durability") + ""));
                        break;
                    }

                    case "Amount": {
                        Integer amount = d.get("Amount");
                        if(amount == null) break;

                        setAmount(amount);
                        break;
                    }

                    case "HideStandardLore": {
                        Object obj = d.get("HideStandardLore");
                        if(obj == null) break;

                        setHideStandardLore(d.get("HideStandardLore"));
                        break;
                    }

                    case "HideEnchantments": {
                        Object obj = d.get("HideEnchantments");
                        if(obj == null) break;

                        setHideEnchantments(d.get("HideEnchantments"));
                        break;
                    }

                    case "Unbreakable": {
                        Object obj = d.get("Unbreakable");
                        if(obj == null) break;

                        setUnbreakable(d.get("Unbreakable"));
                        break;
                    }

                    case "HideName": {
                        Object obj = d.get("HideName");
                        if(obj == null) break;

                        setHideName(d.get("HideName"));
                        break;
                    }

                    case "SkullOwner": {
                        String data = d.getRaw("SkullOwner");
                        if(data == null) break;

                        if(data.contains("Property_Signature")) {
                            //old
                            setSkullId(GameProfileUtils.extractSkinId(GameProfileUtils.gameProfileFromJSON(data)));
                        } else setSkullId((String) d.get("SkullOwner"));
                        break;
                    }

                    case "CustomModel": {
                        setCustomModel(d.getInteger("CustomModel"));
                        break;
                    }
                }
            }

            return true;
        } catch(Exception ex) {
            ex.printStackTrace();
            return false;
        }
    }

    @Override
    public void write(DataWriter d) {
        JSON color = new JSON();
        JSON enchantments = new JSON();
        JSONArray lore = new JSONArray();

        if(this.enchantments != null) {
            for(Enchantment ench : this.enchantments.keySet()) {
                enchantments.put(ench.getName(), this.enchantments.get(ench));
            }
        }

        if(this.lore != null) {
            for(String s : this.lore) {
                lore.add(s.replace("§", "&"));
            }
        }

        if(this.color != null) {
            color.put("Red", this.color.getColor().getRed());
            color.put("Green", this.color.getColor().getGreen());
            color.put("Blue", this.color.getColor().getBlue());
        }

        if(this.name != null) d.put("Name", this.name.replace("§", "&"));
        if(this.type != null) d.put("Type", this.type.name());
        d.put("Data", this.data);
        d.put("Durability", this.durability);
        d.put("Amount", this.amount);
        if(this.lore != null) d.put("Lore", lore.isEmpty() ? null : lore);
        if(this.color != null) d.put("Color", color.isEmpty() ? null : color.toJSONString());
        if(this.enchantments != null)
            d.put("Enchantments", enchantments.isEmpty() ? null : enchantments.toJSONString());
        d.put("HideStandardLore", this.hideStandardLore);
        d.put("HideEnchantments", this.hideEnchantments);
        d.put("Unbreakable", this.unbreakable);
        d.put("HideName", this.hideName);
        d.put("PotionData", this.potionData == null ? null : this.potionData.toJSONString());

        d.put("SkullOwner", this.skullId);
        d.put("CustomModel", this.customModel);
    }

    @Override
    public boolean equals(Object o) {
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;
        ItemBuilder builder = (ItemBuilder) o;
        return data == builder.data &&
                durability == builder.durability &&
                amount == builder.amount &&
                customModel == builder.customModel &&
                hideStandardLore == builder.hideStandardLore &&
                hideEnchantments == builder.hideEnchantments &&
                hideName == builder.hideName &&
                unbreakable == builder.unbreakable &&
                Objects.equals(name, builder.name) &&
                type == builder.type &&
                color == builder.color &&
                Objects.equals(preMeta, builder.preMeta) &&
                Objects.equals(potionData, builder.potionData) &&
                Objects.equals(nbt, builder.nbt) &&
                Objects.equals(skullId, builder.skullId) &&
                Objects.equals(lore, builder.lore) &&
                Objects.equals(enchantments, builder.enchantments);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, type, data, durability, amount, color, preMeta, potionData, nbt, customModel, skullId, lore, enchantments, hideStandardLore, hideEnchantments, hideName, unbreakable);
    }

    @Override
    public void destroy() {

    }

    public String toJSONString() {
        JSON jsonObject = new JSON();
        write(jsonObject);
        return jsonObject.toJSONString();
    }

    public static ItemBuilder getFromJSON(String data) {
        JSONParser parser = new JSONParser();
        try {
            JSON jsonObject = new JSON((JSONObject) parser.parse(data));

            return getFromJSON(jsonObject);
        } catch(ParseException e) {
            e.printStackTrace();
            return new ItemBuilder(Material.AIR);
        }
    }

    public static ItemBuilder getFromJSON(JSON jsonObject) {
        if(jsonObject == null) return new ItemBuilder(Material.AIR);

        ItemBuilder builder = new ItemBuilder();
        try {
            builder.read(jsonObject);
            return builder;
        } catch(Exception e) {
            e.printStackTrace();
            return new ItemBuilder(XMaterial.AIR);
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

    public ItemBuilder setType(XMaterial type) {
        return setType(type.parseMaterial());
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
        this.enchantments.remove(enchantment);
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

    public ItemBuilder setPreMeta(ItemMeta preMeta) {
        this.preMeta = preMeta;
        return this;
    }

    public PotionData getPotionData() {
        return potionData;
    }

    public void setPotionData(PotionData potionData) {
        this.potionData = potionData;
    }

    public ItemBuilder clone() {
        ItemBuilder clone = new ItemBuilder();

        clone.name = name;
        clone.type = type;
        clone.data = data;
        clone.durability = durability;
        clone.amount = amount;
        clone.color = color;
        clone.preMeta = preMeta;
        clone.potionData = potionData;
        clone.nbt = nbt;
        clone.customModel = customModel;
        clone.skullId = skullId;
        clone.lore = lore == null ? null : new ArrayList<>(lore);
        clone.enchantments = enchantments == null ? null : new HashMap<>(enchantments);
        clone.hideStandardLore = hideStandardLore;
        clone.hideEnchantments = hideEnchantments;
        clone.hideName = hideName;
        clone.unbreakable = unbreakable;

        return clone;
    }

    public String getSkullId() {
        return skullId;
    }

    public ItemBuilder setSkullId(String skullId) {
        this.skullId = skullId;
        return this;
    }

    public ItemBuilder setSkullId(Player player) {
        this.skullId = GameProfileUtils.extractSkinId(GameProfileUtils.getGameProfile(player));
        return this;
    }

    public boolean isUnbreakable() {
        return this.unbreakable;
    }

    public ItemBuilder setUnbreakable(boolean unbreakable) {
        this.unbreakable = unbreakable;
        return this;
    }

    public int getCustomModel() {
        return customModel;
    }

    public ItemBuilder setCustomModel(int customModel) {
        this.customModel = customModel;
        return this;
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
