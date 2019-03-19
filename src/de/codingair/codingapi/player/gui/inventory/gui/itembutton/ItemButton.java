package de.codingair.codingapi.player.gui.inventory.gui.itembutton;

import de.codingair.codingapi.player.gui.inventory.gui.Interface;
import de.codingair.codingapi.server.SoundData;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

public abstract class ItemButton {
    private ItemStack item;
    private int slot;
    private Interface inv;
    private ItemButtonOption option = new ItemButtonOption();

    @Deprecated
    public ItemButton(ItemStack item) {
        this.item = item;
    }

    public ItemButton(int slot, ItemStack item) {
        this.slot = slot;
        this.item = item;
    }

    public ItemButton(int x, int y, ItemStack item) {
        this.slot = x + y * 9;
        this.item = item;
    }

    public ItemButton(int slot, ItemStack item, boolean onlyLeftClick) {
        this.slot = slot;
        this.item = item;
        this.option.setOnlyLeftClick(onlyLeftClick);
    }

    public ItemStack getItem() {
        return item;
    }

    public ItemButton setMoveable(boolean movable) {
        this.option.setMovable(movable);
        return this;
    }

    public boolean isMovable() {
        return this.option.isMovable();
    }

    public Sound getClickSound() {
        return this.option.getClickSound();
    }

    public ItemButton setClickSound(Sound clickSound) {
        this.option.setClickSound(clickSound);
        return this;
    }

    public ItemButton setClickSound2(SoundData clickSound) {
        this.option.setClickSound(clickSound);
        return this;
    }

    public void addTo(Interface i, int slot) {
        this.slot = slot;
        i.addButton(this);
    }

    public abstract void onClick(InventoryClickEvent e);

    public void playSound(Player p) {
        if(this.option.getClickSound() != null) p.playSound(p.getLocation(), this.option.getClickSound(), 1, 1);
        else if(this.option.getClickSound2() != null) this.option.getClickSound2().play(p);
    }

    @Deprecated
    public void setItem(ItemStack item, Interface i) {
        this.item = item;
        i.removeButton(this.slot);
        i.addButton(this);
    }

    public void setItem(ItemStack item) {
        setItem(item, true);
    }

    public void setItem(ItemStack item, boolean update) {
        this.item = item;
        if(update && inv != null) inv.setItem(getSlot(), getItem());
    }

    public boolean isCloseOnClick() {
        return this.option.isCloseOnClick();
    }

    public ItemButton setCloseOnClick(boolean closeOnClick) {
        this.option.setCloseOnClick(closeOnClick);
        return this;
    }

    public int getSlot() {
        return slot;
    }

    public void setSlot(int slot) {
        this.slot = slot;
    }

    public void updateInInterface() {
        if(this.inv == null) return;

        inv.removeButton(this);
        inv.addButton(this);
    }

    public Interface getInterface() {
        return inv;
    }

    public final void setInterface(Interface inv) {
        this.inv = inv;
    }

    public boolean isOnlyLeftClick() {
        return this.option.isOnlyLeftClick();
    }

    public ItemButton setOnlyLeftClick(boolean onlyLeftClick) {
        this.option.setOnlyLeftClick(onlyLeftClick);
        return this;
    }

    public boolean isOnlyRightClick() {
        return this.option.isOnlyRightClick();
    }

    public ItemButton setOnlyRightClick(boolean onlyRightClick) {
        this.option.setOnlyRightClick(onlyRightClick);
        return this;
    }

    public ItemButtonOption getOption() {
        return option;
    }

    public ItemButton setOption(ItemButtonOption option) {
        this.option = option.clone();
        return this;
    }
}
