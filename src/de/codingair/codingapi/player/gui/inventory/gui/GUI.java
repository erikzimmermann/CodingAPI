package de.codingair.codingapi.player.gui.inventory.gui;

import de.codingair.codingapi.API;
import de.codingair.codingapi.player.gui.inventory.gui.itembutton.ItemButton;
import de.codingair.codingapi.server.Version;
import de.codingair.codingapi.server.sounds.Sound;
import de.codingair.codingapi.server.sounds.SoundData;
import de.codingair.codingapi.tools.Callback;
import de.codingair.codingapi.utils.Removable;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.*;
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
    public static final HashMap<Player, Callback<GUI>> foreignConfirmations = new HashMap<>();
    private UUID uniqueId = UUID.randomUUID();
    private JavaPlugin plugin;
    private Player player;
    private SoundData openSound = null;
    private SoundData cancelSound = null;
    private boolean closingByButton = false;
    private boolean closingByOperation = false;
    private boolean closingForGUI = false;
    private boolean moveOwnItems = true;
    private List<Integer> movableSlots = new ArrayList<>();
    private List<GUIListener> listeners = new ArrayList<>();
    private boolean canDropItems = false;
    protected boolean isClosed = false;
    private boolean buffering = false;
    private Callback<GUI> closingConfirmed = null;
    private GUI fallbackGUI = null;
    private boolean useFallbackGUI = false;
    protected boolean openForFirstTime = true;

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
                if(closingForGUI) return;
                listeners.forEach(l -> l.onInvCloseEvent(e));

                if(useFallbackGUI && fallbackGUI != null) {
                    GUI fb = fallbackGUI;
                    fb.reinitialize();
                    Bukkit.getScheduler().runTaskLater(getPlugin(), fb::open, 1);
                }
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
        isClosed = true;
        closingByButton = false;
        closingByOperation = false;
        closingForGUI = false;

        Callback<GUI> run = new Callback<GUI>() {
            @Override
            public void accept(GUI old) {
                if(fallbackGUI == null) fallbackGUI = old;

                if(openForFirstTime) {
                    if(getOpenSound() != null) getOpenSound().play(player);
                    openForFirstTime = false;
                }

                if(old != null) {
                    //transfer existing inventory
                    if(old.getSize() == getSize()) {
                        isClosed = old.isClosed; //transfer status
                        old.isClosed = true; //cancel closing inventory

                        inventory = old.getInventory();
                        reinitialize(); //initialize new items/buttons

                        if(!isClosed && !old.getTitle().equals(getTitle())) {
                            GUI.super.addToPlayerList(getPlayer());
                            updateTitle(true);
                        }
                    }

                    //remove confirmation runnable
                    old.closingConfirmed = null;

                    //unregister/close old GUI if 'isClosed' is false
                    API.removeRemovable(old);
                }

                API.addRemovable(GUI.this); //register new GUI

                if(isClosed) {
                    isClosed = false;
                    GUI.super.open(p); //open if closed
                }
            }
        };

        if(GUI.usesGUI(p)) {
            if(GUI.getGUI(p) == this) return;

            GUI gui = GUI.getGUI(p);

            if((gui.closingForGUI && gui.getSize() == getSize()) || gui.isClosed) {
                run.accept(gui);
            } else {
                gui.closingConfirmed = run;
                gui.close();
            }
            return;
        } else if(GUI.usesOldGUI(p)) {
            GUI.getOldGUI(p).close(p);
        } else if(p.getOpenInventory() != null && p.getOpenInventory().getTopInventory() != null && p.getOpenInventory().getTopInventory().getType() != InventoryType.CRAFTING) {
            foreignConfirmations.put(p, run);
            return;
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
        closingForGUI = true;
        newGui.open();
    }

    public void changeGUI(GUI newGui, boolean fallback) {
        closingForGUI = true;
        newGui.setUseFallbackGUI(fallback);
        newGui.open();
    }

    protected void setInventory(Inventory inv) {
        this.inventory = inv;
    }

    public void addLine(int x0, int y0, int x1, int y1, ItemStack item, boolean override) {
        double cX = x0, cY = y0;
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
        if(isOldTitle(title)) return;
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
        GUI g = API.getRemovable(p, GUI.class);
        return g;
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

    public boolean useFallbackGUI() {
        return useFallbackGUI;
    }

    public GUI setUseFallbackGUI(boolean useFallbackGUI) {
        this.useFallbackGUI = useFallbackGUI;
        return this;
    }

    public GUI setFallbackGUI(GUI fallbackGUI) {
        this.fallbackGUI = fallbackGUI;
        return this;
    }

    public GUI getFallbackGUI() {
        return fallbackGUI;
    }

    public boolean fallBack() {
        if(fallbackGUI == null) return false;
        useFallbackGUI = false;
        fallbackGUI.reinitialize();
        changeGUI(fallbackGUI);
        return true;
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
