package de.codingair.codingapi.server.particlemessages;

import de.codingair.codingapi.particles.Particle;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * Removing of this disclaimer is forbidden.
 *
 * @author BubbleEgg
 * @verions: 1.0.0
 **/

public class ParticleMessage {
	
	private Plugin plugin;
	private String message;
	private Particle particle;
	private ParticleFont font;
	private Player player;
	private ParticleChar.Direction direction;
	private de.codingair.codingapi.tools.Location loc;
	private int scheduler;
	private double height, length;
	
	
	public ParticleMessage(Plugin plugin, String message, Particle particle, de.codingair.codingapi.tools.Location loc, double height, double length, ParticleFont font, ParticleChar.Direction direction) {
		this.message = message;
		this.particle = particle;
		this.loc = loc;
		this.height = height;
		this.length = length;
		this.plugin = plugin;
		this.font = font;
		this.direction = direction;
	}
	
	public void show(Player player) {
		this.player = player;
		show();
	}
	
	public void show() {
		
		ParticleMessage pm = this;
		
		scheduler = Bukkit.getScheduler().scheduleSyncRepeatingTask(this.plugin, new BukkitRunnable() {
			@Override
			public void run() {
				double nextLoc = 0;
				char Char = ' ';
				for(int i = 0; i < message.length(); i++) {
					Character c = message.charAt(i);
					if(i != 0) {
						nextLoc = nextLoc + (pm.length*1.25) * ParticleChar.getCharLength(Char, pm.font);
					}
					switch(pm.direction) {
						case north: {
							Spawn(c, loc.getX(), loc.getY(), loc.getZ() - nextLoc);
							break;
						}
						case east: {
							Spawn(c, loc.getX() + nextLoc, loc.getY(), loc.getZ());
							break;
						}
						case south: {
							Spawn(c, loc.getX(), loc.getY(), loc.getZ() + nextLoc);
							break;
						}
						case west: {
							Spawn(c, loc.getX() - nextLoc, loc.getY(), loc.getZ());
							break;
						}
					}
					Char = c;
				}
			}
		}, 0, 10);
	}
	
	public void hide() {
		try {
			Bukkit.getScheduler().cancelTask(scheduler);
		} catch(Exception e) {
		
		}
	}
	
	private void Spawn(Character c, double x, double y, double z) {
		ParticleChar Char = new ParticleChar().setChar(c, this.direction, font, height, length);
		
		de.codingair.codingapi.tools.Location loc = new de.codingair.codingapi.tools.Location(new Location(this.loc.getWorld(), x, y, z));
		Char.spawnPoints(this.particle, loc, this.player);
	}
	
	public static ParticleChar.Direction getPlayerDirection(Player player) {
		ParticleChar.Direction dir;
		float y = player.getLocation().getYaw();
		if(y < 0) {
			y += 360;
		}
		y %= 360;
		int i = (int) ((y + 8) / 22.5);
		if(i == 0) {
			dir = ParticleChar.Direction.west;
		} else if(i == 1) {
			dir = ParticleChar.Direction.west;
		} else if(i == 2) {
			dir = ParticleChar.Direction.north;
		} else if(i == 3) {
			dir = ParticleChar.Direction.north;
		} else if(i == 4) {
			dir = ParticleChar.Direction.north;
		} else if(i == 5) {
			dir = ParticleChar.Direction.north;
		} else if(i == 6) {
			dir = ParticleChar.Direction.east;
		} else if(i == 7) {
			dir = ParticleChar.Direction.east;
		} else if(i == 8) {
			dir = ParticleChar.Direction.east;
		} else if(i == 9) {
			dir = ParticleChar.Direction.east;
		} else if(i == 10) {
			dir = ParticleChar.Direction.south;
		} else if(i == 11) {
			dir = ParticleChar.Direction.south;
		} else if(i == 12) {
			dir = ParticleChar.Direction.south;
		} else if(i == 13) {
			dir = ParticleChar.Direction.south;
		} else if(i == 14) {
			dir = ParticleChar.Direction.west;
		} else if(i == 15) {
			dir = ParticleChar.Direction.west;
		} else {
			dir = ParticleChar.Direction.west;
		}
		return dir;
	}
	
}
