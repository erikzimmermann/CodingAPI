package de.codingair.codingapi.bungeecord;

import de.codingair.codingapi.utils.Ticker;
import net.md_5.bungee.api.plugin.Plugin;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class BungeeAPI {
    private static BungeeAPI instance;
    private Plugin plugin;

    private static final List<Ticker> TICKERS = new ArrayList<>();
    private Timer tickerTimer = null;

    public void onEnable(Plugin plugin) {
        this.plugin = plugin;
        System.out.println("ENABLE BUNGEE-ACP");
    }

    public void onDisable() {
        tickerTimer.cancel();
        tickerTimer = null;
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

    public static BungeeAPI getInstance() {
        if(instance == null) instance = new BungeeAPI();
        return instance;
    }

    public static boolean isEnabled() {
        return instance != null;
    }

    public Timer getTickerTimer() {
        return tickerTimer;
    }

    public Plugin getPlugin() {
        return plugin;
    }
}
