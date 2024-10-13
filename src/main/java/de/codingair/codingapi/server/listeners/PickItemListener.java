package de.codingair.codingapi.server.listeners;

import de.codingair.codingapi.nms.NmsLoader;
import de.codingair.codingapi.player.data.PacketReader;
import de.codingair.codingapi.server.AsyncCatcher;
import de.codingair.codingapi.server.events.PlayerPickItemEvent;
import de.codingair.codingapi.server.reflections.IReflection;
import de.codingair.codingapi.server.reflections.PacketUtils;
import de.codingair.codingapi.server.specification.Version;
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
    private static final IReflection.FieldAccessor<?> slot;
    private static final IReflection.FieldAccessor<?> item;

    static {
        PACKET_CLASS = IReflection.getClass(IReflection.ServerPacket.PACKETS, "PacketPlayInSetCreativeSlot");

        if (Version.atLeast(20.5)) {
            slot = IReflection.getField(PACKET_CLASS, short.class, 0);
            item = IReflection.getField(PACKET_CLASS, PacketUtils.ItemStackClass, 0);
        } else {
            if (Version.atLeast(17)) {
                slot = IReflection.getField(PACKET_CLASS, "a");
            } else {
                slot = IReflection.getField(PACKET_CLASS, "slot");
            }

            item = IReflection.getField(PACKET_CLASS, "b");
        }
    }

    private final JavaPlugin plugin;

    @NmsLoader
    private PickItemListener() {
        this(null);
    }

    public PickItemListener(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    private void call(Player player, int slot, ItemStack item) {
        if (item.getType() == Material.AIR) return;

        Block b = player.getTargetBlock(new HashSet<Material>() {{
            add(Material.AIR);
        }}, 10);

        boolean correct = b.getType() == item.getType();

        if (!correct) {
            for (ItemStack drop : b.getDrops()) {
                if (drop.getType() == item.getType()) {
                    correct = true;
                    break;
                }
            }
        }

        if (correct)
            Bukkit.getPluginManager().callEvent(new PlayerPickItemEvent(player, slot, player.getItemInHand(), b));
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        Player p = e.getPlayer();
        new PacketReader(p, "PickItemListener", plugin) {
            @Override
            public boolean readPacket(Object packet) {
                if (PACKET_CLASS == packet.getClass()) {
                    Object slotId = slot.get(packet);
                    int slot;
                    if (slotId instanceof Integer) slot = (Integer) slotId;
                    else if (slotId instanceof Short) slot = (Short) slotId;
                    else throw new IllegalStateException("Cannot cast '" + slotId + "' (" + slotId.getClass() + ")");

                    AsyncCatcher.runSync(
                            plugin,
                            () -> call(p, slot, PacketUtils.getItemStack(item.get(packet))),
                            p.getLocation()
                    );
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
