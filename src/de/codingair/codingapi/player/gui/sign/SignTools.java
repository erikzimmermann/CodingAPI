package de.codingair.codingapi.player.gui.sign;

import de.codingair.codingapi.server.Version;
import de.codingair.codingapi.server.reflections.IReflection;
import de.codingair.codingapi.server.reflections.PacketUtils;
import org.bukkit.ChatColor;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;

public class SignTools {

    public static void updateSign(Sign sign, String[] text) {
        updateSign(sign, text, true);
    }

    public static void updateSign(Sign sign, String[] text, boolean colorSupport) {
        if(colorSupport) {
            for(int i = 0; i < text.length; i++) {
                text[i] = ChatColor.translateAlternateColorCodes('&', text[i]);
            }
        }

        switch(Version.getVersion()) {
            case v1_14:
            case v1_13:
            case v1_12:
            case v1_11:
            case v1_9:
            case v1_8: {
                for(int i = 0; i < 4; i++) {
                    sign.setLine(i, text[i]);
                }

                sign.update(true);
                break;
            }

            case v1_10: {
                Object tileEntitySign = PacketUtils.getTileEntity(sign.getLocation());

                Class<?> packet = IReflection.getClass(IReflection.ServerPacket.MINECRAFT_PACKAGE, "PacketPlayOutTileEntityData");
                IReflection.FieldAccessor lines = IReflection.getField(PacketUtils.TileEntitySignClass, "lines");
                IReflection.MethodAccessor getUpdatePacket = IReflection.getMethod(PacketUtils.TileEntitySignClass, "getUpdatePacket", packet, new Class[] {});

                Object[] array = (Object[]) lines.get(tileEntitySign);

                for(int i = 0; i < 4; i++) {
                    array[i] = text.length >= i ? PacketUtils.getChatComponentText(text[i]) : PacketUtils.getChatComponentText("");
                }

                PacketUtils.sendPacketToAll(getUpdatePacket.invoke(tileEntitySign));
                break;
            }
        }
    }

    public static void setEditable(Sign sign, boolean editable) {
        Object tileEntity;

        try {
            IReflection.FieldAccessor field = IReflection.getField(sign.getClass(), "sign");
            tileEntity = field.get(sign);
        } catch(Exception ex) {
            IReflection.FieldAccessor field = IReflection.getField(sign.getClass(), "tileEntity");
            tileEntity = field.get(sign);
        }

        IReflection.FieldAccessor editableField = IReflection.getField(tileEntity.getClass(), "isEditable");
        editableField.set(tileEntity, editable);
    }

    public static boolean isEditable(Sign sign) {
        Object tileEntity;

        try {
            IReflection.FieldAccessor field = IReflection.getField(sign.getClass(), "sign");
            tileEntity = field.get(sign);
        } catch(Exception ex) {
            IReflection.FieldAccessor field = IReflection.getField(sign.getClass(), "tileEntity");
            tileEntity = field.get(sign);
        }

        IReflection.FieldAccessor editableField = IReflection.getField(tileEntity.getClass(), "isEditable");
        return (boolean) editableField.get(tileEntity);
    }

    public static void setOwner(Sign sign, Player player) {
        Object tileEntity;

        try {
            IReflection.FieldAccessor field = IReflection.getField(sign.getClass(), "sign");
            tileEntity = field.get(sign);
        } catch(Exception ex) {
            IReflection.FieldAccessor field = IReflection.getField(sign.getClass(), "tileEntity");
            tileEntity = field.get(sign);
        }

        IReflection.FieldAccessor owner = IReflection.getField(tileEntity.getClass(), "h");
        owner.set(tileEntity, PacketUtils.getEntityPlayer(player));
    }

}
