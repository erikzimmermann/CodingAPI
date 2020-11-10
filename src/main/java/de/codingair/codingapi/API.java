package de.codingair.codingapi;

import com.google.common.base.Preconditions;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import de.codingair.codingapi.particles.animations.customanimations.AnimationType;
import de.codingair.codingapi.player.Hologram;
import de.codingair.codingapi.player.chat.ChatListener;
import de.codingair.codingapi.player.gui.GUIListener;
import de.codingair.codingapi.player.gui.book.BookListener;
import de.codingair.codingapi.server.commands.builder.CommandBuilder;
import de.codingair.codingapi.server.events.WalkListener;
import de.codingair.codingapi.server.listeners.PickItemListener;
import de.codingair.codingapi.server.reflections.IReflection;
import de.codingair.codingapi.utils.Removable;
import de.codingair.codingapi.utils.Ticker;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
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
import java.util.concurrent.TimeUnit;

public class API {
    private static final Cache<String, HashMap<Class<?>, List<Removable>>> CACHE = CacheBuilder.newBuilder().build();
    private static final Cache<Class<?>, List<Removable>> SPECIFIC = CacheBuilder.newBuilder().expireAfterAccess(10, TimeUnit.SECONDS).build();
    private static final List<Ticker> TICKERS = new ArrayList<>();

    private static API instance;
    private boolean initialized = false;

    private final List<JavaPlugin> plugins = new ArrayList<>();
    private BukkitTask tickerTimer = null;

    public void onEnable(JavaPlugin plugin) {
        if(!this.plugins.contains(plugin)) this.plugins.add(plugin);
        if(initialized) return;
        if(this.plugins.size() == 1) initPlugin(plugin);
    }

    public synchronized void onDisable(JavaPlugin plugin) {
        if(!initialized || !plugins.contains(plugin)) return;

        List<CommandBuilder> toDisable = new ArrayList<>();

        List<CommandBuilder> l = getRemovables(null, CommandBuilder.class);
        for(CommandBuilder commandBuilder : l) {
            if(commandBuilder.getPlugin().equals(plugin)) toDisable.add(commandBuilder);
        }
        l.clear();

        for(CommandBuilder b : toDisable) {
            b.unregister();
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
        enablePlugin(plugin.getName(), plugin);

        for(JavaPlugin p : plugins) {
            if(p == plugin) continue;
            enablePlugin(p.getName(), p);
        }

        plugins.clear();
    }

    public void disablePlugin(JavaPlugin plugin) {
        Bukkit.getPluginManager().disablePlugin(plugin);

        try {
            IReflection.FieldAccessor<?> lookupNames = IReflection.getField(SimplePluginManager.class, "lookupNames");
            IReflection.FieldAccessor<?> plugins = IReflection.getField(SimplePluginManager.class, "plugins");

            Map<String, Plugin> map = (Map<String, Plugin>) lookupNames.get(Bukkit.getPluginManager());
            List<Plugin> pluginList = (List<Plugin>) plugins.get(Bukkit.getPluginManager());

            if(map.remove(plugin.getDescription().getName().toLowerCase()) == null)
                map.remove(plugin.getDescription().getName());
            pluginList.remove(plugin);
        } catch(Exception ignored) {
        }
    }

    public void enablePlugin(String name, JavaPlugin backup) throws InvalidDescriptionException, InvalidPluginException, FileNotFoundException {
        File pluginFile = new File("plugins", name + ".jar");

        if(!pluginFile.exists()) {
            File f = new File("plugins");

            for(File file : f.listFiles()) {
                if(!file.isDirectory() && file.getName().toLowerCase().contains(name.toLowerCase()) && file.getName().endsWith(".jar")) {
                    pluginFile = file;
                    break;
                }
            }
        }

        if(pluginFile.exists()) Bukkit.getPluginManager().enablePlugin(Bukkit.getPluginManager().loadPlugin(pluginFile));
        else Bukkit.getPluginManager().enablePlugin(backup);
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
        Bukkit.getPluginManager().registerEvents(new PickItemListener(plugin), plugin);
        Bukkit.getPluginManager().registerEvents(new Listener() {
            @EventHandler(priority = EventPriority.HIGH)
            public void onQuit(PlayerQuitEvent e) {
                Bukkit.getScheduler().runTaskLater(getMainPlugin(), () -> removeRemovables(e.getPlayer()), 1L);
            }
        }, plugin);

        runTicker(plugin);
    }

    public void runTicker(JavaPlugin plugin) {
        if(this.tickerTimer != null) return;

        this.tickerTimer = Bukkit.getScheduler().runTaskTimer(plugin, new Runnable() {
            int i = 0;
            @Override
            public void run() {
                if(i == 20) {
                    for(Iterator<Ticker> i = TICKERS.iterator(); i.hasNext();) {
                        Ticker t = i.next();
                        t.onTick();
                        t.onSecond();
                    }

                    i = 0;
                } else {
                    for(Iterator<Ticker> i = TICKERS.iterator(); i.hasNext();) {
                        i.next().onTick();
                    }

                    i++;
                }
            }
        }, 0, 1);
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

        updateSpecific(removable, 1);
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

    public static synchronized <T extends Removable> List<T> getRemovables(Class<? extends T> clazz) {
        Preconditions.checkNotNull(clazz);
        List<T> l = (List<T>) SPECIFIC.getIfPresent(clazz);
        if(l != null) return l;
        else l = new ArrayList<>();

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

        SPECIFIC.put(clazz, (List<Removable>) l);
        return l;
    }

    private static synchronized void updateSpecific(Removable r, int action) {
        List<Removable> l = SPECIFIC.getIfPresent(r.getClass());
        if(l == null) return;

        if(action == 1) {
            //add
            l.add(r);
        } else if(action == -1) {
            //delete
            l.remove(r);
            if(l.isEmpty()) SPECIFIC.invalidate(r.getClass());
        }
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
                if(success) {
                    updateSpecific(removable, -1);
                    removable.destroy();

                    if(entries.isEmpty()) {
                        data.remove(enclosingClass);
                        if(data.isEmpty()) CACHE.invalidate(key);
                    }
                }

                return success;
            }
        }

        return false;
    }

    private static synchronized void removeRemovables(Player player) {
        HashMap<Class<?>, List<Removable>> data = CACHE.getIfPresent(getKey(player));

        if(data != null) {
            List<List<Removable>> l = new ArrayList<>(data.values());
            for(List<Removable> value : l) {
                List<Removable> l2 = new ArrayList<>(value);
                l2.forEach(r -> {
                    updateSpecific(r, -1);
                    r.destroy();
                });
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

    public static boolean removeTicker(Ticker ticker) {
        return TICKERS.remove(ticker);
    }

    public static boolean isRunning(Ticker t) {
        return TICKERS.contains(t);
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
}