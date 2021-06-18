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
    private static final Class<?> CONTAINER_CLASS;
    private static final IReflection.FieldAccessor<?> TITLE;
    private static final IReflection.MethodAccessor UPDATE_INVENTORY;
    private static final IReflection.FieldAccessor<?> ACTIVE_CONTAINER;
    private static final Class<?> PACKET_PLAY_OUT_OPEN_WINDOW_CLASS;
    private static final IReflection.FieldAccessor<Integer> WINDOW_ID;
    private static final IReflection.ConstructorAccessor PACKET_CONSTRUCTOR;

    static {
        PACKET_PLAY_OUT_OPEN_WINDOW_CLASS = IReflection.getClass(IReflection.ServerPacket.PACKETS, "PacketPlayOutOpenWindow");

        if (PACKET_PLAY_OUT_OPEN_WINDOW_CLASS != null) {
            CONTAINER_CLASS = IReflection.getClass(IReflection.ServerPacket.INVENTORY, "Container");

            if (Version.atLeast(17)) {
                UPDATE_INVENTORY = IReflection.getMethod(CONTAINER_CLASS, "updateInventory");
            } else {
                UPDATE_INVENTORY = IReflection.getMethod(PacketUtils.EntityPlayerClass, "updateInventory", new Class[] {CONTAINER_CLASS});
            }

            ACTIVE_CONTAINER = IReflection.getField(PacketUtils.EntityHumanClass, Version.since(17, "activeContainer", "bV"));
            WINDOW_ID = IReflection.getField(CONTAINER_CLASS, Version.since(17, "windowId", "j"));

            if (Version.get().isBiggerThan(Version.v1_13)) {
                Class<?> containersClass = IReflection.getClass(IReflection.ServerPacket.INVENTORY, "Containers");
                TITLE = IReflection.getField(CONTAINER_CLASS, "title");

                PACKET_CONSTRUCTOR = IReflection.getConstructor(PACKET_PLAY_OUT_OPEN_WINDOW_CLASS, int.class, containersClass, PacketUtils.IChatBaseComponentClass);
            } else {
                TITLE = null;
                PACKET_CONSTRUCTOR = IReflection.getConstructor(PACKET_PLAY_OUT_OPEN_WINDOW_CLASS, int.class, String.class, PacketUtils.IChatBaseComponentClass, int.class);
            }
        } else {
            CONTAINER_CLASS = null;
            UPDATE_INVENTORY = null;
            ACTIVE_CONTAINER = null;
            WINDOW_ID = null;
            PACKET_CONSTRUCTOR = null;
            TITLE = null;
        }
    }

    private static boolean isReady() {
        return PACKET_PLAY_OUT_OPEN_WINDOW_CLASS != null;
    }

    public static void updateTitle(@NotNull Player player, @NotNull String title, @NotNull Inventory inventory) {
        if (!isReady()) return;

        Object entityPlayer = PacketUtils.getEntityPlayer(player);
        Object activeContainer = getActiveContainer(entityPlayer);

        Object openWindowPacket = preparePacket(activeContainer, title, inventory);
        PacketUtils.sendPacket(player, openWindowPacket);

        if (Version.atLeast(17)) {
            UPDATE_INVENTORY.invoke(activeContainer);
        } else {
            UPDATE_INVENTORY.invoke(entityPlayer, activeContainer);
        }
    }

    private static Object getActiveContainer(Object entityPlayer) {
        return ACTIVE_CONTAINER.get(entityPlayer);
    }

    private static Object preparePacket(@NotNull Object activeContainer, @NotNull String title, @NotNull Inventory inventory) {
        Object messageComponent = getTitleComponent(title);
        int size = inventory.getSize();
        int id = WINDOW_ID.get(activeContainer);

        Object packet;
        if (Version.get().isBiggerThan(Version.v1_13)) {
            InventoryUtils.TITLE.set(activeContainer, messageComponent);
            packet = PACKET_CONSTRUCTOR.newInstance(id, getContainerType(size), messageComponent);
        } else {
            packet = PACKET_CONSTRUCTOR.newInstance(id, "minecraft:chest", messageComponent, size);
        }

        return packet;
    }

    private static Object getTitleComponent(@NotNull String title) {
        String raw = ComponentSerializer.toString(TextComponent.fromLegacyText(title));
        return PacketUtils.getRawIChatBaseComponent(raw);
    }

    private static Object getContainerType(int size) {
        Class<?> containersClass = IReflection.getClass(IReflection.ServerPacket.INVENTORY, "Containers");
        IReflection.FieldAccessor<?> generic = IReflection.getField(containersClass, getContainerTypeName(size));
        return generic.get(null);
    }

    private static String getContainerTypeName(int size) {
        int id = size / 9;

        if (Version.atLeast(17)) {
            switch (id) {
                case 1:
                    return "a";
                case 2:
                    return "b";
                case 3:
                    return "c";
                case 4:
                    return "d";
                case 5:
                    return "e";
                case 6:
                    return "f";
                default: throw new IllegalArgumentException("Cannot find an inventory type for size: " + size);
            }
        } else return "GENERIC_9X" + id;
    }
}
