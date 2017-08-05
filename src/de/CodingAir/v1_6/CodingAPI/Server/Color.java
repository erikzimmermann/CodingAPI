package de.CodingAir.v1_6.CodingAPI.Server;

import org.bukkit.DyeColor;

public class Color {
	
	public static String dyeColorToChatColor(DyeColor color){
		switch(color){
			case BLACK:
				return "§0";
			case BLUE:
				return "§1";
			case GREEN:
				return "§2";
			case CYAN:
				return "§3";
			case MAGENTA:
				return "§4";
			case PURPLE:
				return "§5";
			case ORANGE:
				return "§6";
			case BROWN:
				return "§6";
			case SILVER:
				return "§7";
			case GRAY:
				return "§8";
			case LIME:
				return "§a";
			case LIGHT_BLUE:
				return "§b";
			case RED:
				return "§c";
			case PINK:
				return "§d";
			case YELLOW:
				return "§e";
			case WHITE:
				return "§f";
			default:
				return "§f";
		}
	}
	
}
