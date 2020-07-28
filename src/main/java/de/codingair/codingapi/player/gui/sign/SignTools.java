package de.codingair.codingapi.player.gui.sign;

import org.bukkit.ChatColor;
import org.bukkit.block.Sign;

public class SignTools {

    public static void updateSign(Sign sign, String[] text) {
        updateSign(sign, text, true);
    }

    public static void updateSign(Sign sign, String[] text, boolean colorSupport) {
        if(colorSupport) {
            for(int i = 0; i < text.length; i++) {
                text[i] = ChatColor.translateAlternateColorCodes('&', text[i]);
            }
        }

        for(int i = 0; i < 4; i++) {
            sign.setLine(i, text[i]);
        }

        sign.update(true);
    }

}
