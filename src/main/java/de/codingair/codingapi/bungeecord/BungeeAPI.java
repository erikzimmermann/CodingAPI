package de.codingair.codingapi.bungeecord;

import de.codingair.codingapi.bungeecord.listeners.ChatButtonListener;
import de.codingair.codingapi.server.reflections.IReflection;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.Plugin;

public class BungeeAPI {
    private static BungeeAPI instance;
    private Plugin plugin;
    private ProxyServer proxy;

    public static BungeeAPI getInstance() {
        if (instance == null) instance = new BungeeAPI();
        return instance;
    }

    public static ProxyServer getProxy() {
        return getInstance().proxy;
    }

    public void onEnable(Plugin plugin) {
        if (this.plugin != null) return;
        this.plugin = plugin;
        this.proxy = plugin.getProxy();

        plugin.getProxy().getPluginManager().registerListener(plugin, (Listener) IReflection.getConstructor(ChatButtonListener.class).newInstance());
    }

    public void onDisable(Plugin plugin) {
        plugin.getProxy().getPluginManager().unregisterListeners(plugin);

        if (this.plugin.equals(plugin)) this.plugin = null;
    }

    public Plugin getPlugin() {
        return plugin;
    }
}
