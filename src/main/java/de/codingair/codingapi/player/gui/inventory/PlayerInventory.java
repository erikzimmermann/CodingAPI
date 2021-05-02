package de.codingair.codingapi.player.gui.inventory;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 * Removing of this disclaimer is forbidden.
 *
 * @author codingair
 **/

public class PlayerInventory {
    private final Player player;
    private final ItemStack[] content;
    private final ItemStack[] armor;
    private final int heldSlot;

    public PlayerInventory(Player player) {
        this.player = player;

        content = new ItemStack[36];

        for(int i = 0; i < 36; i++) {
            content[i] = player.getInventory().getContents()[i] == null ? null : player.getInventory().getContents()[i].clone();
        }

        armor = player.getInventory().getArmorContents().clone();
        heldSlot = player.getInventory().getHeldItemSlot();
    }

    public PlayerInventory(ItemStack[] content) {
        this.player = null;
        this.content = content;
        this.armor = null;
        this.heldSlot = 0;
    }

    public Player getPlayer() {
        return player;
    }

    public void restore() {
        this.player.getInventory().clear();
        this.player.getInventory().setContents(this.content.clone());
        this.player.getInventory().setArmorContents(this.armor.clone());
        this.player.getInventory().setHeldItemSlot(heldSlot);
        this.player.updateInventory();
    }

    public ItemStack[] getContent() {
        return content;
    }

    public ItemStack[] getArmor() {
        return armor;
    }

    public int addUntilPossible(ItemStack item, boolean justStack) {
        item = item.clone();

        int empty = -999;

        for(int i = 0; i < this.content.length; i++) {
            ItemStack other = this.content[i];

            if(other == null) {
                if(empty == -999 && !justStack) empty = i;
            } else if(other.isSimilar(item) && other.getAmount() < other.getMaxStackSize()) {
                int space = other.getMaxStackSize() - other.getAmount();
                int transport = Math.min(item.getAmount(), space);
                other.setAmount(other.getAmount() + transport);
                item.setAmount(item.getAmount() - transport);

                if(item.getAmount() <= 0) return 0;
            }
        }

        if(empty == -999) {
            if(justStack) return addUntilPossible(item, false);
            else return item.getAmount();
        }

        this.content[empty] = item;
        return 0;
    }

    public boolean addItem(ItemStack item) {
        return addItem(item, true);
    }

    public boolean addItem(ItemStack item, boolean justStack) {
        item = item.clone();

        int empty = -999;

        for(int i = 0; i < this.content.length; i++) {
            ItemStack other = this.content[i];

            if(other == null) {
                if(empty == -999 && !justStack) empty = i;
            } else if(other.isSimilar(item) && other.getAmount() < other.getMaxStackSize()) {
                int space = other.getMaxStackSize() - other.getAmount();
                int transport = Math.min(item.getAmount(), space);
                other.setAmount(other.getAmount() + transport);
                item.setAmount(item.getAmount() - transport);

                if(item.getAmount() <= 0) return true;
            }
        }

        if(empty == -999) {
            if(justStack) return addItem(item, false);
            else return false;
        }

        this.content[empty] = item;
        return true;
    }
}
