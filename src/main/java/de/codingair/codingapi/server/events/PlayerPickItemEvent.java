package de.codingair.codingapi.server.events;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class PlayerPickItemEvent extends PlayerEvent {
    private static final HandlerList handlers = new HandlerList();
    private final int slot;
    private final ItemStack itemStack;
    private final Block from;

    public PlayerPickItemEvent(@NotNull Player who, int slot, @NotNull ItemStack itemStack, @NotNull Block from) {
        super(who);
        this.slot = slot;
        this.itemStack = itemStack;
        this.from = from;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    public int getSlot() {
        return slot;
    }

    public @NotNull ItemStack getItemStack() {
        return itemStack;
    }

    public @NotNull Block getFrom() {
        return from;
    }

    @Override
    public HandlerList getHandlers() {
        return getHandlerList();
    }

    @Override
    public String toString() {
        return "PlayerPickItemEvent{" +
                "slot=" + slot +
                ", itemStack=" + itemStack +
                ", from=" + from +
                ", player=" + player +
                '}';
    }
}
