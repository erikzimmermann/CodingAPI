package de.codingair.codingapi.bungeecord;

import de.codingair.codingapi.bungeecord.listeners.ChatButtonListener;
import de.codingair.codingapi.server.reflections.IReflection;
import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.Plugin;

public class BungeeAPI {
    private static BungeeAPI instance;
    private Plugin plugin;

    public void onEnable(Plugin plugin) {
        if(this.plugin != null) return;
        this.plugin = plugin;

        BungeeCord.getInstance().getPluginManager().registerListener(plugin, (Listener) IReflection.getConstructor(ChatButtonListener.class).newInstance());
    }

    public void onDisable(Plugin plugin) {
        BungeeCord.getInstance().getPluginManager().unregisterListeners(plugin);

        if(this.plugin.equals(plugin)) this.plugin = null;
    }

    public static BungeeAPI getInstance() {
        if(instance == null) instance = new BungeeAPI();
        return instance;
    }

    public Plugin getPlugin() {
        return plugin;
    }
}
