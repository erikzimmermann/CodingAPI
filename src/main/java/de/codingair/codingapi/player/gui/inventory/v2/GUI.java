package de.codingair.codingapi.player.gui.inventory.v2;

import com.google.common.base.Preconditions;
import de.codingair.codingapi.API;
import de.codingair.codingapi.player.gui.inventory.v2.exceptions.*;
import de.codingair.codingapi.server.reflections.IReflection;
import de.codingair.codingapi.server.reflections.PacketUtils;
import de.codingair.codingapi.server.specification.Version;
import de.codingair.codingapi.tools.Callback;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Objects;

/**
 * GUI only for static purpose. Buttons cannot be moved (except with different pages which is not recommended).
 */
public class GUI extends InventoryBuilder {
    protected GUIListener listener;
    protected GUI fallback;

    protected final HashMap<Class<? extends Page>, Page> pages = new HashMap<>();
    protected Page active;
    Callback<Player> closing;
    protected boolean waiting = false; //for AnvilGUI e.g. (when this GUI has to be closed for a certain amount of time)

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
        if(waiting) throw new IsWaitingException();
        if(isOpen()) throw new AlreadyOpenedException();
        if(active == null) throw new NoPageException();

        this.listener = new GUIListener(this);
        Bukkit.getPluginManager().registerEvents(this.listener, plugin);

        Callback<Player> callback = new Callback<Player>() {
            @Override
            public void accept(Player player) {
                API.addRemovable(GUI.this);
                player.openInventory(inventory);
            }
        };

        GUI gui = API.getRemovable(player, GUI.class);
        if(gui != null) {
            try {
                gui.close(callback);
            } catch(AlreadyClosedException ignored) {
            }
        } else callback.accept(player);
    }

    void continueGUI() throws IsNotWaitingException {
        if(!waiting) throw new IsNotWaitingException();

        waiting = false;
        player.openInventory(inventory);
    }

    public void close() throws AlreadyClosedException {
        close(null);
    }

    public void close(Callback<Player> callback) throws AlreadyClosedException {
        if(!isOpen()) throw new AlreadyClosedException();
        GUIListener listener = this.listener;
        this.listener = null;

        closing = new Callback<Player>() {
            @Override
            public void accept(Player player) {
                forceClose(listener, callback);

                if(fallback != null) {
                    try {
                        fallback.continueGUI();
                    } catch(IsNotWaitingException e) {
                        e.printStackTrace();
                    }
                }
            }
        };

        player.closeInventory();
    }

    void forceClose(GUIListener listener, Callback<Player> callback) {
        HandlerList.unregisterAll(listener);
        this.listener = null;

        API.removeRemovable(GUI.this);
        if(callback != null) callback.accept(player);
        closing = null;
    }

    public void registerPage(Page page) {
        registerPage(page, false);
    }

    public void registerPage(Page page, boolean active) {
        pages.put(page.getClass(), page);
        if(active) {
            try {
                switchTo(page);
            } catch(PageAlreadyOpenedException e) {
                e.printStackTrace();
            }
        }
    }

    public void switchTo(Class<? extends Page> pageClass) throws PageAlreadyOpenedException {
        switchTo(pages.get(pageClass));
    }

    public void switchTo(Page page) throws PageAlreadyOpenedException {
        Preconditions.checkNotNull(page);

        if(active == null) {
            active = page;
            page.apply(true);
            return;
        }

        if(active.equals(page)) throw new PageAlreadyOpenedException();

        boolean basic = !Objects.equals(active.getBasic(), page.getBasic());
        this.active.clear(basic);
        page.apply(basic);
        this.active = page;
    }

    @Override
    public void destroy() {
        super.destroy();

        this.pages.forEach((clazz, p) -> p.destroy());
        this.pages.clear();
    }

    public void updateTitle(String title) {
        if(title == null) title = originalTitle;
        if(this.title.equals(title)) return;
        this.title = title;

        Class<?> containerClass = IReflection.getClass(IReflection.ServerPacket.MINECRAFT_PACKAGE, "Container");
        Class<?> packetPlayOutOpenWindowClass = IReflection.getClass(IReflection.ServerPacket.MINECRAFT_PACKAGE, "PacketPlayOutOpenWindow");

        IReflection.MethodAccessor updateInventory = IReflection.getMethod(PacketUtils.EntityPlayerClass, "updateInventory", new Class[] {containerClass});

        IReflection.FieldAccessor<?> activeContainer = IReflection.getField(PacketUtils.EntityHumanClass, "activeContainer");
        IReflection.FieldAccessor<Integer> windowId = IReflection.getField(containerClass, "windowId");

        Object ep = PacketUtils.getEntityPlayer(player);
        Object packet;
        Object icbcTitle = PacketUtils.getChatMessage(this.title);

        Object active = activeContainer.get(ep);
        int id = windowId.get(active);

        if(Version.get().isBiggerThan(Version.v1_13)) {
            Class<?> containersClass = IReflection.getClass(IReflection.ServerPacket.MINECRAFT_PACKAGE, "Containers");
            IReflection.FieldAccessor<?> titleF = IReflection.getField(containerClass, "title");

            titleF.set(active, icbcTitle);
            IReflection.ConstructorAccessor con = IReflection.getConstructor(packetPlayOutOpenWindowClass, int.class, containersClass, PacketUtils.IChatBaseComponentClass);
            packet = con.newInstance(id, getContainerType(this.size), icbcTitle);
        } else {
            IReflection.ConstructorAccessor con = IReflection.getConstructor(packetPlayOutOpenWindowClass, int.class, String.class, PacketUtils.IChatBaseComponentClass, int.class);
            packet = con.newInstance(id, "minecraft:chest", icbcTitle, player.getOpenInventory().getTopInventory().getSize());
        }

        PacketUtils.sendPacket(player, packet);
        updateInventory.invoke(ep, active);
    }

    private Object getContainerType(int size) {
        Class<?> containersClass = IReflection.getClass(IReflection.ServerPacket.MINECRAFT_PACKAGE, "Containers");
        IReflection.FieldAccessor<?> generic = IReflection.getField(containersClass, "GENERIC_9X" + (size / 9));
        return generic.get(null);
    }

    public GUI getFallback() {
        return fallback;
    }

    public void setFallback(GUI fallback) {
        this.fallback = fallback;
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
