package de.codingair.codingapi.player.gui.inventory.gui.simple;

import de.codingair.codingapi.player.gui.inventory.gui.GUI;
import de.codingair.codingapi.player.gui.inventory.gui.itembutton.ItemButton;
import de.codingair.codingapi.player.gui.inventory.gui.itembutton.ItemButtonOption;
import de.codingair.codingapi.server.sounds.SoundData;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

public abstract class Button extends ItemButton {
    private Page link;
    private ClickType[] linkTrigger = null;

    public Button(int slot, ItemStack item) {
        super(slot, item);
    }

    public Button(int x, int y, ItemStack item) {
        super(x, y, item);
    }

    @Override
    public void onClick(InventoryClickEvent e) {
        onClick(e, (Player) e.getWhoClicked());

        if (linkTrigger == null) proceed(e);
        else {
            for (ClickType clickType : linkTrigger) {
                if (clickType == e.getClick()) {
                    proceed(e);
                    break;
                }
            }
        }
    }

    private void proceed(InventoryClickEvent e) {
        if (this.link != null) getGUI().changePage(this.link);
    }

    public abstract void onClick(InventoryClickEvent e, Player player);

    public Page getLink() {
        return link;
    }

    public void setLink(Page link) {
        this.link = link;
    }

    @Override
    public GUI getInterface() {
        return (GUI) super.getInterface();
    }

    public SimpleGUI getGUI() {
        return (SimpleGUI) super.getInterface();
    }

    @Override
    public Button setOption(ItemButtonOption option) {
        return (Button) super.setOption(option);
    }

    @Override
    public Button setMoveable(boolean movable) {
        return (Button) super.setMoveable(movable);
    }

    @Override
    public Button setClickSound(Sound clickSound) {
        return (Button) super.setClickSound(clickSound);
    }

    @Override
    public Button setClickSound2(SoundData clickSound) {
        return (Button) super.setClickSound2(clickSound);
    }

    @Override
    public Button setCloseOnClick(boolean closeOnClick) {
        return (Button) super.setCloseOnClick(closeOnClick);
    }

    @Override
    public Button setOnlyLeftClick(boolean onlyLeftClick) {
        return (Button) super.setOnlyLeftClick(onlyLeftClick);
    }

    @Override
    public Button setOnlyRightClick(boolean onlyRightClick) {
        return (Button) super.setOnlyRightClick(onlyRightClick);
    }

    public ClickType[] getLinkTrigger() {
        return linkTrigger;
    }

    public Button setLinkTrigger(ClickType... linkTrigger) {
        this.linkTrigger = linkTrigger;
        return this;
    }
}
