package de.codingair.codingapi.player.gui.inventory.gui;

import de.codingair.codingapi.API;
import de.codingair.codingapi.player.gui.inventory.gui.itembutton.ItemButton;
import de.codingair.codingapi.server.Sound;
import de.codingair.codingapi.server.SoundData;
import de.codingair.codingapi.server.Version;
import de.codingair.codingapi.tools.Callback;
import de.codingair.codingapi.utils.Removable;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;

import java.util.*;

/**
 * Removing of this disclaimer is forbidden.
 *
 * @author codingair
 **/

public abstract class GUI extends Interface implements Removable {
    private UUID uniqueId = UUID.randomUUID();
    private JavaPlugin plugin;
    private Player player;
    private SoundData openSound = null;
    private SoundData cancelSound = null;
    private boolean closingByButton = false;
    private boolean closingByOperation = false;
    private boolean closingForGUI = false;
    private boolean moveOwnItems = false;
    private List<Integer> movableSlots = new ArrayList<>();
    private List<GUIListener> listeners = new ArrayList<>();
    private boolean canDropItems = false;
    protected boolean isClosed = false;
    private boolean buffering = false;
    private Callback<GUI> closingConfirmed = null;
    private GUI fallbackGUI = null;
    private boolean useFallbackGUI = false;

    public GUI(Player p, String title, int size, JavaPlugin plugin) {
        this(p, title, size, plugin, true);
    }

    public GUI(Player p, String title, int size, JavaPlugin plugin, boolean preInitialize) {
        super(p, title, size, plugin);
        oldUsage = false;

        super.setEditableItems(false);

        this.plugin = plugin;
        this.player = p;

        super.addListener(new InterfaceListener() {
            @Override
            public void onInvClickEvent(InventoryClickEvent e) {
                listeners.forEach(l -> l.onInvClickEvent(e));
            }

            @Override
            public void onInvOpenEvent(InventoryOpenEvent e) {
                listeners.forEach(l -> l.onInvOpenEvent(e));
            }

            @Override
            public void onInvCloseEvent(InventoryCloseEvent e) {
                listeners.forEach(l -> l.onInvCloseEvent(e));
                if(!closingByOperation && !closingByButton && !closingForGUI && useFallbackGUI && fallbackGUI != null) fallbackGUI.open();
            }

            @Override
            public void onClickBottomInventory(InventoryClickEvent e) {
                listeners.forEach(l -> l.onClickBottomInventory(e));
            }

            @Override
            public void onDropItem(InventoryClickEvent e) {
                listeners.forEach(l -> l.onDropItem(e));
            }

            @Override
            public void onInvDragEvent(InventoryDragEvent e) {
                listeners.forEach(l -> l.onInvDragEvent(e));
            }
        });

        this.setEditableItems(false);

        if(preInitialize) initialize(p);
    }

    public List<GUIListener> getGUIListeners() {
        return listeners;
    }

    @Override
    public UUID getUniqueId() {
        return uniqueId;
    }

    @Override
    public Class<? extends Removable> getAbstractClass() {
        return GUI.class;
    }

    @Override
    public void destroy() {
        close();
    }

    public abstract void initialize(Player p);

    public void addListener(GUIListener listener) {
        this.listeners.add(listener);
    }

    public void removeListener(GUIListener listener) {
        this.listeners.remove(listener);
    }

    public void reinitialize() {
        clear();
        initialize(this.player);
    }

    public void reinitialize(String title) {
        this.reinitialize();
        setTitle(title);
    }

    @Deprecated
    public void onClose(Player p) {
    }

    @Deprecated
    public void onOpen(Player p) {
    }

