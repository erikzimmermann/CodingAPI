package de.codingair.codingapi;

import de.codingair.codingapi.customentity.fakeplayer.FakePlayer;
import de.codingair.codingapi.particles.animations.customanimations.AnimationType;
import de.codingair.codingapi.player.Hologram;
import de.codingair.codingapi.player.chat.ChatListener;
import de.codingair.codingapi.player.gui.GUIListener;
import de.codingair.codingapi.player.gui.book.BookListener;
import de.codingair.codingapi.server.commands.CommandBuilder;
import de.codingair.codingapi.server.events.WalkListener;
import de.codingair.codingapi.server.reflections.IReflection;
import de.codingair.codingapi.utils.Removable;
import de.codingair.codingapi.utils.Ticker;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.InvalidDescriptionException;
import org.bukkit.plugin.InvalidPluginException;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.SimplePluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;

public class API {
    private static final List<Removable> REMOVABLES = new ArrayList<>();
    private static final List<Ticker> TICKERS = new ArrayList<>();

    private static API instance;

    private boolean initialized = false;

    private List<JavaPlugin> plugins = new ArrayList<>();
    private BukkitTask tickerTimer = null;

    public void onEnable(JavaPlugin plugin) {
        if(!this.plugins.contains(plugin)) this.plugins.add(plugin);
        if(initialized) return;
        if(this.plugins.size() == 1) initPlugin(plugin);
    }

    public synchronized void onDisable(JavaPlugin plugin) {
        if(!initialized || !plugins.contains(plugin)) return;

        for(String s : plugin.getDescription().getCommands().keySet()) {
            PluginCommand command = Bukkit.getPluginCommand(s);
            if(command == null) continue;

            if(command.getExecutor() instanceof CommandBuilder) {
                CommandBuilder b = (CommandBuilder) command.getExecutor();
                b.unregister(plugin);
            }
        }

        HandlerList.unregisterAll(plugin);

        removePlugin(plugin);
        this.plugins.remove(plugin);
        if(!plugins.isEmpty()) initPlugin(this.plugins.get(0));
    }

    public void reload(JavaPlugin plugin) throws InvalidDescriptionException, FileNotFoundException, InvalidPluginException {
        List<JavaPlugin> plugins = new ArrayList<>(this.plugins);

        for(JavaPlugin p : plugins) {
            if(p == plugin) continue;
            disablePlugin(p);
        }

        disablePlugin(plugin);


        for(JavaPlugin p : plugins) {
            enablePlugin(p.getName());
        }

        plugins.clear();
    }

    public void disablePlugin(JavaPlugin plugin) {
        Bukkit.getPluginManager().disablePlugin(plugin);

        try {
            IReflection.FieldAccessor lookupNames = IReflection.getField(SimplePluginManager.class, "lookupNames");
            IReflection.FieldAccessor plugins = IReflection.getField(SimplePluginManager.class, "plugins");

            Map<String, Plugin> map = (Map<String, Plugin>) lookupNames.get(Bukkit.getPluginManager());
            List<Plugin> pluginList = (List<Plugin>) plugins.get(Bukkit.getPluginManager());

            map.remove(plugin.getDescription().getName());
            pluginList.remove(plugin);
        } catch(Exception ignored) {
        }
    }

    public void enablePlugin(String name) throws InvalidDescriptionException, InvalidPluginException, FileNotFoundException {
        File pluginFile = new File("plugins", name + ".jar");

        if(!pluginFile.exists()) {
            File f = new File("plugins");

            for(File file : f.listFiles()) {
                if(file.getName().toLowerCase().contains(name.toLowerCase())) {
                    pluginFile = file;
                    break;
                }
            }

            if(pluginFile == null) throw new FileNotFoundException("Could not find any " + name + ".jar!");
        }

        Bukkit.getPluginManager().enablePlugin(Bukkit.getPluginManager().loadPlugin(pluginFile));
    }

    private void removePlugin(JavaPlugin plugin) {
        HandlerList.unregisterAll(plugin);
        if(this.tickerTimer.getOwner() == plugin) this.tickerTimer.cancel();

        List<Removable> removables = getRemovables(plugin);
        removables.forEach(Removable::destroy);
        removables.clear();
        AnimationType.clearCache();

        initialized = false;
    }

