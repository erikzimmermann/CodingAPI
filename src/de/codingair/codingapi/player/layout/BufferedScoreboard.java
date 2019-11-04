package de.codingair.codingapi.player.layout;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.*;

import java.lang.reflect.Constructor;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class BufferedScoreboard {
    private static HashMap<Player, BufferedScoreboard> bufferedScoreboards = new HashMap<>();
    private static Map<String, OfflinePlayer> cache = new HashMap<>();

    private Scoreboard scoreboard;
    private String title;
    private Map<String, Integer> scores;
    private Objective obj;
    private List<Team> teams;
    private List<Integer> removed;
    private Set<String> updated;

    public BufferedScoreboard(String title) {
        this.scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();

        this.title = title;

        if(this.title != null) this.title = ChatColor.translateAlternateColorCodes('&', title);

        this.scores = new ConcurrentHashMap<>();
        this.teams = Collections.synchronizedList(Lists.newArrayList());
        this.removed = Lists.newArrayList();
        this.updated = Collections.synchronizedSet(new HashSet<>());
    }

    public BufferedScoreboard(Player p, String title, boolean save) {
        this.scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();

        this.title = title;

        if(this.title != null) this.title = ChatColor.translateAlternateColorCodes('&', title);

        this.scores = new ConcurrentHashMap<>();
        this.teams = Collections.synchronizedList(Lists.newArrayList());
        this.removed = Lists.newArrayList();
        this.updated = Collections.synchronizedSet(new HashSet<>());

        if(save) {
            bufferedScoreboards.put(p, this);
        }
    }

    public void add(String text, int score) {
        if(text == null) return;

        text = ChatColor.translateAlternateColorCodes('&', text);

        if(remove(score, text, false) || !scores.containsValue(score)) {
            updated.add(text);
        }

        scores.put(text, score);
    }

    public boolean remove(int score, String text) {
        return remove(score, text, true);
    }

    public boolean remove(int score, String n, boolean b) {
        String toRemove = get(score, n);

        if(toRemove == null)
            return false;

        scores.remove(toRemove);

        if(b)
            removed.add(score);

        return true;
    }

    public void clearScores() {
        removed.addAll(getScores().values());
        scores.clear();
    }

    public String get(int score, String n) {
        String str = null;

        for(Map.Entry<String, Integer> entry : scores.entrySet()) {
            if(entry.getValue().equals(score) && !entry.getKey().equals(n)) {
                str = entry.getKey();
            }
        }

        return str;
    }

    public Map<String, Integer> getScores() {
        return scores;
    }

    private Map.Entry<Team, OfflinePlayer> createTeam(String text, int pos) {
        Team team;
        ChatColor color = ChatColor.values()[pos];
        OfflinePlayer result;

        if(!cache.containsKey(color.toString()))
            cache.put(color.toString(), getOfflinePlayerSkipLookup(color.toString()));

        result = cache.get(color.toString());

        try {
            team = scoreboard.registerNewTeam("text-" + (teams.size() + 1));
        } catch(IllegalArgumentException e) {
            team = scoreboard.getTeam("text-" + (teams.size()));
        }

        applyText(team, text, result);

        teams.add(team);
        return new AbstractMap.SimpleEntry<>(team, result);
    }

    private void applyText(Team team, String text, OfflinePlayer result) {
        Iterator<String> iterator = Splitter.fixedLength(16).split(text).iterator();
        String prefix = iterator.next();

        team.setPrefix(prefix);

        if(!team.hasPlayer(result))
            team.addPlayer(result);

        if(text.length() > 16) {
            String prefixColor = ChatColor.getLastColors(prefix);
            String suffix = iterator.next();

            if(prefix.endsWith(String.valueOf(ChatColor.COLOR_CHAR))) {
                prefix = prefix.substring(0, prefix.length() - 1);
                team.setPrefix(prefix);
                prefixColor = ChatColor.getByChar(suffix.charAt(0)).toString();
                suffix = suffix.substring(1);
            }

            if(prefixColor == null)
                prefixColor = "";

            if(suffix.length() > 16) {
                suffix = suffix.substring(0, (13 - prefixColor.length())); // cut off suffix, done if text is over 30 characters
            }

            team.setSuffix((prefixColor.equals("") ? ChatColor.RESET : prefixColor) + suffix);
        }
    }

    private void update() {
        if(this.title == null) return;

        if(updated.isEmpty()) {
            return;
        }

        if(obj == null) {
            obj = scoreboard.registerNewObjective((title.length() > 16 ? title.substring(0, 15) : title), "dummy");
            obj.setDisplayName(title);
            obj.setDisplaySlot(DisplaySlot.SIDEBAR);
        }

        removed.stream().forEach((remove) -> {
            for(String s : scoreboard.getEntries()) {
                Score score = obj.getScore(s);

                if(score == null)
                    continue;

                if(score.getScore() != remove)
                    continue;

                scoreboard.resetScores(s);
            }
        });

        removed.clear();

        int index = scores.size();

        for(Map.Entry<String, Integer> text : scores.entrySet()) {
            Team t = scoreboard.getTeam(ChatColor.values()[text.getValue()].toString());
            Map.Entry<Team, OfflinePlayer> team;

            if(!updated.contains(text.getKey())) {
                continue;
            }

            if(t != null) {
                String color = ChatColor.values()[text.getValue()].toString();

                if(!cache.containsKey(color)) {
                    cache.put(color, getOfflinePlayerSkipLookup(color));
                }

                team = new AbstractMap.SimpleEntry<>(t, cache.get(color));
                applyText(team.getKey(), text.getKey(), team.getValue());
                index -= 1;

                continue;
            } else {
                team = createTeam(text.getKey(), text.getValue());
            }

            int score = text.getValue() != null ? text.getValue() : index;

            obj.getScore(team.getValue()).setScore(score);
            index -= 1;
        }

        updated.clear();
    }

    public void setTitle(String title) {
        this.title = ChatColor.translateAlternateColorCodes('&', title);

        if(obj != null)
            obj.setDisplayName(this.title);
    }

    public void reset() {
        for(Team t : teams)
            t.unregister();
        teams.clear();
        scores.clear();
    }

    public Scoreboard getScoreboard() {
        return scoreboard;
    }

    public void send(List<Player> players) {
        this.update();

        for(Player player : players) {
            player.setScoreboard(scoreboard);
        }
    }

    public void send(Player... players) {
        this.update();

        for(Player p : players)
            p.setScoreboard(scoreboard);
    }

    private final UUID invalidUserUUID = UUID.nameUUIDFromBytes("InvalidUsername".getBytes());
    private Class<?> gameProfileClass;
    private Constructor<?> gameProfileConstructor;
    private Constructor<?> craftOfflinePlayerConstructor;

    @SuppressWarnings("deprecation")
    private OfflinePlayer getOfflinePlayerSkipLookup(String name) {
        try {
            if(gameProfileConstructor == null) {
                try { // 1.7
                    gameProfileClass = Class.forName("net.minecraft.util.com.mojang.authlib.GameProfile");
                } catch(ClassNotFoundException e) { // 1.8
                    gameProfileClass = Class.forName("com.mojang.authlib.GameProfile");
                }
                gameProfileConstructor = gameProfileClass.getDeclaredConstructor(UUID.class, String.class);
                gameProfileConstructor.setAccessible(true);
            }
            if(craftOfflinePlayerConstructor == null) {
                Class<?> serverClass = Bukkit.getServer().getClass();
                Class<?> craftOfflinePlayerClass = Class.forName(serverClass.getName()
                        .replace("CraftServer", "CraftOfflinePlayer"));
                craftOfflinePlayerConstructor = craftOfflinePlayerClass.getDeclaredConstructor(
                        serverClass, gameProfileClass
                );
                craftOfflinePlayerConstructor.setAccessible(true);
            }
            Object gameProfile = gameProfileConstructor.newInstance(invalidUserUUID, name);
            Object craftOfflinePlayer = craftOfflinePlayerConstructor.newInstance(Bukkit.getServer(), gameProfile);
            return (OfflinePlayer) craftOfflinePlayer;
        } catch(Throwable t) { // Fallback if fail
            return Bukkit.getOfflinePlayer(name);
        }
    }

    public String getTitle() {
        return title;
    }

    public Team getTeam(String name) {
        if(name == null) return null;
        return this.scoreboard.getTeam(name);
    }

    public Set<Team> getTeams() {
        return this.scoreboard.getTeams();
    }

    public Team registerNewTeam(String name) {
        return this.scoreboard.registerNewTeam(name);
    }

    public static BufferedScoreboard getBufferedScoreboard(Player p) {
        BufferedScoreboard board = bufferedScoreboards.get(p);
        return board == null ? new BufferedScoreboard(p, null, true) : board;
    }

    public static void destroyAll() {
        List<Player> players = new ArrayList<>(bufferedScoreboards.keySet());
        for(Player player : players) {
            destroyBoard(player);
        }

        players.clear();
    }

    public static BufferedScoreboard destroyBoard(Player p) {
        BufferedScoreboard board = bufferedScoreboards.remove(p);
        if(board != null) board.reset();
        return board;
    }
}
