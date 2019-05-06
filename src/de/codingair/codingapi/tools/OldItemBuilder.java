package de.codingair.codingapi.tools;

import com.mojang.authlib.GameProfile;
import de.codingair.codingapi.player.data.gameprofile.GameProfileUtils;
import de.codingair.codingapi.player.gui.inventory.gui.Skull;
import de.codingair.codingapi.server.reflections.IReflection;
import de.codingair.codingapi.tools.items.ItemBuilder;
import de.codingair.codingapi.utils.TextAlignment;
import org.bukkit.Color;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.material.MaterialData;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class OldItemBuilder {
	public static ItemStack getItem(Material material) {
		ItemStack item = new ItemStack(material);
		return item;
	}
	
	public static ItemStack getItem(Material material, String name) {
		ItemStack item = new ItemStack(material);
		ItemMeta meta = item.getItemMeta();
		meta.setDisplayName(name);
		item.setItemMeta(meta);
		return item;
	}
	
	public static ItemStack getItem(Material material, String name, boolean lore) {
		ItemStack item = new ItemStack(material);
		ItemMeta meta = item.getItemMeta();
		meta.setDisplayName(name);
		item.setItemMeta(meta);
		
		if(lore) {
			removeLore(item);
			removeEnchantLore(item);
			removeStandardLore(item);
		}
		
		return item;
	}
	
	public static ItemStack getItem(Material material, int data) {
		ItemStack item = new ItemStack(material, 1, (short) data);
		return item;
	}
	
	public static ItemStack getItem(Material material, int data, String name) {
		ItemStack item = new ItemStack(material, 1, (short) data);
		ItemMeta meta = item.getItemMeta();
		meta.setDisplayName(name);
		item.setItemMeta(meta);
		return item;
	}
	
	public static ItemStack getItem(Material material, String name, int amount) {
		ItemStack item = new ItemStack(material);
		item.setAmount(amount);
		ItemMeta meta = item.getItemMeta();
		meta.setDisplayName(name);
		item.setItemMeta(meta);
		return item;
	}
	
	public static ItemStack getItem(Material material, int data, int amount) {
		ItemStack item = new ItemStack(material, 1, (short) data);
		item.setAmount(amount);
		return item;
	}
	
	public static ItemStack getItem(Material material, int data, String name, int amount) {
		ItemStack item = new ItemStack(material, 1, (short) data);
		item.setAmount(amount);
		ItemMeta meta = item.getItemMeta();
		meta.setDisplayName(name);
		item.setItemMeta(meta);
		return item;
	}
	
	@SuppressWarnings("deprecation")
	public static ItemStack getItem(int id) {
		ItemStack item = new ItemStack(id);
		return item;
	}
	
	@SuppressWarnings("deprecation")
	public static ItemStack getItem(int id, String name) {
		ItemStack item = new ItemStack(id);
		ItemMeta meta = item.getItemMeta();
		meta.setDisplayName(name);
		item.setItemMeta(meta);
		return item;
	}
	
	@SuppressWarnings("deprecation")
	public static ItemStack getItem(int id, int data) {
		ItemStack item = new ItemStack(id, 1, (short) data);
		return item;
	}
	
	@SuppressWarnings("deprecation")
	public static ItemStack getItem(int id, int data, String name) {
		ItemStack item = new ItemStack(id, 1, (short) 0, (byte) data);
		ItemMeta meta = item.getItemMeta();
		meta.setDisplayName(name);
		item.setItemMeta(meta);
		return item;
	}
	
	@SuppressWarnings("deprecation")
	public static ItemStack getItem(int id, String name, int amount) {
		ItemStack item = new ItemStack(id);
		item.setAmount(amount);
		ItemMeta meta = item.getItemMeta();
		meta.setDisplayName(name);
		item.setItemMeta(meta);
		return item;
	}
	
	@SuppressWarnings("deprecation")
	public static ItemStack getItem(int id, int data, int amount) {
		ItemStack item = new ItemStack(id, 1, (short) data);
		item.setAmount(amount);
		return item;
	}
	
	@SuppressWarnings("deprecation")
	public static ItemStack getItem(int id, int data, String name, int amount) {
		ItemStack item = new ItemStack(id, 1, (short) data);
		item.setAmount(amount);
		ItemMeta meta = item.getItemMeta();
		meta.setDisplayName(name);
		item.setItemMeta(meta);
		return item;
	}
	
	public static int getAmountOf(Inventory inv, Material m, MaterialData data) {
		int amount = 0;
		for(ItemStack item : inv.getContents()) {
			if(item == null) continue;
			if(item.getType().equals(m) && item.getData().equals(data)) amount += item.getAmount();
		}
		
		return amount;
	}
	
	public static ItemStack getStackOf(Inventory inv, Material m, MaterialData data) {
		for(ItemStack item : inv.getContents()) {
			if(item.getType().equals(m) && item.getData().equals(data) && item.getAmount() == 64) return item;
		}
		
		return null;
	}
	
	public static ItemStack getItemWithMaxAmount(Inventory inv, Material m, MaterialData data) {
		ItemStack item = null;
		
		for(ItemStack items : inv.getContents()) {
			if(items == null) continue;
			if(!items.getType().equals(m) || !items.getData().equals(data)) continue;
			
			if(item == null) {
				item = items;
				continue;
			}
			
			if(item.getAmount() == 64) return item;
			
			if(item.getAmount() < items.getAmount()) item = items;
		}
		
		return item;
	}
	
	@SuppressWarnings("deprecation")
	public static boolean isSameType(ItemStack item, ItemStack other) {
		if(item == null || other == null) return false;
		
		if(!item.hasItemMeta() || !other.hasItemMeta()) {
			return other.getType() == item.getType() && other.getData().getData() == item.getData().getData();
		} else {
			if(item.getItemMeta().hasLore() && other.getItemMeta().hasLore()) {
				int i = 0;
				List<String> lore_ = other.getItemMeta().getLore();
				for(String lore : item.getItemMeta().getLore()) {
					try {
						if(!lore.equalsIgnoreCase(lore_.get(i))) return false;
						i++;
					} catch(IndexOutOfBoundsException ex) {
						return false;
					}
				}
			} else if(item.getItemMeta().hasLore() != other.getItemMeta().hasLore()) return false;
			
			return other.getType() == item.getType() && other.getData().getData() == item.getData().getData() && item.getItemMeta().getDisplayName().equalsIgnoreCase(other.getItemMeta().getDisplayName());
		}
	}
	
	public static ItemStack setItemStackWithoutNameAndLore(ItemStack item) {
		ItemMeta meta = item.getItemMeta();
		meta.setDisplayName("§0");
		meta.setLore(new ArrayList<String>());
		item.setItemMeta(meta);
		return item;
	}
	
	public static ItemStack setLore(ItemStack item, String... loreItems) {
		List<String> lore = new ArrayList<>();
		for(String loreItem : loreItems) {
			while(loreItem.contains("\n")) {
				String part = loreItem.split("\n")[0];
				lore.add(part);
				loreItem = loreItem.replace(part + "\n", "");
			}
			
			while(loreItem.contains("%n%")) {
				String part = loreItem.split("%n%")[0];
				lore.add(part);
				loreItem = loreItem.replace(part + "%n%", "");
			}
			
			lore.add(loreItem);
		}
		
		if(lore.size() == 0 && !item.hasItemMeta()) return item;
		
		ItemMeta meta = item.getItemMeta();
		meta.setLore(lore);
		item.setItemMeta(meta);
		
		return item;
	}
	
	public static ItemStack setLore(ItemStack item, List<String> loreItems) {
		List<String> lore = new ArrayList<>();
		for(String loreItem : loreItems) {
			while(loreItem.contains("\n")) {
				String part = loreItem.split("\n")[0];
				lore.add(part);
				loreItem = loreItem.replace(part + "\n", "");
			}
			
			while(loreItem.contains("%n%")) {
				String part = loreItem.split("%n%")[0];
				lore.add(part);
				loreItem = loreItem.replace(part + "%n%", "");
			}
			
			lore.add(loreItem);
		}
		
		if(lore.size() == 0 && !item.hasItemMeta()) return item;
		
		ItemMeta meta = item.getItemMeta();
		meta.setLore(lore);
		item.setItemMeta(meta);
		
		return item;
	}
	
	public static ItemStack removeLore(ItemStack item) {
		if(!item.hasItemMeta()) return item;
		
		ItemMeta meta = item.getItemMeta();
		meta.setLore(new ArrayList<String>());
		item.setItemMeta(meta);
		
		return item;
	}
	
	public static ItemStack addLore(ItemStack item, List<String> loreItems) {
		return addLore(item, loreItems.toArray(new String[loreItems.size()]));
	}
	
	public static ItemStack addLore(ItemStack item, String... loreItems) {
		ItemMeta meta = item.getItemMeta();
		
		
		List<String> lore = meta.getLore();
		
		if(lore == null) lore = new ArrayList<>();
		
		for(String loreItem : loreItems) {
			while(loreItem.contains("\n")) {
				String part = loreItem.split("\n")[0];
				lore.add(part);
				loreItem = loreItem.replace(part + "\n", "");
			}
			
			while(loreItem.contains("%n%")) {
				String part = loreItem.split("%n%")[0];
				lore.add(part);
				loreItem = loreItem.replace(part + "%n%", "");
			}
			
			lore.add(loreItem);
		}
		
		meta.setLore(lore);
		item.setItemMeta(meta);
		
		return item;
	}
	
	public static ItemStack getHead(Skull skull, String displayName) {
		return OldItemBuilder.setDisplayName(skull.getItemStack(), displayName);
	}
	
	public static ItemStack getHead(Player p, String displayName) {
		return OldItemBuilder.setDisplayName(getHead(GameProfileUtils.getGameProfile(p)), displayName);
	}
	
	public static ItemStack getHead(GameProfile gameProfile, String displayName) {
		return OldItemBuilder.setDisplayName(getHead(gameProfile), displayName);
	}

	public static ItemStack getHead(GameProfile gameProfile) {
		return ItemBuilder.getHead(gameProfile);
	}
	
	@Deprecated
	public static ItemStack getHead(String name, String... loreItems) {
		try {
			ItemStack head = new ItemStack(Material.SKULL_ITEM, 1, (short) 3);
			SkullMeta hMeta = (SkullMeta) head.getItemMeta();
			hMeta.setOwner(name);
			head.setItemMeta(hMeta);
			
			ItemStack item = head;
			ItemMeta meta = item.getItemMeta();
			item.setItemMeta(meta);
			item = setLore(item, loreItems);
			return item;
		} catch(Exception ex) {
			return null;
		}
	}
	
	public static ItemStack setDisplayName(ItemStack item, String displayName) {
		ItemMeta meta = item.getItemMeta();
		meta.setDisplayName(displayName);
		item.setItemMeta(meta);
		return item;
	}
	
	/**
	 * Works with StainedGlass/-Pane, Wool, HardenedClay, Leather-Armor and INK_SACK
	 *
	 * @param material (Material), color (DyeColor)
	 * @return ItemStack
	 */
	public static ItemStack getColored(Material material, String name, DyeColor color) {
		if(name != null) return getColored(getItem(material, name), color);
		else return getColored(getItem(material), color);
	}
	
	/**
	 * Works with StainedGlass/-Pane, Wool, HardenedClay, Leather-Armor and INK_SACK
	 *
	 * @param item (ItemStack), color (DyeColor)
	 * @return ItemStack
	 */
	public static ItemStack getColored(ItemStack item, DyeColor color) {
		if(item.getType().equals(Material.LEATHER_BOOTS) || item.getType().equals(Material.LEATHER_LEGGINGS) || item.getType().equals(Material.LEATHER_CHESTPLATE) || item.getType().equals(Material.LEATHER_HELMET)) {
			LeatherArmorMeta meta = (LeatherArmorMeta) item.getItemMeta();
			meta.setColor(color.getColor());
			item.setItemMeta(meta);
			return item;
		}
		
		if(!item.getType().equals(Material.STAINED_GLASS)
				&& !item.getType().equals(Material.STAINED_GLASS_PANE)
				&& !item.getType().equals(Material.WOOL)
				&& !item.getType().equals(Material.STAINED_CLAY)
				&& !item.getType().equals(Material.INK_SACK)) return item;
		
		if(item.getType().equals(Material.INK_SACK)) {
			if(color.equals(DyeColor.WHITE))
				return getItem(item.getType(), 15, item.getItemMeta().getDisplayName());
			else if(color.equals(DyeColor.ORANGE))
				return getItem(item.getType(), 14, item.getItemMeta().getDisplayName());
			else if(color.equals(DyeColor.MAGENTA))
				return getItem(item.getType(), 13, item.getItemMeta().getDisplayName());
			else if(color.equals(DyeColor.LIGHT_BLUE))
				return getItem(item.getType(), 12, item.getItemMeta().getDisplayName());
			else if(color.equals(DyeColor.YELLOW))
				return getItem(item.getType(), 11, item.getItemMeta().getDisplayName());
			else if(color.equals(DyeColor.LIME))
				return getItem(item.getType(), 10, item.getItemMeta().getDisplayName());
			else if(color.equals(DyeColor.PINK))
				return getItem(item.getType(), 9, item.getItemMeta().getDisplayName());
			else if(color.equals(DyeColor.GRAY))
				return getItem(item.getType(), 8, item.getItemMeta().getDisplayName());
			else if(color.equals(DyeColor.SILVER))
				return getItem(item.getType(), 7, item.getItemMeta().getDisplayName());
			else if(color.equals(DyeColor.CYAN))
				return getItem(item.getType(), 6, item.getItemMeta().getDisplayName());
			else if(color.equals(DyeColor.PURPLE))
				return getItem(item.getType(), 5, item.getItemMeta().getDisplayName());
			else if(color.equals(DyeColor.BLUE))
				return getItem(item.getType(), 4, item.getItemMeta().getDisplayName());
			else if(color.equals(DyeColor.BROWN))
				return getItem(item.getType(), 3, item.getItemMeta().getDisplayName());
			else if(color.equals(DyeColor.GREEN))
				return getItem(item.getType(), 2, item.getItemMeta().getDisplayName());
			else if(color.equals(DyeColor.RED))
				return getItem(item.getType(), 1, item.getItemMeta().getDisplayName());
			else if(color.equals(DyeColor.BLACK))
				return getItem(item.getType(), 0, item.getItemMeta().getDisplayName());
			else return getItem(item.getType(), 0, item.getItemMeta().getDisplayName());
		} else if(color.equals(DyeColor.WHITE))
			return getItem(item.getType(), 0, item.getItemMeta().getDisplayName());
		else if(color.equals(DyeColor.ORANGE))
			return getItem(item.getType(), 1, item.getItemMeta().getDisplayName());
		else if(color.equals(DyeColor.MAGENTA))
			return getItem(item.getType(), 2, item.getItemMeta().getDisplayName());
		else if(color.equals(DyeColor.LIGHT_BLUE))
			return getItem(item.getType(), 3, item.getItemMeta().getDisplayName());
		else if(color.equals(DyeColor.YELLOW))
			return getItem(item.getType(), 4, item.getItemMeta().getDisplayName());
		else if(color.equals(DyeColor.LIME))
			return getItem(item.getType(), 5, item.getItemMeta().getDisplayName());
		else if(color.equals(DyeColor.PINK))
			return getItem(item.getType(), 6, item.getItemMeta().getDisplayName());
		else if(color.equals(DyeColor.GRAY))
			return getItem(item.getType(), 7, item.getItemMeta().getDisplayName());
		else if(color.equals(DyeColor.SILVER))
			return getItem(item.getType(), 8, item.getItemMeta().getDisplayName());
		else if(color.equals(DyeColor.CYAN))
			return getItem(item.getType(), 9, item.getItemMeta().getDisplayName());
		else if(color.equals(DyeColor.PURPLE))
			return getItem(item.getType(), 10, item.getItemMeta().getDisplayName());
		else if(color.equals(DyeColor.BLUE))
			return getItem(item.getType(), 11, item.getItemMeta().getDisplayName());
		else if(color.equals(DyeColor.BROWN))
			return getItem(item.getType(), 12, item.getItemMeta().getDisplayName());
		else if(color.equals(DyeColor.GREEN))
			return getItem(item.getType(), 13, item.getItemMeta().getDisplayName());
		else if(color.equals(DyeColor.RED))
			return getItem(item.getType(), 14, item.getItemMeta().getDisplayName());
		else if(color.equals(DyeColor.BLACK))
			return getItem(item.getType(), 15, item.getItemMeta().getDisplayName());
		else return getItem(item.getType(), 0, item.getItemMeta().getDisplayName());
	}
	
	public static ItemStack addEnchantment(ItemStack item, Enchantment ench, int level, boolean visible) {
		item.addUnsafeEnchantment(ench, level);
		
		ItemMeta meta = item.getItemMeta();
		
		if(!visible) meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
		else meta.removeItemFlags(ItemFlag.HIDE_ENCHANTS);
		
		item.setItemMeta(meta);
		
		return item;
	}
	
	public static ItemStack setUnbreakable(ItemStack item, boolean unbreakable, boolean hide) {
		ItemMeta meta = item.getItemMeta();
		meta.spigot().setUnbreakable(unbreakable);
		
		if(hide) meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
		else meta.removeItemFlags(ItemFlag.HIDE_UNBREAKABLE);
		
		item.setItemMeta(meta);
		
		return item;
	}
	
	public static ItemStack removeStandardLore(ItemStack item) {
		if(!item.hasItemMeta()) return item;
		
		ItemMeta meta = item.getItemMeta();
		
		meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
		
		item.setItemMeta(meta);
		
		return item;
	}
	
	public static ItemStack removeEnchantLore(ItemStack item) {
		if(!item.hasItemMeta()) return item;
		
		ItemMeta meta = item.getItemMeta();
		
		meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
		
		item.setItemMeta(meta);
		
		return item;
	}
	
	public static String toJSONString(ItemStack item) {
		JSONObject jsonObject = new JSONObject();
		
		if(item == null) return jsonObject.toJSONString();
		
		JSONArray lore = new JSONArray();
		String displayname = null;
		JSONObject color = new JSONObject();
		JSONObject enchants = new JSONObject();
		
		if(item.hasItemMeta()) {
			displayname = item.getItemMeta().getDisplayName().replace("§", "&");
			
			if(item.getItemMeta().getLore() == null || item.getItemMeta().getLore().size() == 0) {
				lore = null;
			} else {
				lore.addAll(item.getItemMeta().getLore());
			}
			
			try {
				LeatherArmorMeta meta = (LeatherArmorMeta) item.getItemMeta();
				color.put("Red", meta.getColor().getRed());
				color.put("Blue", meta.getColor().getBlue());
				color.put("Green", meta.getColor().getGreen());
			} catch(Exception ex) {
				color = null;
			}
			
			if(item.getItemMeta().hasEnchants()) {
				for(Enchantment ench : item.getItemMeta().getEnchants().keySet()) {
					enchants.put(ench.getName(), item.getItemMeta().getEnchants().get(ench));
				}
			} else enchants = null;
		} else {
			color = null;
		}
		
		jsonObject.put("Material", item.getType().toString());
		jsonObject.put("Data", item.getData().getData());
		jsonObject.put("Displayname", displayname);
		jsonObject.put("Amount", item.getAmount() + "");
		jsonObject.put("Lore", (lore == null ? null : lore.toJSONString()));
		jsonObject.put("Color", (color == null ? null : color.toJSONString()));
		jsonObject.put("Enchants", (enchants == null ? null : enchants.toJSONString()));
		
		return jsonObject.toJSONString();
	}
	
	public static String toEasyJSONString(ItemStack item) {
		JSONObject jsonObject = new JSONObject();
		
		JSONObject color = new JSONObject();
		JSONObject enchants = new JSONObject();
		
		if(item.hasItemMeta()) {
			try {
				LeatherArmorMeta meta = (LeatherArmorMeta) item.getItemMeta();
				color.put("Red", meta.getColor().getRed());
				color.put("Blue", meta.getColor().getBlue());
				color.put("Green", meta.getColor().getGreen());
			} catch(Exception ex) {
				color = null;
			}
			
			item.getItemMeta().getEnchants().forEach((ench, level) -> {
				enchants.put(ench.getName(), level);
			});
			
		} else {
			color = null;
		}
		
		jsonObject.put("Material", item.getType().toString());
		jsonObject.put("Data", item.getData().getData());
		jsonObject.put("Amount", item.getAmount() + "");
		jsonObject.put("Color", (color == null ? null : color.toJSONString()));
		jsonObject.put("Enchants", (enchants == null ? null : enchants.toJSONString()));
		
		return jsonObject.toJSONString();
	}
	
	public static ItemStack toItemStack(String jsonCode) {
		try {
			return toItemStack((JSONObject) new JSONParser().parse(jsonCode));
		} catch(ParseException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public static ItemStack toItemStack(JSONObject jsonObject) {
		Material material = null;
		byte data = 0;
		String displayname = null;
		int amount = -1;
		List<String> lore = new ArrayList<>();
		Color color = null;
		HashMap<Enchantment, Integer> enchantments = new HashMap<>();
		JSONParser parser = new JSONParser();
		
		try {
			JSONObject enchants = (JSONObject) parser.parse((String) jsonObject.get("Enchants"));
			enchants.forEach((key, value) -> enchantments.put(Enchantment.getByName((String) key), (int) value));
		} catch(Exception ex) {
		}
		
		try {
			JSONObject jsonColor = (JSONObject) parser.parse((String) jsonObject.get("Color"));
			
			int red = (int) jsonColor.get("Red");
			int blue = (int) jsonColor.get("Blue");
			int green = (int) jsonColor.get("Green");
			
			color = Color.fromRGB(red, green, blue);
		} catch(Exception ex) {
		}
		
		try {
			JSONArray jsonLore = (JSONArray) parser.parse((String) jsonObject.get("Lore"));
			
			jsonLore.forEach(key -> lore.add(((String) key).replace("&", "§")));
		} catch(Exception ex) {
		}
		
		try {
			material = Material.valueOf((String) jsonObject.get("Material"));
		} catch(Exception ex) {
			return null;
		}
		
		try {
			amount = Integer.parseInt((String) jsonObject.get("Amount"));
		} catch(Exception ex) {
		}
		
		try {
			data = Byte.parseByte((String) jsonObject.get("Data"));
			data = (byte) jsonObject.get("Data");
		} catch(Exception ex) {
		}
		
		try {
			displayname = (String) jsonObject.get("Displayname");
			if(displayname != null) displayname = displayname.replace("&", "§");
		} catch(Exception ex) {
		}
		
		ItemStack item = OldItemBuilder.getItem(material, data, amount);
		
		if(displayname != null) OldItemBuilder.setDisplayName(item, displayname);
		if(lore.size() > 0) OldItemBuilder.setLore(item, lore);
		if(enchantments.size() > 0) {
			enchantments.forEach((ench, level) -> OldItemBuilder.addEnchantment(item, ench, level, true));
		}
		if(color != null) {
			try {
				LeatherArmorMeta meta = (LeatherArmorMeta) item.getItemMeta();
				meta.setColor(color);
				item.setItemMeta(meta);
			} catch(Exception ex) {
			}
		}
		
		return item;
	}
	
	public static String translateSimple(ItemStack item) {
		if(item == null) return null;
		
		JSONObject json = new JSONObject();
		
		json.put("Type", item.getType().name());
		//noinspection deprecation
		json.put("Data", item.getData().getData());
		
		return json.toJSONString();
	}
	
	public static ItemStack translateSimple(String code) {
		if(code == null) return null;
		
		try {
			JSONObject json = (JSONObject) new JSONParser().parse(code);
			
			String type = (String) json.get("Type");
			int data = Math.toIntExact((long) json.get("Data"));
			
			return OldItemBuilder.getItem(Material.valueOf(type), data);
		} catch(Exception e) {
			return null;
		}
	}
	
	public static ItemStack setText(ItemStack item, TextAlignment alignment, String... text) {
		List<String> lines = new ArrayList<>();
		
		for(String line : text) {
			while(line.contains("\n")) {
				String part = line.split("\n")[0];
				lines.add(part);
				line = line.replace(part + "\n", "");
			}
			
			while(line.contains("%n%")) {
				String part = line.split("%n%")[0];
				lines.add(part);
				line = line.replace(part + "%n%", "");
			}
			
			lines.add(line);
		}
		
		return setText(item, alignment, lines);
	}
	
	public static ItemStack setText(ItemStack item, TextAlignment alignment, List<String> text) {
		text = alignment.apply(text);
		
		if(text.size() > 0) {
			setDisplayName(item, text.get(0));
			text.remove(0);
			setLore(item, text);
		}
		
		return item;
	}
	
	public static ItemStack centerLore(ItemStack item) {
		if(item == null || !item.hasItemMeta() || item.getItemMeta().getLore() == null || item.getItemMeta().getLore().size() == 0)
			return item;
		
		ItemMeta meta = item.getItemMeta();
		meta.setLore(TextAlignment.CENTER.apply(item.getItemMeta().getLore()));
		item.setItemMeta(meta);
		
		return item;
	}
	
	public static ItemStack centerLines(ItemStack item) {
		List<String> lines = new ArrayList<>();
		
		lines.add(item.getItemMeta().getDisplayName());
		lines.addAll((item.getItemMeta().getLore() == null ? new ArrayList<>() : item.getItemMeta().getLore()));
		
		return setText(item, TextAlignment.CENTER, lines);
	}
}