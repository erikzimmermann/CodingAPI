package de.codingair.codingapi.player.gui.anvil;

import de.codingair.codingapi.API;
import de.codingair.codingapi.nms.NmsLoader;
import de.codingair.codingapi.player.gui.anvil.depended.PrepareAnvilEventHelp;
import de.codingair.codingapi.player.gui.inventory.InventoryUtils;
import de.codingair.codingapi.server.reflections.IReflection;
import de.codingair.codingapi.server.reflections.PacketUtils;
import de.codingair.codingapi.server.specification.Version;
import de.codingair.codingapi.utils.Removable;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.logging.Level;

public class AnvilGUI implements Removable {
    private static final Class<?> ENTITY_PLAYER_CLASS;
    private static final Class<?> PLAYER_INVENTORY_CLASS;
    private static final IReflection.MethodAccessor GET_WORLD;
    private static final IReflection.ConstructorAccessor BLOCK_POSITION_CON;
    private static final IReflection.MethodAccessor NEXT_CONTAINER_COUNTER;
    private static final Class<?> WORLD_CLASS;
    private static final Class<?> BLOCK_POSITION_CLASS;
    private static final IReflection.ConstructorAccessor ANVIL_CONTAINER_CON;
    private static final Class<?> CONTAINER_CLASS;
    private static final IReflection.FieldAccessor<Object> REACHABLE;
    private static final IReflection.MethodAccessor GET_TOP_INVENTORY;
    private static final IReflection.MethodAccessor GET_BUKKIT_VIEW;
    private static final Class<?> PACKET_PLAY_OUT_OPEN_WINDOW_CLASS;
    private static final Class<?> CHAT_MESSAGE_CLASS;
    private static final IReflection.ConstructorAccessor CHAT_MESSAGE_CON;
    private static final IReflection.FieldAccessor<InventoryType.SlotType> SLOT_TYPE_FIELD;
    private static final Function<Object, Object> GET_INVENTORY;

    static {
        Class<?> containerAnvilClass = IReflection.getClass(IReflection.ServerPacket.INVENTORY, "ContainerAnvil");
        PLAYER_INVENTORY_CLASS = IReflection.getClass(IReflection.ServerPacket.MINECRAFT_PACKAGE("net.minecraft.world.entity.player"), "PlayerInventory");
        WORLD_CLASS = PacketUtils.WorldClass;
        BLOCK_POSITION_CLASS = PacketUtils.BlockPositionClass;
        ENTITY_PLAYER_CLASS = PacketUtils.EntityPlayerClass;
        assert ENTITY_PLAYER_CLASS != null;

        // paper made the switch already in late 1.21 but spigot not before 1.21.1
        Class<?> craftInventoryViewClass = IReflection.getClass(IReflection.ServerPacket.CRAFTBUKKIT_PACKAGE, Version.choose("inventory.CraftInventoryView", Version.choose(21D, 21.1), "inventory.view.CraftAnvilView"));
        PACKET_PLAY_OUT_OPEN_WINDOW_CLASS = IReflection.getClass(IReflection.ServerPacket.PACKETS, "PacketPlayOutOpenWindow");
        CONTAINER_CLASS = IReflection.getClass(IReflection.ServerPacket.INVENTORY, "Container");

        if (Version.atLeast(14)) {
            Class<?> containerAccessClass = IReflection.getClass(IReflection.ServerPacket.INVENTORY, "ContainerAccess");
            ANVIL_CONTAINER_CON = IReflection.getConstructor(containerAnvilClass, int.class, PLAYER_INVENTORY_CLASS, containerAccessClass);
        } else {
            ANVIL_CONTAINER_CON = IReflection.getConstructor(containerAnvilClass, PLAYER_INVENTORY_CLASS, WORLD_CLASS, BLOCK_POSITION_CLASS, ENTITY_PLAYER_CLASS);
        }
        BLOCK_POSITION_CON = IReflection.getConstructor(BLOCK_POSITION_CLASS, Integer.class, Integer.class, Integer.class);

        if (Version.before(14)) {
            CHAT_MESSAGE_CLASS = IReflection.getClass(IReflection.ServerPacket.CHAT, "ChatMessage");
            CHAT_MESSAGE_CON = IReflection.getConstructor(CHAT_MESSAGE_CLASS, String.class, Object[].class);
        } else {
            CHAT_MESSAGE_CLASS = null;
            CHAT_MESSAGE_CON = null;
        }

        GET_BUKKIT_VIEW = IReflection.getMethod(containerAnvilClass, "getBukkitView", craftInventoryViewClass, (Class<?>[]) null);
        GET_TOP_INVENTORY = IReflection.getMethod(craftInventoryViewClass, "getTopInventory", Inventory.class, (Class<?>[]) null);
        NEXT_CONTAINER_COUNTER = IReflection.getMethod(ENTITY_PLAYER_CLASS, "nextContainerCounter", int.class, (Class<?>[]) null);
        GET_WORLD = IReflection.getMethod(PacketUtils.EntityClass, PacketUtils.WorldClass, new Class[0]);
        REACHABLE = IReflection.getField(containerAnvilClass, "checkReachable");
        SLOT_TYPE_FIELD = IReflection.getField(InventoryClickEvent.class, InventoryType.SlotType.class, 0);

        if (Version.atLeast(17)) {
            IReflection.MethodAccessor getInventory = IReflection.getMethod(PacketUtils.EntityHumanClass, PLAYER_INVENTORY_CLASS, new Class[0]);
            GET_INVENTORY = getInventory::invoke;
        } else {
            IReflection.FieldAccessor<?> getInventory = IReflection.getField(ENTITY_PLAYER_CLASS, "inventory");
            GET_INVENTORY = getInventory::get;
        }
    }

