package de.codingair.codingapi.server.events;

import de.codingair.codingapi.tools.nbt.NBTTagCompound;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.inventory.ItemStack;

public class PlayerPickItemEvent extends PlayerEvent {
    private static final HandlerList handlers = new HandlerList();
    private final int slot;
    private final ItemStack itemStack;
    private final Block from;
    private final boolean nbtCopy;

    public PlayerPickItemEvent(Player who, int slot, ItemStack itemStack, Block from, boolean nbtCopy) {
        super(who);
        this.slot = slot;
        this.itemStack = itemStack;
        this.from = from;
        this.nbtCopy = nbtCopy;
    }

    public boolean isNBTCopy() {
        return nbtCopy;
    }

    public int getSlot() {
        return slot;
    }

    public ItemStack getItemStack() {
        return itemStack;
    }

    public Block getFrom() {
        return from;
    }

    @Override
    public HandlerList getHandlers() {
        return getHandlerList();
    }

    public static HandlerList getHandlerList() {
        return handlers;
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
