package de.codingair.codingapi.player.gui.inventory.gui.simple;

import de.codingair.codingapi.player.gui.inventory.gui.GUI;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class SimpleGUI extends GUI {
    private Layout layout;
    private Page main;

    public SimpleGUI(Player p, Page main, JavaPlugin plugin) {
        this(p, null, main, plugin);
    }

    public SimpleGUI(Player p, Layout layout, Page main, JavaPlugin plugin) {
        super(p, main.getTitle(), main.getLayout() == null ? layout.getSize() : main.getLayout().getSize(), plugin, false);

        this.layout = layout;
        this.main = main;

        initialize(p);


    }

    @Override
    public void initialize(Player p) {
        if(this.main != null) this.main.initialize(this);
    }

    void changePage(Page page) {
        if(page.initialize(this)) reopen();
    }

    public Page getMain() {
        return main;
    }

    public Layout getLayout() {
        return layout;
    }
}
