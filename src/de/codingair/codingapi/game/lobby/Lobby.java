package de.codingair.codingapi.game.lobby;

import de.codingair.codingapi.game.Game;
import de.codingair.codingapi.tools.Location;
import de.codingair.codingapi.game.map.Map;
import de.codingair.codingapi.game.map.MapVoting;
import de.codingair.codingapi.game.utils.GameState;
import de.codingair.codingapi.player.layout.loadingbar.LoadingBar;
import de.codingair.codingapi.player.layout.loadingbar.LoadingBarType;
import de.codingair.codingapi.time.Countdown;
import de.codingair.codingapi.time.CountdownListener;
import de.codingair.codingapi.time.TimeFetcher;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;

/**
 * Removing of this disclaimer is forbidden.
 *
 * @author CodingAir
 * @verions: 1.0.0
 **/

public class Lobby {
	private Game game;
	private LobbyListener listener = null;
	private Location spawn;
	private PlayerLayout playerLayout;
	private Countdown countdown;
	
	private List<LoadingBar> loadingBars = new ArrayList<>();
	private LoadingBarType loadingBarType;
	
	private int lobbyTime;
	private TimeFetcher.Time time;
	private int currentLobbyTime = 0;
	
	private boolean started = false;
	private boolean mapVoting = false;
	
	public Lobby(Game game, Location spawn, int lobbyTime, TimeFetcher.Time time) {
		this.game = game;
		this.spawn = spawn;
		this.lobbyTime = lobbyTime;
		this.time = time;
		
		setListener(new LobbyListener(this));
	}
	
	public Lobby(Game game, Location spawn, int lobbyTime, TimeFetcher.Time time, LoadingBarType loadingBarType) {
		this(game, spawn, lobbyTime, time);
		
		this.loadingBarType = loadingBarType;
	}
	
	public void startScheduler() {
		started = true;
		
		game.setMapVoting(new MapVoting(game.getMaps(), game.getMapVotingTime(), game.getPlugin()) {
			@Override
			public void onStart() {
				getListener().onStartMapVoting();
			}
			
			@Override
			public void onCancel() {
			}
			
			@Override
			public void onTick(int timeLeft) {
			}
			
			@Override
			public void onEvaluate(Map map) {
				game.setCurrentMap(map);
				getListener().onEndMapVoting(map);
			}
		});
		
		new BukkitRunnable() {
			int ticks = 0;
			
			@Override
			public void run() {
				if(getGame().getGameState().equals(GameState.STOPPED) ||getGame().getGameState().equals(GameState.STOPPING)) {
					this.cancel();
					return;
				}

				if(checkPlayers()) {
					startCountdown();
					this.cancel();
				} else if(ticks >= 20) {
					ticks = 0;
					listener.onNeededPlayers(game.getMinPlayers() - game.getPlayers().size());
				} else ticks++;
			}
		}.runTaskTimer(game.getPlugin(), 20L, 20L);
	}
	
