package de.CodingAir.v1_6.CodingAPI.BungeeCord;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

/**
 * Removing of this disclaimer is forbidden.
 *
 * @author CodingAir
 * @verions: 1.0.0
 **/

public class BungeeCord {
	public static void send(Player player, String channel, String subChannel, String argument, Plugin plugin) {
		ByteArrayDataOutput out = ByteStreams.newDataOutput();
		out.writeUTF(subChannel);
		out.writeUTF(argument);
		player.sendPluginMessage(plugin, channel, out.toByteArray());
	}
	
	public static void connect(Player player, String server, Plugin plugin) {
		send(player, "BungeeCord", "Connect", server, plugin);
	}
}
