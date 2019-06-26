package de.codingair.codingapi.player.gui.inventory.gui.simple;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;

public abstract class SyncTriggerButton extends SyncButton {
    private ClickType[] trigger;
    private ClickType lastTrigger = null;

    public SyncTriggerButton(int slot) {
        super(slot);
    }

    public SyncTriggerButton(int x, int y) {
        this(x + y * 9);
    }

    public SyncTriggerButton(int slot, ClickType... trigger) {
        super(slot);
        this.trigger = trigger;
    }

    public SyncTriggerButton(int x, int y, ClickType... trigger) {
        this(x + y * 9);
        this.trigger = trigger;
    }

    public ClickType getLastTrigger() {
        return lastTrigger;
    }

    @Override
    public void onClick(InventoryClickEvent e, Player player) {
        boolean triggered = lastTrigger == null || trigger.length == 0;
        if(trigger != null)
            for(ClickType clickType : trigger) {
                if(e.getClick() == clickType) {
                    triggered = true;
                    lastTrigger = clickType;
                    break;
                }
            }

        if(trigger == null || triggered) {
            onTrigger(e, e.getClick(), player);
        } else onOtherClick(e);
    }

    public abstract void onTrigger(InventoryClickEvent e, ClickType trigger, Player player);

    public void onOtherClick(InventoryClickEvent e) {
    }
}
