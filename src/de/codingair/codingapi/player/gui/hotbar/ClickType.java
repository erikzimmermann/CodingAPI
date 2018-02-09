package de.codingair.codingapi.player.gui.hotbar;

import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;

public enum ClickType {
    LEFT_CLICK, RIGHT_CLICK, SHIFT_LEFT_CLICK, SHIFT_RIGHT_CLICK, UNKNOWN;

    public static ClickType getByAction(Action action, Player player) {
        switch(action) {
            case LEFT_CLICK_BLOCK:
            case LEFT_CLICK_AIR:
                if(player.isSneaking()) return SHIFT_LEFT_CLICK;
                else return LEFT_CLICK;
            case RIGHT_CLICK_BLOCK:
            case RIGHT_CLICK_AIR:
                if(player.isSneaking()) return SHIFT_RIGHT_CLICK;
                else return RIGHT_CLICK;
            default:
                return UNKNOWN;
        }
    }
}
