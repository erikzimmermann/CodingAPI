package de.codingair.codingapi.transfer.spigot;

import de.codingair.codingapi.tools.Callback;
import de.codingair.codingapi.tools.time.TimeList;
import de.codingair.codingapi.transfer.DataHandler;
import de.codingair.codingapi.transfer.packets.utils.AnswerPacket;
import de.codingair.codingapi.transfer.packets.utils.AssignedPacket;
import de.codingair.codingapi.transfer.packets.utils.Packet;
import de.codingair.codingapi.transfer.packets.utils.RequestPacket;
import de.codingair.codingapi.transfer.utils.PacketListener;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public abstract class SpigotDataHandler extends DataHandler {
    protected final JavaPlugin plugin;
    protected final ChannelListener listener = new ChannelListener(this);
    protected final TimeList<UUID> timeOut = new TimeList<UUID>() {
        @Override
        public void timeout(UUID item) {
            Callback<?> callback = callbacks.remove(item);
            if(callback != null) callback.accept(null);
        }
    };

    public SpigotDataHandler(JavaPlugin plugin) {
        super(plugin.getDescription().getName().toLowerCase().trim().replace(" ", "_"));
        this.plugin = plugin;
    }

    @Override
    public void onEnable() {
        Bukkit.getMessenger().registerOutgoingPluginChannel(this.plugin, "BungeeCord");
        Bukkit.getMessenger().registerIncomingPluginChannel(this.plugin, getChannel, this.listener);
    }

    @Override
    public void onDisable() {
        Bukkit.getMessenger().unregisterOutgoingPluginChannel(this.plugin, "BungeeCord");
        Bukkit.getMessenger().unregisterIncomingPluginChannel(this.plugin, getChannel, this.listener);

        this.listeners.clear();
    }

    public void send(Packet packet) {
        send(packet, -1);
    }

    public void send(Packet packet, int timeOut) {
        if(!Bukkit.getOnlinePlayers().isEmpty()) {
            Player player = Bukkit.getOnlinePlayers().toArray(new Player[0])[0];

            ByteArrayOutputStream b = new ByteArrayOutputStream();
            DataOutputStream out = new DataOutputStream(b);

            int id = getId(packet.getClass());
            if(id == -1) throw new IllegalStateException(packet.getClass() + " is not registered!");

            if(packet instanceof RequestPacket && ((RequestPacket<?>) packet).getCallback() != null) {
                if(callbacks.get(((RequestPacket<?>) packet).getUniqueId()) != null) ((RequestPacket<?>) packet).checkUUID(this.callbacks.keySet());
                callbacks.put(((RequestPacket<?>) packet).getUniqueId(), ((RequestPacket<?>) packet).getCallback());

                if(timeOut > 0) this.timeOut.add(((RequestPacket<?>) packet).getUniqueId(), timeOut);
            }

            try {
                out.writeUTF(requestChannel);
                out.writeShort(id);
                packet.write(out);
            } catch(IOException e) {
                e.printStackTrace();
            }

            List<PacketListener> listeners = new ArrayList<>(this.listeners);
            for(PacketListener listener : listeners) {
                if(listener.onSend(packet)) return;
            }
            listeners.clear();

            player.sendPluginMessage(this.plugin, "BungeeCord", b.toByteArray());
        }
    }

    public void onReceive(Packet packet) {
        if(packet instanceof AnswerPacket) {
            UUID uniqueId = ((AssignedPacket) packet).getUniqueId();
            Callback callback;
            if((callback = this.callbacks.remove(uniqueId)) == null) return;
            callback.accept(((AnswerPacket) packet).getValue());

            this.timeOut.remove(uniqueId);
        }

        List<PacketListener> listeners = new ArrayList<>(this.listeners);
        for(PacketListener listener : listeners) {
            listener.onReceive(packet, null);
        }
        listeners.clear();
    }
}
