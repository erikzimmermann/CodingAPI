package de.codingair.codingapi.player.gui.hotbar;

import de.codingair.codingapi.server.SoundData;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

public class ItemComponent {
    private final ItemStack item;
    private HotbarGUI link;
    private SoundData clickSound;
    private boolean silent;
    private boolean closeOnClick;
    private ClickEvent action;

    public ItemComponent(ItemStack item) {
        this.item = item;
    }

    public ItemComponent(ItemStack item, ClickEvent action) {
        this.item = item;
        this.action = action;
    }

    public ItemStack getItem() {
        return item;
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

    public ClickEvent getAction() {
        return action;
    }

    public ItemComponent setAction(ClickEvent action) {
        this.action = action;
        return this;
    }
}
