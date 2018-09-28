package de.codingair.codingapi.player.gui.inventory.gui.simple;

import de.codingair.codingapi.player.gui.anvil.AnvilClickEvent;
import de.codingair.codingapi.player.gui.anvil.AnvilCloseEvent;
import de.codingair.codingapi.player.gui.anvil.AnvilGUI;
import de.codingair.codingapi.player.gui.anvil.AnvilListener;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

public abstract class SyncAnvilGUIButton extends SyncButton {
    private ItemStack anvilItem;

    public SyncAnvilGUIButton(int slot) {
        super(slot);
        this.anvilItem = craftAnvilItem();
    }

    public SyncAnvilGUIButton(int x, int y) {
        this(x + y * 9);
    }

    @Override
    public void onClick(InventoryClickEvent e, Player player) {
        if(interrupt()) return;

        getInterface().setClosingByButton(true);

        AnvilGUI.openAnvil(getInterface().getPlugin(), player, new AnvilListener() {
            @Override
            public void onClick(AnvilClickEvent e) {
                e.setCancelled(true);
                e.setClose(false);

                SyncAnvilGUIButton.this.onClick(e);
            }

            @Override
            public void onClose(AnvilCloseEvent e) {
                SyncAnvilGUIButton.this.onClose(e);

                if(e.getPost() == null) {
                    getInterface().reinitialize();
                    e.setPost(() -> getInterface().open());
                }
            }
        }, this.anvilItem);
    }

    @Override
    public void reinitialize(boolean update) {
        super.reinitialize(update);
        this.anvilItem = craftAnvilItem();
    }

    public boolean interrupt() {
        return false;
    }

    public abstract void onClick(AnvilClickEvent e);

    public abstract void onClose(AnvilCloseEvent e);

    public abstract ItemStack craftItem();

    public abstract ItemStack craftAnvilItem();
}
