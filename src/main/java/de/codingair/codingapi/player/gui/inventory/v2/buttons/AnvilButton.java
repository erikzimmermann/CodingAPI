package de.codingair.codingapi.player.gui.inventory.v2.buttons;

import de.codingair.codingapi.player.gui.anvil.*;
import de.codingair.codingapi.player.gui.inventory.v2.GUI;
import de.codingair.codingapi.tools.Call;
import org.bukkit.inventory.ItemStack;

public abstract class AnvilButton extends Button implements GUISwitchButton {
    public abstract void onAnvil(AnvilClickEvent e);

    public abstract ItemStack buildAnvilItem();

    @Override
    public void open(GUI gui, Call call) {
        AnvilGUI.openAnvil(gui.getPlugin(), gui.getPlayer(), new AnvilListener() {
            @Override
            public void onClick(AnvilClickEvent e) {
                e.setCancelled(true);
                e.setClose(false);

                if(e.getSlot() != AnvilSlot.OUTPUT) return;

                onAnvil(e);
                e.setKeepInventory(true);
            }

            @Override
            public void onClose(AnvilCloseEvent e) {
                e.setPost(call::proceed);
            }
        }, buildAnvilItem());
    }
}
