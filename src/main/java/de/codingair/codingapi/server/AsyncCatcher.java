package de.codingair.codingapi.server;

import com.github.Anon8281.universalScheduler.UniversalScheduler;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

public class AsyncCatcher {
    public static boolean isOnMainThread() {
        return Bukkit.isPrimaryThread();
    }

    public static boolean mightAsync() {
        return !isOnMainThread();
    }

    public static void runSync(@NotNull JavaPlugin plugin, @NotNull Runnable runnable, @NotNull Location loc) {
        if (isOnMainThread()) runnable.run();
        else UniversalScheduler.getScheduler(plugin).runTask(loc, runnable);
    }
}
