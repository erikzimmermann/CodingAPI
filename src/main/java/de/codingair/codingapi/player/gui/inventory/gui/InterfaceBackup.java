package de.codingair.codingapi.player.gui.inventory.gui;

import de.codingair.codingapi.player.gui.inventory.gui.itembutton.ItemButton;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;

public class InterfaceBackup {
    private final ItemStack[] contents;
    private final HashMap<Integer, ItemButton> buttons;
    private final String wrappersName;

    public InterfaceBackup(ItemStack[] contents, HashMap<Integer, ItemButton> buttons, String wrappersName) {
        this.contents = contents;
        this.buttons = buttons;
        this.wrappersName = wrappersName;
    }

    public ItemStack[] getContents() {
        return contents;
    }

    public HashMap<Integer, ItemButton> getButtons() {
        return buttons;
    }

    public String getWrappersName() {
        return wrappersName;
    }
}
