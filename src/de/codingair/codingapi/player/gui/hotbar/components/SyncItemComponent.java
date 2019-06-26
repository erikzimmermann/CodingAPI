package de.codingair.codingapi.player.gui.hotbar.components;

import de.codingair.codingapi.player.gui.hotbar.ItemListener;
import org.bukkit.inventory.ItemStack;

public abstract class SyncItemComponent extends ItemComponent {
    public SyncItemComponent() {
        super(null);
    }

    public SyncItemComponent(ItemListener action) {
        super(null, action);
    }

    @Override
    public ItemStack getItem() {
        return craftItem();
    }

    public abstract ItemStack craftItem();
}
