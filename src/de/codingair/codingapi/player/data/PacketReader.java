package de.codingair.codingapi.player.data;

import de.codingair.codingapi.API;
import de.codingair.codingapi.server.reflections.IReflection;
import de.codingair.codingapi.server.reflections.PacketUtils;
import de.codingair.codingapi.utils.Removable;
import io.netty.channel.Channel;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.UUID;

public abstract class PacketReader implements Removable {
	private UUID uniqueId = UUID.randomUUID();
	private Player player;
	private Channel channel;
	private String name;
	private JavaPlugin plugin;
	
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
	public Class<? extends Removable> getAbstractClass() {
		return PacketReader.class;
	}
	
	@Override
	public void destroy() {
		unInject();
	}
	
	public void inject() {
		IReflection.FieldAccessor getPlayerConnection = IReflection.getField(PacketUtils.EntityPlayerClass, "playerConnection");
		IReflection.FieldAccessor getNetworkManager = IReflection.getField(PacketUtils.PlayerConnectionClass, "networkManager");
		IReflection.FieldAccessor getChannel = IReflection.getField(PacketUtils.NetworkManagerClass, "channel");
		
		channel = (Channel) getChannel.get(getNetworkManager.get(getPlayerConnection.get(PacketUtils.getEntityPlayer(player))));
		
		if(channel.pipeline().get(name) != null) channel.pipeline().remove(name);

		channel.pipeline().addBefore("packet_handler", name, new ChannelDuplexHandler() {
			@Override
			public void channelRead(ChannelHandlerContext ctx, Object o) throws Exception {
				try {
					if(!readPacket(o)) super.channelRead(ctx, o);
				} catch(Exception ex) {
					ex.printStackTrace();
					super.channelRead(ctx, o);
				}
			}

			@Override
			public void write(ChannelHandlerContext ctx, Object o, ChannelPromise promise) throws Exception {
				try {
					if(!writePacket(o)) super.write(ctx, o, promise);
				} catch(Exception ex) {
					ex.printStackTrace();
					super.write(ctx, o, promise);
				}
			}
		});
		
		API.addRemovable(this);
	}
	
	public void unInject() {
		if(channel.pipeline().get(name) != null) {
			channel.pipeline().remove(name);
			API.removeRemovable(this);
		}
	}
	
	public void refresh() {
		unInject();
		inject();
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
