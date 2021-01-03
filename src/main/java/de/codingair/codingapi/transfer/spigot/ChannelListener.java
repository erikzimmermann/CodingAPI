package de.codingair.codingapi.transfer.spigot;

import org.bukkit.entity.Player;
import org.bukkit.plugin.messaging.PluginMessageListener;

public class ChannelListener implements PluginMessageListener {
    protected SpigotDataHandler spigotDataHandler;

    public ChannelListener(SpigotDataHandler spigotDataHandler) {
        this.spigotDataHandler = spigotDataHandler;
    }

    @Override
    public void onPluginMessageReceived(String tag, Player player, byte[] bytes) {
        if(tag.equals(spigotDataHandler.getChannelBackend())) {
            spigotDataHandler.onReceive(bytes, player);
        }
    }
}
