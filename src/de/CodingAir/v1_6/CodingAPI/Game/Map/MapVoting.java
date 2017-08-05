package de.CodingAir.v1_6.CodingAPI.Game.Map;

import de.CodingAir.v1_6.CodingAPI.Time.Countdown;
import de.CodingAir.v1_6.CodingAPI.Time.CountdownListener;
import de.CodingAir.v1_6.CodingAPI.Time.TimeFetcher;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public abstract class MapVoting {
	private HashMap<Map, Integer> maps = new HashMap<>();
	private List<Player> voters = new ArrayList<>();
	private int time;
	private Countdown timer;
	private Plugin plugin;
	
	public MapVoting(List<Map> maps, int time, Plugin plugin) {
		this.plugin = plugin;
		
		Map[] voting = this.getRandomMaps(maps);
		if(voting[0] != null) this.maps.put(voting[0], 0);
		if(voting[1] != null) this.maps.put(voting[1], 0);
		if(voting[2] != null) this.maps.put(voting[2], 0);
		
		this.time = time;
		
		this.timer = new Countdown(plugin, TimeFetcher.Time.SECONDS, this.time);
		
		this.timer.addListener(new CountdownListener() {
			@Override
			protected void CountdownStartEvent() {
				MapVoting.this.onStart();
			}
			
			@Override
			protected void CountdownEndEvent() {
				MapVoting.this.evaluate();
			}
			
			@Override
			protected void CountdownCancelEvent() {
				MapVoting.this.onCancel();
			}
			
			@Override
			protected void onTick(int timeLeft) {
				MapVoting.this.onTick(timeLeft);
			}
		});
	}
	
	public void start() {
		this.timer.start();
	}
	
	public void cancel() {
		this.timer.cancel();
	}
	
	public void evaluate() {
		if(this.maps.size() == 0) {
			this.onEvaluate(null);
			return;
		}
		
		Map winner = null;
		for(Map map : this.maps.keySet()) {
			if(winner == null) winner = map;
			else if(this.maps.get(winner) < this.maps.get(map)) {
				winner = map;
			}
		}
		
		this.onEvaluate(winner);
	}
	
	private Map[] getRandomMaps(List<Map> maps) {
		Map[] voting = new Map[3];
		
		if(maps.size() >= 3) {
			voting[0] = maps.get((int) (Math.random() * maps.size()));
			voting[1] = maps.get((int) (Math.random() * maps.size()));
			
			while(voting[0].getName().equals(voting[1].getName())) {
				voting[1] = maps.get((int) (Math.random() * maps.size()));
			}
			
			voting[2] = maps.get((int) (Math.random() * maps.size()));
			
			while(voting[0].getName().equals(voting[2].getName()) || voting[1].getName().equals(voting[2].getName())) {
				voting[2] = maps.get((int) (Math.random() * maps.size()));
			}
			
		} else if(maps.size() == 2) {
			voting[0] = maps.get(0);
			voting[1] = maps.get(1);
			voting[2] = null;
		} else if(maps.size() == 1) {
			voting[0] = maps.get(0);
			voting[1] = null;
			voting[2] = null;
		} else {
			voting[0] = null;
			voting[1] = null;
			voting[2] = null;
		}
		
		return voting;
	}
	
	public Map[] getMaps() {
		Map[] voting = new Map[3];
		
		int i = 0;
		for(Map map : this.maps.keySet()) {
			voting[i] = map;
			i++;
		}
		
		return voting;
	}
	
	public boolean vote(Player p, Map vote) {
		if(this.voters.contains(p)) return false;
		this.voters.add(p);
		
		Map map = null;
		
		for(Map maps : this.maps.keySet()) {
			if(maps.getName().equals(vote.getName())) map = maps;
		}
		
		if(map != null) this.maps.replace(map, this.maps.get(map) + 1);
		
		return true;
	}
	
	public int getVotes(Map map) {
		for(Map maps : this.maps.keySet()) {
			if(maps.getName().equals(map.getName())) return this.maps.get(maps);
		}
		
		return -1;
	}
	
	public int getTime() {
		return time;
	}
	
	public void setTime(int time) {
		this.time = time;
		this.timer.setTime_value(time);
	}
	
	public abstract void onStart();
	
	public abstract void onCancel();
	
	public abstract void onTick(int timeLeft);
	
	public abstract void onEvaluate(Map map);
}
