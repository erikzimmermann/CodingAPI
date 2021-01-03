package de.codingair.codingapi.transfer.spigot;

import de.codingair.codingapi.transfer.core.BackendDataHandler;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Optional;

public abstract class SpigotDataHandler extends BackendDataHandler<Player> {
    protected final JavaPlugin plugin;
    protected final ChannelListener listener = new ChannelListener(this);

    public SpigotDataHandler(JavaPlugin plugin) {
        super(plugin.getDescription().getName().toLowerCase().trim().replace(" ", "_"));
        this.plugin = plugin;
    }

    @Override
    public void onEnable() {
        Bukkit.getMessenger().registerOutgoingPluginChannel(this.plugin, channelProxy);
        Bukkit.getMessenger().registerIncomingPluginChannel(this.plugin, channelBackend, this.listener);
    }

    @Override
    public void onDisable() {
        Bukkit.getMessenger().unregisterOutgoingPluginChannel(this.plugin, channelProxy);
        Bukkit.getMessenger().unregisterIncomingPluginChannel(this.plugin, channelBackend, this.listener);

        this.listeners.clear();
    }

    @Override
    public Optional<Player> getRandomPlayer() {
        return (Optional<Player>) Bukkit.getOnlinePlayers().stream().findAny();
    }

    @Override
    public void send(byte[] data, Player sender) {
        sender.sendPluginMessage(plugin, channelProxy, data);
    }
}
