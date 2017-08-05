package de.CodingAir.v1_6.CodingAPI.CustomEntity.FakePlayer.Extras.Modules;

import de.CodingAir.v1_6.CodingAPI.CustomEntity.FakePlayer.Extras.FakePlayerListener;
import de.CodingAir.v1_6.CodingAPI.CustomEntity.FakePlayer.Extras.Modules.Utils.Module;
import de.CodingAir.v1_6.CodingAPI.CustomEntity.FakePlayer.Extras.Modules.Utils.Type;
import de.CodingAir.v1_6.CodingAPI.CustomEntity.FakePlayer.FakePlayer;
import de.CodingAir.v1_6.CodingAPI.Player.Data.PacketReader;
import de.CodingAir.v1_6.CodingAPI.Server.Reflections.IReflection;
import de.CodingAir.v1_6.CodingAPI.Server.Reflections.PacketUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

/**
 * Removing of this disclaimer is forbidden.
 *
 * @author CodingAir
 * @verions: 1.0.0
 **/

public class InteractModule extends Module {
	private List<PacketReader> readers = new ArrayList<>();
	private boolean registered = false;
	
	public InteractModule(FakePlayer player) {
		super(player, Type.InteractModule);
	}
	
	public void register() {
		if(registered) return;
		
		Bukkit.getOnlinePlayers().forEach(p -> register(p));
		
		registered = true;
	}
	
	public void register(Player p) {
		String name = "InteractPacketReader_" + InteractModule.this.getPlayer().toString() + "_" + InteractModule.this.getPlayer().getGameProfile().getId().toString() + "_" + InteractModule.this.getPlayer().getGameProfile().getName();
		
		for(PacketReader reader : this.readers) {
			if(reader.getName().equals(name)) return;
		}
		
		PacketReader reader = new PacketReader(p, name) {
			
			@Override
			public boolean readPacket(Object packet) {
				if(packet.getClass().getSimpleName().equals("PacketPlayInUseEntity")) {
					if(InteractModule.this.getPlayer().getEntityId() == (int) IReflection.getField(packet.getClass(), "a").get(packet)) {
						FakePlayerListener.InteractAction action;
						
						IReflection.FieldAccessor playerAction = IReflection.getField(packet.getClass(), "action");
						
						if(playerAction.get(packet).equals(PacketUtils.EnumEntityUseActionClass.getEnumConstants()[0]))
							action = FakePlayerListener.InteractAction.INTERACT;
						else if(playerAction.get(packet).equals(PacketUtils.EnumEntityUseActionClass.getEnumConstants()[1]))
							action = FakePlayerListener.InteractAction.ATTACK;
						else if(playerAction.get(packet).equals(PacketUtils.EnumEntityUseActionClass.getEnumConstants()[2]))
							action = FakePlayerListener.InteractAction.INTERACT_AT;
						else return false;
						
						if(InteractModule.this.getPlayer().hasListener()) InteractModule.this.getPlayer().getListener().onInteract(p, action);
						return true;
					}
				}
				
				return false;
			}
		};
		
		readers.add(reader);
		reader.inject();
		
		if(!registered) registered = true;
	}
	
	public void unRegister() {
		if(!registered) return;
		
		readers.forEach(PacketReader::unInject);
		readers = new ArrayList<>();
		
		registered = false;
	}
	
	@Override
	public void onEvent() {
		
	}
}