    private final UUID uniqueId = UUID.randomUUID();
    private final JavaPlugin plugin;
    private final Player player;
    private AnvilListener listener;
    private HashMap<AnvilSlot, ItemStack> items = new HashMap<>();

    private String submittedText = null;
    private boolean submitted = false;
    private boolean onlyWithChanges = true; //Triggers the AnvilClickEvent only if the output is filled
    private boolean keepSubmittedText = true;

    private AnvilCloseEvent closeEvent = null;
    private Listener bukkitListener;
    private PrepareAnvilEventHelp prepareListener;
    private Inventory inv;

    //Only for 1.14+
    private String title;

    @NmsLoader
    private AnvilGUI() {
        this.plugin = null;
        this.player = null;
    }

    public AnvilGUI(JavaPlugin plugin, Player player, AnvilListener listener, String title) {
        this.plugin = plugin;
        this.player = player;
        this.listener = listener;
        this.title = title == null ? "Repair & Name" : title;

        registerBukkitListener();
        if (Version.atLeast(9))
            Bukkit.getPluginManager().registerEvents(prepareListener = new PrepareAnvilEventHelp(), this.plugin);
    }

    public AnvilGUI(JavaPlugin plugin, Player player, AnvilListener listener) {
        this(plugin, player, listener, "Repair & Name");
    }

    public static AnvilGUI openAnvil(JavaPlugin plugin, Player p, AnvilListener listener, ItemStack item) {
        return new AnvilGUI(plugin, p, listener).setSlot(AnvilSlot.INPUT_LEFT, item).open();
    }

    public static AnvilGUI openAnvil(JavaPlugin plugin, Player p, AnvilListener listener, ItemStack item, String title) {
        return new AnvilGUI(plugin, p, listener, title).setSlot(AnvilSlot.INPUT_LEFT, item).open();
    }

    @Override
    public Player getPlayer() {
        return this.player;
    }

    @Override
    public UUID getUniqueId() {
        return uniqueId;
    }

    @Override
    public JavaPlugin getPlugin() {
        return plugin;
    }

    @Override
    public void destroy() {
        if (remove()) {
            try {
                this.player.closeInventory();
            } catch (Throwable ex) {
                Bukkit.getScheduler().runTask(plugin, this.player::closeInventory);
            }
        }
    }

