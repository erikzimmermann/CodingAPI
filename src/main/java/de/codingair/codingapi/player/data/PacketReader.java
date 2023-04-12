package de.codingair.codingapi.player.data;

import de.codingair.codingapi.API;
import de.codingair.codingapi.server.reflections.IReflection;
import de.codingair.codingapi.server.reflections.PacketUtils;
import de.codingair.codingapi.utils.Removable;
import io.netty.channel.*;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;
import java.util.function.Consumer;

public abstract class PacketReader implements Removable {
    private static final IReflection.FieldAccessor<?> getPlayerConnection;
    private static final IReflection.FieldAccessor<?> getNetworkManager;
    private static final IReflection.FieldAccessor<?> getChannel;

    static {
        getPlayerConnection = PacketUtils.playerConnection;
        getNetworkManager = IReflection.getField(PacketUtils.PlayerConnectionClass, PacketUtils.NetworkManagerClass, 0);
        getChannel = IReflection.getField(PacketUtils.NetworkManagerClass, Channel.class, 0);
    }

    private final UUID uniqueId = UUID.randomUUID();
    private final Player player;
    private final String name;
    private final UUID id = UUID.randomUUID();
    private final JavaPlugin plugin;
    private boolean injected = false;

    public PacketReader(Player player, String name, JavaPlugin plugin) {
        this.player = player;
        this.name = name;
        this.plugin = plugin;
    }

    @Override
    public JavaPlugin getPlugin() {
        return plugin;
    }

    @Override
    public UUID getUniqueId() {
        return uniqueId;
    }

    @Override
    public void destroy() {
        unInject();
    }

    public synchronized void inject() {
        if (injected) {
            unInject(this::inject);
            return;
        }

        injected = true;
        modify(pipe -> {
            ChannelDuplexHandler currentHandler = new ChannelDuplexHandler() {
                @Override
                public void channelRead(ChannelHandlerContext ctx, Object o) throws Exception {
                    try {
                        if (!readPacket(o)) super.channelRead(ctx, o);
                    } catch (Exception ex) {
                        super.channelRead(ctx, o);
                    }
                }

                @Override
                public void write(ChannelHandlerContext ctx, Object o, ChannelPromise promise) throws Exception {
                    try {
                        if (!writePacket(o)) super.write(ctx, o, promise);
                    } catch (Exception ex) {
                        super.write(ctx, o, promise);
                    }
                }
            };

            if (pipe.get(getCombinedName()) != null) pipe.remove(getCombinedName());
            if (pipe.get("packet_handler") != null) pipe.addBefore("packet_handler", getCombinedName(), currentHandler);
            else pipe.addFirst(getCombinedName(), currentHandler);
        });

        API.addRemovable(this);
    }

    public void unInject() {
        unInject(null);
    }

    public synchronized void unInject(@Nullable Runnable later) {
        modify(pipe -> {
            if (pipe.get(getCombinedName()) != null) pipe.remove(getCombinedName());
        });

        API.removeRemovable(this);
        injected = false;

        if (later != null) {
            if (plugin.isEnabled()) {
                //uninject asynchronously.
                Bukkit.getScheduler().runTask(plugin, later);
            } else later.run();
        }
    }

    private void modify(@NotNull Consumer<ChannelPipeline> modifier) {
        Object ep = PacketUtils.getEntityPlayer(player);
        if (ep == null) return;
        Object playerCon = getPlayerConnection.get(ep);
        if (playerCon == null) return;
        Object networkMan = getNetworkManager.get(playerCon);
        if (networkMan == null) return;
        Channel channel = (Channel) getChannel.get(networkMan);
        if (channel == null) return;

        channel.eventLoop().execute(() -> {
            if (!player.isOnline()) return;

            try {
                modifier.accept(channel.pipeline());
            } catch (Throwable t) {
                throw new RuntimeException("Cannot modify channel pipeline of player '" + player.getName() + "'", t);
            }
        });
    }

    @Override
    public Player getPlayer() {
        return player;
    }

    /**
     * @return The name that was provided in the constructor.
     */
    public String getName() {
        return name;
    }

    /**
     * @return The name and the linked id to that packet reader.
     */
    public String getCombinedName() {
        return name + "#" + this.id;
    }

    /**
     * @param packet Object
     * @return intercept
     */
    public abstract boolean readPacket(Object packet);

    /**
     * @param packet Object
     * @return intercept
     */
    public abstract boolean writePacket(Object packet);
}
