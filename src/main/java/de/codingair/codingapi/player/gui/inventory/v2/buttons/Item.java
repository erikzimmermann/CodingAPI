package de.codingair.codingapi.player.gui.inventory.v2.buttons;

import de.codingair.codingapi.player.gui.inventory.v2.GUI;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

public class Item extends StaticButton {
    public Item(ItemStack item) {
        super(item);
    }

    @Override
    public boolean canClick(ClickType type) {
        return false;
    }

    @Override
    public void onClick(GUI gui, InventoryClickEvent e) {
    }
}
