package de.codingair.codingapi.player.gui.inventory.gui.simple;

import de.codingair.codingapi.player.gui.hotbar.HotbarGUI;
import de.codingair.codingapi.utils.Node;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;

public abstract class SyncHotbarGUIButton extends SyncTriggerButton {
    private Node<ClickType, HotbarGUI>[] hotbars;

    public SyncHotbarGUIButton(int slot, HotbarGUI gui) {
        super(slot);
        this.hotbars = new Node[]{new Node(null, gui)};
    }

    public SyncHotbarGUIButton(int x, int y, HotbarGUI gui) {
        super(x, y);
        this.hotbars = new Node[]{new Node(null, gui)};
    }

    public SyncHotbarGUIButton(int slot, Node<ClickType, HotbarGUI>... clicks) {
        super(slot);
        this.hotbars = clicks;
    }

    public SyncHotbarGUIButton(int x, int y, Node<ClickType, HotbarGUI>... clicks) {
        super(x, y);
        this.hotbars = clicks;
    }

    @Override
    public void onTrigger(InventoryClickEvent e, ClickType trigger, Player player) {
        for(Node<ClickType, HotbarGUI> hotbar : hotbars) {
            if(hotbar.getValue() == null) continue;

            if(hotbar.getKey() == null || hotbar.getKey() == trigger) {
                hotbar.getValue().setOnFinish(() -> onFinish(player));
                getInterface().setClosingForGUI(true);
                getInterface().close();
                hotbar.getValue().open(true);
                break;
            }
        }
    }

    public abstract void onFinish(Player player);
}
