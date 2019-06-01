package de.codingair.codingapi.player.gui.anvil;

import de.codingair.codingapi.API;
import de.codingair.codingapi.player.gui.anvil.depended.ContainerAccess;
import de.codingair.codingapi.server.Version;
import de.codingair.codingapi.server.reflections.IReflection;
import de.codingair.codingapi.server.reflections.PacketUtils;
import de.codingair.codingapi.utils.Removable;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.UUID;
import java.util.logging.Level;

/**
 * Removing of this disclaimer is forbidden.
 *
 * @author codingair
 * @verion: 1.0.0
 **/

public class AnvilGUI implements Removable {
    private UUID uniqueId = UUID.randomUUID();
    private JavaPlugin plugin;
    private Player player;
    private AnvilListener listener;
    private HashMap<AnvilSlot, ItemStack> items = new HashMap<>();

    private String submittedText = null;
    private boolean submitted = false;

    private AnvilCloseEvent closeEvent = null;
    private Listener bukkitListener;
    private Inventory inv;

    //Only for 1.14+
    private String title;

    public AnvilGUI(JavaPlugin plugin, Player player, AnvilListener listener, String title) {
        this.plugin = plugin;
        this.player = player;
        this.listener = listener;
        this.title = title;

        registerBukkitListener();
    }

    public AnvilGUI(JavaPlugin plugin, Player player, AnvilListener listener) {
        this(plugin, player, listener, "Repair & Name");
    }

    @Override
    public Class<? extends Removable> getAbstractClass() {
        return AnvilGUI.class;
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
        remove();
        Bukkit.getScheduler().runTask(plugin, () -> this.player.closeInventory());
    }

    private void registerBukkitListener() {
        this.bukkitListener = new Listener() {
            @EventHandler
            public void onInventoryClick(InventoryClickEvent e) {
                if(e.getWhoClicked() instanceof Player) {
                    Player p = (Player) e.getWhoClicked();

                    if(e.getInventory().equals(inv)) {
                        e.setCancelled(true);

                        ItemStack item = e.getCurrentItem();
                        int slot = e.getRawSlot();

                        AnvilClickEvent clickEvent = new AnvilClickEvent(p, AnvilSlot.bySlot(slot), item, AnvilGUI.this);

                        if(listener != null) listener.onClick(clickEvent);
                        Bukkit.getPluginManager().callEvent(clickEvent);

                        if(clickEvent.getSlot().equals(AnvilSlot.OUTPUT)) {
                            submitted = true;
                            submittedText = clickEvent.getInput();
                        }

                        e.setCancelled(clickEvent.isCancelled());
                        e.setCancelled(true);

                        if(clickEvent.getWillClose()) {
                            closeEvent = new AnvilCloseEvent(player, AnvilGUI.this, submitted, submittedText);

                            Bukkit.getPluginManager().callEvent(closeEvent);
                            if(listener != null) listener.onClose(closeEvent);

                            if(!closeEvent.isCancelled()) {
                                inv.clear();
                                p.closeInventory();
                            }
                        }

                        if(clickEvent.getSlot() == AnvilSlot.OUTPUT && !clickEvent.isPayExp())
                            p.setLevel(player.getLevel());
                    }
                }
            }

            @EventHandler
            public void onInventoryClose(InventoryCloseEvent e) {
                if(e.getPlayer() instanceof Player) {
                    if(e.getInventory().equals(inv)) {

                        if(closeEvent == null) {
                            closeEvent = new AnvilCloseEvent(player, AnvilGUI.this);
                            Bukkit.getPluginManager().callEvent(closeEvent);
                            if(listener != null) listener.onClose(closeEvent);
                        }

                        inv.clear();
                        remove();

                        if(closeEvent.getPost() != null) {
                            Bukkit.getScheduler().runTask(plugin, closeEvent.getPost());
                        }
                    }
                }
            }

            @EventHandler
            public void onPlayerQuit(PlayerQuitEvent e) {
                if(!player.getName().equals(e.getPlayer().getName())) return;
                remove();
            }
        };

        Bukkit.getPluginManager().registerEvents(this.bukkitListener, this.plugin);
    }

