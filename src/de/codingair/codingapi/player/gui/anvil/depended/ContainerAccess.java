package de.codingair.codingapi.player.gui.anvil.depended;

import net.minecraft.server.v1_14_R1.BlockPosition;
import net.minecraft.server.v1_14_R1.World;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;

public class ContainerAccess implements net.minecraft.server.v1_14_R1.ContainerAccess {
    private Player player;
    private Object world, blockPosition;

    public ContainerAccess(Player player, Object world, Object blockPosition) {
        this.player = player;
        this.world = world;
        this.blockPosition = blockPosition;
    }

    @Override
    public <T> Optional<T> a(BiFunction<World, BlockPosition, T> biFunction) {
        return Optional.empty();
    }

    @Override
    public World getWorld() {
        return (World) world;
    }

    @Override
    public BlockPosition getPosition() {
        return (BlockPosition) blockPosition;
    }

    @Override
    public Location getLocation() {
        return player.getLocation();
    }

    @Override
    public <T> T a(BiFunction<World, BlockPosition, T> bifunction, T t0) {
        return null;
    }

    @Override
    public void a(BiConsumer<World, BlockPosition> biconsumer) {

    }
}
