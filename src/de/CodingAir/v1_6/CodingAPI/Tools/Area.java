package de.CodingAir.v1_6.CodingAPI.Tools;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;

import java.util.ArrayList;
import java.util.List;

public class Area {
	
	public static Boolean isInArea(Location target, Location edge1, Location edge2, boolean yCords){
		
		int minX = Math.min(edge1.getBlockX(), edge2.getBlockX());
		int minY = Math.min(edge1.getBlockY(), edge2.getBlockY());
		int minZ = Math.min(edge1.getBlockZ(), edge2.getBlockZ());

		int maxX = Math.max(edge1.getBlockX(), edge2.getBlockX());
		int maxY = Math.max(edge1.getBlockY(), edge2.getBlockY());
		int maxZ = Math.max(edge1.getBlockZ(), edge2.getBlockZ());
		
		int targetX = target.getBlockX();
		int targetZ = target.getBlockZ();
		int targetY = target.getBlockY();
		
		
		if(targetX >= minX && targetX <= maxX){
			if(targetZ >= minZ && targetZ <= maxZ){
				if(yCords){
					if(targetY >= minY && targetY <= maxY){
						return true;
					}
				} else {
					return true;
				}
			}
		}
		
		return false;
	}
	
	public static Integer getBlocksAmount(Location edge1, Location edge2, boolean yCords){
		
		int minX = Math.min(edge1.getBlockX(), edge2.getBlockX());
		int minY = Math.min(edge1.getBlockY(), edge2.getBlockY());
		int minZ = Math.min(edge1.getBlockZ(), edge2.getBlockZ());

		int maxX = Math.max(edge1.getBlockX(), edge2.getBlockX());
		int maxY = Math.max(edge1.getBlockY(), edge2.getBlockY());
		int maxZ = Math.max(edge1.getBlockZ(), edge2.getBlockZ());
		
		int amount = 0;
		
		for(int z = minZ; z <= maxZ; z++){
			for(int x = minX; x <= maxX; x++){
				if(yCords){
					for(int y = minY; y <= maxY; y++){
						amount++;
					}
				} else {
					amount++;
				}
			}
		}
		
		return amount;
		
	}
	
	public static List<Block> getBlocks(Location edge1, Location edge2, boolean yCords){
		List<Block> blockList = new ArrayList<Block>();
		
		Location loc = edge1.getBlock().getLocation().clone();
		
		int minX = Math.min(edge1.getBlockX(), edge2.getBlockX());
		int minY = Math.min(edge1.getBlockY(), edge2.getBlockY());
		int minZ = Math.min(edge1.getBlockZ(), edge2.getBlockZ());

		int maxX = Math.max(edge1.getBlockX(), edge2.getBlockX());
		int maxY = Math.max(edge1.getBlockY(), edge2.getBlockY());
		int maxZ = Math.max(edge1.getBlockZ(), edge2.getBlockZ());
		
		for(int z = minZ; z <= maxZ; z++){
			loc.setZ(z);
			for(int x = minX; x <= maxX; x++){
				loc.setX(x);
				
				if(yCords){
					for(int y = minY; y <= maxY; y++){
						loc.setY(y);

						blockList.add(loc.getBlock());
					}
				} else {
					loc.setY(minY);
					blockList.add(loc.getBlock());
				}
			}
			
		}
		
		return blockList;
	}
	
	
	public static Location getLocAroundFor(Block b, Material search){
		Location loc = b.getLocation();
		
		loc.setX(loc.getX()-1);
		
		if(loc.getBlock().getType() == search){
			return loc;
		}
		
		loc.setX(loc.getX()+2);
		
		if(loc.getBlock().getType() == search){
			return loc;
		}

		loc.setX(loc.getX()-1);
		loc.setZ(loc.getZ()-1);
		
		if(loc.getBlock().getType() == search){
			return loc;
		}
		
		loc.setZ(loc.getZ()+2);
		
		if(loc.getBlock().getType() == search){
			return loc;
		}
		
		return null;
	}
	
	
	public static List<Location> getLocsAroundFor(Block basic, Material search, int radius){
		List<Location> locations = new ArrayList<Location>();
		Location loc = basic.getLocation().clone();
		
		for(int i = 1; i <= radius; i++){
			Location f = loc.clone();
			f.setX(f.getX()-i);
			f.setY(f.getY()-i);
			f.setZ(f.getZ()-i);

			Location s = loc.clone();
			s.setX(s.getX()+i);
			s.setY(s.getY()+i);
			s.setZ(s.getZ()+i);
			
			List<Block> blocks = Area.getBlocks(f, s, false);
			
			for(Block b : blocks){
				if(!b.getLocation().equals(basic.getLocation())){
					if(b.getType().equals(search)){
						locations.add(b.getLocation());
					}
				}
			}
		}
		
		return locations;
	}
}
