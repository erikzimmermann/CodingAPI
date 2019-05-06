package de.codingair.codingapi.player.layout.loadingbar;

import de.codingair.codingapi.API;
import de.codingair.codingapi.time.CountdownListener;
import de.codingair.codingapi.time.TimeFetcher;
import de.codingair.codingapi.tools.Converter;
import de.codingair.codingapi.player.gui.bossbar.BarColor;
import de.codingair.codingapi.player.gui.bossbar.BossBar;
import de.codingair.codingapi.player.gui.bossbar.BossBarAction;
import de.codingair.codingapi.time.Countdown;
import de.codingair.codingapi.utils.Removable;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.HashMap;
import java.util.UUID;

public class LoadingBar implements Removable {
	private UUID uniqueId = UUID.randomUUID();
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

		API.addRemovable(this);
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

		API.addRemovable(this);
	}

	@Override
	public void destroy() {
		this.cancel();
	}

	@Override
	public Class<? extends Removable> getAbstractClass() {
		return LoadingBar.class;
	}

	@Override
	public UUID getUniqueId() {
		return this.uniqueId;
	}

	public void play() {
		if(!API.isRegistered(this)) API.addRemovable(this);
		
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

		this.runBackup();

		this.countdown.cancel();
		this.countdown = null;
		API.removeRemovable(this);
	}

	public boolean isRunning() {
		return this.countdown != null;
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
	
	public Plugin getPlugin() {
		return plugin;
	}
	
	public static LoadingBar getLoadingBar(Player p) {
		return API.getRemovable(p, LoadingBar.class);
	}
}
