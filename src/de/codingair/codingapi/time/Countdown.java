package de.codingair.codingapi.time;

import org.bukkit.Bukkit;
import org.bukkit.plugin.IllegalPluginAccessException;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.List;

public class Countdown {
	private Plugin plugin;
	private TimeFetcher.Time time;
	private int time_value;
	private List<CountdownListener> listener = new ArrayList<>();
	private int runnableID = -1;
	private boolean firstDelay = true;
	private boolean running = false;
	
	public Countdown(Plugin plugin, TimeFetcher.Time time, int time_value) {
		this.plugin = plugin;
		this.time = time;
		this.time_value = time_value;
	}
	
	public Countdown(Plugin plugin, TimeFetcher.Time time, int time_value, boolean firstDelay) {
		this.plugin = plugin;
		this.time = time;
		this.time_value = time_value + (!firstDelay ? 1 : 0);
		this.firstDelay = firstDelay;
	}
	
	public void start() {
		for(CountdownListener l : listener) {
			l.CountdownStartEvent();
		}
		
		this.runnableID = Bukkit.getScheduler().scheduleSyncRepeatingTask(this.plugin, new Runnable() {
			int timeLeft = (int) Countdown.this.getExactTime();
			
			@Override
			public void run() {
				timeLeft--;
				
				if(timeLeft <= 0) {
					end();
				} else {
					for(CountdownListener l : listener) {
						l.onTick(timeLeft);
					}
				}
				
			}
		}, firstDelay ? 20L : 0L, 20L);

		running = true;
	}
	
	public void end() throws IllegalPluginAccessException {
		running = false;

		for(CountdownListener l : listener) {
			l.CountdownEndEvent();
		}
		
		try {
			Bukkit.getScheduler().cancelTask(this.runnableID);
		} catch(IllegalPluginAccessException ex) {
			throw ex;
		}
	}
	
	public void cancel() {
		running = false;

		for(CountdownListener l : listener) {
			l.CountdownCancelEvent();
		}
		
		Bukkit.getScheduler().cancelTask(this.runnableID);
	}
	
	public long getExactTime() {
		if(this.time.equals(TimeFetcher.Time.TICKS)) return this.time_value * 20;
		else if(this.time.equals(TimeFetcher.Time.SECONDS)) return this.time_value;
		else if(this.time.equals(TimeFetcher.Time.MINUTES)) return this.time_value * 60;
		else if(this.time.equals(TimeFetcher.Time.HOURS)) return this.time_value * 60 * 60;
		else if(this.time.equals(TimeFetcher.Time.DAYS)) return this.time_value * 60 * 60 * 24;
		else return this.time_value;
	}
	
	public void addListener(CountdownListener countdownListener) {
		if(!this.listener.contains(countdownListener)) this.listener.add(countdownListener);
	}
	
	public void removeListener(CountdownListener countdownListener) {
		if(this.listener.contains(countdownListener)) {
			List<CountdownListener> listeners = new ArrayList<>();
			listeners.addAll(this.listener);
			
			listener.remove(countdownListener);
			
			this.listener = listeners;
		}
	}
	
	public List<CountdownListener> getListener() {
		return listener;
	}
	
	public TimeFetcher.Time getTime() {
		return time;
	}
	
	public int getTime_value() {
		return time_value;
	}
	
	public void setTime_value(int time_value) {
		this.time_value = time_value;
	}
	
	public void setTime(TimeFetcher.Time time) {
		this.time = time;
	}
	
	public boolean isFirstDelay() {
		return firstDelay;
	}

	public boolean isRunning() {
		return running;
	}
}
