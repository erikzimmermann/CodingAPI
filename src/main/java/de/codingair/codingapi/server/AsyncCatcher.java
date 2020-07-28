package de.codingair.codingapi.server;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class AsyncCatcher {
    public static boolean isOnMainThread() {
        return Bukkit.isPrimaryThread();
    }

    public static boolean mightAsync() {
        return !isOnMainThread();
    }

    public static void runSync(JavaPlugin plugin, Runnable runnable) {
        if(isOnMainThread()) runnable.run();
        else Bukkit.getScheduler().runTask(plugin, runnable);
    }
}
