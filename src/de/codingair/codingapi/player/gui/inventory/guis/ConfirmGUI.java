package de.codingair.codingapi.player.gui.inventory.guis;

import de.codingair.codingapi.player.gui.inventory.gui.GUI;
import de.codingair.codingapi.player.gui.inventory.gui.InterfaceListener;
import de.codingair.codingapi.player.gui.inventory.gui.itembutton.ItemButton;
import de.codingair.codingapi.player.gui.inventory.gui.itembutton.ItemButtonOption;
import de.codingair.codingapi.server.Sound;
import de.codingair.codingapi.tools.Callback;
import de.codingair.codingapi.tools.items.ItemBuilder;
import de.codingair.codingapi.tools.items.XMaterial;
import de.codingair.codingapi.utils.TextAlignment;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;

public class ConfirmGUI extends GUI {
    private Callback<Boolean> callback;
    private Runnable close;

    private String accept;
    private String message;
    private String decline;

    public ConfirmGUI(Player p, String title, String accept, String message, String decline, JavaPlugin plugin, Callback<Boolean> callback) {
        super(p, title, 9, plugin, false);

        this.callback = callback;
        this.accept = accept;
        this.message = message;
        this.decline = decline;

        initialize(p);
    }

    public ConfirmGUI(Player p, String title, String accept, String message, String decline, JavaPlugin plugin, Callback<Boolean> callback, Runnable close) {
        super(p, title, 9, plugin, false);

        this.callback = callback;
        this.close = close;
        this.accept = accept;
        this.message = message;
        this.decline = decline;

        addListener(new InterfaceListener() {
            boolean run = false;

            @Override
            public void onInvClickEvent(InventoryClickEvent e) {}

            @Override
            public void onInvOpenEvent(InventoryOpenEvent e) {}

            @Override
            public void onInvCloseEvent(InventoryCloseEvent e) {
                if(run) return;
                run = true;

                if(ConfirmGUI.this.close != null) Bukkit.getScheduler().runTaskLater(plugin, ConfirmGUI.this.close, 1L);
            }

            @Override
            public void onInvDragEvent(InventoryDragEvent e) {}
        });

        initialize(p);
    }

    @Override
    public void initialize(Player p) {
        ItemButtonOption option = new ItemButtonOption();
        option.setClickSound(Sound.CLICK.bukkitSound());
        option.setOnlyLeftClick(true);
        option.setCloseOnClick(true);

        addButton(new ItemButton(2, new ItemBuilder(XMaterial.LIME_TERRACOTTA).setName(accept).getItem()) {
            @Override
            public void onClick(InventoryClickEvent e) {
                callback.accept(true);
            }
        }.setOption(option));

        List<String> lines = TextAlignment.lineBreak(message, 80);

        setItem(4, new ItemBuilder(Material.NETHER_STAR).setName(lines.remove(0)).setLore(lines).getItem());

        addButton(new ItemButton(6, new ItemBuilder(XMaterial.RED_TERRACOTTA).setName(decline).getItem()) {
            @Override
            public void onClick(InventoryClickEvent e) {
                callback.accept(false);
            }
        }.setOption(option));
    }
}
