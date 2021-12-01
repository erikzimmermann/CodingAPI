package de.codingair.codingapi.player.gui.anvil;

import com.google.common.base.CharMatcher;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

public class AnvilClickEvent extends Event {
    private final Player player;
    private final AnvilSlot slot;
    private final ClickType clickType;

    private final ItemStack item;
    private String submitted = null;

    private boolean keepInventory = false; //for smoother GUI switch
    private boolean close = false;
    private boolean cancelled = true;

    private boolean payExp = false;

    private final AnvilGUI anvil;

    public static HandlerList handlers = new HandlerList();

    public AnvilClickEvent(Player player, ClickType clickType, AnvilSlot slot, ItemStack item, AnvilGUI anvil) {
        this.anvil = anvil;
        this.player = player;
        this.clickType = clickType;
        this.slot = slot;
        this.item = item;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public AnvilGUI getAnvil() {
        return anvil;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    public AnvilSlot getSlot() {
        return slot;
    }

    public boolean getWillClose() {
        return close;
    }

    public void setWillClose(boolean close) {
        this.close = close;
    }

    public void setClose(boolean close) {
        this.close = close;
    }

    public boolean willClose() {
        return this.close;
    }

    public boolean isCancelled() {
        return cancelled;
    }

    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    public ItemStack getItem() {
        return item;
    }

    public Player getPlayer() {
        return player;
    }

    public String getInput() {
        return getInput(true);
    }

    public String getInput(boolean colors) {
        if(this.item == null || !this.item.hasItemMeta()) return null;
        String input = this.item.getItemMeta().getDisplayName();

        if(colors) input = input.replace("§", "&");
        else input = ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&', input));

        return input == null ? null : CharMatcher.whitespace().trimFrom(input);
    }

    public String getRawInput() {
        if(this.item == null || !this.item.hasItemMeta()) return null;

        return this.item.getItemMeta().getDisplayName();
    }

    public boolean isPayExp() {
        return payExp;
    }

    public void setPayExp(boolean payExp) {
        this.payExp = payExp;
    }

    public ClickType getClickType() {
        return clickType;
    }

    public String getSubmitted() {
        return submitted;
    }

    public void setSubmitted(String submitted) {
        this.submitted = submitted;
    }

    public boolean isKeepInventory() {
        return keepInventory;
    }

    public void setKeepInventory(boolean keepInventory) {
        this.keepInventory = keepInventory;
    }
}
