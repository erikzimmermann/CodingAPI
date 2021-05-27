package de.codingair.codingapi.player.gui.inventory.v2;

import com.google.common.base.Preconditions;
import de.codingair.codingapi.API;
import de.codingair.codingapi.player.gui.inventory.InventoryUtils;
import de.codingair.codingapi.player.gui.inventory.v2.exceptions.*;
import de.codingair.codingapi.tools.Callback;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Objects;

/**
 * GUI only for static purpose. Buttons cannot be moved (except with different pages which is not recommended).
 */
public class GUI extends InventoryBuilder {
    protected final HashSet<Page> pages = new HashSet<>();
    protected final HashMap<Class<? extends Page>, Page> pageLink = new HashMap<>();
    protected GUIListener listener;
    protected GUI fallback;
    protected Page active;
    protected boolean waiting = false; //for AnvilGUI e.g. (when this GUI has to be closed for a certain amount of time)
    Callback<Player> closing;
    private int size;
    private String title;
    private String originalTitle;

    public GUI(Player player, JavaPlugin plugin) {
        super(player, plugin);
    }

    public GUI(Player player, JavaPlugin plugin, int size, String title) {
        super(player, plugin);
        buildInventory(size, title);
    }

    @Override
    public void buildInventory(int size, String title) {
        this.size = size;
        this.originalTitle = (this.title = title);
        super.buildInventory(size, title);
    }

    public void open() throws AlreadyOpenedException, NoPageException, IsWaitingException {
        if (waiting) throw new IsWaitingException();
        if (isOpen()) throw new AlreadyOpenedException();
        if (active == null) throw new NoPageException();

        this.listener = new GUIListener(this);
        Bukkit.getPluginManager().registerEvents(this.listener, plugin);

        GUI gui = API.getRemovable(player, GUI.class);
        if (gui != null) {
            gui.waiting = true;
            setFallback(gui);
        }

        API.addRemovable(GUI.this);
        player.openInventory(inventory);
    }

    protected void continueGUI() throws IsNotWaitingException {
        if (!waiting) throw new IsNotWaitingException();

        waiting = false;
        player.openInventory(inventory);
    }

    public void close() throws AlreadyClosedException {
        close(null);
    }

    public void close(Callback<Player> callback) throws AlreadyClosedException {
        if (!isOpen()) throw new AlreadyClosedException();
        GUIListener listener = this.listener;

        //indicates that this GUI is no open anymore
        this.listener = null;

        if (fallback != null) {
            //set waiting true since changing the inventory also calls the InventoryCloseEvent and we don't want to trigger the manual close
            waiting = true;

            try {
                fallback.continueGUI();
            } catch (IsNotWaitingException e) {
                e.printStackTrace();
            }

            //avoid closing the inventory in superclass
            inventory = null;

            //don't open the fallback GUI again afterwards
            fallback = null;

            forceClose(listener, callback);
            waiting = false;
        } else {
            closing = new Callback<Player>() {
                @Override
                public void accept(Player player) {
                    forceClose(listener, callback);
                }
            };

            player.closeInventory();
        }
    }

    void forceClose(GUIListener listener, Callback<Player> callback) {
        HandlerList.unregisterAll(listener);
        this.listener = null;

        API.removeRemovable(GUI.this);
        if (callback != null) callback.accept(player);
        closing = null;

        if (fallback != null) {
            Bukkit.getScheduler().runTaskLater(getPlugin(), () -> {
                try {
                    //safety check
                    if (fallback != null) fallback.continueGUI();
                } catch (IsNotWaitingException e) {
                    e.printStackTrace();
                }
            }, 1);
        }
    }

    public void registerPage(Page page) {
        registerPage(page, false);
    }

    public void registerPage(Page page, boolean active) {
        pageLink.putIfAbsent(page.getClass(), page);
        pages.add(page);
        if (active) {
            try {
                switchTo(page);
            } catch (PageAlreadyOpenedException e) {
                e.printStackTrace();
            }
        }
    }

    public void switchTo(Class<? extends Page> pageClass) throws PageAlreadyOpenedException {
        switchTo(pageLink.get(pageClass));
    }

    public void switchTo(Page page) throws PageAlreadyOpenedException {
        Preconditions.checkNotNull(page);

        if (active == null) {
            active = page;
            page.apply(true);
            return;
        }

        if (active.equals(page)) throw new PageAlreadyOpenedException();

        boolean basic = !Objects.equals(active.getBasic(), page.getBasic());
        this.active.clear(basic);
        page.apply(basic);
        this.active = page;
    }

    @Override
    public void destroy() {
        super.destroy();

        this.pages.forEach(Page::destroy);
        this.pages.clear();
        this.pageLink.clear();
    }

    public void updateTitle(String title) {
        if (title == null) title = originalTitle;
        if (this.title.equals(title)) return;
        this.title = title;

        InventoryUtils.updateTitle(player, title, inventory);
    }

    public GUI getFallback() {
        return fallback;
    }

    public GUI setFallback(GUI fallback) {
        this.fallback = fallback;
        return this;
    }

    public boolean isOpen() {
        return this.listener != null;
    }

    public Page getActive() {
        return active;
    }

    public String getTitle() {
        return title;
    }

    public int getSize() {
        return size;
    }

    public String getOriginalTitle() {
        return originalTitle;
    }
}
