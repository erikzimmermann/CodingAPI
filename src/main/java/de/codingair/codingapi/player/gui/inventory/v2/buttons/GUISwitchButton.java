package de.codingair.codingapi.player.gui.inventory.v2.buttons;

import de.codingair.codingapi.player.gui.inventory.v2.GUI;
import de.codingair.codingapi.tools.Call;
import org.bukkit.event.inventory.ClickType;

public interface GUISwitchButton {
    boolean canSwitch(ClickType type);
    void open(GUI gui, Call call);
}
