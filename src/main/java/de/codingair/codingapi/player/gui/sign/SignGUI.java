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
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public abstract class SignGUI {
    private static final Class<?> packetClass;
    private static final Class<?> updatePacket;
    private static final Class<?> baseBlockPosition;
    private static final IReflection.FieldAccessor<?> pos;
    private static final IReflection.MethodAccessor getX;
    private static final IReflection.MethodAccessor getY;
    private static final IReflection.MethodAccessor getZ;

    static {
        packetClass = IReflection.getClass(IReflection.ServerPacket.MINECRAFT_PACKAGE, "PacketPlayInUpdateSign");

        updatePacket = IReflection.getClass(IReflection.ServerPacket.MINECRAFT_PACKAGE, "PacketPlayOutTileEntityData");
        pos = IReflection.getField(updatePacket, "a");
        baseBlockPosition = IReflection.getClass(IReflection.ServerPacket.MINECRAFT_PACKAGE, "BaseBlockPosition");
        assert baseBlockPosition != null;
        getX = IReflection.getMethod(baseBlockPosition, "getX", int.class, new Class[0]);
        getY = IReflection.getMethod(baseBlockPosition, "getY", int.class, new Class[0]);
        getZ = IReflection.getMethod(baseBlockPosition, "getZ", int.class, new Class[0]);
    }

    private final Player player;
    private final JavaPlugin plugin;
    private final Sign sign;
    private final String[] lines;

    //used for instant opening -> open directly after sending the sign update packet
    private Location tempSign = null;
    private Runnable waiting = null;

    public SignGUI(@NotNull Player player, @NotNull JavaPlugin plugin, @Nullable Sign sign, @Nullable String[] lines) {
        if (lines != null) {
            this.lines = new String[4];
            for (int i = 0; i < 4; i++) {
                if (i < lines.length) this.lines[i] = lines[i];
                else break;
            }
        } else this.lines = null;

        this.player = player;
        this.plugin = plugin;
        this.sign = sign;
    }

    public SignGUI(@NotNull Player player, @NotNull JavaPlugin plugin, @NotNull String... lines) {
        this(player, plugin, null, lines);
    }

    public SignGUI(@NotNull Player player, @Nullable Sign edit, @NotNull JavaPlugin plugin) {
        this(player, plugin, edit, null);
    }

    public SignGUI(@NotNull Player player, @NotNull JavaPlugin plugin) {
        this(player, plugin, null, null);
    }

    public abstract void onSignChangeEvent(String[] lines);

    public void onClose(InventoryCloseEvent e) {

    }

    public void open() {
        if (Version.get().equals(Version.v1_7)) {
            throw new IllegalStateException("The SignEditor does not work on 1.7!");
        }

        //close current inventories
        player.closeInventory();

        tempSign = null;
        Sign sign = this.sign;

        //inject before updating sign to save time between setting and removing the sign
        injectPacketReader();

        boolean needsTempSign = sign == null && this.lines != null;
        if (needsTempSign) {
            tempSign = calculateSignLocation();
            if (tempSign != null) {
                sign = prepareTemporarySign(tempSign);
            }
        }

        //finalize vars
        final Sign finalSign = sign;
        final Location finalTempSign = tempSign;

        Runnable runnable = () -> {
            prepareSignEditing(finalSign);
            openEditor(finalSign);

            //clean temporary sign
            if (finalTempSign != null) {
                //reset blocks later so the editor can be opened without issues (mainly for older versions)
                Bukkit.getScheduler().runTaskLater(plugin, () -> finalTempSign.getBlock().setType(Material.AIR), 1L);
            }

            tempSign = null;
            waiting = null;
        };

        if (needsTempSign) waiting = runnable;
        else runnable.run();
    }

    private void injectPacketReader() {
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
                if (packet.getClass().equals(updatePacket)) {
                    Object position = pos.get(packet);

                    int x = (int) getX.invoke(position);
                    int y = (int) getY.invoke(position);
                    int z = (int) getZ.invoke(position);

                    if (waiting != null && tempSign != null &&
                            tempSign.getBlockX() == x &&
                            tempSign.getBlockY() == y &&
                            tempSign.getBlockZ() == z) {
                        Bukkit.getScheduler().runTask(plugin, waiting);
                    }
                }

                return false;
            }
        }.inject();
    }

    @Nullable
    private Location calculateSignLocation() {
        World w = player.getWorld();
        double x = player.getLocation().getX();
        double z = player.getLocation().getZ();

        Location l = new Location(w, x, 0, z);
        assert l.getWorld() != null;

        double minDistance = 10;

        //increment from lowest block
        do {
            l.add(0, 1, 0);

            //check distance
            if (Math.abs(player.getLocation().getY() - l.getY()) < minDistance) {
                l.setY(l.getWorld().getMaxHeight() - 1);

                //decrement from highest block
                while (l.getBlock().getType() != Material.AIR) {
                    l.subtract(0, 1, 0);

                    //check distance
                    if (Math.abs(player.getLocation().getY() - l.getY()) < minDistance) {
                        //no suitable location found -> ignore text and show empty sign
                        return null;
                    }
                }

                break;
            }
        } while (l.getBlock().getType() != Material.AIR);

        return l;
    }

    @NotNull
    private Sign prepareTemporarySign(@NotNull Location tempSign) {
        if (this.lines == null) throw new NullPointerException("Cannot update a sign without content.");

        Sign sign;
        Material m = XMaterial.OAK_SIGN.parseMaterial();
        assert m != null;

        //fix wrong parsing -> we need the block material
        if (m.name().equals("SIGN")) m = Material.valueOf("SIGN_POST");

        Block b = tempSign.getBlock();
        b.setType(m, false);

        sign = (Sign) b.getState();

        //update sign
        for (int i = 0; i < 4; i++) {
            sign.setLine(i, this.lines[i]);
        }
        sign.update(true, false);

        return sign;
    }

    private void prepareSignEditing(@Nullable Sign sign) {
        if (sign != null) {
            Object tileEntity;

            if (Version.get().isBiggerThan(Version.v1_11)) {
                IReflection.MethodAccessor getTileEntity = IReflection.getMethod(sign.getClass(), "getTileEntity");
                tileEntity = getTileEntity.invoke(sign);
            } else tileEntity = IReflection.getField(sign.getClass(), "sign").get(sign);

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
