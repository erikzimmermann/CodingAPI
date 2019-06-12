package de.codingair.codingapi.tools;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.LivingEntity;

import java.util.ArrayList;
import java.util.List;

public class Area {
    
    public static boolean isInBlock(LivingEntity entity, Block b) {
        return isInBlock(entity, entity.getLocation(), b);
    }
    
    public static boolean isInBlock(LivingEntity entity, Location target, Block b) {
        Location middle = b.getLocation().clone().add(0.5, 0.5, 0.5);

        Location eye = target.clone().add(0, entity.getEyeHeight(), 0);
        if(eye.getY() >= middle.getY() - 0.5 && target.getY() <= middle.getY() + 0.5) {
            //Ist in y ebene

            if(target.getX() + 0.3 >= middle.getX() - 0.5 && target.getX() - 0.3 <= middle.getX() + 0.5) {
                //Ist in x ebene

                if(target.getZ() + 0.3 >= middle.getZ() - 0.5 && target.getZ() - 0.3 <= middle.getZ() + 0.5) {
                    //Ist in z ebene
                    return true;
                }
            }
        }

        return false;
    }

    public static boolean isInArea(LivingEntity entity, Location a, Location b) {
        return isInArea(entity, entity.getLocation(), a, b);
    }

    public static boolean isInArea(LivingEntity entity, Location target, Location a, Location b) {
        if(!entity.getWorld().equals(a.getWorld()) || !a.getWorld().equals(b.getWorld())) return false;

        double height = Math.abs(b.getY() - a.getY());
        double xWidth = Math.abs(b.getX() - a.getX());
        double zWidth = Math.abs(b.getZ() - a.getZ());

        Location middle = a.clone().add(b.toVector().subtract(a.toVector()).multiply(0.5));

        Location eye = target.clone().add(0, entity.getEyeHeight(), 0);
        
        if(eye.getY() >= middle.getY() - height / 2 && target.getY() <= middle.getY() + height / 2) {
            //Ist in y ebene

            if(target.getX() + 0.3 >= middle.getX() - xWidth / 2 && target.getX() - 0.3 <= middle.getX() + xWidth / 2) {
                //Ist in x ebene

                if(target.getZ() + 0.3 >= middle.getZ() - zWidth / 2 && target.getZ() - 0.3 <= middle.getZ() + zWidth / 2) {
                    //Ist in z ebene
                    return true;
                }
            }
        }

        return false;
    }

    public static boolean isInArea(Location target, Location mid, int radius, boolean yCords, double puffer) {
        Location e0 = mid.clone().subtract(radius, radius, radius);
        Location e1 = mid.clone().add(radius, radius, radius);
        return isInArea(target, e0, e1, yCords, puffer);
    }

    public static boolean isInArea(Location target, Location edge1, Location edge2, boolean yCords, double puffer) {
        if(!target.getWorld().equals(edge1.getWorld()) || !edge1.getWorld().equals(edge2.getWorld())) return false;

        double minX = Math.min(edge1.getX(), edge2.getX()) - puffer;
        double minY = Math.min(edge1.getY(), edge2.getY()) - puffer;
        double minZ = Math.min(edge1.getZ(), edge2.getZ()) - puffer;

        double maxX = Math.max(edge1.getX(), edge2.getX()) + puffer;
        double maxY = Math.max(edge1.getY(), edge2.getY()) + puffer;
        double maxZ = Math.max(edge1.getZ(), edge2.getZ()) + puffer;

        double targetX = target.getX();
        double targetY = target.getY();
        double targetZ = target.getZ();

        if(targetX >= minX && targetX <= maxX) {
            if(targetZ >= minZ && targetZ <= maxZ) {
                if(yCords) {
                    return targetY >= minY && targetY <= maxY;
                } else {
                    return true;
                }
            }
        }

        return false;
    }

    public static boolean isInArea(Location target, Location edge1, Location edge2, boolean yCords) {
        return isInArea(target, edge1, edge2, yCords, 0D);
    }

    public static int getBlocksAmount(Location edge1, Location edge2, boolean yCords) {

        int minX = Math.min(edge1.getBlockX(), edge2.getBlockX());
        int minY = Math.min(edge1.getBlockY(), edge2.getBlockY());
        int minZ = Math.min(edge1.getBlockZ(), edge2.getBlockZ());

        int maxX = Math.max(edge1.getBlockX(), edge2.getBlockX());
        int maxY = Math.max(edge1.getBlockY(), edge2.getBlockY());
        int maxZ = Math.max(edge1.getBlockZ(), edge2.getBlockZ());

        int amount = 0;

        for(int z = minZ; z <= maxZ; z++) {
            for(int x = minX; x <= maxX; x++) {
                if(yCords) {
                    for(int y = minY; y <= maxY; y++) {
                        amount++;
                    }
                } else {
                    amount++;
                }
            }
        }

        return amount;

    }

    public static List<Block> getBlocks(Location edge1, Location edge2, boolean yCords) {
        List<Block> blockList = new ArrayList<Block>();

        Location loc = edge1.getBlock().getLocation().clone();

        int minX = Math.min(edge1.getBlockX(), edge2.getBlockX());
        int minY = Math.min(edge1.getBlockY(), edge2.getBlockY());
        int minZ = Math.min(edge1.getBlockZ(), edge2.getBlockZ());

        int maxX = Math.max(edge1.getBlockX(), edge2.getBlockX());
        int maxY = Math.max(edge1.getBlockY(), edge2.getBlockY());
        int maxZ = Math.max(edge1.getBlockZ(), edge2.getBlockZ());

        for(int z = minZ; z <= maxZ; z++) {
            loc.setZ(z);
            for(int x = minX; x <= maxX; x++) {
                loc.setX(x);

                if(yCords) {
                    for(int y = minY; y <= maxY; y++) {
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


    public static Location getLocAroundFor(Block b, Material search) {
        Location loc = b.getLocation();

        loc.setX(loc.getX() - 1);

        if(loc.getBlock().getType() == search) {
            return loc;
        }

        loc.setX(loc.getX() + 2);

        if(loc.getBlock().getType() == search) {
            return loc;
        }

        loc.setX(loc.getX() - 1);
        loc.setZ(loc.getZ() - 1);

        if(loc.getBlock().getType() == search) {
            return loc;
        }

        loc.setZ(loc.getZ() + 2);

        if(loc.getBlock().getType() == search) {
            return loc;
        }

        return null;
    }


    public static List<Location> getLocsAroundFor(Block basic, Material search, int radius) {
        List<Location> locations = new ArrayList<Location>();
        Location loc = basic.getLocation().clone();

        for(int i = 1; i <= radius; i++) {
            Location f = loc.clone();
            f.setX(f.getX() - i);
            f.setY(f.getY() - i);
            f.setZ(f.getZ() - i);

            Location s = loc.clone();
            s.setX(s.getX() + i);
            s.setY(s.getY() + i);
            s.setZ(s.getZ() + i);

            List<Block> blocks = Area.getBlocks(f, s, false);

            for(Block b : blocks) {
                if(!b.getLocation().equals(basic.getLocation())) {
                    if(b.getType().equals(search)) {
                        locations.add(b.getLocation());
                    }
                }
            }
        }

        return locations;
    }
}
