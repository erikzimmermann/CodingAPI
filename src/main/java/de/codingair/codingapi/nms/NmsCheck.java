package de.codingair.codingapi.nms;

import de.codingair.codingapi.player.Hologram;
import de.codingair.codingapi.player.data.PacketReader;
import de.codingair.codingapi.player.gui.anvil.AnvilGUI;
import de.codingair.codingapi.player.gui.inventory.InventoryUtils;
import de.codingair.codingapi.player.gui.sign.SignGUI;
import de.codingair.codingapi.server.listeners.PickItemListener;
import de.codingair.codingapi.server.reflections.PacketUtils;
import de.codingair.codingapi.server.specification.Version;
import de.codingair.codingapi.tools.nbt.NBTTagCompound;
import de.codingair.codingapi.tools.nbt.specific.BlockEntityNBTTagCompound;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

public class NmsCheck {
    private static final Class<?>[] C;
    private static boolean internalApiTested = false;

    static {
        // add all classes that should be initialized on startup to see version-dependant NMS issues
        C = new Class[]{
                PacketUtils.class,
                AnvilGUI.class, SignGUI.NmsWrapper.class,
                InventoryUtils.class,
                PickItemListener.class,
                NBTTagCompound.class, BlockEntityNBTTagCompound.class,
                Hologram.HologramPackets.class, Hologram.class,
                PacketReader.class
        };
    }

    private NmsCheck() {
    }

    public static void testInternalApi() {
        internalApiTested = true;
        test(C);
    }

    /**
     * Creates instances of the given classes to execute their static bodies.
     *
     * @param classes The classes that should be tested.
     */
    public static void test(@NotNull Class<?> @NotNull [] classes) {
        if (!internalApiTested) testInternalApi();
        for (Class<?> c : classes) {
            test(c);
        }
    }

    private static void test(Class<?> c) {
        try {
            runNmsLoader(c);
        } catch (Throwable t) {
            throw new NmsCheckError("Could not initialize class: " + c.getName() + ". Version=" + Version.get() + ", Type=" + Version.type() + ", Bukkit='" + Bukkit.getVersion() + "'", t);
        }
    }

    private static void runNmsLoader(Class<?> c) throws InvocationTargetException, InstantiationException, IllegalAccessException {
        Object obj = null;
        for (Constructor<?> con : c.getDeclaredConstructors()) {
            if (con.getParameterCount() != 0) continue;
            if (!con.isAnnotationPresent(NmsLoader.class)) continue;

            // init for static initializer
            con.setAccessible(true);
            obj = con.newInstance();
            break;
        }

        if (obj == null) {
            if (c.getSuperclass() != Object.class) {
                try {
                    runNmsLoader(c.getSuperclass());
                    return;
                } catch (IllegalStateException ignored) {
                }
            }


            throw new IllegalStateException(c.getSimpleName() + " is missing an empty constructor!");
        }
    }
}
