package de.codingair.codingapi.player.gui.inventory.v2.buttons;

import org.bukkit.inventory.ItemStack;

public abstract class StaticButton extends Button {
    private final ItemStack item;

    public StaticButton(ItemStack item) {
        this.item = item;
    }

    @Override
    public ItemStack buildItem() {
        return this.item;
    }
}
