package de.CodingAir.v1_6.CodingAPI.Game;

import de.CodingAir.v1_6.CodingAPI.BungeeCord.BungeeCord;
import de.CodingAir.v1_6.CodingAPI.Game.GUI.TeamVoting;
import de.CodingAir.v1_6.CodingAPI.Game.Lobby.Lobby;
import de.CodingAir.v1_6.CodingAPI.Game.Lobby.LobbyListener;
import de.CodingAir.v1_6.CodingAPI.Game.Lobby.PlayerLayout;
import de.CodingAir.v1_6.CodingAPI.Game.Map.Map;
import de.CodingAir.v1_6.CodingAPI.Game.Map.MapVoting;
import de.CodingAir.v1_6.CodingAPI.Game.Utils.GameState;
import de.CodingAir.v1_6.CodingAPI.Game.Utils.Team;
import de.CodingAir.v1_6.CodingAPI.Game.Utils.Weather;
import de.CodingAir.v1_6.CodingAPI.Player.GUI.PlayerItem;
import de.CodingAir.v1_6.CodingAPI.Time.Countdown;
import de.CodingAir.v1_6.CodingAPI.Time.CountdownListener;
import de.CodingAir.v1_6.CodingAPI.Time.TimeFetcher;
import de.CodingAir.v1_6.CodingAPI.Tools.Converter;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.IllegalPluginAccessException;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;

/**
 * Removing of this disclaimer is forbidden.
 *
 * @author CodingAir
 * @verions: 1.0.0
 **/

public abstract class Game {
	public static final int MAX_TEAM_AMOUNT = 14;
	
	private Plugin plugin;
	private String name;
	private String prefix;
	private Lobby lobby;
	
	private GameState gameState = GameState.STOPPED;
	private GameListener gameListener = new GameListener(this);
	
	private List<Player> players = new ArrayList<>();
	
	private Map currentMap = null;
	private List<Map> maps = new ArrayList<>();
	private List<Team> teams = new ArrayList<>();
	private Weather weather = null;
	
	private PlayerLayout playerLayout;
	
	private int teamSize;
	private int minPlayers;
	private int maxPlayers;
	private boolean joinOnServerJoin = false;
	private boolean kickOnEnd = false;
	
	private int gameStartTimeValue;
	private TimeFetcher.Time gameStartTime;
	private int gameTime;
	private TimeFetcher.Time time;
	private int currentGameTime = 0;
	private BukkitRunnable runnable;
	
	private String fallbackServer;
	private int kickCountdown = 15;
	
	private MapVoting mapVoting;
	private int mapVotingTime = 0;
	private int mapVotingTimeBeforeEnd = 0;
	
	private TeamVoting teamVoting;
	private Countdown countdown;
	
	public Game(String name, String prefix, int teamSize, int minPlayers, int maxPlayers, int gameTimeValue, TimeFetcher.Time gameTime, int gameStartTimeValue, TimeFetcher.Time gameStartTime, Location lobbySpawn, int lobbyTimeValue, TimeFetcher.Time lobbyTime, int mapVotingTime, int mapVotingTimeBeforeEnd, Plugin plugin) {
		this.plugin = plugin;
		this.name = name;
		this.prefix = prefix;
		this.teamSize = teamSize;
		this.minPlayers = minPlayers;
		this.maxPlayers = maxPlayers;
		this.gameTime = gameTimeValue;
		this.time = gameTime;
		this.gameStartTimeValue = gameStartTimeValue;
		this.gameStartTime = gameStartTime;
		this.mapVotingTime = mapVotingTime;
		this.mapVotingTimeBeforeEnd = mapVotingTimeBeforeEnd;
		
		if(lobbySpawn != null) {
			this.lobby = new Lobby(this, de.CodingAir.v1_6.CodingAPI.Tools.Location.getByLocation(lobbySpawn), lobbyTimeValue, lobbyTime);
			this.lobby.setListener(new LobbyListener(lobby) {
				@Override
				public void onEndCountdown() {
					setGameState(GameState.STARTING);
				}
			});
		}
		
		this.setGameState(GameState.WAITING);
		
		runnable = new BukkitRunnable() {
			@Override
			public void run() {
				if(hasListener()) getGameListener().onTick();
				if(weather != null) weather.setup(null);
			}
		};
		
		runnable.runTaskTimer(this.plugin, 0, 1L);
	}
	
