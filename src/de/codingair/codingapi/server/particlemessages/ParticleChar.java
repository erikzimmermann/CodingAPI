package de.codingair.codingapi.server.particlemessages;

import de.codingair.codingapi.tools.Location;
import de.codingair.codingapi.particles.Particle;
import de.codingair.codingapi.particles.ParticlePacket;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

/**
 * Removing of this disclaimer is forbidden.
 *
 * @author BubbleEgg
 * @verions: 1.0.0
 **/

public class ParticleChar {
	
	private List<Point> points = new ArrayList<>();
	private ParticleFont font;
	private double height, length;
	
	
	public ParticleChar setChar(char c, Direction d, ParticleFont font, double height, double length) {
		this.height = height;
		this.length = length;
		this.font = font;
		
		setPoint(d, this.font.getChar(c));
		
		return this;
	}
	
	private void setPoint(Direction d, FontChar fontChar) {
		
		String[] points = fontChar.lines.toArray(new String[fontChar.lines.size()]);
		
		for(int e = 0; e < points.length; e++) {
			String point = points[e];
			int f = point.length() - 1;
			for(int i = 0; i < point.length(); i++) {
				if((point.charAt(i) + "").equalsIgnoreCase("X")) {
					Point p = new Point();
					switch(d) {
						case north: {
							p.setZ(length / point.length() * i);
							p.setY(height / points.length * e);
							break;
						}
						case east: {
							p.setX(length / point.length() * f);
							p.setY(height / points.length * e);
							break;
						}
						case south: {
							p.setZ(length / point.length() * f);
							p.setY(height / points.length * e);
							break;
						}
						case west: {
							p.setX(length / point.length() * i);
							break;
						}
					}
					this.points.add(p);
				}
				f--;
			}
		}
	}
	
	public static double getCharLength(char c, ParticleFont font) {
		return font.getChar(c).length;
	}
	
	public static double getCharHeigth(char c, ParticleFont font) {
		return font.getChar(c).height;
	}
	
	public void spawnPoints(Particle p, Location loc, Player player) {
		for(Point point : points) {
			Location pLoc = loc.clone();
			pLoc = Location.getByLocation(pLoc.subtract(point.x, point.y, point.z));
			
			ParticlePacket packet = new ParticlePacket(p);
			packet.setLongDistance(true);
			packet.initialize(pLoc);
			
			if(player == null) {
				packet.send(Bukkit.getOnlinePlayers().toArray(new Player[Bukkit.getOnlinePlayers().size()]));
			} else {
				packet.send(player);
			}
		}
	}
	
	public double getHeight() {
		return height;
	}
	
	public void setHeight(double height) {
		this.height = height;
	}
	
	public double getLength() {
		return length;
	}
	
	public void setLength(double length) {
		this.length = length;
	}
	
	
	public enum Direction {
		north,
		east,
		south,
		west
	}
	
	
	public class Point {
		
		public double x = 0;
		public double y = 0;
		public double z = 0;
		
		
		public double getX() {
			return x;
		}
		
		public void setX(double x) {
			this.x = x;
		}
		
		public double getY() {
			return y;
		}
		
		public void setY(double y) {
			this.y = y;
		}
		
		public double getZ() {
			return z;
		}
		
		public void setZ(double z) {
			this.z = z;
		}
	}
	
	
}
