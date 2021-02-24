package de.codingair.codingapi.player.gui.inventory;

import de.codingair.codingapi.server.reflections.IReflection;
import de.codingair.codingapi.server.reflections.PacketUtils;
import de.codingair.codingapi.server.specification.Version;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.chat.ComponentSerializer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;

public class InventoryUtils {
    private static final Class<?> containerClass;
    private static IReflection.FieldAccessor<?> title;
    private static final IReflection.MethodAccessor updateInventory;
    private static final IReflection.FieldAccessor<?> activeContainer;
    private static final Class<?> packetPlayOutOpenWindowClass;
    private static final IReflection.FieldAccessor<Integer> windowId;
    private static final IReflection.ConstructorAccessor packetConstructor;

    static {
        packetPlayOutOpenWindowClass = IReflection.getClass(IReflection.ServerPacket.MINECRAFT_PACKAGE, "PacketPlayOutOpenWindow");

        if (packetPlayOutOpenWindowClass != null) {
            //initialize classes
            containerClass = IReflection.getClass(IReflection.ServerPacket.MINECRAFT_PACKAGE, "Container");
            updateInventory = IReflection.getMethod(PacketUtils.EntityPlayerClass, "updateInventory", new Class[] {containerClass});
            activeContainer = IReflection.getField(PacketUtils.EntityHumanClass, "activeContainer");
            windowId = IReflection.getField(containerClass, "windowId");

            if (Version.get().isBiggerThan(Version.v1_13)) {
                Class<?> containersClass = IReflection.getClass(IReflection.ServerPacket.MINECRAFT_PACKAGE, "Containers");
                title = IReflection.getField(containerClass, "title");

                packetConstructor = IReflection.getConstructor(packetPlayOutOpenWindowClass, int.class, containersClass, PacketUtils.IChatBaseComponentClass);
            } else {
                packetConstructor = IReflection.getConstructor(packetPlayOutOpenWindowClass, int.class, String.class, PacketUtils.IChatBaseComponentClass, int.class);
            }
        } else {
            containerClass = null;
            updateInventory = null;
            activeContainer = null;
            windowId = null;
            packetConstructor = null;
        }
    }

    private static boolean isReady() {
        return packetPlayOutOpenWindowClass != null;
    }

    public static void updateTitle(@NotNull Player player, @NotNull String title, @NotNull Inventory inventory) {
        if (!isReady()) return;

        Object entityPlayer = PacketUtils.getEntityPlayer(player);
        Object activeContainer = getActiveContainer(entityPlayer);

        Object openWindowPacket = preparePacket(activeContainer, title, inventory);
        PacketUtils.sendPacket(player, openWindowPacket);

        updateInventory.invoke(entityPlayer, activeContainer);
    }

    private static Object getActiveContainer(Object entityPlayer) {
        return activeContainer.get(entityPlayer);
    }

    private static Object preparePacket(@NotNull Object activeContainer, @NotNull String title, @NotNull Inventory inventory) {
        Object messageComponent = getTitleComponent(title);
        int size = inventory.getSize();
        int id = windowId.get(activeContainer);

        Object packet;
        if (Version.get().isBiggerThan(Version.v1_13)) {
            InventoryUtils.title.set(activeContainer, messageComponent);
            packet = packetConstructor.newInstance(id, getContainerType(size), messageComponent);
        } else {
            packet = packetConstructor.newInstance(id, "minecraft:chest", messageComponent, size);
        }

        return packet;
    }

    private static Object getTitleComponent(@NotNull String title) {
        String raw = ComponentSerializer.toString(TextComponent.fromLegacyText(title));
        return PacketUtils.getRawIChatBaseComponent(raw);
    }

    private static Object getContainerType(int size) {
        Class<?> containersClass = IReflection.getClass(IReflection.ServerPacket.MINECRAFT_PACKAGE, "Containers");
        IReflection.FieldAccessor<?> generic = IReflection.getField(containersClass, "GENERIC_9X" + (size / 9));
        return generic.get(null);
    }
}