	public void initialize() {
		this.teams = initializeTeams(new ArrayList<>());
		
		if(teams.size() > MAX_TEAM_AMOUNT)
			throw new IllegalArgumentException("Maximum of teams exceeded. Don't create more than " + MAX_TEAM_AMOUNT + " Teams!");
		
		this.maps = initializeMaps(new ArrayList<>());
	}
	
	public abstract List<Team> initializeTeams(List<Team> teams);
	
	public abstract List<Map> initializeMaps(List<Map> maps);
	
	public boolean join(Player p) {
		if(this.gameState.equals(GameState.NOT_PLAYABLE) || this.maps.isEmpty()) {
			this.gameListener.onJoin(p);
			quit(p);
			return false;
		}
		
		this.players.add(p);
		initializePlayer(p);
		
		if(this.gameState.equals(GameState.WAITING)) {
			if(this.lobby != null) this.lobby.initializePlayer(p);
			this.teamVoting.register(p);
		}
		
		this.gameListener.onJoin(p);
		return true;
	}
	
	public void quit(Player p) {
		this.gameListener.onQuit(p);
		this.players.remove(p);
		this.teamVoting.unregister(p);
	}
	
	private void initializePlayer(Player p) {
		initializePlayer(p, false);
	}
	
	private void initializePlayer(Player p, boolean start) {
		PlayerItem.getPlayerItems(p).forEach(PlayerItem::remove);
		
		p.getInventory().clear();
		p.getInventory().setArmorContents(new ItemStack[4]);
		
		if(this.playerLayout != null) this.playerLayout.initializeInventory(p);
		
		p.setLevel(0);
		p.setExp(0);
		
		if(!start) {
			if(gameState.equals(GameState.RUNNING)) p.setGameMode(GameMode.SPECTATOR);
			else p.setGameMode(GameMode.ADVENTURE);
		} else {
			p.setGameMode(GameMode.SURVIVAL);
		}
	}
	
	private void balancePlayers() {
		for(Player p : this.players) {
			if(this.currentMap == null) {
				if(!hasTeam(p)) {
					Team team = this.getBalancedTeam();
					if(team != null) team.addMember(p);
				}
			} else {
				if(!hasTeam(p)) {
					this.currentMap.assignToBalancedTeam(p);
				}
			}
		}
	}
	
	public void broadcast(boolean prefix, String message, Player... exceptions) {
		List<Player> ex = Converter.fromArrayToList(exceptions);
		
		for(Player p : this.players) {
			if(!ex.contains(p)) {
				p.sendMessage((prefix ? this.prefix + "§r" : "") + message);
			}
		}
	}
	
	public void sendMessage(boolean prefix, String message, Player... player) {
		List<Player> players = Converter.fromArrayToList(player);
		
		for(Player p : players) {
			p.sendMessage((prefix ? this.prefix + "§r" : "") + message);
		}
	}
	
	private void startKickCountdown() {
		Countdown c = new Countdown(this.plugin, TimeFetcher.Time.SECONDS, this.kickCountdown, false);
		c.addListener(new CountdownListener() {
			@Override
			protected void CountdownStartEvent() {
			
			}
			
			@Override
			protected void CountdownEndEvent() {
				List<Player> players = new ArrayList<>();
				players.addAll(Game.this.players);
				
				if(kickOnEnd) {
					if(fallbackServer == null)
						throw new NullPointerException("You have to set a 'FallbackServer' if 'kickOnEnd'=true!");
					
					for(Player p : players) {
						for(Player other : Bukkit.getOnlinePlayers()) {
							p.showPlayer(other);
						}
						
						BungeeCord.connect(p, fallbackServer, plugin);
					}
				} else {
					for(Player p : players) {
						for(Player other : Bukkit.getOnlinePlayers()) {
							p.showPlayer(other);
						}
						
						quit(p);
					}
				}
				
				setGameState(GameState.STOPPED);
			}
			
			@Override
			protected void CountdownCancelEvent() {
			
			}
			
			@Override
			protected void onTick(int timeLeft) {
				gameListener.onKickCountdownTick(timeLeft);
			}
		});
		
		c.start();
		if(this.kickCountdown == 0) c.end();
	}
	