    public AnvilGUI open() {
        API.addRemovable(this);
        this.player.closeInventory();

        Class<?> containerAnvilClass = IReflection.getClass(IReflection.ServerPacket.MINECRAFT_PACKAGE, "ContainerAnvil");
        Class<?> playerInventoryClass = IReflection.getClass(IReflection.ServerPacket.MINECRAFT_PACKAGE, "PlayerInventory");
        Class<?> worldClass = IReflection.getClass(IReflection.ServerPacket.MINECRAFT_PACKAGE, "World");
        Class<?> blockPositionClass = IReflection.getClass(IReflection.ServerPacket.MINECRAFT_PACKAGE, "BlockPosition");
        Class<?> entityPlayerClass = IReflection.getClass(IReflection.ServerPacket.MINECRAFT_PACKAGE, "EntityPlayer");
        Class<?> craftInventoryViewClass = IReflection.getClass(IReflection.ServerPacket.CRAFTBUKKIT_PACKAGE, "inventory.CraftInventoryView");
        Class<?> packetPlayOutOpenWindowClass = IReflection.getClass(IReflection.ServerPacket.MINECRAFT_PACKAGE, "PacketPlayOutOpenWindow");
        Class<?> containerClass = IReflection.getClass(IReflection.ServerPacket.MINECRAFT_PACKAGE, "Container");
        Class<?> chatMessageClass = IReflection.getClass(IReflection.ServerPacket.MINECRAFT_PACKAGE, "ChatMessage");

        IReflection.ConstructorAccessor anvilContainerCon;
        if(Version.getVersion().isBiggerThan(Version.v1_13)) {
            Class<?> containerAccessClass = IReflection.getClass(IReflection.ServerPacket.MINECRAFT_PACKAGE, "ContainerAccess");
            anvilContainerCon = IReflection.getConstructor(containerAnvilClass, int.class, playerInventoryClass, containerAccessClass);
        } else {
            anvilContainerCon = IReflection.getConstructor(containerAnvilClass, playerInventoryClass, worldClass, blockPositionClass, entityPlayerClass);
        }
        IReflection.ConstructorAccessor blockPositionCon = IReflection.getConstructor(blockPositionClass, Integer.class, Integer.class, Integer.class);
        IReflection.ConstructorAccessor chatMessageCon = IReflection.getConstructor(chatMessageClass, String.class, Object[].class);

        IReflection.MethodAccessor getBukkitView = IReflection.getMethod(containerAnvilClass, "getBukkitView", craftInventoryViewClass, null);
        IReflection.MethodAccessor getTopInventory = IReflection.getMethod(craftInventoryViewClass, "getTopInventory", Inventory.class, null);
        IReflection.MethodAccessor nextContainerCounter = IReflection.getMethod(entityPlayerClass, "nextContainerCounter", int.class, null);
        IReflection.MethodAccessor addSlotListener = IReflection.getMethod(containerClass, "addSlotListener", new Class[] {entityPlayerClass});

        IReflection.FieldAccessor getInventory = IReflection.getField(entityPlayerClass, "inventory");
        IReflection.FieldAccessor getWorld = IReflection.getField(entityPlayerClass, "world");
        IReflection.FieldAccessor reachable = IReflection.getField(containerAnvilClass, "checkReachable");
        IReflection.FieldAccessor activeContainer = IReflection.getField(entityPlayerClass, "activeContainer");
        IReflection.FieldAccessor windowId = IReflection.getField(containerClass, "windowId");

        Object entityPlayer = PacketUtils.getEntityPlayer(this.player);
        Object inventory = getInventory.get(entityPlayer);
        Object world = getWorld.get(entityPlayer);
        Object blockPosition = blockPositionCon.newInstance(0, 0, 0);

        int c = (int) nextContainerCounter.invoke(entityPlayer);

        Object container;
        if(Version.getVersion().isBiggerThan(Version.v1_13)) {
            container = anvilContainerCon.newInstance(c, inventory, new ContainerAccess(player, world, blockPosition));
        } else {
            container = anvilContainerCon.newInstance(inventory, world, blockPosition, entityPlayer);
        }
        reachable.set(container, false);

        inv = (Inventory) getTopInventory.invoke(getBukkitView.invoke(container));

        for(AnvilSlot slot : items.keySet()) {
            inv.setItem(slot.getSlot(), items.get(slot));
        }

        try {
            if(Version.getVersion().isBiggerThan(Version.v1_13)) {
                Class<?> containersClass = IReflection.getClass(IReflection.ServerPacket.MINECRAFT_PACKAGE, "Containers");
                IReflection.FieldAccessor generic = IReflection.getField(containersClass, "ANVIL");
                IReflection.ConstructorAccessor packetPlayOutOpenWindowCon = IReflection.getConstructor(packetPlayOutOpenWindowClass, int.class, containersClass, PacketUtils.IChatBaseComponentClass);

                Object packet = packetPlayOutOpenWindowCon.newInstance(c, generic.get(null), PacketUtils.getChatMessage(title));
                PacketUtils.sendPacket(this.player, packet);
            } else {
                IReflection.ConstructorAccessor packetPlayOutOpenWindowCon = IReflection.getConstructor(packetPlayOutOpenWindowClass, Integer.class, String.class, chatMessageClass, int.class);
                PacketUtils.sendPacket(this.player, packetPlayOutOpenWindowCon.newInstance(c, "minecraft:anvil", chatMessageCon.newInstance("AnvilGUI", new Object[] {}), 0));
            }
        } catch(Exception e) {
            e.printStackTrace();
            plugin.getLogger().log(Level.SEVERE, "Error: Cannot open the AnvilGUI in " + Version.getVersion().name() + "!");
        }


        activeContainer.set(entityPlayer, container);
        windowId.set(activeContainer.get(entityPlayer), c);
        addSlotListener.invoke(activeContainer.get(entityPlayer), entityPlayer);

        updateInventory();
        return this;
    }

