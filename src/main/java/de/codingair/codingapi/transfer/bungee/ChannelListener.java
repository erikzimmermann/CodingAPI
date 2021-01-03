package de.codingair.codingapi.transfer.bungee;

import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PluginMessageEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

public class ChannelListener implements Listener {
    protected BungeeDataHandler bungeeDataHandler;

    public ChannelListener(BungeeDataHandler bungeeDataHandler) {
        this.bungeeDataHandler = bungeeDataHandler;
    }

    @EventHandler
    public void onPluginMessage(PluginMessageEvent e) {
        if(e.getTag().equals(bungeeDataHandler.getChannelProxy())) {
            bungeeDataHandler.onReceive(e.getData(), ((ProxiedPlayer) e.getReceiver()).getServer().getInfo());
        }
    }

}
