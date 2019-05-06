package de.codingair.codingapi.customentity.fakeplayer.extras.modules;

import de.codingair.codingapi.customentity.fakeplayer.extras.FakePlayerListener;
import de.codingair.codingapi.customentity.fakeplayer.extras.modules.utils.Module;
import de.codingair.codingapi.customentity.fakeplayer.extras.modules.utils.Type;
import de.codingair.codingapi.player.data.PacketReader;
import de.codingair.codingapi.server.reflections.IReflection;
import de.codingair.codingapi.server.reflections.PacketUtils;
import de.codingair.codingapi.customentity.fakeplayer.FakePlayer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

/**
 * Removing of this disclaimer is forbidden.
 *
 * @author codingair
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
		
		PacketReader reader = new PacketReader(p, name, getPlayer().getPlugin()) {
			
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

			@Override
			public boolean writePacket(Object packet) {
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