    @Override
    public void open(Player p) {
        isClosed = false;
        closingByButton = false;
        closingByOperation = false;
        closingForGUI = false;

        Callback<GUI> run = new Callback<GUI>() {
            @Override
            public void accept(GUI old) {
                if(fallbackGUI == null) fallbackGUI = old;
                if(GUI.this.openSound != null) GUI.this.openSound.play(p);
                API.addRemovable(GUI.this);
                GUI.this.reinitialize(getTitle());
                GUI.super.open(p);

                if(old != null) old.closingConfirmed = null;
            }
        };

        if(GUI.usesGUI(p)) {
            if(GUI.getGUI(p) == this) return;

            GUI gui = GUI.getGUI(p);

            if(getSize() == gui.getSize()) {
                if(!gui.closingByButton && !gui.closingByOperation && !gui.closingForGUI) {
                    gui.closingConfirmed = run;
                } else {
                    gui.isClosed = true;
                    API.removeRemovable(gui);
                    this.inventory = gui.inventory;
                    API.addRemovable(this);
                    addToPlayerList(p);
                    this.reinitialize(this.getTitle());
                }
            } else {
                gui.closingConfirmed = run;
                gui.close();
            }
            return;
        }

        if(GUI.usesOldGUI(p)) {
            GUI.getOldGUI(p).close(p);
        }

        run.accept(null);
    }

    public void open() {
        this.open(this.player);
    }

    @Override
    public void close(Player p, boolean isClosing) {
        if(isClosed) return;
        else isClosed = true;

        closingByOperation = !isClosing;
        super.close(p, isClosing);
    }

    @Override
    public void close(Player p) {
        close(p, false);
    }

    public void close() {
        close(this.player);
    }

    public void confirmClosing() {
        API.removeRemovable(this);
        if(this.closingConfirmed != null) {
            Bukkit.getScheduler().runTaskLater(getPlugin(), () -> closingConfirmed.accept(this), 1L);
        }
    }

    public void setEditableSlots(boolean movable, Integer... slots) {
        setEditableSlots(movable, Arrays.asList(slots));
    }

    public void setEditableSlots(boolean movable, List<Integer> slots) {
        if(movable) {
            for(int slot : slots) {
                if(this.movableSlots.contains(slot)) continue;
                this.movableSlots.add(slot);
            }
        } else {
            for(int slot : slots) {
                this.movableSlots.remove((Object) slot);
            }
        }

        Collections.sort(this.movableSlots);
    }

    public List<Integer> getMovableSlots() {
        return Collections.unmodifiableList(this.movableSlots);
    }

    public boolean isMovable(int slot) {
        return this.movableSlots.contains(slot);
    }

    @Override
    public Player getPlayer() {
        return player;
    }

    @Override
    public boolean isUsing(Player player) {
        return this.player.getName().equals(player.getName());
    }

    public JavaPlugin getPlugin() {
        return plugin;
    }

    public SoundData getCancelSound() {
        return cancelSound;
    }

    public GUI setCancelSound(Sound sound) {
        this.cancelSound = new SoundData(sound, 1, 1);
        return this;
    }

    public GUI setCancelSound(SoundData cancelSound) {
        this.cancelSound = cancelSound;
        return this;
    }

    public boolean isClosingByButton() {
        return closingByButton;
    }

    public void setClosingByButton(boolean closingByButton) {
        this.closingByButton = closingByButton;
    }

    public void changeGUI(GUI newGui) {
        if(getSize() == newGui.getSize()) {
            newGui.setInventory(getInventory());
            newGui.reinitialize(newGui.getTitle());
            close(player, true);
        } else {
            close();
            newGui.open();
        }
    }

    protected void setInventory(Inventory inv) {
        this.inventory = inv;
    }

    public void addLine(int x0, int y0, int x1, int y1, ItemStack item, boolean override) {
        double cX = (double) x0, cY = (double) y0;
        Vector v = new Vector(x1, y1, 0).subtract(new Vector(x0, y0, 0)).normalize();

        do {
            if(override || getItem((int) cX, (int) cY) == null || getItem((int) cX, (int) cY).getType().equals(Material.AIR)) setItem((int) cX, (int) cY, item.clone());
            cX += v.getX();
            cY += v.getY();
        } while((int) cX != x1 || (int) cY != y1);

        if(override || getItem((int) cX, (int) cY) == null || getItem((int) cX, (int) cY).getType().equals(Material.AIR)) setItem((int) cX, (int) cY, item.clone());
    }

    @Override
    public void addButton(int slot, ItemButton button) {
        super.addButton(slot, button);
        if(!buffering) updateInventory(this.player);
    }

    @Override
    public HashMap<Integer, ItemStack> addItem(ItemStack... arg0) throws IllegalArgumentException {
        HashMap<Integer, ItemStack> map = super.addItem(arg0);
        if(!buffering) updateInventory(this.player);
        return map;
    }

