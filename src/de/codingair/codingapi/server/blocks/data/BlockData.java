package de.codingair.codingapi.server.blocks.data;

import de.codingair.codingapi.server.Version;
import de.codingair.codingapi.server.reflections.IReflection;
import org.bukkit.Material;
import org.bukkit.block.Block;

public abstract class BlockData {
    public abstract Object getData(Block block);

    public void setDataTo(Block block, Object obj) {
        if(Version.getVersion().isBiggerThan(Version.v1_12)) {
            Class<?> blockData = IReflection.getClass(IReflection.ServerPacket.BUKKIT_PACKET, "block.data.BlockData");
            IReflection.MethodAccessor setBlockData = IReflection.getMethod(Block.class, "setBlockData", new Class[]{blockData});

            setBlockData.invoke(block, obj);
        } else {
            IReflection.MethodAccessor setData = IReflection.getMethod(Block.class, "setData", new Class[]{byte.class});
            setData.invoke(block, obj);
        }
    }

    public void setTypeAndDataTo(Block block, Material material, byte data, boolean applyPhysics) {
        if(Version.getVersion().isBiggerThan(Version.v1_12)) throw new IllegalStateException("setTypeIdAndData() cannot be set in v1.13+");

        IReflection.MethodAccessor setTypeIdAndData = IReflection.getMethod(Block.class, "setTypeIdAndData", new Class[]{int.class, byte.class, boolean.class});
        setTypeIdAndData.invoke(block, getTypeId(material), data, applyPhysics);
    }

    public int getTypeId(Material material) {
        if(Version.getVersion().isBiggerThan(Version.v1_12)) throw new IllegalStateException("getTypeId() cannot be set in v1.13+");

        IReflection.MethodAccessor getId = IReflection.getMethod(Material.class, "getId", int.class, new Class[]{});
        return (int) getId.invoke(material);
    }

    public Object getFrom(Block block) {
        if(Version.getVersion().isBiggerThan(Version.v1_12)) {
            Class<?> blockData = IReflection.getClass(IReflection.ServerPacket.BUKKIT_PACKET, "block.data.BlockData");
            IReflection.MethodAccessor getBlockData = IReflection.getMethod(Block.class, "getBlockData", blockData, new Class[]{});

            return getBlockData.invoke(block);
        } else {
            IReflection.MethodAccessor getData = IReflection.getMethod(Block.class, "getData", byte.class, new Class[]{});
            return getData.invoke(block);
        }
    }
}
