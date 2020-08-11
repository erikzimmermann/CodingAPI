package de.codingair.codingapi.transfer.spigot;

import de.codingair.codingapi.transfer.packets.utils.Packet;
import org.bukkit.entity.Player;
import org.bukkit.plugin.messaging.PluginMessageListener;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;

public class ChannelListener implements PluginMessageListener {
    protected SpigotDataHandler spigotDataHandler;

    public ChannelListener(SpigotDataHandler spigotDataHandler) {
        this.spigotDataHandler = spigotDataHandler;
    }

    @Override
    public void onPluginMessageReceived(String tag, Player player, byte[] bytes) {
        if(tag.equals(spigotDataHandler.getGetChannel())) {
            DataInputStream in = new DataInputStream(new ByteArrayInputStream(bytes));

            try {
                Packet packet = spigotDataHandler.produce(in.readUnsignedShort());
                if(packet == null) return;

                packet.read(in);
                this.spigotDataHandler.onReceive(packet);
            } catch(IOException e1) {
                e1.printStackTrace();
            }
        }
    }
}
