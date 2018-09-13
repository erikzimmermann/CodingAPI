package de.codingair.codingapi.player.gui.hotbar;

import de.codingair.codingapi.API;
import de.codingair.codingapi.player.gui.inventory.PlayerInventory;
import de.codingair.codingapi.server.SoundData;
import de.codingair.codingapi.utils.Removable;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;
import java.util.UUID;

public class HotbarGUI implements Removable {
    private final UUID uniqueId = UUID.randomUUID();
    private final ItemComponent[] menu = new ItemComponent[9];

    private Player player;
    private PlayerInventory backup;

    private SoundData clickSound;
    private SoundData openSound;
    private SoundData closeSound;

    private JavaPlugin plugin;

    public HotbarGUI(Player player, JavaPlugin plugin) {
        this.player = player;

        this.plugin = plugin;
        EventListener.register(plugin);
    }

    public boolean isOpened() {
        return API.getRemovable(this.player, HotbarGUI.class) != null;
    }

    public void open(boolean sound) {
        this.backup = new PlayerInventory(this.player);

        this.player.getInventory().clear();
        for(int i = 0; i < 9; i++) {
            if(this.menu[i] != null) this.player.getInventory().setItem(i, this.menu[i].getItem());
        }
        this.player.updateInventory();

        if(this.openSound != null && sound) this.openSound.play(this.player);

        API.addRemovable(this);
    }

    public void close(boolean sound) {
        this.backup.restore();
        if(this.closeSound != null && sound) this.closeSound.play(this.player);

        API.removeRemovable(this);
    }

    public int getSlot(ItemComponent ic) {
        for(int i = 0; i < 9; i++) {
            if(this.menu[i] == ic) return i;
        }

        return -999;
    }

    public void updateDisplayName(ItemComponent ic, String s) {
        int slot = getSlot(ic);
        if(slot == -999) return;

        ItemMeta meta = ic.getItem().getItemMeta();
        meta.setDisplayName(s);
        ic.getItem().setItemMeta(meta);

        updateSingle(slot);
    }

    public void updateLore(ItemComponent ic, List<String> list) {
        int slot = getSlot(ic);
        if(slot == -999) return;

        ItemMeta meta = ic.getItem().getItemMeta();
        meta.setLore(list);
        ic.getItem().setItemMeta(meta);

        updateSingle(slot);
    }

    public void updateSingle(int slot) {
        ItemComponent ic = getItem(slot);
        this.player.getInventory().setItem(slot, ic == null || ic.getItem() == null ? new ItemStack(Material.AIR) : ic.getItem());
    }

    public void update() {
        if(!this.isOpened()) return;

        this.player.getInventory().clear();
        for(int i = 0; i < 9; i++) {
            if(this.menu[i] == null || this.menu[i].getItem() == null) {
                this.player.getInventory().setItem(i, new ItemStack(Material.AIR));
                continue;
            }

            this.player.getInventory().setItem(i, this.menu[i].getItem());
        }
    }

    public ItemComponent setItem(int slot, ItemComponent ic) {
        return setItem(slot, ic, true);
    }

    public ItemComponent setItem(int slot, ItemComponent ic, boolean update) {
        ItemComponent old = this.menu[slot];
        this.menu[slot] = ic;

        if(update) update();
        return old;
    }

    public ItemComponent getItem(int slot) {
        return this.menu[slot];
    }

    public boolean addItem(ItemComponent ic) {
        for(int i = 0; i < 9; i++) {
            if(this.menu[i] == null) {
                this.menu[i] = ic;
                return true;
            }
        }

        return false;
    }

    @Override
    public void destroy() {
        close(false);
    }

    @Override
    public Player getPlayer() {
        return this.player;
    }

    @Override
    public Class<? extends Removable> getAbstractClass() {
        return HotbarGUI.class;
    }

    @Override
    public UUID getUniqueId() {
        return uniqueId;
    }

    public SoundData getClickSound() {
        return clickSound;
    }

    public void setClickSound(SoundData clickSound) {
        this.clickSound = clickSound;
    }

    public ItemComponent[] getMenu() {
        return menu;
    }

    public SoundData getOpenSound() {
        return openSound;
    }

    public void setOpenSound(SoundData openSound) {
        this.openSound = openSound;
    }

    public SoundData getCloseSound() {
        return closeSound;
    }

    public void setCloseSound(SoundData closeSound) {
        this.closeSound = closeSound;
    }

    @Override
    public JavaPlugin getPlugin() {
        return plugin;
    }
}
