package de.codingair.codingapi.player.gui.inventory.v2.buttons;

import de.codingair.codingapi.player.gui.anvil.*;
import de.codingair.codingapi.player.gui.inventory.v2.GUI;
import de.codingair.codingapi.tools.Call;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class AnvilButton extends Button implements GUISwitchButton {
    private String title = null;

    public abstract void onAnvil(GUI fallback, AnvilClickEvent e);

    public abstract ItemStack buildAnvilItem();

    @Override
    public boolean open(ClickType clickType, GUI gui, Call call) {
        AnvilGUI.openAnvil(gui.getPlugin(), gui.getPlayer(), new AnvilListener() {
            @Override
            public void onClick(AnvilClickEvent e) {
                e.setCancelled(true);
                e.setClose(false);

                if (e.getSlot() != AnvilSlot.OUTPUT) return;

                onAnvil(gui, e);
                e.setKeepInventory(true);
            }

            @Override
            public void onClose(AnvilCloseEvent e) {
                e.setPost(call::proceed);
            }
        }, buildAnvilItem(), title);
        return false;
    }

    @Nullable
    public String getTitle() {
        return title;
    }

    @NotNull
    public AnvilButton setTitle(@Nullable String title) {
        this.title = title;
        return this;
    }
}