    @Override
    public void addButton(ItemButton button) {
        super.addButton(button);
        if(!buffering) updateInventory(this.player);
    }

    @Override
    public void setItem(ItemStack item, int startSlot, int endSlot) {
        super.setItem(item, startSlot, endSlot);
        if(!buffering) updateInventory(this.player);
    }

    @Override
    public void setFrame(ItemStack item) {
        super.setFrame(item);
        if(!buffering) updateInventory(this.player);
    }

    @Override
    public void setBackground(ItemStack background) {
        super.setBackground(background);
        if(!buffering) updateInventory(this.player);
    }

    @Override
    public Interface setDisplayname(int slot, String name) {
        super.setDisplayname(slot, name);
        if(!buffering) updateInventory(this.player);
        return this;
    }

    @Override
    public Interface setLore(int slot, String... lore) {
        super.setLore(slot, lore);
        if(!buffering) updateInventory(this.player);
        return this;
    }

    @Override
    public Interface setAmount(int slot, int amount) {
        super.setAmount(slot, amount);
        if(!buffering) updateInventory(this.player);
        return this;
    }

    @Override
    public boolean setSize(int size) {
        boolean result = super.setSize(size);
        if(!buffering) updateInventory(this.player);
        return result;
    }

    @Override
    public void setTitle(String title, boolean update) {
        boolean isOpened = isOpen();

        super.setTitle(title, update && isOpened);
        if(!isOpened) {
            rebuildInventory();
        }

        if(!buffering) updateInventory(this.player);
    }

    @Override
    public void setTitle(String title) {
        this.setTitle(title, true);
    }

    @Override
    public void setContents(ItemStack[] arg0) throws IllegalArgumentException {
        super.setContents(arg0);
        if(!buffering) updateInventory(this.player);
    }

    @Override
    public void setItem(int x, ItemStack item) {
        super.setItem(x, item);
        if(!buffering) updateInventory(this.player);
    }

    @Override
    public void setItem(int x, int y, ItemStack item) {
        super.setItem(x, y, item);
        if(!buffering) updateInventory(this.player);
    }

    public ItemButton getButtonAt(int x, int y) {
        return getButtonAt(x + y * 9);
    }

    public static GUI getGUI(Player p) {
        return API.getRemovable(p, GUI.class);
    }

    public boolean isOpen() {
        return getGUI(getPlayer()) == this;
    }

    public static Interface getOldGUI(Player p) {
        for(Interface i : interfaces) {
            if(i.isUsing(p)) return i;
        }

        return null;
    }

    public static boolean usesGUI(Player p) {
        return getGUI(p) != null;
    }

    public static boolean usesOldGUI(Player p) {
        return getOldGUI(p) != null;
    }

    public SoundData getOpenSound() {
        return openSound;
    }

    public GUI setOpenSound(SoundData openSound) {
        this.openSound = openSound;
        return this;
    }

    public boolean isMoveOwnItems() {
        return moveOwnItems;
    }

    public void setMoveOwnItems(boolean moveOwnItems) {
        this.moveOwnItems = moveOwnItems;
    }

    public boolean isCanDropItems() {
        return canDropItems;
    }

    public void setCanDropItems(boolean canDropItems) {
        this.canDropItems = canDropItems;
    }

    public boolean isClosingByOperation() {
        return closingByOperation;
    }

    public boolean isClosed() {
        return isClosed;
    }

    public boolean isBuffering() {
        return buffering;
    }

    public void setBuffering(boolean buffering) {
        this.buffering = buffering;
    }

    public void release() {
        updateInventory(getPlayer());
    }

    public boolean isUseFallbackGUI() {
        return useFallbackGUI;
    }

    public void setUseFallbackGUI(boolean useFallbackGUI) {
        this.useFallbackGUI = useFallbackGUI;
    }

    public GUI getFallbackGUI() {
        return fallbackGUI;
    }

    public boolean isClosingForGUI() {
        return closingForGUI;
    }

    public void setClosingForGUI(boolean closingForGUI) {
        this.closingForGUI = closingForGUI;
    }

    public static boolean updateInventory(Player player) {
        if(Version.getVersion().isBiggerThan(Version.v1_13)) return false;
        player.updateInventory();
        return true;
    }
}
