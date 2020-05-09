package de.codingair.codingapi;

import com.google.common.base.Preconditions;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import de.codingair.codingapi.customentity.fakeplayer.FakePlayer;
import de.codingair.codingapi.particles.animations.customanimations.AnimationType;
import de.codingair.codingapi.player.Hologram;
import de.codingair.codingapi.player.chat.ChatListener;
import de.codingair.codingapi.player.gui.GUIListener;
import de.codingair.codingapi.player.gui.book.BookListener;
import de.codingair.codingapi.server.commands.builder.CommandBuilder;
import de.codingair.codingapi.server.events.PlayerWalkEvent;
import de.codingair.codingapi.server.events.WalkListener;
import de.codingair.codingapi.server.reflections.IReflection;
import de.codingair.codingapi.utils.Removable;
import de.codingair.codingapi.utils.Ticker;
import de.codingair.codingapi.utils.Value;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
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
    private static final Cache<String, HashMap<Class<?>, List<Removable>>> CACHE = CacheBuilder.newBuilder().build();
    private static final List<Ticker> TICKERS = new ArrayList<>();

    private static API instance;

    private boolean initialized = false;

    private List<JavaPlugin> plugins = new ArrayList<>();
    private BukkitTask tickerTimer = null;
    private BukkitTask tickerSecondTimer = null;

    public void onEnable(JavaPlugin plugin) {
        if(!this.plugins.contains(plugin)) this.plugins.add(plugin);
        if(initialized) return;
        if(this.plugins.size() == 1) initPlugin(plugin);
    }

    public synchronized void onDisable(JavaPlugin plugin) {
        if(!initialized || !plugins.contains(plugin)) return;

        List<CommandBuilder> toDisable = new ArrayList<>();

        for(Map.Entry<String, CommandBuilder> e : CommandBuilder.REGISTERED.entrySet()) {
            if(e.getValue().getMain().getPlugin().equals(plugin)) {
                toDisable.add(e.getValue());
            }
        }

        for(CommandBuilder b : toDisable) {
            b.unregister(plugin);
        }

        toDisable.clear();

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

            if(map.remove(plugin.getDescription().getName().toLowerCase()) == null)
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
                if(!file.isDirectory() && file.getName().toLowerCase().contains(name.toLowerCase()) && file.getName().endsWith(".jar")) {
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
        if(this.tickerSecondTimer.getOwner() == plugin) this.tickerSecondTimer.cancel();

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
            public void onMove(PlayerWalkEvent e) {
                Player p = e.getPlayer();
                Location from = e.getFrom();
                Location to = e.getTo();

                List<FakePlayer> l = API.getRemovables(null, FakePlayer.class);
                for(FakePlayer fakePlayer : l) {
                    if(!fakePlayer.isInRange(from) && fakePlayer.isInRange(to)) {
                        fakePlayer.updatePlayer(p);
                    }
                }
                l.clear();
            }

            /* FakePlayerListener - End */

        }, plugin);

        runTicker(plugin);
    }

    public void runTicker(JavaPlugin plugin) {
        if(this.tickerTimer != null) return;

        Value<Integer> last = new Value<>(0);
        this.tickerSecondTimer = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            for(Ticker ticker : TICKERS) {
                ticker.onSecond();
            }
            int i = count();
            if(last.getValue() != i) {
                last.setValue(i);
            }
        }, 0, 20);

        this.tickerTimer = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            List<FakePlayer> l = API.getRemovables(null, FakePlayer.class);
            l.forEach(FakePlayer::onTick);
            l.clear();
            GUIListener.onTick();

            for(Ticker ticker : TICKERS) {
                ticker.onTick();
            }
        }, 0, 1);
    }

    private int count() {
        int i = 0;
        Map<String, HashMap<Class<?>, List<Removable>>> data = CACHE.asMap();

        for(HashMap<Class<?>, List<Removable>> value : data.values()) {
            for(List<Removable> removables : value.values()) {
                i += removables.size();
            }
        }

        return i;
    }

    public static API getInstance() {
        if(instance == null) instance = new API();
        return instance;
    }

    private static String getKey(Player player) {
        return player == null ? "§" : player.getName();
    }

    private static Class<?> getRemovableClass(Removable removable) {
        return getRemovableClass(removable.getClass());
    }

    private static Class<?> getRemovableClass(Class<?> c) {
        if(!containsRemovableInterface(c.getInterfaces())) {
            Class<?> deep = c.getSuperclass();
            if(deep == null) return null;
            else {
                Class<?> deeper = getRemovableClass(deep);
                return deeper == null ? c : deeper;
            }
        } else return c;
    }

    private static boolean containsRemovableInterface(Class<?>[] classes) {
        for(Class<?> aClass : classes) {
            if(aClass.equals(Removable.class)) return true;
        }
        return false;
    }

    public static synchronized boolean addRemovable(Removable removable) {
        Preconditions.checkNotNull(removable);

        HashMap<Class<?>, List<Removable>> data = CACHE.getIfPresent(getKey(removable.getPlayer()));

        Class<?> enclosingClass = getRemovableClass(removable);

        List<Removable> entries;
        if(data == null) {
            data = new HashMap<>();
            entries = new ArrayList<>();
            data.put(enclosingClass, entries);
            CACHE.put(getKey(removable.getPlayer()), data);
        } else {
            entries = data.get(enclosingClass);
            if(entries == null) {
                entries = new ArrayList<>();
                data.put(enclosingClass, entries);
            } else if(entries.contains(removable)) return false;
        }

        entries.add(removable);
        return true;
    }

    public static synchronized <T extends Removable> T getRemovable(Player player, Class<? extends T> clazz) {
        Preconditions.checkNotNull(clazz);
        HashMap<Class<?>, List<Removable>> data = CACHE.getIfPresent(getKey(player));

        if(data != null) {
            List<T> l = (List<T>) data.get(getRemovableClass(clazz));

            if(l != null) {
                for(T t : l) {
                    if(clazz.isInstance(t)) return t;
                }
            }

            return null;
        }

        return null;
    }

    public static synchronized <T extends Removable> T getRemovable(Class<? extends T> clazz, UUID uniqueId) {
        Preconditions.checkNotNull(clazz);
        Preconditions.checkNotNull(uniqueId);

        HashMap<Class<?>, List<Removable>> data = CACHE.getIfPresent(getKey(null));
        if(data == null) return null;

        List<Removable> entries = data.get(getRemovableClass(clazz));
        if(entries != null) {
            for(Removable entry : entries) {
                if(entry.getUniqueId().equals(uniqueId)) return (T) entry;
            }
        }

        return null;
    }

    private static synchronized List<Removable> getRemovables(JavaPlugin plugin) {
        List<Removable> found = new ArrayList<>();
        Map<String, HashMap<Class<?>, List<Removable>>> data = CACHE.asMap();

        for(HashMap<Class<?>, List<Removable>> value : data.values()) {
            for(List<Removable> removables : value.values()) {
                for(Removable removable : removables) {
                    if(removable.getPlugin().equals(plugin)) found.add(removable);
                }
            }
        }

        return found;
    }

    public static synchronized <T extends Removable> List<T> getRemovables(Player player, Class<? extends T> clazz) {
        Preconditions.checkNotNull(clazz);
        HashMap<Class<?>, List<Removable>> data = CACHE.getIfPresent(getKey(player));

        if(data != null) {
            List<?> entries = data.get(getRemovableClass(clazz));

            if(entries != null) {
                List<T> l = new ArrayList<>();
                for(Object removable : entries) {
                    if(clazz.isInstance(removable)) l.add((T) removable);
                }
                return l;
            }

            return new ArrayList<>();
        }

        return new ArrayList<>();
    }

    @Deprecated
    public static synchronized <T extends Removable> List<T> getRemovables(Class<? extends T> clazz) {
        Preconditions.checkNotNull(clazz);
        List<T> l = new ArrayList<>();

        Map<String, HashMap<Class<?>, List<Removable>>> data = CACHE.asMap();
        Class<?> rClazz = getRemovableClass(clazz);

        for(HashMap<Class<?>, List<Removable>> value : data.values()) {
            List<Removable> r = value.get(rClazz);
            if(r != null) {
                for(Removable removable : r) {
                    if(clazz.isInstance(removable)) l.add((T) removable);
                }
            }
        }

        return l;
    }

    public static synchronized boolean removeRemovable(Removable removable) {
        Preconditions.checkNotNull(removable);
        String key = getKey(removable.getPlayer());
        HashMap<Class<?>, List<Removable>> data = CACHE.getIfPresent(key);

        if(data != null) {
            Class<?> enclosingClass = getRemovableClass(removable);
            List<Removable> entries = data.get(enclosingClass);
            if(entries != null) {
                boolean success = entries.remove(removable);

                if(entries.isEmpty()) {
                    data.remove(enclosingClass);
                    if(data.isEmpty()) CACHE.invalidate(key);
                }

                return success;
            }
        }

        return false;
    }

    public static synchronized void removeRemovables(Player player) {
        HashMap<Class<?>, List<Removable>> data = CACHE.getIfPresent(getKey(player));

        if(data != null) {
            List<List<Removable>> l = new ArrayList<>(data.values());
            for(List<Removable> value : l) {
                List<Removable> l2 = new ArrayList<>(value);
                l2.forEach(Removable::destroy);
                l2.clear();
                value.clear();
            }
            l.clear();
            data.clear();
            CACHE.invalidate(getKey(player));
        }
    }

    public static void addTicker(Ticker ticker) {
        TICKERS.add(ticker);
    }

    public static Ticker removeTicker(Ticker ticker) {
        int index = getTickerIndex(ticker);
        if(index == -999) return null;

        return TICKERS.remove(index);
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