    private void initPlugin(JavaPlugin plugin) {
        initialized = true;
        GUIListener.register(plugin);
        Bukkit.getPluginManager().registerEvents(Hologram.getListener(), plugin);
        Bukkit.getPluginManager().registerEvents(new WalkListener(), plugin);
        Bukkit.getPluginManager().registerEvents(new BookListener(), plugin);
        Bukkit.getPluginManager().registerEvents(new ChatListener(), plugin);
        Bukkit.getPluginManager().registerEvents(new Listener() {

            /** PlayerDataListener - Start */

            @EventHandler
            public void onQuit(PlayerQuitEvent e) {
                removeRemovables(e.getPlayer());
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

        runTicker(plugin);
    }

    public void runTicker(JavaPlugin plugin) {
        if(this.tickerTimer != null) return;

        this.tickerTimer = Bukkit.getScheduler().runTaskTimer(plugin, new Runnable() {
            int second = 0;

            @Override
            public void run() {
                API.getRemovables(FakePlayer.class).forEach(FakePlayer::onTick);
                GUIListener.onTick();

                if(second >= 20) {
                    second = 0;
                    for(Ticker ticker : TICKERS) {
                        ticker.onSecond();
                    }
                } else second++;

                for(Ticker ticker : TICKERS) {
                    ticker.onTick();
                }
            }
        }, 1, 1);
    }

    public static API getInstance() {
        if(instance == null) instance = new API();
        return instance;
    }

    public static synchronized boolean addRemovable(Removable removable) {
        if(isRegistered(removable)) removeRemovable(removable);
        return REMOVABLES.add(removable);
    }

    public static synchronized <T extends Removable> T getRemovable(Player player, Class<? extends T> clazz) {
        for(Removable r : REMOVABLES) {
            if(clazz.isInstance(r)) {
                if((r.getPlayer() == null) != (player == null)) continue;
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

    public static synchronized <T extends Removable> T getRemovable(Class<? extends T> clazz, UUID uniqueId) {
        for(Removable r : REMOVABLES) {
            if(clazz.isInstance(r)) {
                if(r.getUniqueId() == uniqueId) return clazz.cast(r);
            }
        }

        return null;
    }

    public static synchronized List<Removable> getRemovables(JavaPlugin plugin) {
        List<Removable> l = new ArrayList<>();

        if(plugin == null) return l;

        for(Removable r : REMOVABLES) {
            if(r.getPlugin() == plugin) {
                l.add(r);
            }
        }

        return l;
    }

    public static synchronized <T extends Removable> List<T> getRemovables(JavaPlugin plugin, Class<? extends T> clazz) {
        List<T> l = new ArrayList<>();

        if(plugin == null) return l;

        for(Removable r : REMOVABLES) {
            if(clazz.isInstance(r) && r.getPlugin() == plugin) {
                l.add(clazz.cast(r));
            }
        }

        return l;
    }

    public static synchronized <T extends Removable> List<T> getRemovables(Player player, Class<? extends T> clazz) {
        List<T> l = new ArrayList<>();

        for(Removable r : REMOVABLES) {
            if(clazz.isInstance(r)) {
                if((r.getPlayer() == null) != (player == null)) continue;
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

    public static synchronized boolean removeRemovable(Removable removable) {
        return removeRemovable(getID(removable));
    }

    public static synchronized boolean removeRemovable(int id) {
        if(id == -999) return false;

        Removable removable = getRemovable(id, Removable.class);
        if(removable == null) return false;

        boolean removed = REMOVABLES.contains(id);
        REMOVABLES.remove(id);
        removable.destroy();

        return removed;
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

        List<Removable> REMOVABLES = new ArrayList<>(API.REMOVABLES);

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

    public static void addTicker(Ticker ticker) {
        TICKERS.add(ticker);
    }

    public static Ticker removeTicker(Ticker ticker) {
        int index = getTickerIndex(ticker);
        if(index == -999) return null;

        Ticker t = TICKERS.remove(index);

        if(TICKERS.size() == 0) {
            getInstance().tickerTimer.cancel();
            getInstance().tickerTimer = null;
        }

        return t;
    }

    public static Ticker getTicker(Object instance) {
        List<Ticker> tickers = new ArrayList<>(TICKERS);
        Ticker ticker = null;

        for(Ticker t : tickers) {
            if(t.getInstance().equals(instance)) {
                ticker = t;
                break;
            }
        }

        tickers.clear();
        return ticker;
    }

    public static <V extends Ticker> List<V> getTickers(Class<V> clazz) {
        List<Ticker> tickers = new ArrayList<>(TICKERS);
        List<V> list = new ArrayList<>();

        for(Ticker t : tickers) {
            if(clazz.isInstance(t)) {
                list.add((V) t);
            }
        }

        tickers.clear();
        return list;
    }

    public static int getTickerIndex(Ticker ticker) {
        List<Ticker> tickers = new ArrayList<>(TICKERS);
        boolean contains = false;
        int i = 0;

        for(Ticker t : tickers) {
            if(t.getInstance().equals(ticker.getInstance())) {
                contains = true;
                break;
            }

            i++;
        }

        tickers.clear();
        return contains ? i : -999;
    }

    public boolean isInitialized() {
        return initialized;
    }

    public List<JavaPlugin> getPlugins() {
        return plugins;
    }

    public JavaPlugin getMainPlugin() {
        return this.plugins.isEmpty() ? null : this.plugins.get(0);
    }

    public static PluginCommand getPluginCommand(String name) {
        for(JavaPlugin plugin : getInstance().plugins) {
            PluginCommand command;
            if((command = plugin.getCommand(name)) != null) return command;
        }

        return null;
    }
}