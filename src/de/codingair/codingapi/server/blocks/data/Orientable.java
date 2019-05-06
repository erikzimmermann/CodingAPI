package de.codingair.codingapi.server.blocks.data;

import de.codingair.codingapi.server.Version;
import de.codingair.codingapi.server.blocks.utils.Axis;
import de.codingair.codingapi.server.reflections.IReflection;
import org.bukkit.block.Block;

public class Orientable extends BlockData {
    private Axis axis;

    public Orientable(Axis axis) {
        this.axis = axis;
    }

    public Axis getAxis() {
        return axis;
    }

    public void setAxis(Axis axis) {
        this.axis = axis;
    }

    @Override
    public Object getData(Block block) {
        if(Version.getVersion().isBiggerThan(Version.v1_12)) {
            Class<?> orientable = IReflection.getClass(IReflection.ServerPacket.BUKKIT_PACKET, "block.data.Orientable");
            Class<?> axis = IReflection.getClass(IReflection.ServerPacket.BUKKIT_PACKET, "Axis");
            IReflection.MethodAccessor setAxis = IReflection.getMethod(orientable, "setAxis", new Class[]{axis});

            Object data = getFrom(block);
            if(!orientable.isInstance(data)) throw new IllegalStateException("BlockData cannot be casted to Orientable.class");

            Object o = orientable.cast(data);
            setAxis.invoke(o, axis.getEnumConstants()[this.axis.getId()]);

            return o;
        } else {
            return this.axis.getByteId();
        }
    }
}
