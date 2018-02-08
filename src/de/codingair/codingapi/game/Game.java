package de.codingair.codingapi.game;

import de.codingair.codingapi.bungeecord.BungeeCordHelper;
import de.codingair.codingapi.game.gui.TeamVoting;
import de.codingair.codingapi.game.lobby.Lobby;
import de.codingair.codingapi.game.lobby.LobbyListener;
import de.codingair.codingapi.game.lobby.PlayerLayout;
import de.codingair.codingapi.game.map.Map;
import de.codingair.codingapi.game.map.MapVoting;
import de.codingair.codingapi.game.utils.GameState;
import de.codingair.codingapi.game.utils.Team;
import de.codingair.codingapi.game.utils.Weather;
import de.codingair.codingapi.player.gui.PlayerItem;
import de.codingair.codingapi.player.layout.BufferedScoreboard;
import de.codingair.codingapi.time.Countdown;
import de.codingair.codingapi.time.CountdownListener;
import de.codingair.codingapi.time.TimeFetcher;
import de.codingair.codingapi.tools.Converter;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.IllegalPluginAccessException;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.DisplaySlot;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

/**
 * Removing of this disclaimer is forbidden.
 *
 * @author codingair
 * @verions: 1.0.0
 **/

public abstract class Game {
	public static final int MAX_TEAM_AMOUNT = 14;

	private List<String> warnings = new ArrayList<>();
	private Plugin plugin;
	private String name;
	private String prefix;
	private Lobby lobby;
	
	private GameState gameState = GameState.STOPPED;
	private GameListener gameListener = new GameListener(this);
	
	private List<Player> players = new ArrayList<>();

	private Team spectator = new Team("Spectator", null);
	private double distanceToInfluence = 5D;
	
	private Map currentMap = null;
	private List<Map> maps = new ArrayList<>();
	private List<Team> teams = new ArrayList<>();
	private Weather weather = null;
	
	private PlayerLayout playerLayout;
	private PlayerLayout spectatorLayout;
	private BufferedScoreboard scoreboard = null;
	
	private int teamSize;
	private int minPlayers;
	private int maxPlayers;
	private boolean joinOnServerJoin = false;
	private boolean kickOnEnd = false;
	private boolean balancePlayers = false;
	private boolean explodeProtection = false;

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
		
		if(lobbySpawn != null && lobbySpawn.getWorld() != null) {
			this.lobby = new Lobby(this, de.codingair.codingapi.tools.Location.getByLocation(lobbySpawn), lobbyTimeValue, lobbyTime);
			this.lobby.setListener(new LobbyListener(lobby) {
				@Override
				public void onEndCountdown() {
					setGameState(GameState.STARTING);
				}
			});

			this.setGameState(GameState.WAITING);
		} else {
			warnings.add("The Lobby-Spawn cannot be initialized! Maybe the plugin couldn't find the World. (Multiverse-Core?)");
			this.setGameState(GameState.NOT_PLAYABLE);
		}
		
		runnable = new BukkitRunnable() {
			@Override
			public void run() {
				if(hasListener()) getGameListener().onTick();
				if(weather != null) weather.setup(null);
			}
		};
		
