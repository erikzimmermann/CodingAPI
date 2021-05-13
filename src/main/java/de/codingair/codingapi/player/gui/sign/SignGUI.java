package de.codingair.codingapi.player.gui.sign;

import de.codingair.codingapi.API;
import de.codingair.codingapi.player.data.PacketReader;
import de.codingair.codingapi.server.AsyncCatcher;
import de.codingair.codingapi.server.reflections.IReflection;
import de.codingair.codingapi.server.reflections.Packet;
import de.codingair.codingapi.server.reflections.PacketUtils;
import de.codingair.codingapi.server.specification.Version;
import de.codingair.codingapi.tools.Call;
import de.codingair.codingapi.tools.items.XMaterial;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public abstract class SignGUI {
    private final Player player;
    private final JavaPlugin plugin;
    private final Sign sign;
    private final String[] lines;

    public SignGUI(@NotNull Player player, @NotNull JavaPlugin plugin) {
        this(player, null, plugin);
    }

    public SignGUI(@NotNull Player player, @Nullable Sign edit, @NotNull JavaPlugin plugin) {
        this.player = player;
        this.sign = edit;
        this.plugin = plugin;
        this.lines = null;
    }

    public SignGUI(@NotNull Player player, @NotNull JavaPlugin plugin, @NotNull String... lines) {
        this.lines = new String[4];
        for (int i = 0; i < 4; i++) {
            if (i < lines.length) this.lines[i] = lines[i];
            else break;
        }

        this.player = player;
        this.sign = null;
        this.plugin = plugin;
    }

    public abstract void onSignChangeEvent(String[] lines);

    public void onClose(InventoryCloseEvent e) {

    }

    public void open() {
        if (Version.get().equals(Version.v1_7)) {
            throw new IllegalStateException("The SignEditor does not work with 1.7!");
        }

        //close current inventories
        player.closeInventory();

        Location tempSign = null;
        Sign sign = this.sign;

        boolean needsTempSign = sign == null && this.lines != null;
        if (needsTempSign) {
            tempSign = calculateSignLocation();
            if (tempSign != null) {
                sign = prepareTemporarySign(tempSign);
            }
        }

        //finalize vars
        Sign finalSign = sign;
        Location finalTempSign = tempSign;

        Runnable runnable = () -> {
            //inject before updating sign to save time between setting and removing the sign
            injectPacketReader();

            prepareSignEditing(finalSign);
            openEditor(finalSign);

            //clean temporary sign
            if (finalTempSign != null) finalTempSign.getBlock().setType(Material.AIR);
        };

        if (needsTempSign) Bukkit.getScheduler().runTaskLater(plugin, runnable, 4);
        else runnable.run();
    }

    private void injectPacketReader() {
        Class<?> packetClass = IReflection.getClass(IReflection.ServerPacket.MINECRAFT_PACKAGE, "PacketPlayInUpdateSign");
        new PacketReader(this.player, "SignEditor", this.plugin) {
            @Override
            public boolean readPacket(Object packet) {
                if (packet.getClass().equals(packetClass)) {
                    IReflection.FieldAccessor<?> b = IReflection.getField(PacketUtils.PacketPlayInUpdateSignClass, "b");
                    assert PacketUtils.PacketPlayInUpdateSignClass != null;
                    Object p = PacketUtils.PacketPlayInUpdateSignClass.cast(packet);

                    String[] lines;

                    if (Version.get().isBiggerThan(Version.v1_8)) {
                        lines = (String[]) b.get(p);
                    } else {
                        lines = sign == null ? new String[4] : sign.getLines();

                        Object[] data = (Object[]) b.get(p);

                        assert PacketUtils.IChatBaseComponentClass != null;
                        IReflection.MethodAccessor getText = IReflection.getMethod(PacketUtils.IChatBaseComponentClass, "getText", String.class, new Class[] {});
                        IReflection.MethodAccessor getSiblings = IReflection.getMethod(PacketUtils.IChatBaseComponentClass, "a", List.class, new Class[] {});

                        for (int i = 0; i < 4; i++) {
                            Object icbc;

                            try {
                                icbc = PacketUtils.IChatBaseComponentClass.cast(data[i]);
                            } catch (Exception ex) {
                                icbc = PacketUtils.getChatMessage((String) data[i]);
                            }

                            int siblings = ((List<?>) getSiblings.invoke(icbc)).size();
                            String line = (String) getText.invoke(icbc);

                            if (!line.isEmpty() || siblings == 0) lines[i] = line;
                        }
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
    }

    @Nullable
    private Location calculateSignLocation() {
        World w = player.getWorld();
        double x = player.getLocation().getX();
        double z = player.getLocation().getZ();

        Location l = new Location(w, x, 1, z);
        assert l.getWorld() != null;

        double minDistance = 10;

        //increment from 0
        while (l.getBlock().getType() != Material.AIR) {
            l.add(0, 1, 0);

            //check distance
            if (player.getLocation().getY() - l.getY() < minDistance) {
                l.setY(l.getWorld().getMaxHeight());

                //decrement from highest block
                while (l.getBlock().getType() != Material.AIR) {
                    l.subtract(0, 1, 0);

                    //check distance
                    if (player.getLocation().getY() - l.getY() < minDistance) {
                        //no suitable location found -> ignore text and show empty sign
                        return null;
                    }
                }

                break;
            }
        }

        return l;
    }

    @NotNull
    private Sign prepareTemporarySign(@NotNull Location tempSign) {
        if (this.lines == null) throw new NullPointerException("Cannot update a sign without content.");

        Sign sign;
        assert XMaterial.OAK_SIGN.parseMaterial() != null;

        tempSign.getBlock().setType(XMaterial.OAK_SIGN.parseMaterial());
        sign = (Sign) tempSign.getBlock().getState();

        //update sign
        for (int i = 0; i < 4; i++) {
            sign.setLine(i, this.lines[i]);
        }
        sign.update(true, true);
        return sign;
    }

    private void prepareSignEditing(@Nullable Sign sign) {
        if (sign != null) {
            Object tileEntity;

            IReflection.FieldAccessor<?> field;
            if (Version.get().isBiggerThan(Version.v1_11)) field = IReflection.getField(sign.getClass(), "tileEntity");
            else field = IReflection.getField(sign.getClass(), "sign");

            tileEntity = field.get(sign);

            IReflection.FieldAccessor<Boolean> editable = IReflection.getField(tileEntity.getClass(), "isEditable");
            editable.set(tileEntity, true);

            IReflection.FieldAccessor<?> owner;
            switch (Version.get().getId()) {
                case 16:
                case 15:
                    owner = IReflection.getField(tileEntity.getClass(), "c");
                    break;
                case 14:
                    owner = IReflection.getField(tileEntity.getClass(), "j");
                    break;
                case 13:
                    owner = IReflection.getField(tileEntity.getClass(), "g");
                    break;
                default:
                    owner = IReflection.getField(tileEntity.getClass(), "h");
                    break;
            }
            owner.set(tileEntity, PacketUtils.getEntityPlayer(this.player));
        }
    }

    private void openEditor(@Nullable Sign sign) {
        Packet packet = new Packet(PacketUtils.PacketPlayOutOpenSignEditorClass, player);

        Object location;
        if (sign == null) location = PacketUtils.getBlockPosition(new Location(player.getWorld(), 0, 0, 0));
        else location = PacketUtils.getBlockPosition(sign.getLocation());

        packet.initialize(location);
        packet.send();
    }

    public void close() {
        close(null);
    }

    public void close(Call call) {
        PacketReader packetReader = null;

        List<PacketReader> l = API.getRemovables(this.player, PacketReader.class);
        for (PacketReader reader : l) {
            if (reader.getName().equals("SignEditor")) {
                packetReader = reader;
                break;
            }
        }
        l.clear();

        if (packetReader != null) packetReader.unInject();
        AsyncCatcher.runSync(plugin, () -> {
            player.closeInventory();
            if (call != null) call.proceed();
        });
    }

    public String[] getLines() {
        return lines;
    }
}
