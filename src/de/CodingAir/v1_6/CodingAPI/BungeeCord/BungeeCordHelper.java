package de.CodingAir.v1_6.CodingAPI.BungeeCord;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import de.CodingAir.v1_6.CodingAPI.Tools.Callback;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.messaging.PluginMessageListener;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;

/**
 * Removing of this disclaimer is forbidden.
 *
 * @author CodingAir
 * @verions: 1.0.0
 **/

public class BungeeCordHelper {
    private static BungeeMessenger bungeeMessenger;

    public static void connect(Player player, String server, Plugin plugin) {
        send(player, "BungeeCordHelper", "Connect", server, plugin);
    }

    public static void send(Player player, String channel, String subChannel, String argument, Plugin plugin) {
        if (bungeeMessenger == null) bungeeMessenger = new BungeeMessenger(plugin);

        bungeeMessenger.sendMessage(channel, subChannel, argument, player);
    }

    public static void runningOnBungeeCord(Plugin plugin, double timeOutTicks, Callback<Boolean> callback) {
        if (bungeeMessenger == null) bungeeMessenger = new BungeeMessenger(plugin);
        if(Bukkit.getOnlinePlayers().isEmpty()) throw new IllegalStateException("There is no player to transfer the BungeeCordHelper-Message!");

        Player p = Bukkit.getOnlinePlayers().iterator().next();

        BungeeMessengerListener listener;
        bungeeMessenger.addListener(listener = new BungeeMessengerListener() {
            @Override
            void onReceive(String subChannel, String message) {
                bungeeMessenger.removeListener(this);
                callback.accept(true);
            }
        });

        bungeeMessenger.sendMessage("BungeeCordHelper", "GetServers", null, p);
        new BukkitRunnable() {
            long currentTick = 0;

            @Override
            public void run() {
                currentTick++;

                if(currentTick >= timeOutTicks) {
                    callback.accept(false);
                    bungeeMessenger.removeListener(listener);
                    this.cancel();
                }
            }
        }.runTaskTimer(plugin, 1, 1);
    }

    private static class BungeeMessenger implements PluginMessageListener {
        private Plugin plugin;
        private List<BungeeMessengerListener> listeners = new ArrayList<>();

        public BungeeMessenger(Plugin plugin) {
            this.plugin = plugin;
            plugin.getServer().getMessenger().registerOutgoingPluginChannel(plugin, "BungeeCordHelper");
            plugin.getServer().getMessenger().registerIncomingPluginChannel(plugin, "BungeeCordHelper", this);
        }

        @Override
        public void onPluginMessageReceived(String channel, Player player, byte[] bytes) {
            if (!channel.equals("BungeeCordHelper")) return;

            ByteArrayDataInput in = ByteStreams.newDataInput(bytes);
            String subChannel = in.readUTF();
            String message = in.readUTF();

            this.listeners.forEach(l -> l.onReceive(subChannel, message));
        }

        public void sendMessage(String channel, String subChannel, String message, Player player) {
            ByteArrayDataOutput out = ByteStreams.newDataOutput();
            out.writeUTF(subChannel);
            if(message != null) out.writeUTF(message);

            if (Bukkit.getPlayer(player.getUniqueId()) == null) throw new IllegalArgumentException("The player '" + player.getName() + "' is not online!");

            player.sendPluginMessage(this.plugin, channel, out.toByteArray());
        }

        public void addListener(BungeeMessengerListener listener) {
            this.listeners.add(listener);
        }

        public void removeListener(BungeeMessengerListener listener) {
            this.listeners.remove(listener);
        }
    }

    private static abstract class BungeeMessengerListener {
        abstract void onReceive(String subChannel, String message);
    }
}