	public void startCountdown() {
		this.countdown = new Countdown(this.game.getPlugin(), this.time, this.lobbyTime, false);
		this.countdown.addListener(new CountdownListener() {
			@Override
			protected void CountdownStartEvent() {
				getLoadingBars().forEach(LoadingBar::play);
			}
			
			@Override
			protected void CountdownEndEvent() {
				if(getGame().getCurrentMap() == null) {
					if(!getGame().getMapVoting().isRunning()) game.getMapVoting().start();
					getGame().getMapVoting().end();
				}

				getLoadingBars().forEach(LoadingBar::cancel);
				listener.onEndCountdown();
				if(game.getGameState().equals(GameState.WAITING)) game.start();
			}
			
			@Override
			protected void CountdownCancelEvent() {
				if(getGame().getGameState().equals(GameState.STOPPED) ||getGame().getGameState().equals(GameState.STOPPING)) return;

				startScheduler();
				getLoadingBars().forEach(LoadingBar::cancel);
				listener.onCancelStartCountdown(game.getMinPlayers() - game.getPlayers().size());
				getGame().setCurrentMap(null);
				
				for(Player player : getGame().getPlayers()) {
					initializePlayer(player, false);
				}
			}
			
			@Override
			protected void onTick(int timeLeft) {
				if(!checkPlayers()) {
					mapVoting = false;
					game.getMapVoting().cancel();
					countdown.cancel();
					return;
				}
				
				if(!mapVoting && game.getCurrentMap() == null) {
					if(timeLeft == game.getMapVoting().getTime() + game.getMapVotingTimeBeforeEnd()) {
						game.getMapVoting().start();
						mapVoting = true;
					} else if(timeLeft < game.getMapVoting().getTime() + game.getMapVotingTimeBeforeEnd()) {
						game.getMapVoting().setTime(timeLeft - game.getMapVotingTimeBeforeEnd() < 0 ? 0 : timeLeft - game.getMapVotingTimeBeforeEnd());
						game.getMapVoting().start();
						mapVoting = true;
					}
				}
				
				listener.onTick(timeLeft);
				currentLobbyTime++;
			}
		});
		
		this.countdown.start();
	}

	public boolean forceStart() {
		if(!getGame().getGameState().equals(GameState.WAITING) || this.countdown == null) return false;
		this.countdown.end();
		return true;
	}

	public void interrupt() {
		if(!getGame().getGameState().equals(GameState.WAITING)) return;
		if(getGame().getMapVoting().isRunning()) getGame().getMapVoting().interrupt();
		this.countdown.cancel();
	}
	
	public boolean checkPlayers() {
		return game.getPlayers().size() >= game.getMinPlayers();
	}
	
	public void initializePlayer(Player p) {
		initializePlayer(p, true);
	}
	
	public void initializePlayer(Player p, boolean teleport) {
		if(this.spawn == null) throw new NullPointerException("Players can not join to a 'Lobby' without a spawn!");
		
		if(this.loadingBarType != null)
			this.loadingBars.add(new LoadingBar(getGame().getPlugin(), p, this.loadingBarType, this.time.toSeconds(this.lobbyTime)));
		
		if(!started) startScheduler();
		
		if(teleport) p.teleport(this.spawn);
		
		p.getInventory().clear();
		p.getInventory().setArmorContents(new ItemStack[4]);
		
		if(this.playerLayout != null) this.playerLayout.initializeInventory(p);
		p.updateInventory();
		
		this.listener.onInitialize(p);
	}

	public void quit(Player p) {
		if(getLoadingBar(p) != null) getLoadingBar(p).cancel();
	}
	
	public Game getGame() {
		return game;
	}
	
	public Location getSpawn() {
		return spawn;
	}
	
	public void setListener(LobbyListener listener) {
		if(hasListener()) HandlerList.unregisterAll(this.listener);
		
		this.listener = listener;
		Bukkit.getPluginManager().registerEvents(this.listener, this.game.getPlugin());
	}
	
	public LobbyListener getListener() {
		return listener;
	}
	
	public PlayerLayout getPlayerLayout() {
		return playerLayout;
	}
	
	public void setPlayerLayout(PlayerLayout playerLayout) {
		this.playerLayout = playerLayout;
	}
	
	public boolean hasListener() {
		return this.listener != null;
	}
	
	public int getCurrentLobbyTime() {
		return currentLobbyTime;
	}
	
	public int getLobbyTime() {
		return this.time.toSeconds(this.lobbyTime);
	}
	
	public List<LoadingBar> getLoadingBars() {
		return loadingBars;
	}
	
	public LoadingBar getLoadingBar(Player p) {
		for(LoadingBar bar : this.loadingBars) {
			if(bar.getPlayer().getName().equals(p.getName())) return bar;
		}
		
		return null;
	}
	
	public LoadingBarType getLoadingBarType() {
		return loadingBarType;
	}
	
	public void setLoadingBarType(LoadingBarType loadingBarType) {
		this.loadingBarType = loadingBarType;
	}
}