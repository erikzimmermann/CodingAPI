package de.codingair.codingapi.player.data;

import de.codingair.codingapi.API;
import de.codingair.codingapi.server.reflections.IReflection;
import de.codingair.codingapi.server.reflections.PacketUtils;
import de.codingair.codingapi.server.specification.Version;
import de.codingair.codingapi.utils.Removable;
import io.netty.channel.Channel;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public abstract class PacketReader implements Removable {
    private static final IReflection.FieldAccessor<?> getPlayerConnection;
    private static final IReflection.FieldAccessor<?> getNetworkManager;
    private static final IReflection.FieldAccessor<?> getChannel;

    static {
        getPlayerConnection = PacketUtils.playerConnection;
        getNetworkManager = IReflection.getField(PacketUtils.PlayerConnectionClass, Version.since(17, "networkManager", "a"));
        getChannel = IReflection.getField(PacketUtils.NetworkManagerClass, Version.since(17, "channel", "k"));
    }

    private final UUID uniqueId = UUID.randomUUID();
    private final Player player;
    private final String name;
    private final UUID id = UUID.randomUUID();
    private final JavaPlugin plugin;
    private Channel channel;
    private ChannelDuplexHandler currentHandler = null;

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

    public void inject() {
        if (currentHandler != null) {
            unInject(this::inject);
            return;
        }

        Object ep = PacketUtils.getEntityPlayer(player);
        if (ep == null) return;
        Object playerCon = getPlayerConnection.get(ep);
        if (playerCon == null) return;
        Object networkMan = getNetworkManager.get(playerCon);
        if (networkMan == null) return;
        channel = (Channel) getChannel.get(networkMan);
        if (channel == null) return;

        currentHandler = new ChannelDuplexHandler() {
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

        if (channel.pipeline().get(getCombinedName()) != null) channel.pipeline().remove(getCombinedName());
        if (channel.pipeline().get("packet_handler") != null) channel.pipeline().addBefore("packet_handler", getCombinedName(), currentHandler);
        else channel.pipeline().addFirst(getCombinedName(), currentHandler);

        API.addRemovable(this);
    }

    public void unInject() {
        unInject(null);
    }

    public synchronized void unInject(@Nullable Runnable later) {
        JavaPlugin plugin = API.getInstance().getMainPlugin();

        Runnable runnable = () -> {
            if (currentHandler != null && channel != null && player.isOnline()) {
                try {
                    channel.pipeline().remove(this.currentHandler);
                } catch (Throwable ignored) {
                    //thrown when this handler is not registered anymore -> ignore
                }
            }

            this.currentHandler = null;
            if (later != null) Bukkit.getScheduler().runTask(plugin, later);
        };

        if (plugin.isEnabled()) {
            //uninject asynchronously.
            new Thread(runnable).start();
        } else runnable.run();

        API.removeRemovable(this);
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
