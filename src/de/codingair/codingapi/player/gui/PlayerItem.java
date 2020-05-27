package de.codingair.codingapi.player.gui;

import de.codingair.codingapi.API;
import de.codingair.codingapi.utils.Removable;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.UUID;

public abstract class PlayerItem extends ItemStack implements Removable {
    private UUID uniqueId = UUID.randomUUID();
    private JavaPlugin plugin;
    private Player player;
    private boolean freezed = true;
    private long lastClick = 0;

    public PlayerItem(JavaPlugin plugin, Player player, ItemStack item) {
        this.setToItemStack(item);

        this.plugin = plugin;
        this.player = player;

        API.addRemovable(this);
        GUIListener.register(plugin);

        //remove();
    }

    @Override
    public UUID getUniqueId() {
        return uniqueId;
    }

    @Override
    public void destroy() {
        remove();
    }

    public JavaPlugin getPlugin() {
        return plugin;
    }

    @Override
    public Player getPlayer() {
        return player;
    }

    private void setToItemStack(ItemStack item) {
        this.setAmount(item.getAmount());
        this.setType(item.getType());
        this.setData(item.getData());
        this.setDurability(item.getDurability());
        this.setItemMeta(item.getItemMeta());
    }

    public void remove() {
        int slot = 0;
        for(ItemStack stack : player.getInventory().getContents()) {
            if(!this.equals(stack)) slot++;
            else break;
        }

        player.getInventory().setItem(slot, null);
        player.updateInventory();

        API.removeRemovable(this);
    }

    public void trigger(PlayerInteractEvent e) {
        if(System.currentTimeMillis() - lastClick <= 50) return;
        lastClick = System.currentTimeMillis();
        onInteract(e);
    }

    public abstract void onInteract(PlayerInteractEvent e);

    public abstract void onHover(PlayerItemHeldEvent e);

    public abstract void onUnhover(PlayerItemHeldEvent e);

    public boolean isFreezed() {
        return freezed;
    }

    public PlayerItem setFreezed(boolean freezed) {
        this.freezed = freezed;
        return this;
    }

    public static boolean isUsing(Player p) {
        return API.getRemovable(p, PlayerItem.class) != null;
    }

    public void setDisplayName(String name) {
        ItemMeta meta = getItemMeta();
        meta.setDisplayName(name);
        setItemMeta(meta);
    }

    public String getDisplayName() {
        ItemMeta meta = getItemMeta();
        return meta.hasDisplayName() ? meta.getDisplayName() : null;
    }
}
