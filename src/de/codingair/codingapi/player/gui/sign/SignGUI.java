package de.codingair.codingapi.player.gui.sign;

import de.codingair.codingapi.API;
import de.codingair.codingapi.player.data.PacketReader;
import de.codingair.codingapi.server.Version;
import de.codingair.codingapi.server.reflections.IReflection;
import de.codingair.codingapi.server.reflections.Packet;
import de.codingair.codingapi.server.reflections.PacketUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;

public abstract class SignGUI {
    private Player player;
    private JavaPlugin plugin;
    private Sign sign;

    public SignGUI(Player player, JavaPlugin plugin) {
        this(player, null, plugin);
    }

    public SignGUI(Player player, Sign edit, JavaPlugin plugin) {
        this.player = player;
        this.sign = edit;
        this.plugin = plugin;
    }

    public abstract void onSignChangeEvent(String[] lines);

    public void onClose(InventoryCloseEvent e) {

    }

    public void open() {
        if(Version.getVersion().equals(Version.v1_7)) {
            throw new IllegalStateException("The SignEditor does not work with 1.7!");
        }

        new PacketReader(this.player, "SignEditor", this.plugin) {
            @Override
            public boolean readPacket(Object packet) {
                if(packet.getClass().getSimpleName().equalsIgnoreCase("PacketPlayInUpdateSign")) {
                    IReflection.FieldAccessor b = IReflection.getField(PacketUtils.PacketPlayInUpdateSignClass, "b");
                    Object p = PacketUtils.PacketPlayInUpdateSignClass.cast(packet);

                    String[] lines = sign == null ? new String[4] : sign.getLines();

                    Object[] data = (Object[]) b.get(p);

                    IReflection.MethodAccessor getText = IReflection.getMethod(PacketUtils.IChatBaseComponentClass, "getText", String.class, new Class[] {});
                    IReflection.MethodAccessor getSiblings = IReflection.getMethod(PacketUtils.IChatBaseComponentClass, "a", List.class, new Class[] {});

                    for(int i = 0; i < 4; i++) {
                        Object icbc;

                        try {
                            icbc = PacketUtils.IChatBaseComponentClass.cast(data[i]);
                        } catch(Exception ex) {
                            icbc = PacketUtils.getChatMessage((String) data[i]);
                        }

                        int siblings = ((List) getSiblings.invoke(icbc)).size();
                        String line = (String) getText.invoke(icbc);

                        if(!line.isEmpty() || siblings == 0) lines[i] = line;
                    }

                    onSignChangeEvent(lines);

                    return true;
                }

                return false;
            }

            @Override
            public boolean writePacket(Object packet) {
                return false;
            }
        }.inject();

        if(this.sign != null) {
            Object tileEntity;

            if(Version.getVersion().isBiggerThan(Version.v1_11)) {
                IReflection.FieldAccessor field = IReflection.getField(this.sign.getClass(), "tileEntity");
                tileEntity = field.get(this.sign);
            } else {
                IReflection.FieldAccessor field = IReflection.getField(this.sign.getClass(), "sign");
                tileEntity = field.get(this.sign);
            }

            IReflection.FieldAccessor editable = IReflection.getField(tileEntity.getClass(), "isEditable");
            editable.set(tileEntity, true);

            IReflection.FieldAccessor owner;
            if(Version.getVersion().isBiggerThan(Version.v1_13)) {
                owner = IReflection.getField(tileEntity.getClass(), "j");
            } else if(Version.getVersion().isBiggerThan(Version.v1_12)) {
                owner = IReflection.getField(tileEntity.getClass(), "g");
            } else {
                owner = IReflection.getField(tileEntity.getClass(), "h");
            }

            owner.set(tileEntity, PacketUtils.getEntityPlayer(this.player));
        }

        Packet packet = new Packet(PacketUtils.PacketPlayOutOpenSignEditorClass, this.player);
        packet.initialize(this.sign == null ? PacketUtils.getBlockPosition(new Location(null, 0, 0, 0)) : PacketUtils.getBlockPosition(sign.getLocation()));
        packet.send();
    }

    public void close() {
        PacketReader packetReader = null;

        for(PacketReader reader : API.getRemovables(this.player, PacketReader.class)) {
            if(reader.getName().equals("SignEditor")) {
                packetReader = reader;
                break;
            }
        }

        packetReader.unInject();
        Bukkit.getScheduler().runTask(plugin, () -> this.player.closeInventory());
    }

}
