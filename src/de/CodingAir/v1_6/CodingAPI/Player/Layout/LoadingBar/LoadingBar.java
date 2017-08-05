package de.CodingAir.v1_6.CodingAPI.Player.Layout.LoadingBar;

import de.CodingAir.v1_6.CodingAPI.Player.GUI.BossBar.BarColor;
import de.CodingAir.v1_6.CodingAPI.Player.GUI.BossBar.BossBar;
import de.CodingAir.v1_6.CodingAPI.Player.GUI.BossBar.BossBarAction;
import de.CodingAir.v1_6.CodingAPI.Time.Countdown;
import de.CodingAir.v1_6.CodingAPI.Time.CountdownListener;
import de.CodingAir.v1_6.CodingAPI.Time.TimeFetcher;
import de.CodingAir.v1_6.CodingAPI.Tools.Converter;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.HashMap;

public class LoadingBar {
	private static HashMap<Player, LoadingBar> loadingBars = new HashMap<>();
	
	private Plugin plugin;
	private Player player;
	private LoadingBarType type;
	private int seconds;
	private Countdown countdown;
	
	private BossBar bossBar;
	private BarColor color;
	
	private float exp;
	private int level;
	
	public LoadingBar(Plugin plugin, Player player, LoadingBarType type, int seconds) {
		this.plugin = plugin;
		this.player = player;
		this.type = type;
		this.seconds = seconds;
		
		this.exp = player.getExp();
		this.level = player.getLevel();
		
		loadingBars.remove(player);
		loadingBars.put(player, this);
	}
	
	public LoadingBar(Plugin plugin, Player player, LoadingBarType type, int seconds, BarColor color) {
		this.plugin = plugin;
		this.player = player;
		this.type = type;
		this.seconds = seconds;
		this.color = color;
		
		this.exp = player.getExp();
		this.level = player.getLevel();
		
		if(type.equals(LoadingBarType.EXPERIENCE_BAR)) {
			player.setLevel(0);
			player.setExp(0);
		}
		
		loadingBars.remove(player);
		loadingBars.put(player, this);
	}
	
	public void play() {
		if(!loadingBars.containsKey(this.player)) loadingBars.put(player, this);
		
		this.countdown = new Countdown(this.plugin, TimeFetcher.Time.SECONDS, this.seconds);
		
		this.countdown.addListener(new CountdownListener() {
			@Override
			protected void CountdownStartEvent() {
				if(type.equals(LoadingBarType.EXPERIENCE_BAR)) show(seconds);
			}
			
			@Override
			protected void CountdownEndEvent() {
				if(type.equals(LoadingBarType.EXPERIENCE_BAR)) show(0);
			}
			
			@Override
			protected void CountdownCancelEvent() {
				if(type.equals(LoadingBarType.EXPERIENCE_BAR)) show(0);
			}
			
			@Override
			protected void onTick(int timeLeft) {
				if(type.equals(LoadingBarType.EXPERIENCE_BAR)) show(timeLeft);
			}
		});
		
		this.countdown.start();
	}
	
	private void show(int timeLeft) {
		float loading = (float) (timeLeft == 0 ? 0 : ((double) timeLeft) / ((double) this.seconds));
		
		switch(this.type) {
			case EXPERIENCE_BAR: {
				this.player.setLevel(timeLeft);
				this.player.setExp(loading);
				break;
			}
			
			case BOSS_BAR: {
				if(this.bossBar == null) {
					this.bossBar = new BossBar(player, timeLeft + "", loading, this.color, this.plugin);
					this.bossBar.update(BossBarAction.ADD);
				}
				
				this.bossBar.setMessage(timeLeft + "");
				this.bossBar.setProgress(loading);
				this.bossBar.update(BossBarAction.UPDATE_NAME);
				this.bossBar.update(BossBarAction.UPDATE_PCT);
				break;
			}
		}
	}
	
	public void runBackup() {
		this.player.setExp(this.exp);
		this.player.setLevel(this.level);
	}
	
	public void cancel() {
		if(this.countdown == null) return;
		
		this.countdown.cancel();
		loadingBars = Converter.removeSafely(loadingBars, this.player);
	}
	
	public Player getPlayer() {
		return player;
	}
	
	public LoadingBarType getType() {
		return type;
	}
	
	public int getRemainingSeconds() {
		return seconds;
	}
	
	public static HashMap<Player, LoadingBar> getLoadingBars() {
		return loadingBars;
	}
	
	public Plugin getPlugin() {
		return plugin;
	}
	
	public static LoadingBar getLoadingBar(Player p) {
		for(Player player : loadingBars.keySet()) {
			if(player.getName().equals(p.getName())) return loadingBars.get(player);
		}
		
		return null;
	}
}
