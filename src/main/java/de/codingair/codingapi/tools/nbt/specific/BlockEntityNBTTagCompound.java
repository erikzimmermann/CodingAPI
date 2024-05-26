package de.codingair.codingapi.tools.nbt.specific;

import de.codingair.codingapi.server.reflections.IReflection;
import de.codingair.codingapi.server.reflections.PacketUtils;
import de.codingair.codingapi.tools.nbt.NBTTagCompound;

public class BlockEntityNBTTagCompound extends NBTTagCompound {
    private static final Class<?> CRAFT_BLOCK_ENTITY_STATE;
    private static final IReflection.FieldAccessor<?> TILE_ENTITY;
    private static final IReflection.MethodAccessor SAVE;

    static {
        CRAFT_BLOCK_ENTITY_STATE = IReflection.getClass(IReflection.ServerPacket.CRAFTBUKKIT_PACKAGE, "block.CraftBlockEntityState");
        Class<?> tileClass = IReflection.getClass(IReflection.ServerPacket.MINECRAFT_PACKAGE("net.minecraft.world.level.block.entity"), "TileEntity");

        TILE_ENTITY = IReflection.getField(CRAFT_BLOCK_ENTITY_STATE, "tileEntity");
        SAVE = IReflection.getMethod(tileClass, "save", PacketUtils.NBTTagCompoundClass, new Class[] {PacketUtils.NBTTagCompoundClass});
    }

    private Object entity;
    private Object tile;

    public BlockEntityNBTTagCompound() {
    }

    public BlockEntityNBTTagCompound(Object entity) {
        if(!CRAFT_BLOCK_ENTITY_STATE.isInstance(entity)) throw new IllegalArgumentException(entity.getClass() + " cannot be cast to " + CRAFT_BLOCK_ENTITY_STATE);

        this.entity = entity;
        tile = TILE_ENTITY.get(entity);

        tag = create();
        save(tag);
    }

    public Object save(Object nbtTag) {
        return SAVE.invoke(this.tile, nbtTag);
    }

    public Object getEntity() {
        return entity;
    }

    public Object getTile() {
        return tile;
    }
}
