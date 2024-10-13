package de.codingair.codingapi.player.gui.inventory;

import de.codingair.codingapi.nms.NmsLoader;
import de.codingair.codingapi.server.reflections.IReflection;
import de.codingair.codingapi.server.specification.Version;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class PlayerInventory {
    private static IReflection.FieldAccessor<ItemMeta> META = null;

    static {
        if (Version.before(21)) META = IReflection.getField(ItemStack.class, ItemMeta.class, 0);
    }

    private final Player player;
    private final ItemStack[] content;
    private final ItemStack[] armor;
    private final int heldSlot;
    private final boolean exact;
    private final Map<Integer, Integer> type = new HashMap<>();
    private Integer emptySlot = 0;

    @NmsLoader
    private PlayerInventory() {
        this((ItemStack[]) null);
    }

    public PlayerInventory(Player player) {
        this(player, true);
    }

    /**
     * @param player The player which inventory will be copied.
     * @param exact  True (less performant) copies the exact inventory contents to the cached array. False (optimized) sorts the inventory into the current cache and provides a much faster add algorithm.
     */
    public PlayerInventory(Player player, boolean exact) {
        this.player = player;
        this.exact = exact;

        content = new ItemStack[36];

        if (exact) {
            for (int i = 0; i < 36; i++) {
                content[i] = player.getInventory().getContents()[i] == null ? null : player.getInventory().getContents()[i].clone();
            }
        } else {
            for (int i = 0; i < 36; i++) {
                ItemStack item = player.getInventory().getContents()[i] == null ? null : player.getInventory().getContents()[i].clone();
                if (item == null || item.getType() == Material.AIR) continue;

                optimizedAddUntilPossible(item);
            }
        }

        armor = player.getInventory().getArmorContents().clone();
        heldSlot = player.getInventory().getHeldItemSlot();
    }

    public PlayerInventory(ItemStack[] content) {
        this.player = null;
        this.exact = true;
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

    /**
     * @param item The ItemStack that will be added.
     * @return The amount of the given item that could not be sorted into the inventory.
     */
    public int addUntilPossible(@NotNull ItemStack item) {
        return addUntilPossible(item, true);
    }

    /**
     * @param item                         The ItemStack that will be added.
     * @param stackBeforeAddingToEmptySlot If the give item should be stacked on all available ItemStacks with the same type before adding to a free slot.
     * @return The amount of the given item that could not be sorted into the inventory.
     */
    public int addUntilPossible(@NotNull ItemStack item, boolean stackBeforeAddingToEmptySlot) {
        if (exact) return naiveAddUntilPossible(item, stackBeforeAddingToEmptySlot);
        else return optimizedAddUntilPossible(item);
    }

    /**
     * @param item The ItemStack that will be added.
     * @return True if the given item was fully added to the inventory.
     */
    public boolean addItem(@NotNull ItemStack item) {
        return addItem(item, true);
    }

    /**
     * @param item                         The ItemStack that will be added.
     * @param stackBeforeAddingToEmptySlot If the give item should be stacked on all available ItemStacks with the same type before adding to a free slot.
     * @return True if the given item was fully added to the inventory.
     */
    public boolean addItem(@NotNull ItemStack item, boolean stackBeforeAddingToEmptySlot) {
        return naiveAddUntilPossible(item, stackBeforeAddingToEmptySlot) == 0;
    }

    /**
     * @param item                         The ItemStack that will be added.
     * @param stackBeforeAddingToEmptySlot If the give item should be stacked on all available ItemStacks with the same type before adding to a free slot.
     * @return The amount of the given item that could not be sorted into the inventory.
     */
    private int naiveAddUntilPossible(@NotNull ItemStack item, boolean stackBeforeAddingToEmptySlot) {
        item = item.clone();

        Integer empty = null;

        //try to stack given item onto other items in this inventory
        for (int slot = 0; slot < this.content.length; slot++) {
            ItemStack other = this.content[slot];

            if (other == null || other.getType() == Material.AIR) {
                if (empty == null) {
                    empty = slot;

                    if (!stackBeforeAddingToEmptySlot) {
                        //stop for-int if we ignore future items and just place this item into the empty slot
                        break;
                    }
                }
            } else if (other.getAmount() < other.getMaxStackSize() && other.isSimilar(item)) {
                stack(item, other);
                if (item.getAmount() <= 0) return 0;
            }
        }

        if (empty == null) {
            //no empty slot found
            return item.getAmount();
        }

        this.content[empty] = item;
        return 0;
    }

    /**
     * @param itemStack The ItemStack that will be added.
     * @return The amount of the given item that could not be sorted into the inventory.
     */
    private int optimizedAddUntilPossible(@NotNull ItemStack itemStack) {
        ItemStack item = itemStack.clone();
        Integer cacheEmptySlot = emptySlot;
        int hashCode = itemHashCode(item);

        this.type.compute(hashCode, (hash, slot) -> {
            //stack on current
            if (slot == null) {
                //add to inventory
                boolean isFull = emptySlot == null;
                if (isFull) return null;

                content[emptySlot] = item;
                return emptySlot++;
            }

            ItemStack to = content[slot];
            boolean notFullyStacked = stack(item, to);

            boolean stillSpace = emptySlot != null;

            if (notFullyStacked && stillSpace) {
                //place item into next free slot since this was the last same item in the inventory
                content[emptySlot] = item;
                slot = emptySlot;

                boolean isFull = emptySlot + 1 == content.length;
                if (isFull) emptySlot = null;
                else emptySlot++;
            }

            return slot;
        });

        boolean addedToEmptySlot = !Objects.equals(cacheEmptySlot, emptySlot);
        if (addedToEmptySlot) return 0;

        return item.getAmount();
    }

    /**
     * @param from The ItemStack which amount should be decreased.
     * @param to   The ItemStack which amount should be increased.
     * @return True if the origin ItemStack has not been fully stacked onto the target ItemStack.
     */
    private boolean stack(ItemStack from, ItemStack to) {
        int space = to.getMaxStackSize() - to.getAmount();
        int transport = Math.min(from.getAmount(), space);
        to.setAmount(to.getAmount() + transport);
        from.setAmount(from.getAmount() - transport);
        return from.getAmount() > 0;
    }

    private int itemHashCode(ItemStack item) {
        int hash = 1;

        hash = hash * 31 + item.getType().hashCode();
        //noinspection deprecation
        hash = hash * 31 + (item.getDurability() & 0xffff);

        ItemMeta meta = null;
        if (item.hasItemMeta()) {
            if (META != null) {
                // workaround: try reflections to potentially avoid computations
                meta = META.get(item);
            }
            if (meta == null) meta = item.getItemMeta();
        }

        hash = hash * 31 + (meta == null ? 0 : meta.hashCode());

        return hash;
    }
}
