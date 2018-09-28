package de.codingair.codingapi.player.gui.inventory.gui.simple;

import org.bukkit.inventory.ItemStack;

public abstract class SyncButton extends Button {
    public SyncButton(int slot) {
        super(slot, null);
        setItem(craftItem(), false);
    }

    public SyncButton(int x, int y) {
        this(x + y * 9);
    }

    public abstract ItemStack craftItem();

    public void reinitialize() {
        reinitialize(true);
    }

    public void reinitialize(boolean update) {
        setItem(craftItem(), update);
    }
}
