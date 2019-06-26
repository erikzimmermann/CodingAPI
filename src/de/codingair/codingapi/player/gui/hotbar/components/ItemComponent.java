package de.codingair.codingapi.player.gui.hotbar.components;

import de.codingair.codingapi.player.gui.hotbar.HotbarGUI;
import de.codingair.codingapi.player.gui.hotbar.ItemListener;
import de.codingair.codingapi.server.SoundData;
import org.bukkit.inventory.ItemStack;

public class ItemComponent {
    private ItemStack item;
    private HotbarGUI link;
    private SoundData clickSound;
    private boolean silent;
    private boolean closeOnClick;
    private ItemListener action;

    public ItemComponent(ItemStack item) {
        this.item = item;
    }

    public ItemComponent(ItemStack item, ItemListener action) {
        this.item = item;
        this.action = action;
    }

    public ItemStack getItem() {
        return item;
    }

    public void setItem(ItemStack item) {
        this.item = item;
    }

    public HotbarGUI getLink() {
        return link;
    }

    public ItemComponent setLink(HotbarGUI link) {
        this.link = link;
        return this;
    }

    public SoundData getClickSound() {
        return clickSound;
    }

    public ItemComponent setClickSound(SoundData clickSound) {
        this.clickSound = clickSound;
        return this;
    }

    public boolean isSilent() {
        return silent;
    }

    public ItemComponent setSilent(boolean silent) {
        this.silent = silent;
        return this;
    }

    public boolean isCloseOnClick() {
        return closeOnClick;
    }

    public ItemComponent setCloseOnClick(boolean closeOnClick) {
        this.closeOnClick = closeOnClick;
        return this;
    }

    public ItemListener getAction() {
        return action;
    }

    public ItemComponent setAction(ItemListener action) {
        this.action = action;
        return this;
    }
}