		runnable.runTaskTimer(this.plugin, 0, 1L);
	}
	
	public boolean initialize() {
		this.teams = initializeTeams(new ArrayList<>());

		if(teams.size() > MAX_TEAM_AMOUNT) {
			this.warnings.add("Maximum of teams exceeded. Don't create more than " + MAX_TEAM_AMOUNT + " teams!");
			getPlugin().getLogger().log(Level.WARNING, "Maximum of teams exceeded. Don't create more than " + MAX_TEAM_AMOUNT + " teams!");
			return false;
		}

		if(teams.isEmpty()) {
			this.warnings.add("There are no teams to use. Create some teams to initialize the game!");
			getPlugin().getLogger().log(Level.WARNING, "There are no teams to use. Create some teams to initialize the game!");
			return false;
		}

		this.maps = initializeMaps(new ArrayList<>());

		if(this.maps.isEmpty()) {
			this.warnings.add("There are no maps to use. Create some maps to initialize the game!");
			getPlugin().getLogger().log(Level.WARNING, "There are no maps to use. Create some maps to initialize the game!");
			return false;
		}

		return true;
	}
	
	public abstract List<Team> initializeTeams(List<Team> teams);
	
	public abstract List<Map> initializeMaps(List<Map> maps);
	
	public boolean join(Player p) {
		if(isPlaying(p)) return false;

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
		updateScoreboard();
		return true;
	}
	
	public boolean quit(Player p) {
		if(!this.isPlaying(p)) return false;

		if(this.lobby != null) this.lobby.quit(p);
		if(hasTeam(p)) getTeam(p).removeMember(p);

		this.gameListener.onQuit(p);
		this.players.remove(p);
		this.teamVoting.unregister(p);

		clearPlayer(p, true);
		setSpectator(p, false);

		updateScoreboard();
		return true;
	}
	
	private void initializePlayer(Player p) {
		initializePlayer(p, false);
	}
	
	private void initializePlayer(Player p, boolean start) {
		clearPlayer(p);
		
		if(this.playerLayout != null) this.playerLayout.initializeInventory(p);

		for(Player other : this.players) {
			p.showPlayer(other);
			other.showPlayer(p);
		}

		for(Player player : Bukkit.getOnlinePlayers()) {
			if(!isPlaying(player)) p.hidePlayer(player);
		}

		if(!start) {
			setSpectator(p, gameState.equals(GameState.STARTING) || gameState.equals(GameState.RUNNING) || gameState.equals(GameState.STOPPING));
		} else {
			p.setGameMode(GameMode.SURVIVAL);
		}
	}

	private void clearPlayer(Player p, boolean scoreboard) {
		PlayerItem.getPlayerItems(p).forEach(PlayerItem::remove);

		p.getInventory().clear();
		p.getInventory().setArmorContents(new ItemStack[4]);

		p.setLevel(0);
		p.setExp(0);

		if(!scoreboard) return;

		if(this.scoreboard != null) {
			new BufferedScoreboard(p, "clear", false).send(p);
		}
	}

	private void clearPlayer(Player p) {
		clearPlayer(p, false);
	}
	
	private void balancePlayers() {
		for(Player p : this.players) {
			if(!hasTeam(p)) {
				getSmallestTeam().addMember(p);
			}
		}

		Team s, b;
		while(!(s = getSmallestTeam()).getName().equals((b = getBiggestTeam()).getName()) && Math.abs(b.getMembers().size() - s.getMembers().size()) > 1) {
			Player player = b.getMembers().get((int) (Math.random() * (double) b.getMembers().size()));

			b.removeMember(player);
			s.addMember(player);

			this.gameListener.onTeamChange(player, b, s);
		}
	}

	private Team getSmallestTeam() {
		Team team = null;

		List<Team> teams = new ArrayList<>(this.teams);
		for(Team t : teams) {
			if(team == null) team = t;
			else if(team.getMembers().size() > t.getMembers().size()) team = t;
		}

		teams.clear();

		return team;
	}

	private Team getBiggestTeam() {
		Team team = null;

		List<Team> teams = new ArrayList<>(this.teams);
		for(Team t : teams) {
			if(team == null) team = t;
			else if(team.getMembers().size() < t.getMembers().size()) team = t;
		}

		teams.clear();

		return team;
	}
	
	public void broadcast(boolean prefix, String message, Player... exceptions) {
		List<Player> ex = Converter.fromArrayToList(exceptions);
		
		for(Player p : this.players) {
			if(!ex.contains(p)) {
				p.sendMessage((prefix ? this.prefix + "§r" : "") + message);
			}
		}

		ex.clear();
	}
	
	public void sendMessage(boolean prefix, String message, Player... player) {
		List<Player> players = Converter.fromArrayToList(player);
		
		for(Player p : players) {
			p.sendMessage((prefix ? this.prefix + "§r" : "") + message);
		}

		players.clear();
	}

	public void updateScoreboard() {
		if(this.scoreboard == null) return;
		this.scoreboard.send(this.players);
	}

	public boolean forceMap(Map map) {
		if(!getGameState().equals(GameState.WAITING)) return false;
		if(getMapVoting() != null) getMapVoting().interrupt();
		setCurrentMap(map);
		getLobby().getListener().onEndMapVoting(map);
		return true;
	}

	public boolean forceStart() {
		boolean result = getGameState().equals(GameState.WAITING) && getLobby().forceStart();
		updateScoreboard();
		return result;
	}
	
	private void startKickCountdown() {
		Countdown c = new Countdown(this.plugin, TimeFetcher.Time.SECONDS, this.kickCountdown, false);
		c.addListener(new CountdownListener() {
			@Override
			protected void CountdownStartEvent() {
			
			}
			
			@Override
			protected void CountdownEndEvent() {
				gameListener.onKickCountdownTick(0);

				List<Player> players = new ArrayList<>();
				players.addAll(Game.this.players);

				if(kickOnEnd && fallbackServer == null) {
					throw new NullPointerException("You have to set a 'FallbackServer' if 'kickOnEnd'=true!");
				}

				for(Player p : players) {
					for(Player other : Bukkit.getOnlinePlayers()) {
						p.showPlayer(other);
						other.showPlayer(p);
					}

					quit(p);

					if(kickOnEnd && fallbackServer != null) {
						BungeeCordHelper.connect(p, fallbackServer, plugin);
					}
				}

				players.clear();
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
		
		if(getPlugin().isEnabled()) c.start();
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
		
		if(balancePlayers) balancePlayers();
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
		List<Player> players = new ArrayList<>(this.players);
		for(Player player : players) {
			quit(player);
		}

		players.clear();

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
		GameState old = this.gameState;
		this.gameState = gameState;
		if(hasListener()) this.gameListener.onGameStateChange(gameState);
		
		switch(gameState) {
			case WAITING: {
				this.teamVoting = new TeamVoting(this);
				this.teamVoting.register();
				break;
			}
			
			case STOPPING: {
				if(old.equals(GameState.STARTING) || old.equals(GameState.RUNNING)) this.countdown.cancel();
				else if(old.equals(GameState.WAITING)) this.lobby.interrupt();
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
				if(this.teamVoting != null) this.teamVoting.unregister();
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

	public boolean changeTeam(Player player, Team team) {
		Team old = getTeam(player);

		if(old == null && team == null) return false;
//		if(old != null && team == null || (team != null && old.getName().equals(team.getName()))) old.removeMember(player);
		if(old != null && (team == null || old.getName().equals(team.getName()) || team.getMembers().size() < this.teamSize)) old.removeMember(player);

		if(team != null && old == null || !old.getName().equals(team.getName())) {
			if(team.getMembers().size() >= this.teamSize) {
				getGameListener().onTeamChangeErrorIsFull(player, team);
				return false;
			}

			team.addMember(player);
		}

		if((old != null && team == null) || (old != null && team != null && old.getName().equals(team.getName()))) getGameListener().onTeamQuit(player, old);
		if(old != null && team != null && !old.getName().equals(team.getName())) getGameListener().onTeamChange(player, old, team);
		if(old == null && team != null) getGameListener().onTeamJoin(player, team);

		updateScoreboard();
		return true;
	}
	
	public void setCurrentMap(Map currentMap) {
		this.currentMap = currentMap;

		if(currentMap == null) return;

		this.currentMap.setTeams(this.teams);
		this.spectator.setSpawn(currentMap.getSpectatorSpawn());
	}

	public void setSpectator(Player player, boolean spectator) {
		player.setGameMode(GameMode.ADVENTURE);

		if(spectator && !this.spectator.isMember(player)) {
			if(hasTeam(player)) getTeam(player).removeMember(player);
			this.spectator.addMember(player);

			for(Player other : this.players) {
				if(other.getName().equals(player.getName())) continue;

				if(!this.spectator.isMember(other)) other.hidePlayer(player);
			}

			for(Player other : this.spectator.getMembers()) {
				player.showPlayer(other);
			}

			clearPlayer(player);

			player.setFlySpeed(0.8F);
			player.setAllowFlight(true);
			player.setFlying(true);

			if(this.spectator.getSpawn() != null) player.teleport(this.spectator.getSpawn());

			player.setFlying(true);

			Bukkit.getScheduler().runTaskLater(getPlugin(), () -> {
				if(this.spectatorLayout != null) this.spectatorLayout.initializeInventory(player);
			}, 1L);
		} else if(!spectator && this.spectator.isMember(player)) {
			this.spectator.removeMember(player);

			for(Player other : this.spectator.getMembers()) {
				player.hidePlayer(other);
			}

			for(Player other : this.players) {
				if(!isSpectator(other)) other.showPlayer(player);
			}

			clearPlayer(player);

			player.setFlying(false);
			player.setAllowFlight(false);
			player.setFlySpeed(0.2F);
		}
	}

	public boolean isSpectator(Player player) {
		return this.spectator.isMember(player);
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

	public boolean isBalancePlayers() {
		return balancePlayers;
	}

	public void setBalancePlayers(boolean balancePlayers) {
		this.balancePlayers = balancePlayers;
	}

	public TeamVoting getTeamVoting() {
		return teamVoting;
	}

	public List<String> getWarnings() {
		return warnings;
	}

	public BufferedScoreboard getScoreboard() {
		return scoreboard;
	}

	public void setScoreboard(BufferedScoreboard scoreboard) {
		this.scoreboard = scoreboard;
	}

	public Team getSpectator() {
		return spectator;
	}

	public PlayerLayout getSpectatorLayout() {
		return spectatorLayout;
	}

	public void setSpectatorLayout(PlayerLayout spectatorLayout) {
		this.spectatorLayout = spectatorLayout;
	}

	public double getDistanceToInfluence() {
		return distanceToInfluence;
	}

	public void setDistanceToInfluence(double distanceToInfluence) {
		this.distanceToInfluence = distanceToInfluence;
	}

	public boolean isExplodeProtection() {
		return explodeProtection;
	}

	public void setExplodeProtection(boolean explodeProtection) {
		this.explodeProtection = explodeProtection;
	}

	public boolean isFull() {
		if(this.teams.size() == 0 || this.teamSize == 0 || this.players.size() == 0) return false;
		return this.players.size() >= this.teams.size() * this.teamSize;
	}
}
