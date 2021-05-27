package de.codingair.codingapi.player.gui.inventory.v2;

import de.codingair.codingapi.player.gui.inventory.v2.buttons.Button;
import de.codingair.codingapi.player.gui.inventory.v2.buttons.GUISwitchButton;
import de.codingair.codingapi.player.gui.inventory.v2.exceptions.IsNotWaitingException;
import de.codingair.codingapi.tools.Call;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class GUIListener implements Listener {
    private final GUI gui;
    private Call closeListener = null;

    public GUIListener(GUI gui) {
        this.gui = gui;
    }

    @EventHandler
    public void onClose(InventoryCloseEvent e) {
        if (gui == null || gui.getInventory() == null) return;

        if (!gui.waiting && gui.getInventory().equals(e.getInventory())) {
            if (gui.closing != null) Bukkit.getScheduler().runTaskLater(gui.getPlugin(), () -> gui.closing.accept(gui.getPlayer()), 1); //use short delay
            else gui.forceClose(this, null);
        } else if (closeListener != null && e.getPlayer().equals(gui.getPlayer())) {
            Bukkit.getScheduler().runTaskLater(gui.getPlugin(), () -> closeListener.proceed(), 1);
        }
    }

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        if (gui == null || gui.getInventory() == null) return;

        if (!gui.waiting && gui.getInventory().equals(e.getInventory())) {
            if (e.getClickedInventory() != null && e.getClickedInventory().equals(e.getView().getTopInventory())) {
                e.setCancelled(true);
                Button b = gui.getActive().getButtonAt(e.getSlot());

                if (b != null) {
                    if (b.canClick(e.getClick())) {
                        b.onClick(gui, e);
                        b.playSound(gui.getPlayer());

                        if (b instanceof GUISwitchButton) {
                            if (((GUISwitchButton) b).canSwitch(e.getClick())) {
                                gui.waiting = true;

                                Call closing;
                                boolean listenOnClose = ((GUISwitchButton) b).open(e.getClick(), gui, closing = () -> {
                                    try {
                                        closeListener = null;
                                        gui.continueGUI();
                                    } catch (IsNotWaitingException ex) {
                                        ex.printStackTrace();
                                    }
                                });

                                if (listenOnClose) closeListener = closing;
                            }
                        }
                    }
                }
            } else {
                if (e.getAction() == InventoryAction.MOVE_TO_OTHER_INVENTORY) {
                    e.setCancelled(true);
                } else if (e.getAction() == InventoryAction.COLLECT_TO_CURSOR) {
                    e.setCancelled(true);

                    ItemStack cursor = e.getCursor();
                    if (cursor != null && cursor.getAmount() < cursor.getMaxStackSize()) {
                        Inventory inv = e.getView().getBottomInventory();

                        List<Integer> list = new ArrayList<>();
                        for (int i = 0; i < inv.getSize(); i++) {
                            ItemStack current = inv.getItem(i);
                            if (cursor.isSimilar(current)) list.add(i);
                        }

                        list.sort((i, i1) -> {
                            ItemStack current = inv.getItem(i);
                            ItemStack other = inv.getItem(i1);
                            return Integer.compare(current.getAmount(), other.getAmount());
                        });

                        for (Integer i : list) {
                            ItemStack current = inv.getItem(i);
                            int trade = Math.min(cursor.getMaxStackSize() - cursor.getAmount(), current.getAmount());

                            cursor.setAmount(cursor.getAmount() + trade);
                            current.setAmount(current.getAmount() - trade);

                            if (current.getAmount() == 0) inv.clear(i);
                            if (cursor.getAmount() == cursor.getMaxStackSize()) break;
                        }

                        list.clear();
                    }
                }
            }
        }
    }

    @EventHandler
    public void onDrag(InventoryDragEvent e) {
        if (gui == null || gui.getInventory() == null) return;

        if (!gui.waiting && gui.getInventory().equals(e.getInventory())) {
            int max = gui.getInventory().getSize();
            for (Integer rawSlot : e.getRawSlots()) {
                if (rawSlot < max) {
                    e.setCancelled(true);
                }
            }
        }
    }
}
