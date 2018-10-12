package de.codingair.codingapi.player.gui.inventory.gui.simple;

import de.codingair.codingapi.player.gui.inventory.gui.GUI;
import de.codingair.codingapi.player.gui.inventory.gui.GUIListener;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;

public class SimpleGUI extends GUI {
    private Layout layout;
    private Page main;
    private Page current;
    private boolean closingForAnvil = false;

    public SimpleGUI(Player p, Page main, JavaPlugin plugin) {
        this(p, null, main, plugin);
    }

    public SimpleGUI(Player p, Layout layout, Page main, JavaPlugin plugin) {
        super(p, main.getTitle(), main.getLayout() == null ? layout.getSize() : main.getLayout().getSize(), plugin, false);

        this.layout = layout;
        this.main = main;
        this.current = main;

        addListener(new GUIListener() {
            @Override
            public void onInvClickEvent(InventoryClickEvent e) {

            }

            @Override
            public void onInvOpenEvent(InventoryOpenEvent e) {

            }

            @Override
            public void onInvCloseEvent(InventoryCloseEvent e) {
                if(!isClosingByButton() && !isClosingByOperation() && current != null) current.onExitByPlayer();
            }

            @Override
            public void onInvDragEvent(InventoryDragEvent e) {

            }

            @Override
            public void onMoveToTopInventory(ItemStack item, int oldRawSlot, List<Integer> newRawSlots) {

            }

            @Override
            public void onCollectToCursor(ItemStack item, List<Integer> oldRawSlots, int newRawSlot) {

            }
        });

        initialize(p);
    }

    @Override
    public void initialize(Player p) {
        if(this.main != null) this.main.initialize(this);
    }

    void changePage(Page page) {
        this.current = page;
        if(page.initialize(this)) reopen();
    }

    public Page getMain() {
        return main;
    }

    public Layout getLayout() {
        return layout;
    }

    public boolean isClosingForAnvil() {
        return closingForAnvil;
    }

    public void setClosingForAnvil(boolean closingForAnvil) {
        this.closingForAnvil = closingForAnvil;
    }
}
