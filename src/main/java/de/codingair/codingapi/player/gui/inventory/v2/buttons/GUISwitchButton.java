package de.codingair.codingapi.player.gui.inventory.v2.buttons;

import de.codingair.codingapi.player.gui.inventory.v2.GUI;
import de.codingair.codingapi.tools.Call;
import org.bukkit.event.inventory.ClickType;

public interface GUISwitchButton {
    /**
     * Similar to {@link Button#canClick(ClickType)} but for the GUI switch.
     *
     * @param type The click type.
     * @return Whether we can switch to another GUI or not.
     */
    boolean canSwitch(ClickType type);

    /**
     * Implement your method here to open your custom GUI. Finish by using the {@link Call} to go back to the old GUI.
     *
     * @param clickType The click type.
     * @param gui       The current GUI.
     * @param call      A callback to go back to 'gui'.
     * @return true if the GUI should listen on the {@link org.bukkit.event.inventory.InventoryCloseEvent} to fallback automatically.
     */
    boolean open(ClickType clickType, GUI gui, Call call);
}
