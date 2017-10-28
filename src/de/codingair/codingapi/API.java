package de.codingair.codingapi;

import de.codingair.codingapi.customentity.CustomEntityType;
import de.codingair.codingapi.customentity.fakeplayer.FakePlayer;
import de.codingair.codingapi.customentity.networkentity.NetworkEntity;
import de.codingair.codingapi.particles.Animations.Animation;
import de.codingair.codingapi.player.data.PacketReader;
import de.codingair.codingapi.player.gui.GUIListener;
import de.codingair.codingapi.player.gui.bossbar.BossBar;
import de.codingair.codingapi.server.Version;
import de.codingair.codingapi.server.events.WalkListener;
import de.codingair.codingapi.server.playerdata.PlayerData;
import de.codingair.codingapi.server.reflections.IReflection;
import de.codingair.codingapi.server.reflections.PacketUtils;
import de.codingair.codingapi.utils.Removable;
import de.codingair.codingapi.utils.Ticker;
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
import java.util.Timer;
import java.util.TimerTask;

//import de.codingair.codingapi.files.TempFile;

public class API {
    private static final List<Removable> REMOVABLES = new ArrayList<>();
    private static final List<Ticker> TICKERS = new ArrayList<>();

    private static API instance;

    private Plugin plugin;
    private List<PlayerData> dataList = new ArrayList<>();
    private Timer tickerTimer = null;

    public void onEnable(Plugin plugin) {
        this.plugin = plugin;
        GUIListener.register(plugin);

//        this.dataList = TempFile.loadTempFiles(this.plugin, "/PlayerData/", PlayerData.class, new PlayerDataTypeAdapter(), true);

        Bukkit.getPluginManager().registerEvents(new WalkListener(), plugin);
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

                    @Override
                    public boolean writePacket(Object packet) {
                        return false;
                    }
                }.inject();
            }

            @EventHandler
            public void onQuit(PlayerQuitEvent e) {
                removePlayerData(e.getPlayer().getName());
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

//        TempFile.saveTempFiles(this.plugin, "/PlayerData/", PlayerData.class);
    }

    public void runTicker() {
        if(this.tickerTimer != null) return;

        this.tickerTimer = new Timer();

        this.tickerTimer.schedule(new TimerTask() {
            int second = 0;

            @Override
            public void run() {
                List<Ticker> tickers = new ArrayList<>(TICKERS);
                for(Ticker ticker : tickers) {
                    ticker.onTick();
                }

                if(second == 20) {
                    second = 0;

                    for(Ticker ticker : tickers) {
                        ticker.onSecond();
                    }
                } else second++;

                tickers.clear();
            }
        }, 50, 50);
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

    public static synchronized boolean addRemovable(Removable removable) {
        if(isRegistered(removable)) removeRemovable(removable);
        return REMOVABLES.add(removable);
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

    public static void addTicker(Ticker ticker) {
        TICKERS.add(ticker);
        getInstance().runTicker();
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

    public Timer getTickerTimer() {
        return tickerTimer;
    }
}