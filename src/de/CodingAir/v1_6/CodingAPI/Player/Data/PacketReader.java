package de.CodingAir.v1_6.CodingAPI.Player.Data;

import de.CodingAir.v1_6.CodingAPI.API;
import de.CodingAir.v1_6.CodingAPI.Server.Reflections.IReflection;
import de.CodingAir.v1_6.CodingAPI.Server.Reflections.PacketUtils;
import de.CodingAir.v1_6.CodingAPI.Utils.Removable;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.UUID;

public abstract class PacketReader implements Removable {
	private UUID uniqueId = UUID.randomUUID();
	private Player player;
	private Channel channel;
	private String name = "PacketListener";
	
	public PacketReader(Player player) {
		this.player = player;
	}
	
	public PacketReader(Player player, String name) {
		this.player = player;
		this.name = name;
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
		Object ep = PacketUtils.getEntityPlayer(player);
		
		IReflection.FieldAccessor getPlayerConnection = IReflection.getField(PacketUtils.EntityPlayerClass, "playerConnection");
		IReflection.FieldAccessor getNetworkManager = IReflection.getField(PacketUtils.PlayerConnectionClass, "networkManager");
		IReflection.FieldAccessor getChannel = IReflection.getField(PacketUtils.NetworkManagerClass, "channel");
		
		channel = (Channel) getChannel.get(getNetworkManager.get(getPlayerConnection.get(ep)));
		
		if(channel.pipeline().get(name) != null) channel.pipeline().remove(name);
		
		channel.pipeline().addAfter("decoder", name, new MessageToMessageDecoder<Object>() {
			
			@Override
			protected void decode(ChannelHandlerContext chc, Object packet, List<Object> out) throws Exception {
				if(!readPacket(packet)) out.add(packet);
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
}
