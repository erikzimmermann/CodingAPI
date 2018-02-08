package de.codingair.codingapi.player.gui.hotbar;

import org.bukkit.entity.Player;

public interface ClickEvent {
    void onClick(HotbarGUI gui, ItemComponent ic, Player player, ClickType clickType);
}