    public void clearInventory() {
        items = new HashMap<>();
        this.updateInventory();
    }

    public void updateInventory() {
        Class<?> entityPlayerClass = IReflection.getClass(IReflection.ServerPacket.MINECRAFT_PACKAGE, "EntityPlayer");
        Class<?> containerAnvilClass = IReflection.getClass(IReflection.ServerPacket.MINECRAFT_PACKAGE, "ContainerAnvil");
        Class<?> craftInventoryViewClass = IReflection.getClass(IReflection.ServerPacket.CRAFTBUKKIT_PACKAGE, "inventory.CraftInventoryView");

        IReflection.FieldAccessor activeContainer = IReflection.getField(entityPlayerClass, "activeContainer");

        IReflection.MethodAccessor getBukkitView = IReflection.getMethod(containerAnvilClass, "getBukkitView", craftInventoryViewClass, null);
        IReflection.MethodAccessor getTopInventory = IReflection.getMethod(craftInventoryViewClass, "getTopInventory", Inventory.class, null);

        Object entityPlayer = PacketUtils.getEntityPlayer(this.player);
        Object container = activeContainer.get(entityPlayer);

        if(!container.toString().toLowerCase().contains("anvil")) return;

        inv = (Inventory) getTopInventory.invoke(getBukkitView.invoke(container));

        inv.clear();

        for(AnvilSlot slot : items.keySet()) {
            inv.setItem(slot.getSlot(), items.get(slot));
        }

        this.player.updateInventory();
    }

    public AnvilGUI setSlot(AnvilSlot slot, ItemStack item) {
        items.remove(slot);
        items.put(slot, item);
        return this;
    }

    public void remove() {
        this.clearInventory();
        this.listener = null;
        this.items = null;

        HandlerList.unregisterAll(this.bukkitListener);
        listener = null;

        API.removeRemovable(this);
    }

    public static AnvilGUI openAnvil(JavaPlugin plugin, Player p, AnvilListener listener, ItemStack item) {
        return new AnvilGUI(plugin, p, listener).setSlot(AnvilSlot.INPUT_LEFT, item).open();
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
