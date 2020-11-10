package de.codingair.codingapi.server.listeners;

import de.codingair.codingapi.player.data.PacketReader;
import de.codingair.codingapi.server.AsyncCatcher;
import de.codingair.codingapi.server.events.PlayerPickItemEvent;
import de.codingair.codingapi.server.reflections.IReflection;
import de.codingair.codingapi.server.reflections.PacketUtils;
import de.codingair.codingapi.tools.nbt.NBTTagCompound;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashSet;

public class PickItemListener implements Listener {
    private static final Class<?> PACKET_CLASS;
    private static final IReflection.FieldAccessor<Integer> slot;
    private static final IReflection.FieldAccessor<?> b;

    static {
        PACKET_CLASS = IReflection.getClass(IReflection.ServerPacket.MINECRAFT_PACKAGE, "PacketPlayInSetCreativeSlot");
        slot = IReflection.getField(PACKET_CLASS, "slot");
        b = IReflection.getField(PACKET_CLASS, "b");
    }

    private final JavaPlugin plugin;

    public PickItemListener(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    private void call(Player player, int slot, ItemStack item) {
        if(item.getType() == Material.AIR) return;

        Block b = player.getTargetBlock(new HashSet<Material>(){{add(Material.AIR);}}, 10);
        if(b.getType() != item.getType()) return;

        AsyncCatcher.runSync(plugin, () -> Bukkit.getPluginManager().callEvent(new PlayerPickItemEvent(player, slot, player.getInventory().getItemInHand(), b, !new NBTTagCompound(item).getMap().isEmpty())));
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        Player p = e.getPlayer();
        new PacketReader(p, "PickItemListener", plugin) {
            @Override
            public boolean readPacket(Object packet) {
                if(PACKET_CLASS == packet.getClass()) {
                    call(p, slot.get(packet), PacketUtils.getItemStack(b.get(packet)));
                }

                return false;
            }

            @Override
            public boolean writePacket(Object packet) {
                return false;
            }
        }.inject();
    }

}
