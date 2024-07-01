package de.codingair.codingapi.player.gui.inventory.v2.buttons;

import com.github.Anon8281.universalScheduler.UniversalScheduler;
import de.codingair.codingapi.player.gui.inventory.v2.GUI;
import de.codingair.codingapi.player.gui.sign.SignGUI;
import de.codingair.codingapi.player.gui.sign.SignTools;
import de.codingair.codingapi.tools.Call;
import org.bukkit.Bukkit;
import org.bukkit.block.Sign;
import org.bukkit.event.inventory.ClickType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

public abstract class SignButton extends Button implements GUISwitchButton {
    private final Sign sign;
    protected final Supplier<String[]> lines;

    private SignButton(@Nullable Sign sign, @NotNull Supplier<String[]> lines) {
        this.sign = sign;
        this.lines = lines;
    }

    private SignButton(@Nullable Sign sign, @Nullable String[] lines) {
        this(sign, () -> lines);
    }

    public SignButton(@Nullable Sign sign) {
        this(sign, (String[]) null);
    }

    public SignButton(@Nullable String[] lines) {
        this(null, lines);
    }

    public SignButton(@NotNull Supplier<String[]> lines) {
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
        new SignGUI(gui.getPlayer(), gui.getPlugin(), this.sign, this.lines.get()) {
            @Override
            public void onSignChangeEvent(String[] lines) {
                //update sign
                if(sign != null)
                    UniversalScheduler.getScheduler(gui.getPlugin()).runTask(() -> SignTools.updateSign(sign, lines, true));

                if (SignButton.this.onSignChangeEvent(gui, lines)) close(call);
                else UniversalScheduler.getScheduler(gui.getPlugin()).runTask(this::open);
            }
        }.open();
        return false;
    }
}