	public void start() {
		if(!this.ready()) return;
		
		this.players.forEach(p -> {
			initializePlayer(p, true);
			p.closeInventory();
			
			for(Player other : Bukkit.getOnlinePlayers()) {
				if(!isPlaying(other)) p.hidePlayer(other);
			}
		});
		
		balancePlayers();
		this.currentMap.getTeams().forEach(Team::teleportAllToSpawn);
		
		this.countdown = new Countdown(this.plugin, this.gameStartTime, this.gameStartTimeValue, false);
		this.countdown.addListener(new CountdownListener() {
			boolean startCountdown = true;
			
			@Override
			protected void CountdownStartEvent() {
				if(startCountdown) {
					setGameState(GameState.STARTING);
				} else {
					setGameState(GameState.RUNNING);
				}
				
			}
			
			@Override
			protected void CountdownEndEvent() {
				if(startCountdown) {
					currentGameTime = 0;
					startCountdown = false;
					countdown = new Countdown(plugin, time, gameTime);
					countdown.addListener(this);
					countdown.start();
				} else {
					setGameState(GameState.STOPPING);
				}
			}
			
			@Override
			protected void CountdownCancelEvent() {
			
			}
			
			@Override
			protected void onTick(int timeLeft) {
				if(startCountdown) {
					currentGameTime++;
					gameListener.onStartCountdownTick(timeLeft);
				} else {
					currentGameTime++;
					gameListener.onTick(timeLeft, true);
					gameListener.onTick(currentGameTime, false);
				}
			}
		});
		
		this.countdown.start();
	}
	
	public void end() {
		if(this.countdown != null) {
			this.countdown.end();
			if(getGameState().equals(GameState.RUNNING)) this.countdown.end();
		}
	}
	
	public void interrupt() {
		try {
			end();
		} catch(IllegalPluginAccessException ex) {
			setGameState(GameState.STOPPED);
		}
	}
	
	public String getName() {
		return name;
	}
	
	public String getPrefix() {
		return prefix;
	}
	
	public Map getCurrentMap() {
		return currentMap;
	}
	
	public void setGameState(GameState gameState) {
		this.gameState = gameState;
		if(hasListener()) this.gameListener.onGameStateChange(gameState);
		
		switch(gameState) {
			case WAITING: {
				this.teamVoting = new TeamVoting(this);
				this.teamVoting.register();
				break;
			}
			
			case STOPPING: {
				startKickCountdown();
				break;
			}
			
			case STOPPED: {
				if(this.hasListener()) HandlerList.unregisterAll(this.gameListener);
				if(this.lobby != null && this.lobby.hasListener()) HandlerList.unregisterAll(this.lobby.getListener());
				this.runnable.cancel();
				break;
			}
			
			default: {
				this.teamVoting.unregister();
			}
		}
	}
	
	public GameState getGameState() {
		return gameState;
	}
	
	public GameListener getGameListener() {
		return gameListener;
	}
	
	public void setGameListener(GameListener gameListener) {
		if(hasListener()) HandlerList.unregisterAll(this.gameListener);
		
		this.gameListener = gameListener;
		Bukkit.getPluginManager().registerEvents(this.gameListener, this.plugin);
	}
	
	public boolean hasListener() {
		return this.gameListener != null;
	}
	
	public boolean hasMap() {
		return this.currentMap != null;
	}
	
	public boolean isPlaying(Player p) {
		return this.players.contains(p);
	}
	
	public boolean ready() {
		return this.hasMap();
	}
	
	public int getMinPlayers() {
		return minPlayers;
	}
	
