package de.CodingAir.v1_6.CodingAPI.Tools;

import org.bukkit.Color;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.material.MaterialData;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.util.*;

/**
 * Removing of this disclaimer is forbidden.
 *
 * @author CodingAir
 * @verions: 1.0.0
 **/

public class ItemBuilder {
	private String name = null;
	private Material type;
	private byte data = 0;
	private int amount = 1;
	private DyeColor color = null;
	
	private List<String> lore = null;
	private HashMap<Enchantment, Integer> enchantments = null;
	private boolean hideStandardLore = false;
	private boolean hideEnchantments = false;
	private boolean hideName = false;
	
	public ItemBuilder(Material type) {
		this.type = type;
	}
	
	public org.bukkit.inventory.ItemStack getItem() {
		org.bukkit.inventory.ItemStack item = new org.bukkit.inventory.ItemStack(this.type);
		
		item.setAmount(this.amount);
		
		ItemMeta meta = item.getItemMeta();
		if(this.name != null) meta.setDisplayName(this.name);
		if(this.lore != null) meta.setLore(this.lore);
		
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
			MaterialData data = new MaterialData(this.type, this.data);
			item.setData(data);
		}
		
		if(this.enchantments != null) item.addUnsafeEnchantments(this.enchantments);
		
		if(hideName) meta.setDisplayName("§0");
		if(hideEnchantments) meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
		if(hideStandardLore) meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
		
		item.setItemMeta(meta);
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
		jsonObject.put("Amount", this.amount);
		if(this.lore != null) jsonObject.put("Lore", lore.isEmpty() ? null : lore.toJSONString());
		if(this.color != null) jsonObject.put("Color", color.isEmpty() ? null : color.toJSONString());
		if(this.enchantments != null)
			jsonObject.put("Enchantments", enchantments.isEmpty() ? null : enchantments.toJSONString());
		jsonObject.put("HideStandardLore", this.hideStandardLore);
		jsonObject.put("HideEnchantments", this.hideEnchantments);
		jsonObject.put("HideName", this.hideName);
		
		return jsonObject.toJSONString();
	}
	
	public static ItemBuilder getFromJSON(String code) {
		if(code == null) return null;
		
		try {
			ItemBuilder item = new ItemBuilder(null);
			JSONParser parser = new JSONParser();
			JSONObject jsonObject = (JSONObject) parser.parse(code);
			
			for(Object key : jsonObject.keySet()) {
				String keyName = (String) key;
				
				switch(keyName) {
					case "Lore": {
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
						String loreCode = (String) jsonObject.get("Color");
						JSONObject jsonColor = (JSONObject) parser.parse(loreCode);
						int red = Integer.parseInt(jsonColor.get("Red") + "");
						int green = Integer.parseInt(jsonColor.get("Green") + "");
						int blue = Integer.parseInt(jsonColor.get("Blue") + "");
						
						item.setColor(DyeColor.getByColor(Color.fromRGB(red, green, blue)));
						break;
					}
					
					case "Enchantments": {
						String loreCode = (String) jsonObject.get("Enchantments");
						JSONObject jsonEnchantments = (JSONObject) parser.parse(loreCode);
						
						for(Object keySet : jsonEnchantments.keySet()) {
							String name = (String) keySet;
							Enchantment enchantment = Enchantment.getByName(name);
							int level = Integer.parseInt(jsonEnchantments.get(name) + "");
							
							item.addEnchantment(enchantment, level);
						}
						break;
					}
					
					case "Name": {
						item.setName(((String) jsonObject.get("Name")).replace("&", "§"));
						break;
					}
					
					case "Type": {
						item.setType(Material.valueOf((String) jsonObject.get("Type")));
						break;
					}
					
					case "Data": {
						item.setData(Byte.parseByte(jsonObject.get("Data") + ""));
						break;
					}
					
					case "Amount": {
						item.setAmount(Integer.parseInt(jsonObject.get("Amount") + ""));
						break;
					}
					
					case "HideStandardLore": {
						item.setHideStandardLore((boolean) jsonObject.get("HideStandardLore"));
						break;
					}
					
					case "HideEnchantments": {
						item.setHideEnchantments((boolean) jsonObject.get("HideEnchantments"));
						break;
					}
					
					case "HideName": {
						item.setHideName((boolean) jsonObject.get("HideName"));
						break;
					}
				}
			}
			
			return item;
		} catch(Exception ex) {
			ex.printStackTrace();
			return null;
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
		switch(this.type) {
			case INK_SACK:
			case CARPET:
			case WOOL:
			case STAINED_GLASS:
			case STAINED_CLAY:
			case STAINED_GLASS_PANE:
			case LEATHER_HELMET:
			case LEATHER_CHESTPLATE:
			case LEATHER_LEGGINGS:
			case LEATHER_BOOTS:
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
		return this;
	}
	
	public List<String> getLore() {
		return lore;
	}
	
	public ItemBuilder setLore(List<String> lore) {
		if(this.lore != null) this.lore.clear();
		this.lore = lore;
		return this;
	}
	
	public ItemBuilder setLore(String... lore) {
		return setLore(Arrays.asList(lore));
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
}
