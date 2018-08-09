package de.codingair.codingapi.bungeecord;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import de.codingair.codingapi.player.data.UUIDFetcher;
import de.codingair.codingapi.tools.Callback;
import de.codingair.codingapi.utils.Value;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.messaging.PluginMessageListener;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.EOFException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Removing of this disclaimer is forbidden.
 *
 * @author codingair
 * @verions: 1.0.0
 **/

public class BungeeCordHelper {
    public static BungeeMessenger bungeeMessenger;

    public static void connect(Player player, String server, Plugin plugin) {
        send(player, "BungeeCord", "Connect", server, plugin);
    }

    public static void getUUID(Player player, Plugin plugin, Callback<UUID> callback, int timeOut) {
        if(bungeeMessenger == null) bungeeMessenger = new BungeeMessenger(plugin);

        Value<Boolean> timeOuted = new Value<>(false);

        BukkitRunnable runnable = new BukkitRunnable() {
            int ticks = 0;

            @Override
            public void run() {
                if(ticks >= timeOut) {
                    callback.accept(null);
                    this.cancel();
                    timeOuted.setValue(true);
                    return;
                }

                ticks++;
            }
        };

        runnable.runTaskTimer(plugin, 1, 1);

        bungeeMessenger.addListener(new BungeeMessengerListener() {
            @Override
            void onReceive(List<String> args) {
                if(args.size() == 3 && args.get(0).equals("UUIDOther") && args.get(1).equals(player.getName())) {
                    if(!timeOuted.getValue()) {
                        runnable.cancel();

                        callback.accept(UUIDFetcher.getUUIDFromId(args.get(2)));
                    }

                    bungeeMessenger.removeListener(this);
                }
            }
        });

        send(player, "BungeeCord", "UUIDOther", player.getName(), plugin);
    }

    public static void send(Player player, String channel, String subChannel, String argument, Plugin plugin) {
        if(bungeeMessenger == null) bungeeMessenger = new BungeeMessenger(plugin);

        bungeeMessenger.sendMessage(channel, subChannel, argument, player);
    }

    public static void getCurrentServer(Plugin plugin, int timeOutTicks, Callback<String> callback) {
        if(bungeeMessenger == null) bungeeMessenger = new BungeeMessenger(plugin);
        if(Bukkit.getOnlinePlayers().isEmpty())
            throw new IllegalStateException("There is no player to transfer the BungeeCordHelper-Message!");

        Player p = Bukkit.getOnlinePlayers().iterator().next();

        getCurrentServer(p, plugin, timeOutTicks, callback);
    }

    public static void getCurrentServer(Player player, Plugin plugin, int timeOut, Callback<String> callback) {
        if(bungeeMessenger == null) bungeeMessenger = new BungeeMessenger(plugin);

        Value<Boolean> timeOuted = new Value<>(false);

        BukkitRunnable runnable = new BukkitRunnable() {
            int ticks = 0;

            @Override
            public void run() {
                if(ticks >= timeOut) {
                    callback.accept(null);
                    this.cancel();
                    timeOuted.setValue(true);
                    return;
                }

                ticks++;
            }
        };

        runnable.runTaskTimer(plugin, 1, 1);

        bungeeMessenger.addListener(new BungeeMessengerListener() {
            @Override
            void onReceive(List<String> args) {
                if(args.size() == 2 && args.get(0).equals("GetServer")) {
                    if(!timeOuted.getValue()) {
                        runnable.cancel();

                        callback.accept(args.get(1));
                    }

                    bungeeMessenger.removeListener(this);
                }
            }
        });

        send(player, "BungeeCord", "GetServer", null, plugin);
    }

    public static void runningOnBungeeCord(Plugin plugin, int timeOutTicks, Callback<Boolean> callback) {
        if(bungeeMessenger == null) bungeeMessenger = new BungeeMessenger(plugin);
        if(Bukkit.getOnlinePlayers().isEmpty())
            throw new IllegalStateException("There is no player to transfer the BungeeCordHelper-Message!");

        Player p = Bukkit.getOnlinePlayers().iterator().next();

        getUUID(p, plugin, new Callback<UUID>() {
            @Override
            public void accept(UUID object) {
                callback.accept(object != null);
            }
        }, timeOutTicks);
    }

    private static class BungeeMessenger implements PluginMessageListener {
        private Plugin plugin;
        private List<BungeeMessengerListener> listeners = new ArrayList<>();

        public BungeeMessenger(Plugin plugin) {
            this.plugin = plugin;
            plugin.getServer().getMessenger().registerOutgoingPluginChannel(plugin, "BungeeCord");
            plugin.getServer().getMessenger().registerIncomingPluginChannel(plugin, "BungeeCord", this);
        }

        @Override
        public void onPluginMessageReceived(String channel, Player player, byte[] bytes) {
            if(!channel.equals("BungeeCord")) return;

            ByteArrayDataInput in = ByteStreams.newDataInput(bytes);
            List<String> args = new ArrayList<>();

            while(true) {
                try {
                    args.add(in.readUTF());
                } catch(Exception ex) {
                    break;
                }
            }

            List<BungeeMessengerListener> listeners = new ArrayList<>(this.listeners);
            listeners.forEach(l -> l.onReceive(args));
            listeners.clear();
        }

        public void sendMessage(String channel, String subChannel, String message, Player player) {
            ByteArrayDataOutput out = ByteStreams.newDataOutput();
            out.writeUTF(subChannel);
            if(message != null) out.writeUTF(message);

            if(Bukkit.getPlayer(player.getUniqueId()) == null)
                throw new IllegalArgumentException("The player '" + player.getName() + "' is not online!");

            Bukkit.getScheduler().runTask(plugin, () -> player.sendPluginMessage(plugin, channel, out.toByteArray()));
        }

        public void addListener(BungeeMessengerListener listener) {
            this.listeners.add(listener);
        }

        public void removeListener(BungeeMessengerListener listener) {
            this.listeners.remove(listener);
        }
    }

    private static abstract class BungeeMessengerListener {
        abstract void onReceive(List<String> args);
    }
}