	public int getMaxPlayers() {
		return maxPlayers;
	}
	
	public int getGameTime() {
		return gameTime;
	}
	
	public TimeFetcher.Time getTime() {
		return time;
	}
	
	public int getCurrentGameTime() {
		return currentGameTime;
	}
	
	public List<Player> getPlayers() {
		return players;
	}
	
	public Lobby getLobby() {
		return lobby;
	}
	
	public Countdown getCountdown() {
		return countdown;
	}
	
	public List<Map> getMaps() {
		return maps;
	}
	
	public Map getMap(String name) {
		for(Map map : this.maps) {
			if(map.getName().equals(name)) return map;
		}
		
		return null;
	}
	
	public boolean existsTeam(String name) {
		return getTeam(name) != null;
	}
	
	public Team getTeam(String name) {
		for(Team team : this.teams) {
			if(team.getName().equalsIgnoreCase(name)) return team;
		}
		
		return null;
	}
	
	public boolean hasTeam(Player p) {
		for(Team team : this.teams) {
			if(team.isMember(p)) return true;
		}
		
		return false;
	}
	
	public Team getTeam(Player p) {
		for(Team team : this.teams) {
			if(team.isMember(p)) return team;
		}
		
		return null;
	}
	
	public void addTeam(Team team) {
		if(this.teams.size() == MAX_TEAM_AMOUNT) return;
		if(!existsTeam(team.getName())) this.teams.add(team);
	}
	
	public void removeTeam(String name) {
		Team team = getTeam(name);
		
		if(team == null) return;
		this.teams.remove(team);
	}
	
	public List<Team> getTeams() {
		return teams;
	}
	
	public Team getBalancedTeam() {
		if(this.teams.size() == 0) return null;
		
		Team team = null;
		
		for(Team t : this.teams) {
			if(team == null) team = t;
			else if(team.getMembers().size() > t.getMembers().size()) {
				team = t;
			}
		}
		
		return team;
	}
	
	public void setCurrentMap(Map currentMap) {
		this.currentMap = currentMap;
		this.currentMap.setTeams(this.teams);
	}
	
	public void addMap(Map map) {
		this.maps.add(map);
	}
	
	public void removeMap(Map map) {
		this.maps.remove(map);
	}
	
	public MapVoting getMapVoting() {
		return mapVoting;
	}
	
	public void setMapVoting(MapVoting mapVoting) {
		this.mapVoting = mapVoting;
	}
	
	public int getMapVotingTime() {
		return mapVotingTime;
	}
	
	public int getMapVotingTimeBeforeEnd() {
		return mapVotingTimeBeforeEnd;
	}
	
	public int getTeamSize() {
		return teamSize;
	}
	
	public void setJoinOnServerJoin(boolean joinOnServerJoin) {
		this.joinOnServerJoin = joinOnServerJoin;
	}
	
	public boolean joinOnServerJoin() {
		return joinOnServerJoin;
	}
	
	public boolean kickOnEnd() {
		return kickOnEnd;
	}
	
	public void setKickOnEnd(boolean kickOnEnd) {
		this.kickOnEnd = kickOnEnd;
	}
	
	public int getKickCountdown() {
		return kickCountdown;
	}
	
	public void setKickCountdown(int kickCountdown) {
		this.kickCountdown = kickCountdown;
	}
	
	public String getFallbackServer() {
		return fallbackServer;
	}
	
	public void setFallbackServer(String fallbackServer) {
		this.fallbackServer = fallbackServer;
	}
	
	public PlayerLayout getPlayerLayout() {
		return playerLayout;
	}
	
	public void setPlayerLayout(PlayerLayout playerLayout) {
		this.playerLayout = playerLayout;
	}
	
	public int getGameStartTimeValue() {
		return gameStartTimeValue;
	}
	
	public TimeFetcher.Time getGameStartTime() {
		return gameStartTime;
	}
	
	public Weather getWeather() {
		return weather;
	}
	
	public void setWeather(Weather weather) {
		this.weather = weather;
	}
	
	public Plugin getPlugin() {
		return plugin;
	}
}