    private void registerBukkitListener() {
        this.bukkitListener = new Listener() {
            private final Map<String, InventoryType.SlotType> slotType = new HashMap<>();

            @EventHandler(priority = EventPriority.LOWEST)
            public void onInventoryClickBefore(InventoryClickEvent e) {
                if (e.getWhoClicked() instanceof Player) {
                    Player p = (Player) e.getWhoClicked();

                    if (e.getInventory().equals(inv)) {
                        //modify event to prevent external plugins from working here
                        slotType.put(p.getName(), e.getSlotType());
                        SLOT_TYPE_FIELD.set(e, InventoryType.SlotType.OUTSIDE);
                    }
                }
            }

            @EventHandler(priority = EventPriority.HIGHEST)
            public void onInventoryClick(InventoryClickEvent e) {
                if (e.getWhoClicked() instanceof Player) {
                    Player p = (Player) e.getWhoClicked();

                    if (e.getInventory().equals(inv)) {
                        //restore event data
                        SLOT_TYPE_FIELD.set(e, slotType.remove(p.getName()));

                        e.setCancelled(true);

                        ItemStack item = inv.getItem(AnvilSlot.OUTPUT.getSlot());
                        int slot = e.getRawSlot();

                        if (AnvilSlot.bySlot(slot) == AnvilSlot.OUTPUT && (item == null || item.getType() == Material.AIR) && onlyWithChanges)
                            return;

                        AnvilClickEvent clickEvent = new AnvilClickEvent(p, e.getClick(), AnvilSlot.bySlot(slot), item, AnvilGUI.this);
                        if (listener != null) listener.onClick(clickEvent);

                        if (clickEvent.getSlot().equals(AnvilSlot.OUTPUT)) {
                            submitted = true;
                            submittedText = clickEvent.getSubmitted() == null ? clickEvent.getInput() : clickEvent.getSubmitted();
                        }

                        e.setCancelled(clickEvent.isCancelled());

                        if (keepSubmittedText && submitted && item != null && item.hasItemMeta()) {
                            ItemMeta meta = item.getItemMeta();
                            assert meta != null;
                            meta.setDisplayName(submittedText);
                            item.setItemMeta(meta);
                            inv.setItem(AnvilSlot.INPUT_LEFT.getSlot(), item);
                            p.updateInventory();
                        }

                        if (clickEvent.getWillClose()) {
                            close(clickEvent.isKeepInventory());
                            if (clickEvent.isKeepInventory()) onInventoryClose(new InventoryCloseEvent(e.getView()));
                        }

                        if (clickEvent.getSlot() == AnvilSlot.OUTPUT && !clickEvent.isPayExp())
                            p.setLevel(player.getLevel());
                    }
                }
            }

            @EventHandler
            public void onInventoryClose(InventoryCloseEvent e) {
                if (e.getPlayer() instanceof Player) {
                    if (e.getInventory().equals(inv)) {
                        if (closeEvent == null) {
                            closeEvent = new AnvilCloseEvent(player, AnvilGUI.this);
                            if (listener != null) listener.onClose(closeEvent);
                        }

                        inv.clear();
                        remove();

                        if (closeEvent.getPost() != null) {
                            Bukkit.getScheduler().runTask(plugin, closeEvent.getPost());
                        }
                    }
                }
            }
        };

        Bukkit.getPluginManager().registerEvents(this.bukkitListener, this.plugin);
    }

