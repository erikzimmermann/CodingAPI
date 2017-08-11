package de.CodingAir.v1_6.CodingAPI.Player.GUI.Inventory.GUIs;

import de.CodingAir.v1_6.CodingAPI.Player.GUI.Inventory.Interface.GUI;
import de.CodingAir.v1_6.CodingAPI.Player.GUI.Inventory.Interface.InterfaceListener;
import de.CodingAir.v1_6.CodingAPI.Player.GUI.Inventory.Interface.ItemButton.ItemButton;
import de.CodingAir.v1_6.CodingAPI.Player.GUI.Inventory.Interface.ItemButton.ItemButtonOption;
import de.CodingAir.v1_6.CodingAPI.Server.Sound;
import de.CodingAir.v1_6.CodingAPI.Tools.Callback;
import de.CodingAir.v1_6.CodingAPI.Tools.ItemBuilder;
import org.bukkit.Bukkit;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.plugin.Plugin;

public class ConfirmGUI extends GUI {
    private Callback<Boolean> callback;
    private Runnable close;

    private String accept;
    private String message;
    private String decline;

    public ConfirmGUI(Player p, String title, String accept, String message, String decline, Plugin plugin, Callback<Boolean> callback) {
        super(p, title, 9, plugin, false);

        this.callback = callback;
        this.accept = accept;
        this.message = message;
        this.decline = decline;

        initialize(p);
    }

    public ConfirmGUI(Player p, String title, String accept, String message, String decline, Plugin plugin, Callback<Boolean> callback, Runnable close) {
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

                Bukkit.getScheduler().runTaskLater(plugin, close, 1L);
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

        addButton(new ItemButton(2, new ItemBuilder(Material.WOOL).setColor(DyeColor.LIME).setName(accept).getItem()) {
            @Override
            public void onClick(InventoryClickEvent e) {
                callback.accept(true);
            }
        }.setOption(option));

        setItem(4, new ItemBuilder(Material.NETHER_STAR).setName(message).getItem());

        addButton(new ItemButton(6, new ItemBuilder(Material.WOOL).setColor(DyeColor.RED).setName(decline).getItem()) {
            @Override
            public void onClick(InventoryClickEvent e) {
                callback.accept(false);
            }
        }.setOption(option));
    }
}
