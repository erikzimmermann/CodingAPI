package de.codingair.codingapi.player.gui.inventory.gui.simple;

import de.codingair.codingapi.player.gui.anvil.AnvilClickEvent;
import de.codingair.codingapi.player.gui.anvil.AnvilCloseEvent;
import de.codingair.codingapi.player.gui.anvil.AnvilGUI;
import de.codingair.codingapi.player.gui.anvil.AnvilListener;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

public abstract class SyncAnvilGUIButton extends SyncTriggerButton {
    private ItemStack anvilItem;

    public SyncAnvilGUIButton(int slot) {
        super(slot);
    }

    public SyncAnvilGUIButton(int x, int y) {
        this(x + 9 * y);
    }

    public SyncAnvilGUIButton(int slot, ClickType... trigger) {
        super(slot, trigger);
    }

    public SyncAnvilGUIButton(int x, int y, ClickType... trigger) {
        this(x + 9 * y, trigger);
    }

    @Override
    public void onTrigger(InventoryClickEvent e, ClickType trigger, Player player) {
        getInterface().setClosingByButton(true);
        getInterface().setClosingForGUI(true);

        this.anvilItem = craftAnvilItem(trigger);

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
                    getInterface().setClosingForGUI(false);
                }
            }
        }, this.anvilItem);
    }

    @Override
    public void update(boolean updateGUI) {
        super.update(updateGUI);
    }

    public boolean interrupt() {
        return false;
    }

    public abstract void onClick(AnvilClickEvent e);

    public abstract void onClose(AnvilCloseEvent e);

    public abstract ItemStack craftItem();

    public abstract ItemStack craftAnvilItem(ClickType trigger);
}
