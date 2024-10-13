package de.codingair.codingapi.player.gui.sign;

import de.codingair.codingapi.utils.ChatColor;
import org.bukkit.block.Sign;

public class SignTools {

    public static void updateSign(Sign sign, String[] text) {
        updateSign(sign, text, true);
    }

    public static void updateSign(Sign sign, String[] text, boolean colorSupport) {
        if (colorSupport) {
            for (int i = 0; i < text.length; i++) {
                text[i] = ChatColor.translateAll('&', text[i]);
            }
        }

        for (int i = 0; i < 4; i++) {
            sign.setLine(i, text[i]);
        }

        sign.update(true);
    }

}
