package de.codingair.codingapi.transfer.bungee;

import de.codingair.codingapi.bungeecord.BungeeAPI;
import de.codingair.codingapi.transfer.packets.utils.Packet;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.event.PluginMessageEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;

public class ChannelListener implements Listener {
    protected BungeeDataHandler bungeeDataHandler;

    public ChannelListener(BungeeDataHandler bungeeDataHandler) {
        this.bungeeDataHandler = bungeeDataHandler;
    }

    @EventHandler
    public void onPluginMessage(PluginMessageEvent e) {
        if(e.getTag().equals("BungeeCord")) {
            ServerInfo server = BungeeAPI.getProxy().getPlayer(e.getReceiver().toString()).getServer().getInfo();
            DataInputStream in = new DataInputStream(new ByteArrayInputStream(e.getData()));

            try {
                if(!in.readUTF().equals(bungeeDataHandler.getRequestChannel())) return;
                Packet packet = bungeeDataHandler.produce(in.readUnsignedShort());

                if(packet == null) return;

                packet.read(in);
                this.bungeeDataHandler.onReceive(packet, server);
            } catch(IOException e1) {
                e1.printStackTrace();
            }
        }
    }

}
