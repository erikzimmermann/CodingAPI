package de.codingair.codingapi.player.gui.inventory;

import de.codingair.codingapi.nms.NmsLoader;
import de.codingair.codingapi.server.reflections.IReflection;
import de.codingair.codingapi.server.reflections.PacketUtils;
import de.codingair.codingapi.server.specification.Version;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.chat.ComponentSerializer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Modifier;

public class InventoryUtils {
    public static final Class<?> CONTAINERS_CLASS;
    private static final Class<?> CONTAINER_CLASS;
    private static final IReflection.FieldAccessor<?> TITLE;
    private static final IReflection.MethodAccessor UPDATE_INVENTORY;
    private static final IReflection.FieldAccessor<?> ACTIVE_CONTAINER;
    private static final Class<?> PACKET_PLAY_OUT_OPEN_WINDOW_CLASS;
    private static final IReflection.FieldAccessor<Integer> WINDOW_ID;
    private static final IReflection.ConstructorAccessor PACKET_CONSTRUCTOR;

    static {
        PACKET_PLAY_OUT_OPEN_WINDOW_CLASS = IReflection.getClass(IReflection.ServerPacket.PACKETS, "PacketPlayOutOpenWindow");

        CONTAINER_CLASS = IReflection.getClass(IReflection.ServerPacket.INVENTORY, Version.choose("AbstractContainerMenu", "Container"));

        if (Version.atLeast(20.5)) UPDATE_INVENTORY = null;
        else if (Version.atLeast(17)) {
            UPDATE_INVENTORY = IReflection.getMethod(CONTAINER_CLASS, Version.choose("updateInventory", 18, "b"));
        } else {
            UPDATE_INVENTORY = IReflection.getMethod(PacketUtils.EntityPlayerClass, "updateInventory", new Class[]{CONTAINER_CLASS});
        }

        WINDOW_ID = IReflection.getField(CONTAINER_CLASS, int.class, 0, i -> Modifier.isPublic(i) && !Modifier.isStatic(i));
        ACTIVE_CONTAINER = IReflection.getField(PacketUtils.EntityHumanClass, CONTAINER_CLASS, 1);

        if (Version.atLeast(14)) {
            TITLE = IReflection.getField(CONTAINER_CLASS, "title");

            CONTAINERS_CLASS = IReflection.getClass(IReflection.ServerPacket.INVENTORY, Version.choose("MenuType", "Containers"));
            PACKET_CONSTRUCTOR = IReflection.getConstructor(PACKET_PLAY_OUT_OPEN_WINDOW_CLASS, int.class, CONTAINERS_CLASS, PacketUtils.IChatBaseComponentClass);
        } else {
            TITLE = null;
            CONTAINERS_CLASS = null;
            PACKET_CONSTRUCTOR = IReflection.getConstructor(PACKET_PLAY_OUT_OPEN_WINDOW_CLASS, int.class, String.class, PacketUtils.IChatBaseComponentClass, int.class);
        }

        // testing NMS
        if (Version.atLeast(14)) {
            for (int i = 0; i < 6; i++) {
                getContainerType(i * 9 + 9);
            }
        }
    }

    @NmsLoader
    private InventoryUtils() {
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

        // Update inventory must be used. Otherwise, the inventory might appear empty.
        if (UPDATE_INVENTORY == null) {
            // use deprecated method instead of NMS
            // noinspection UnstableApiUsage
            player.updateInventory();
        } else if (Version.atLeast(17)) UPDATE_INVENTORY.invoke(activeContainer);
        else UPDATE_INVENTORY.invoke(entityPlayer, activeContainer);
    }

    public static Object getActiveContainer(Object entityPlayer) {
        return ACTIVE_CONTAINER.get(entityPlayer);
    }

    public static void setActiveContainer(Object entityPlayer, Object value) {
        ACTIVE_CONTAINER.set(entityPlayer, value);
    }

    private static Object preparePacket(@NotNull Object activeContainer, @NotNull String title, @NotNull Inventory inventory) {
        Object messageComponent = getTitleComponent(title);
        int size = inventory.getSize();
        int id = getWindowId(activeContainer);

        Object packet;
        if (Version.atLeast(14)) {
            InventoryUtils.TITLE.set(activeContainer, messageComponent);
            packet = PACKET_CONSTRUCTOR.newInstance(id, getContainerType(size), messageComponent);
        } else {
            packet = PACKET_CONSTRUCTOR.newInstance(id, "minecraft:chest", messageComponent, size);
        }

        return packet;
    }

    public static int getWindowId(Object activeContainer) {
        return WINDOW_ID.get(activeContainer);
    }

    public static void setWindowId(Object activeContainer, int value) {
        WINDOW_ID.set(activeContainer, value);
    }

    private static Object getTitleComponent(@NotNull String title) {
        String raw = ComponentSerializer.toString(TextComponent.fromLegacyText(title));
        return PacketUtils.getRawIChatBaseComponent(raw);
    }

    private static Object getContainerType(int size) {
        if (Version.before(14)) throw new IllegalStateException("Not required.");
        IReflection.FieldAccessor<?> generic = IReflection.getField(CONTAINERS_CLASS, getContainerTypeName(size));
        return generic.get(null);
    }

    private static String getContainerTypeName(int size) {
        int id = size / 9;

        if (Version.mojangMapped()) {
            return "GENERIC_9x" + id;
        } else if (Version.atLeast(17)) {
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
                default:
                    throw new IllegalArgumentException("Cannot find an inventory type for size: " + size);
            }
        } else return "GENERIC_9X" + id;
    }
}
