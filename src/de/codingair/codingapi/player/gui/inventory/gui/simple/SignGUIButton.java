package de.codingair.codingapi.player.gui.inventory.gui.simple;

import de.codingair.codingapi.API;
import de.codingair.codingapi.player.gui.sign.SignGUI;
import de.codingair.codingapi.player.gui.sign.SignTools;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

public abstract class SignGUIButton extends Button {
    private Sign sign;
    private ClickType trigger;
    private boolean updateSign;

    public SignGUIButton(int slot, ItemStack item, Location signLocation) {
        this(slot, item, signLocation, null, false);
    }

    public SignGUIButton(int x, int y, ItemStack item, Location signLocation) {
        this(x + y * 9, item, signLocation, null, false);
    }

    public SignGUIButton(int x, int y, ItemStack item, Location signLocation, ClickType trigger) {
        this(x + y * 9, item, signLocation, trigger, false);
    }

    public SignGUIButton(int slot, ItemStack item, Location signLocation, boolean updateSign) {
        this(slot, item, signLocation, null, updateSign);
    }

    public SignGUIButton(int x, int y, ItemStack item, Location signLocation, boolean updateSign) {
        this(x + y * 9, item, signLocation, null, updateSign);
    }

    public SignGUIButton(int x, int y, ItemStack item, Location signLocation, ClickType trigger, boolean updateSign) {
        this(x + y * 9, item, signLocation, trigger, updateSign);
    }

    public SignGUIButton(int slot, ItemStack item, Location signLocation, ClickType trigger, boolean updateSign) {
        super(slot, item);
        if(signLocation == null || !(signLocation.getBlock().getState() instanceof Sign)) throw new IllegalArgumentException("signLocation must be a location of a sign!");

        this.sign = (Sign) signLocation.getBlock().getState();
        this.trigger = trigger;
        this.updateSign = updateSign;
    }

    @Override
    public void onClick(InventoryClickEvent e, Player player) {
        if(interrupt()) return;

        if(trigger == null || e.getClick() == trigger) {
            getInterface().setClosingByButton(true);
            getInterface().setClosingForGUI(true);

            String[] lines = sign.getLines();
            for(int i = 0; i < lines.length; i++) {
                lines[i] = lines[i].replace("§", "&");
            }

            SignTools.updateSign(sign, lines, false);

            new SignGUI(player, this.sign, getInterface().getPlugin()) {
                @Override
                public void onSignChangeEvent(String[] lines) {
                    if(updateSign) {
                        Bukkit.getScheduler().runTask(API.getInstance().getMainPlugin(), () -> SignTools.updateSign((Sign) sign.getLocation().getBlock().getState(), lines));
                    }

                    close();
                    SignGUIButton.this.onSignChangeEvent(lines);
                    getInterface().reinitialize();

                    Bukkit.getScheduler().runTaskLater(API.getInstance().getMainPlugin(), () -> {
                        getInterface().open();
                        getInterface().setClosingForGUI(false);
                    }, 1L);
                }
            }.open();
        } else {
            onOtherClick(e);
        }
    }

    public void onOtherClick(InventoryClickEvent e) {
    }

    public boolean interrupt() {
        return false;
    }

    public abstract void onSignChangeEvent(String[] lines);
}
