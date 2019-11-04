package de.codingair.codingapi.player.gui.inventory.gui.simple;

import org.bukkit.inventory.ItemStack;

public abstract class SyncButton extends Button {
    private boolean buffering = true;

    public SyncButton(int slot) {
        super(slot, null);
        update(false);
    }

    public SyncButton(int x, int y) {
        this(x + y * 9);
    }

    public abstract ItemStack craftItem();

    @Override
    public ItemStack getItem() {
        return buffering ? super.getItem() : craftItem();
    }

    public void update() {
        update(true);
    }

    public void update(boolean updateGUI) {
        setItem(craftItem(), updateGUI);
    }

    public boolean isBuffering() {
        return buffering;
    }

    public SyncButton setBuffering(boolean buffering) {
        this.buffering = buffering;
        return this;
    }
}
