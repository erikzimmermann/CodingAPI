package de.CodingAir.v1_6.CodingAPI;

import de.CodingAir.v1_6.CodingAPI.CustomEntity.CustomEntityType;
import de.CodingAir.v1_6.CodingAPI.CustomEntity.FakePlayer.FakePlayer;
import de.CodingAir.v1_6.CodingAPI.CustomEntity.NetworkEntity.NetworkEntity;
import de.CodingAir.v1_6.CodingAPI.Files.TempFile;
import de.CodingAir.v1_6.CodingAPI.Particles.Animations.Animation;
import de.CodingAir.v1_6.CodingAPI.Player.Data.PacketReader;
import de.CodingAir.v1_6.CodingAPI.Player.GUI.BossBar.BossBar;
import de.CodingAir.v1_6.CodingAPI.Player.GUI.GUIListener;
import de.CodingAir.v1_6.CodingAPI.Server.PlayerData.PlayerData;
import de.CodingAir.v1_6.CodingAPI.Server.PlayerData.PlayerDataTypeAdapter;
import de.CodingAir.v1_6.CodingAPI.Server.Reflections.IReflection;
import de.CodingAir.v1_6.CodingAPI.Server.Reflections.PacketUtils;
import de.CodingAir.v1_6.CodingAPI.Server.Version;
import de.CodingAir.v1_6.CodingAPI.Utils.Removable;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;

public class API {
	private static final List<Removable> REMOVABLES = new ArrayList<>();
	private static boolean editing = false;
	
	private static API instance;
	public static final String VERSION = "v1.6";
	
	private Plugin plugin;
	private List<PlayerData> dataList = new ArrayList<>();
	
	public void onEnable(Plugin plugin) {
		this.plugin = plugin;
		GUIListener.register(plugin);
		
		this.dataList = TempFile.loadTempFiles(this.plugin, "/PlayerData/", PlayerData.class, new PlayerDataTypeAdapter(), false);
		
		Bukkit.getPluginManager().registerEvents(new Listener() {
			
			/** PlayerDataListener - Start */
			
			@EventHandler
			public void onJoin(PlayerJoinEvent e) {
				PlayerData data = getPlayerData(e.getPlayer());
				
				new PacketReader(e.getPlayer(), "PlayerDataListener-" + e.getPlayer().getName()) {
					@Override
					public boolean readPacket(Object packet) {
						if(packet.getClass().getSimpleName().equalsIgnoreCase("PacketPlayInSettings")) {
							IReflection.FieldAccessor b = IReflection.getField(PacketUtils.PacketPlayInSettingsClass, "b");
							data.setViewDistance((int) b.get(packet));
						}
						
						if(packet.getClass().getSimpleName().equalsIgnoreCase("PacketPlayInTeleportAccept")) {
							data.setLoadedSpawnChunk(true);
							this.unInject();
						}
						return false;
					}
				}.inject();
			}
			
			@EventHandler
			public void onQuit(PlayerQuitEvent e) {
				removePlayerData(e.getPlayer().getName());
			}
			
			/* PlayerDataListener - End */
			
			/** FakePlayerListener - Start */
			
			@EventHandler
			public void onMove(PlayerMoveEvent e) {
				Player p = e.getPlayer();
				Location from = e.getFrom();
				Location to = e.getTo();
				
				for(FakePlayer fakePlayer : API.getRemovables(FakePlayer.class)) {
					if(!fakePlayer.isInRange(from) && fakePlayer.isInRange(to)) {
						fakePlayer.updatePlayer(p);
					}
				}
			}
			
			/* FakePlayerListener - End */
			
		}, plugin);
		
		CustomEntityType.registerEntities();
		
		new BukkitRunnable() {
			int quarterSecond = 0;
			int second = 0;
			
			@Override
			public void run() {
				Animation.getAnimations().forEach(Animation::onTick);
				API.getRemovables(FakePlayer.class).forEach(FakePlayer::onTick);
				API.getRemovables(NetworkEntity.class).forEach(NetworkEntity::onTick);
				GUIListener.onTick();
				
				if(second >= 20) {
					second = 0;
				} else second++;
				
				if(quarterSecond >= 5) {
					quarterSecond = 0;
					
					if(!Version.getVersion().isBiggerThan(Version.v1_8)) {
						BossBar.onTick();
					}
					
				} else quarterSecond++;
			}
		}.runTaskTimer(plugin, 0, 1);
	}
	
