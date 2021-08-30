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
	private Channel channel;
	private final String name;
	private final JavaPlugin plugin;
	private ChannelDuplexHandler currentHandler = null;
	
	public PacketReader(Player player, String name, JavaPlugin plugin) {
		this.player = player;
		this.name = name + UUID.randomUUID();
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
		if (currentHandler != null){
			unInject(this::inject);
			return;
		}

		Object ep = PacketUtils.getEntityPlayer(player);
		if(ep == null) return;
		Object playerCon = getPlayerConnection.get(ep);
		if(playerCon == null) return;
		Object networkMan = getNetworkManager.get(playerCon);
		if(networkMan == null) return;
		channel = (Channel) getChannel.get(networkMan);
		if(channel == null) return;

		currentHandler = new ChannelDuplexHandler() {
			@Override
			public void channelRead(ChannelHandlerContext ctx, Object o) throws Exception {
				try {
					if(!readPacket(o)) super.channelRead(ctx, o);
				} catch(Exception ex) {
					super.channelRead(ctx, o);
				}
			}

			@Override
			public void write(ChannelHandlerContext ctx, Object o, ChannelPromise promise) throws Exception {
				try {
					if(!writePacket(o)) super.write(ctx, o, promise);
				} catch(Exception ex) {
					super.write(ctx, o, promise);
				}
			}
		};

		if(channel.pipeline().get(name) != null) channel.pipeline().remove(name);
		if(channel.pipeline().get("packet_handler") != null) channel.pipeline().addBefore("packet_handler", name, currentHandler);
		else channel.pipeline().addFirst(name, currentHandler);
		
		API.addRemovable(this);
	}

	public void unInject() {
		unInject(null);
	}

	public synchronized void unInject(@Nullable Runnable later) {
		Bukkit.getScheduler().runTaskAsynchronously(API.getInstance().getMainPlugin(), () -> {
			if (currentHandler != null && channel != null && player.isOnline()) {
				try {
					channel.pipeline().remove(this.currentHandler);
				} catch(Throwable ignored) {
				}
			}

			this.currentHandler = null;
			if (later != null) later.run();
		});

		API.removeRemovable(this);
	}
	
	@Override
	public Player getPlayer() {
		return player;
	}
	
	public String getName() {
		return name;
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
