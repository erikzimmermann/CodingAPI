package de.codingair.codingapi.transfer.bungee;

import de.codingair.codingapi.bungeecord.BungeeAPI;
import de.codingair.codingapi.transfer.core.DataHandler;
import de.codingair.codingapi.transfer.packets.utils.Packet;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Plugin;

public abstract class BungeeDataHandler extends DataHandler<ServerInfo> {
    protected final Plugin plugin;
    protected final ChannelListener listener = new ChannelListener(this);

    public BungeeDataHandler(Plugin plugin) {
        super(plugin.getDescription().getName().toLowerCase().trim().replace(" ", "_"));
        this.plugin = plugin;
    }

    @Override
    public void onEnable() {
        BungeeAPI.getProxy().getPluginManager().registerListener(this.plugin, this.listener);
        BungeeAPI.getProxy().registerChannel(channelProxy);
        BungeeAPI.getProxy().registerChannel(channelBackend);
    }

    @Override
    public void onDisable() {
        BungeeAPI.getProxy().getPluginManager().unregisterListener(this.listener);
        BungeeAPI.getProxy().unregisterChannel(channelProxy);
        BungeeAPI.getProxy().unregisterChannel(channelBackend);
        this.listeners.clear();
    }

    @Override
    public void send(byte[] data, ServerInfo sender) {
        sender.sendData(channelBackend, data);
    }
}
