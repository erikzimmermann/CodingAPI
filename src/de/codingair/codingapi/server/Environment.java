package de.codingair.codingapi.server;

import de.codingair.codingapi.server.reflections.IReflection;
import de.codingair.codingapi.server.reflections.PacketUtils;
import net.minecraft.server.v1_14_R1.MathHelper;
import org.bukkit.Color;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.material.MaterialData;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Environment {

    public static void playRandomFireworkEffect(Location loc) {
        Random r = new Random();
        int rt = r.nextInt(2) + 1;
        FireworkEffect.Type type = FireworkEffect.Type.BALL;
        if(rt == 1) type = FireworkEffect.Type.BALL;
        if(rt == 2) type = FireworkEffect.Type.BALL_LARGE;

        int r1i = r.nextInt(17) + 1;
        int r2i = r.nextInt(17) + 1;
        org.bukkit.Color c1 = getColor(r1i);
        org.bukkit.Color c2 = getColor(r2i);

        FireworkEffect effect = FireworkEffect.builder().flicker(r.nextBoolean()).withColor(c1).withFade(c2).with(type).trail(r.nextBoolean()).build();

        Firework fw;

        try {
            fw = (Firework) loc.getWorld().spawnEntity(loc, EntityType.FIREWORK);
        } catch(Exception ex) {
            return;
        }

        FireworkMeta fwm = fw.getFireworkMeta();
        fwm.setPower(1);

        fwm.addEffect(effect);

        fw.setFireworkMeta(fwm);
    }

    private static org.bukkit.Color getColor(int i) {
        org.bukkit.Color c = null;

        if(i == 1) c = org.bukkit.Color.AQUA;
        if(i == 2) c = org.bukkit.Color.BLACK;
        if(i == 3) c = org.bukkit.Color.BLUE;
        if(i == 4) c = org.bukkit.Color.FUCHSIA;
        if(i == 5) c = org.bukkit.Color.GRAY;
        if(i == 6) c = org.bukkit.Color.GREEN;
        if(i == 7) c = org.bukkit.Color.LIME;
        if(i == 8) c = org.bukkit.Color.MAROON;
        if(i == 9) c = org.bukkit.Color.NAVY;
        if(i == 10) c = org.bukkit.Color.OLIVE;
        if(i == 11) c = org.bukkit.Color.ORANGE;
        if(i == 12) c = org.bukkit.Color.PURPLE;
        if(i == 13) c = org.bukkit.Color.RED;
        if(i == 14) c = org.bukkit.Color.SILVER;
        if(i == 15) c = org.bukkit.Color.TEAL;
        if(i == 16) c = org.bukkit.Color.WHITE;
        if(i == 17) c = Color.YELLOW;

        return c;
    }

    public static void dropItem(ItemStack item, Player p) {
        if(item == null || item.getType().equals(Material.AIR)) return;

        Location loc = p.getLocation();
        loc.setY(loc.getY() + 1.32);

        Item drop = p.getWorld().dropItemNaturally(loc, item);

        Vector v = p.getEyeLocation().getDirection();
        v.multiply(0.33);
        v.setY(v.getY() + 0.1);

        drop.setVelocity(v);
        drop.setPickupDelay(40);
    }

    public static boolean isBlock(Block block) {
        return block != null && !block.getType().isTransparent() && block.getType().isSolid() && !block.getType().equals(Material.SIGN) && !block.getType().equals(Material.AIR);
    }

    public static List<Chunk> getChunks(Location mid, int chunkRadius) {
        List<Chunk> chunks = new ArrayList<>();
        if(chunkRadius <= 0) return chunks;

        int startX = floor(mid.getBlockX() / 16D);
        int startZ = floor(mid.getBlockZ() / 16D);
        chunkRadius--;

        for(int z = startZ - chunkRadius; z <= startZ + chunkRadius; z++) {
            for(int x = startX - chunkRadius; x <= startX + chunkRadius; x++) {
                chunks.add(mid.getWorld().getChunkAt(x, z));
            }
        }

        return chunks;
    }

    private static int floor(double var0) {
        int var2 = (int)var0;
        return var0 < (double)var2 ? var2 - 1 : var2;
    }

    public static boolean isSlab(Block block) {
        if(block == null) return false;

        switch(block.getType()) {
            case STONE_SLAB2:
                return true;
            case DOUBLE_STONE_SLAB2:
                return true;
            case STEP:
                return true;
            case DOUBLE_STEP:
                return true;
            case WOOD_DOUBLE_STEP:
                return true;
            case WOOD_STEP:
                return true;
            case PURPUR_SLAB:
                return true;
            default:
                return false;
        }
    }

    public static double getBlockHeight(Block block) {
        if(block == null) return 0;

        switch(block.getType()) {
            case STONE_SLAB2:
                return 0.5;
            case DOUBLE_STONE_SLAB2:
                return 0.5;
            case STEP:
                return 0.5;
            case DOUBLE_STEP:
                return 0.5;
            case WOOD_DOUBLE_STEP:
                return 0.5;
            case WOOD_STEP:
                return 0.5;
            case PURPUR_SLAB:
                return 0.5;
            default:
                return 1;
        }
    }

    public static void interact(Block b) {
        //noinspection deprecation
        byte data = b.getData();
        byte newData;

        switch(b.getType()) {
            case DARK_OAK_DOOR:
            case ACACIA_DOOR:
            case BIRCH_DOOR:
            case IRON_DOOR_BLOCK:
            case JUNGLE_DOOR:
            case SPRUCE_DOOR:
            case WOODEN_DOOR: {
                if(data < 4) newData = (byte) (data + 4);
                else if(data >= 4 && data <= 7) newData = (byte) (data - 4);
                else {
                    Block other = b.getLocation().add(0, -1, 0).getBlock();
                    interact(other);
                    return;
                }
                break;
            }

            case TRAP_DOOR:
            case IRON_TRAPDOOR: {
                if(data == 0 || data == 1 || data == 2 || data == 3 || data == 8 || data == 9 || data == 10 || data == 11)
                    newData = (byte) (data + 4);
                else newData = (byte) (data - 4);
                break;
            }

            case WOOD_BUTTON:
            case STONE_BUTTON:
            case LEVER: {
                if(data < 8)
                    newData = (byte) (data + 8);
                else newData = (byte) (data - 8);
                break;
            }

            case BIRCH_FENCE_GATE:
            case DARK_OAK_FENCE_GATE:
            case JUNGLE_FENCE_GATE:
            case SPRUCE_FENCE_GATE:
            case ACACIA_FENCE_GATE:
            case FENCE_GATE: {
                if(data < 4)
                    newData = (byte) (data + 4);
                else newData = (byte) (data - 4);
                break;
            }

            default: {
                return;
            }
        }

        //noinspection deprecation
        b.setData(newData);
    }

    public static boolean isPassableDoor(Block b) {
        switch(b.getType()) {
            case TRAP_DOOR:
            case IRON_TRAPDOOR:
            case FENCE_GATE:
            case DARK_OAK_DOOR:
            case ACACIA_DOOR:
            case BIRCH_DOOR:
            case IRON_DOOR_BLOCK:
            case JUNGLE_DOOR:
            case SPRUCE_DOOR:
            case WOODEN_DOOR:
                return true;
            default:
                return false;
        }
    }

    public static List<Block> getNearbyBlocks(Location location, int radius, boolean yAxis) {
        List<Block> blocks = new ArrayList<>();

        for(int x = location.getBlockX() - radius; x <= location.getBlockX() + radius; x++) {
            for(int z = location.getBlockZ() - radius; z <= location.getBlockZ() + radius; z++) {
                if(yAxis) {
                    for(int y = location.getBlockY() - radius; y <= location.getBlockY() + radius; y++) {
                        blocks.add(location.getWorld().getBlockAt(x, y, z));
                    }
                } else {
                    blocks.add(location.getWorld().getBlockAt(x, location.getBlockY(), z));
                }
            }
        }

        return blocks;
    }

    public static List<Block> getCylinder(Location center, int radius, int height, boolean hollow) {
        List<Block> circle = new ArrayList<>();

        int bX = center.getBlockX(), bY = center.getBlockY(), bZ = center.getBlockZ();

        for(int x = bX - radius; x <= bX + radius; x++) {
            for(int y = bY - height; y <= bY + height; y++) {
                for(int z = bZ - radius; z <= bZ + radius; z++) {
                    double distance = Math.sqrt(Math.pow(bX - x, 2) + Math.pow(bY - y, 2) + Math.pow(bZ - z, 2));

                    if(distance < radius && !(hollow && distance < (radius - 1))) {
                        circle.add(new Location(center.getWorld(), x, y, z).getBlock());
                    }
                }
            }
        }

        return circle;
    }

    public static Block getNextBottomBlock(Location location) {
        while(location.getBlock() == null || !isBlock(location.getBlock())) {
            location.setY(location.getY() - 1);
            if(location.getY() <= 0) return null;
        }

        return location.getBlock();
    }

    public static Block getNextTopBlock(Location location, int max) {
        while(location.getBlock() == null || !isBlock(location.getBlock())) {
            location.setY(location.getY() + 1);
            if(location.getY() >= 255) return null;
        }

        return location.getBlock();
    }

    public static Block getHeighestBlock(Location location, int max, boolean onlySolid) {
        location.setY(max);

        while((onlySolid || location.getBlock().getType() == Material.AIR) && (!onlySolid || !location.getBlock().getType().isSolid())) {
            location.setY(location.getY() - 1);
        }

        return location.getBlock();
    }

    public static Block getNextBottomBlock(Location location, boolean ignoreTransparency) {
        if(!ignoreTransparency) return getNextBottomBlock(location);

        Block b;
        while((b = getNextBottomBlock(location)) != null && !b.getType().isOccluding()) {
            location.setY(location.getY() - 1);
        }

        return b;
    }

    public static Block getNextTopBlock(Location location, int max, boolean ignoreTransparency) {
        if(!ignoreTransparency) return getNextTopBlock(location, max);

        Block b;
        while((b = getNextTopBlock(location, max)) != null && !b.getType().isOccluding()) {
            location.setY(location.getY() + 1);
        }

        return location.getBlock();
    }

    public static Object spawnNonSolidFallingBlock(Location location, MaterialData data) {
        Object iData = PacketUtils.getIBlockData(data);
        IReflection.ConstructorAccessor fallingBlockCon = IReflection.getConstructor(PacketUtils.EntityFallingBlockClass, PacketUtils.WorldClass, double.class, double.class, double.class, PacketUtils.IBlockDataClass);

        Object fallingBlock = fallingBlockCon.newInstance(PacketUtils.getWorldServer(), location.getX(), location.getY(), location.getZ(), iData);
        PacketUtils.EntityPackets.spawnEntity(fallingBlock, 70, PacketUtils.getCombinedId(data.getItemTypeId(), data.getData()), Bukkit.getOnlinePlayers().toArray(new Player[Bukkit.getOnlinePlayers().size()]));

        return fallingBlock;
    }

    public static List<Block> getBlocksBetween(Location loc, Location target, boolean ignoreTransparency) {
        List<Block> blocks = new ArrayList<>();

        Vector v = target.toVector().subtract(loc.toVector());
        Location c = loc.clone();

        double originalLength = v.length();
        double length = v.normalize().length();

        for(double i = 0; i <= originalLength; i += length) {
            if(!blocks.contains(c.getBlock()) && isBlock(c.getBlock())) {
                if(ignoreTransparency && (!c.getBlock().getType().isSolid() || c.getBlock().getType().isTransparent()))
                    continue;
                blocks.add(c.getBlock());
            }

            c.add(v);
        }

        return blocks;
    }

    public static Location getInvisibleLocation(Player p) {
        return getInvisibleLocation(p, 32);
    }

    public static Location getInvisibleLocation(Player p, double multiply) {
        Location loc = p.getLocation().clone();
        return loc.getDirection().multiply(multiply).add(loc.toVector()).toLocation(p.getWorld());
    }

    public static void openChest(Location loc, Player... players) {
        if(loc.getBlock() == null || !loc.getBlock().getType().equals(Material.CHEST)) return;

        Object pos = PacketUtils.getBlockPosition(loc);
        IReflection.ConstructorAccessor con = IReflection.getConstructor(PacketUtils.PacketPlayOutBlockActionClass, PacketUtils.BlockPositionClass, PacketUtils.BlockClass, int.class, int.class);
        Object packet = con.newInstance(pos, PacketUtils.Blocks.findByName("CHEST"), 1, 1);

        if(players.length == 0) PacketUtils.sendPacketToAll(packet);
        else PacketUtils.sendPacket(packet, players);
    }

    public static void closeChest(Location loc, Player... players) {
        if(loc.getBlock() == null || !loc.getBlock().getType().equals(Material.CHEST)) return;

        Object pos = PacketUtils.getBlockPosition(loc);
        IReflection.ConstructorAccessor con = IReflection.getConstructor(PacketUtils.PacketPlayOutBlockActionClass, PacketUtils.BlockPositionClass, PacketUtils.BlockClass, int.class, int.class);
        Object packet = con.newInstance(pos, PacketUtils.Blocks.findByName("CHEST"), 1, 0);

        if(players.length == 0) PacketUtils.sendPacketToAll(packet);
        else PacketUtils.sendPacket(packet, players);
    }
}
