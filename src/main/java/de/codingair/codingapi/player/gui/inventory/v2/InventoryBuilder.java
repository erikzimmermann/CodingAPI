package de.codingair.codingapi.player.gui.inventory.v2;

import com.google.common.base.Preconditions;
import de.codingair.codingapi.server.reflections.IReflection;
import de.codingair.codingapi.server.reflections.PacketUtils;
import de.codingair.codingapi.server.specification.Version;
import de.codingair.codingapi.utils.Removable;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Collection;
import java.util.Objects;
import java.util.UUID;

public class InventoryBuilder implements Removable {
    protected final Player player;
    protected final UUID id = UUID.randomUUID();
    protected final JavaPlugin plugin;
    private String title;

    protected Inventory inventory;

    public InventoryBuilder(Player player, JavaPlugin plugin) {
        this.player = player;
        this.plugin = plugin;
    }

    public void buildInventory(int size, String title) {
        Preconditions.checkState(size % 9 == 0);

        int colors = count(title, '§') * 2;
        if(title.length() > 32 + colors) title = title.substring(0, 32 + colors);
        this.inventory = Bukkit.createInventory(null, size, this.title = title);
    }

    private int count(String s, char c) {
        int i = 0;
        for(char c1 : s.toCharArray()) {
            if(c1 == c) i++;
        }
        return i;
    }

    public void clear() {
        Preconditions.checkNotNull(inventory);
        inventory.clear();
    }

    public void clear(Collection<Integer> slots) {
        Preconditions.checkNotNull(inventory);
        for(Integer slot : slots) {
            inventory.clear(slot);
        }
    }

    public boolean setItem(int slot, ItemStack item) {
        Preconditions.checkNotNull(inventory);
        if(compare(getItem(slot), item)) return false;

        this.inventory.setItem(slot, item);
        return true;
    }

    public boolean setItem(int x, int y, ItemStack item) {
        return setItem(x + y * 9, item);
    }

    public ItemStack getItem(int slot) {
        Preconditions.checkNotNull(inventory);
        return this.inventory.getItem(slot);
    }

    public ItemStack getItem(int x, int y) {
        return getItem(x + y * 9);
    }

    private boolean compare(ItemStack item, ItemStack other) {
        return Objects.equals(item, other);
    }

    public void updateTitle(String invTitle) {
        if(invTitle.length() > 32) invTitle = invTitle.substring(0, 32);
        if(this.title.equals(invTitle)) return;

        Class<?> containerClass = IReflection.getClass(IReflection.ServerPacket.MINECRAFT_PACKAGE, "Container");
        Class<?> packetPlayOutOpenWindowClass = IReflection.getClass(IReflection.ServerPacket.MINECRAFT_PACKAGE, "PacketPlayOutOpenWindow");

        IReflection.MethodAccessor updateInventory = IReflection.getMethod(PacketUtils.EntityPlayerClass, "updateInventory", new Class[] {containerClass});

        IReflection.FieldAccessor<?> activeContainer = IReflection.getField(PacketUtils.EntityHumanClass, "activeContainer");
        IReflection.FieldAccessor<Integer> windowId = IReflection.getField(containerClass, "windowId");

        Object ep = PacketUtils.getEntityPlayer(player);
        Object packet;
        Object icbcTitle = PacketUtils.getChatMessage(invTitle);

        Object active = activeContainer.get(ep);
        int id = windowId.get(active);

        if(Version.get().isBiggerThan(Version.v1_13)) {
            Class<?> containersClass = IReflection.getClass(IReflection.ServerPacket.MINECRAFT_PACKAGE, "Containers");
            IReflection.FieldAccessor<?> title = IReflection.getField(containerClass, "title");

            title.set(active, icbcTitle);
            IReflection.ConstructorAccessor con = IReflection.getConstructor(packetPlayOutOpenWindowClass, int.class, containersClass, PacketUtils.IChatBaseComponentClass);
            packet = con.newInstance(id, getContainerType(inventory.getSize()), icbcTitle);
        } else {
            IReflection.ConstructorAccessor con = IReflection.getConstructor(packetPlayOutOpenWindowClass, int.class, String.class, PacketUtils.IChatBaseComponentClass, int.class);
            packet = con.newInstance(id, "minecraft:chest", icbcTitle, player.getOpenInventory().getTopInventory().getSize());
        }

        this.title = invTitle;
        PacketUtils.sendPacket(player, packet);
        updateInventory.invoke(ep, active);
    }

    private Object getContainerType(int size) {
        Class<?> containersClass = IReflection.getClass(IReflection.ServerPacket.MINECRAFT_PACKAGE, "Containers");
        IReflection.FieldAccessor<?> generic = IReflection.getField(containersClass, "GENERIC_9X" + (size / 9));
        return generic.get(null);
    }

    @Override
    public void destroy() {
        if(inventory != null) player.closeInventory();
    }

    @Override
    public Player getPlayer() {
        return player;
    }

    @Override
    public UUID getUniqueId() {
        return id;
    }

    @Override
    public JavaPlugin getPlugin() {
        return plugin;
    }

    public Inventory getInventory() {
        return inventory;
    }
}
