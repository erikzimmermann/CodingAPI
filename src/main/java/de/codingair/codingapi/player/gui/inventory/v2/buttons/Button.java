package de.codingair.codingapi.player.gui.inventory.v2.buttons;

import de.codingair.codingapi.player.gui.inventory.v2.GUI;
import de.codingair.codingapi.server.sounds.Sound;
import de.codingair.codingapi.server.sounds.SoundData;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

public abstract class Button {
    private SoundData clickSound = new SoundData(Sound.UI_BUTTON_CLICK, 0.7F, 1F);

    public abstract @Nullable ItemStack buildItem();

    public abstract boolean canClick(ClickType type);

    public abstract void onClick(GUI gui, InventoryClickEvent e);

    public void playSound(Player player) {
        if(clickSound != null) clickSound.play(player);
    }

    public SoundData getClickSound() {
        return clickSound;
    }

    public void setClickSound(SoundData clickSound) {
        this.clickSound = clickSound;
    }
}
