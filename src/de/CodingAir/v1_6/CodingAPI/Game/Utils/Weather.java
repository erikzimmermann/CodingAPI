package de.CodingAir.v1_6.CodingAPI.Game.Utils;

import org.bukkit.Bukkit;
import org.bukkit.World;

/**
 * Removing of this disclaimer is forbidden.
 *
 * @author CodingAir
 * @verions: 1.0.0
 **/

public class Weather {
	private boolean stormy = false;
	private int time = 6000;
	
	public Weather(boolean stormy, int time) {
		this.stormy = stormy;
		this.time = time;
	}
	
	public Weather(boolean stormy, Time time) {
		this.stormy = stormy;
		this.time = time.getTime();
	}
	
	public Weather() {
	}
	
	public boolean isStormy() {
		return stormy;
	}
	
	public int getTime() {
		return time;
	}
	
	public void setStormy(boolean stormy) {
		this.stormy = stormy;
	}
	
	public void setTime(Time time) {
		this.time = time.getTime();
	}
	
	public void setTime(int time) {
		this.time = time;
	}
	
	public void setup(World world) {
		if(world == null) {
			for(World w : Bukkit.getWorlds()) {
				w.setTime(this.time);
				w.setStorm(this.stormy);
			}
		} else {
			world.setTime(this.time);
			world.setStorm(this.stormy);
		}
	}
	
	public enum Time {
		MORNING(0),
		LUNCHTIME(6000),
		AFTERNOON(9000),
		EVENING(12000),
		NIGHT(18000);
		
		private int time;
		
		Time(int time) {
			this.time = time;
		}
		
		public int getTime() {
			return time;
		}
	}
}
