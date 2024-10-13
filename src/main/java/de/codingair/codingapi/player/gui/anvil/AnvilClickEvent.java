package de.codingair.codingapi.player.gui.anvil;

import com.google.common.base.CharMatcher;
import de.codingair.codingapi.server.reflections.IReflection;
import de.codingair.codingapi.server.specification.Version;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

public class AnvilClickEvent extends Event {
    public static HandlerList handlers = new HandlerList();
    private final Player player;
    private final AnvilSlot slot;
    private final ClickType clickType;
    private final String input;
    private final AnvilGUI anvil;
    private String submitted = null;
    private boolean keepInventory = false; //for smoother GUI switch
    private boolean close = false;
    private boolean cancelled = true;
    private boolean payExp = false;

    public AnvilClickEvent(Player player, ClickType clickType, AnvilSlot slot, ItemStack item, AnvilGUI anvil) {
        this.anvil = anvil;
        this.player = player;
        this.clickType = clickType;
        this.slot = slot;

        if (item == null || !item.hasItemMeta()) input = null;
        else input = item.getItemMeta().getDisplayName();
    }

    public AnvilClickEvent(Player player, ClickType clickType, AnvilSlot slot, String input, AnvilGUI anvil) {
        this.anvil = anvil;
        this.player = player;
        this.clickType = clickType;
        this.slot = slot;
        this.input = input;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public AnvilGUI getAnvil() {
        return anvil;
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

    public Player getPlayer() {
        return player;
    }

    public String getInput() {
        return getInput(true);
    }

    public String getInput(boolean colors) {
        String input = this.input;

        if (input != null) {
            if (colors) input = input.replace("§", "&");
            else input = ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&', input));
        }

        return input == null ? null : getWhitespace().trimFrom(input);
    }

    private CharMatcher getWhitespace() {
        if (Version.atLeast(18)) return CharMatcher.whitespace();
        else {
            IReflection.FieldAccessor<CharMatcher> WHITESPACE = IReflection.getField(CharMatcher.class, "WHITESPACE");
            return WHITESPACE.get(null);
        }
    }

    public String getRawInput() {
        return input;
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
