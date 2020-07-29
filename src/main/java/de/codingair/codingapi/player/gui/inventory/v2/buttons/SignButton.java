package de.codingair.codingapi.player.gui.inventory.v2.buttons;

import de.codingair.codingapi.API;
import de.codingair.codingapi.player.gui.inventory.v2.GUI;
import de.codingair.codingapi.player.gui.sign.SignGUI;
import de.codingair.codingapi.player.gui.sign.SignTools;
import de.codingair.codingapi.tools.Call;
import org.bukkit.Bukkit;
import org.bukkit.block.Sign;

public abstract class SignButton extends Button implements GUISwitchButton {
    private final Sign sign;

    public SignButton(Sign sign) {
        this.sign = sign;
    }

    public abstract void onSignChangeEvent(String[] lines);

    @Override
    public void open(GUI gui, Call call) {
        new SignGUI(gui.getPlayer(), this.sign, gui.getPlugin()) {
            @Override
            public void onSignChangeEvent(String[] lines) {
                boolean notEmpty = false;
                for(String line : lines) {
                    if(!line.isEmpty()) {
                        notEmpty = true;
                        break;
                    }
                }

                if(notEmpty) {
                    if(sign != null) Bukkit.getScheduler().runTask(API.getInstance().getMainPlugin(), () -> SignTools.updateSign(sign, lines, true));
                    SignButton.this.onSignChangeEvent(lines);
                }

                close(call);
            }
        }.open();
    }
}
