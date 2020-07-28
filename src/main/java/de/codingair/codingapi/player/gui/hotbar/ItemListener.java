package de.codingair.codingapi.player.gui.hotbar;

import de.codingair.codingapi.player.gui.hotbar.components.ItemComponent;
import org.bukkit.entity.Player;

public interface ItemListener {
    void onClick(HotbarGUI gui, ItemComponent ic, Player player, ClickType clickType);
    void onHover(HotbarGUI gui, ItemComponent old, ItemComponent current, Player player);
    void onUnhover(HotbarGUI gui, ItemComponent current, ItemComponent newItem, Player player);
}