    public AnvilGUI open() {
        API.addRemovable(this);
        this.player.closeInventory();
        Object entityPlayer = PacketUtils.getEntityPlayer(this.player);

        Object inventory = GET_INVENTORY.apply(entityPlayer);
        Object world = GET_WORLD.invoke(entityPlayer);
        Object blockPosition = BLOCK_POSITION_CON.newInstance(0, 0, 0);

        int c = (int) NEXT_CONTAINER_COUNTER.invoke(entityPlayer);

        Object container;
        if (Version.atLeast(14)) {
            Class<?> containerAccessClass = IReflection.getClass(IReflection.ServerPacket.INVENTORY, "ContainerAccess");
            IReflection.MethodAccessor at = IReflection.getMethod(containerAccessClass, containerAccessClass, new Class[]{WORLD_CLASS, BLOCK_POSITION_CLASS});

            Object containerAccess = at.invoke(null, world, blockPosition);
            container = ANVIL_CONTAINER_CON.newInstance(c, inventory, containerAccess);
            IReflection.FieldAccessor<?> title = IReflection.getField(CONTAINER_CLASS, PacketUtils.IChatBaseComponentClass, 0);
            title.set(container, PacketUtils.getIChatBaseComponent(this.title));
        } else {
            container = ANVIL_CONTAINER_CON.newInstance(inventory, world, blockPosition, entityPlayer);
        }
        REACHABLE.set(container, false);

        inv = (Inventory) GET_TOP_INVENTORY.invoke(GET_BUKKIT_VIEW.invoke(container));
        if (prepareListener != null) prepareListener.setInv(inv);

        for (AnvilSlot slot : items.keySet()) {
            inv.setItem(slot.getSlot(), items.get(slot));
        }

        try {
            if (Version.atLeast(14)) {
                String genericField = Version.choose("ANVIL", "ANVIL", 17, "h", 20.4, "i");

                IReflection.FieldAccessor<?> generic = IReflection.getField(InventoryUtils.CONTAINERS_CLASS, genericField);
                IReflection.ConstructorAccessor packetPlayOutOpenWindowCon = IReflection.getConstructor(PACKET_PLAY_OUT_OPEN_WINDOW_CLASS, int.class, InventoryUtils.CONTAINERS_CLASS, PacketUtils.IChatBaseComponentClass);

                assert packetPlayOutOpenWindowCon != null;
                Object packet = packetPlayOutOpenWindowCon.newInstance(c, generic.get(null), PacketUtils.getChatMessage(title));
                PacketUtils.sendPacket(this.player, packet);
            } else {
                IReflection.ConstructorAccessor packetPlayOutOpenWindowCon = IReflection.getConstructor(PACKET_PLAY_OUT_OPEN_WINDOW_CLASS, Integer.class, String.class, CHAT_MESSAGE_CLASS, int.class);
                assert packetPlayOutOpenWindowCon != null;
                PacketUtils.sendPacket(this.player, packetPlayOutOpenWindowCon.newInstance(c, "minecraft:anvil", CHAT_MESSAGE_CON.newInstance("AnvilGUI", new Object[]{}), 0));
            }
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Error: Cannot open the AnvilGUI in " + Version.versionTag() + "!", e);
        }

        InventoryUtils.setActiveContainer(entityPlayer, container);
        InventoryUtils.setWindowId(container, c);

        if (Version.atLeast(17)) {
            IReflection.MethodAccessor initMenu = IReflection.getMethod(ENTITY_PLAYER_CLASS, (Class<?>) null, new Class[]{CONTAINER_CLASS});
            initMenu.invoke(entityPlayer, container);
        } else {
            IReflection.MethodAccessor addSlotListener = IReflection.getMethod(CONTAINER_CLASS, "addSlotListener", new Class[]{ENTITY_PLAYER_CLASS});
            addSlotListener.invoke(InventoryUtils.getActiveContainer(entityPlayer), entityPlayer);
        }

        updateInventory();
        return this;
    }

    public void close() {
        close(false);
    }

    public void close(boolean keep) {
        closeEvent = new AnvilCloseEvent(player, AnvilGUI.this, submitted, submittedText);

        Bukkit.getPluginManager().callEvent(closeEvent);
        if (listener != null) listener.onClose(closeEvent);

        if (!closeEvent.isCancelled()) {
            inv.clear();
            if (!keep) getPlayer().closeInventory();
        }
    }

    public void clearInventory() {
        items = new HashMap<>();
        this.updateInventory();
    }

    public void updateInventory() {
        Object entityPlayer = PacketUtils.getEntityPlayer(this.player);
        Object container = InventoryUtils.getActiveContainer(entityPlayer);

        if (!container.toString().toLowerCase().contains("anvil")) return;

        inv = (Inventory) GET_TOP_INVENTORY.invoke(GET_BUKKIT_VIEW.invoke(container));
        if (prepareListener != null) prepareListener.setInv(inv);
        inv.clear();

        for (AnvilSlot slot : items.keySet()) {
            inv.setItem(slot.getSlot(), items.get(slot));
        }

        this.player.updateInventory();
    }

    public AnvilGUI setSlot(AnvilSlot slot, ItemStack item) {
        items.remove(slot);
        items.put(slot, item);
        return this;
    }

    public boolean remove() {
        if (listener == null) return false;
        this.clearInventory();
        this.listener = null;
        this.items = null;

        HandlerList.unregisterAll(this.bukkitListener);
        if (this.prepareListener != null) this.prepareListener.unregister();
        listener = null;

        API.removeRemovable(this);
        return true;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title == null ? "Repair & Name" : title;
    }

    public boolean isKeepSubmittedText() {
        return keepSubmittedText;
    }

    public void setKeepSubmittedText(boolean keepSubmittedText) {
        this.keepSubmittedText = keepSubmittedText;
    }

    public boolean isOnlyWithChanges() {
        return onlyWithChanges;
    }

    public void setOnlyWithChanges(boolean onlyWithChanges) {
        this.onlyWithChanges = onlyWithChanges;
    }
}
