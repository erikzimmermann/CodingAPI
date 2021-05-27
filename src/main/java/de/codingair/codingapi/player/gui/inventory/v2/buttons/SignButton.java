package de.codingair.codingapi.player.gui.inventory.v2.buttons;

import de.codingair.codingapi.player.gui.inventory.v2.GUI;
import de.codingair.codingapi.player.gui.sign.SignGUI;
import de.codingair.codingapi.player.gui.sign.SignTools;
import de.codingair.codingapi.tools.Call;
import org.bukkit.Bukkit;
import org.bukkit.block.Sign;
import org.bukkit.event.inventory.ClickType;

public abstract class SignButton extends Button implements GUISwitchButton {
    private final Sign sign;
    protected final String[] lines;

    private SignButton(Sign sign, String[] lines) {
        this.sign = sign;
        this.lines = lines;
    }

    public SignButton(Sign sign) {
        this(sign, null);
    }

    public SignButton(String[] lines) {
        this(null, lines);
    }

    /**
     *
     * @param lines The applied lines of the sign.
     * @return True if the sign GUI should be closed. False reopens it after a short delay since we need this for MC.
     */
    public abstract boolean onSignChangeEvent(GUI fallback, String[] lines);

    @Override
    public boolean open(ClickType clickType, GUI gui, Call call) {
        new SignGUI(gui.getPlayer(), gui.getPlugin(), this.sign, this.lines) {
            @Override
            public void onSignChangeEvent(String[] lines) {
                //update sign
                if(sign != null) Bukkit.getScheduler().runTask(gui.getPlugin(), () -> SignTools.updateSign(sign, lines, true));

                if (SignButton.this.onSignChangeEvent(gui, lines)) close(call);
                else Bukkit.getScheduler().runTask(gui.getPlugin(), this::open);
            }
        }.open();
        return false;
    }
}