	public synchronized void onDisable() {
		CustomEntityType.unregisterEntities();
		
		List<Removable> REMOVABLES = new ArrayList<>();
		REMOVABLES.addAll(API.REMOVABLES);
		
		API.REMOVABLES.clear();
		REMOVABLES.forEach(Removable::destroy);
		REMOVABLES.clear();
		
		TempFile.saveTempFiles(this.plugin, "/PlayerData/", PlayerData.class);
	}
	
	public static API getInstance() {
		if(instance == null) instance = new API();
		return instance;
	}
	
	public Plugin getPlugin() {
		return plugin;
	}
	
	public PlayerData getPlayerData(Player p) {
		for(PlayerData data : this.dataList) {
			if(data.getName().equalsIgnoreCase(p.getName())) return data;
		}
		
		PlayerData data = new PlayerData(p);
		this.dataList.add(data);
		
		return data;
	}
	
	public boolean removePlayerData(String name) {
		PlayerData playerData = null;
		
		for(PlayerData data : this.dataList) {
			if(data.getName().equalsIgnoreCase(name)) playerData = data;
		}
		
		if(playerData == null) return false;
		return this.dataList.remove(playerData);
	}
	
	public static synchronized void addRemovable(Removable removable) {
		if(isRegistered(removable)) removeRemovable(removable);
		REMOVABLES.add(removable);
	}
	
	public static synchronized <T extends Removable> T getRemovable(Player player, Class<? extends T> clazz) {
		for(Removable r : REMOVABLES) {
			if(clazz.isInstance(r)) {
				if(r.getPlayer().equals(player)) return clazz.cast(r);
			}
		}
		
		return null;
	}
	
	public static synchronized <T extends Removable> T getRemovable(int id, Class<? extends T> clazz) {
		for(Removable r : REMOVABLES) {
			if(clazz.isInstance(r)) {
				if(getID(r) == id) return clazz.cast(r);
			}
		}
		
		return null;
	}
	
	public static synchronized <T extends Removable> List<T> getRemovables(Player player, Class<? extends T> clazz) {
		List<T> l = new ArrayList<>();
		
		for(Removable r : REMOVABLES) {
			if(clazz.isInstance(r)) {
				if(r.getPlayer().equals(player)) l.add(clazz.cast(r));
			}
		}
		
		return l;
	}
	
	public static synchronized <T extends Removable> List<T> getRemovables(Class<? extends T> clazz) {
		List<T> l = new ArrayList<>();
		
		for(Removable r : REMOVABLES) {
			if(clazz.isInstance(r)) {
				l.add(clazz.cast(r));
			}
		}
		
		return l;
	}
	
	public static synchronized void removeRemovable(Removable removable) {
		removeRemovable(getID(removable));
	}
	
	public static synchronized void removeRemovable(int id) {
		if(id == -999) return;
		
		Removable removable = getRemovable(id, Removable.class);
		if(removable == null) return;
		
		REMOVABLES.remove(id);
		removable.destroy();
	}
	
	public static synchronized void removeRemovables(Player player) {
		List<Removable> removables = getRemovables(player, Removable.class);
		
		for(Removable r : removables) {
			REMOVABLES.remove(getID(r));
			r.destroy();
		}
		
		removables.clear();
	}
	
	public static synchronized <T extends Removable> void removeRemovables(Player player, Class<? extends T> clazz) {
		List<Removable> removables = getRemovables(player, clazz);
		
		for(Removable r : removables) {
			REMOVABLES.remove(getID(r));
			r.destroy();
		}
		
		removables.clear();
	}
	
	public static synchronized boolean isRegistered(Removable removable) {
		return getID(removable) != -999;
	}
	
	public static synchronized int getID(Removable removable) {
		int id = 0;
		
		List<Removable> REMOVABLES = new ArrayList<>();
		REMOVABLES.addAll(API.REMOVABLES);
		
		for(Removable r : REMOVABLES) {
			if(r.equals(removable)) {
				REMOVABLES.clear();
				return id;
			}
			id++;
		}
		
		REMOVABLES.clear();
		
		return -999;
	}
}
