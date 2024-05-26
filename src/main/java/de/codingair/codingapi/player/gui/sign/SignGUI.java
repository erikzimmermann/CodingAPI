package de.codingair.codingapi.player.gui.sign;

import de.codingair.codingapi.API;
import de.codingair.codingapi.nms.NmsLoader;
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
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Array;
import java.util.List;
import java.util.UUID;

public abstract class SignGUI {
    private static final Class<?> packetClass;
    private static final Class<?> updatePacket;
    private static final Class<?> baseBlockPosition;
    private static final IReflection.FieldAccessor<?> pos;
    private static final IReflection.MethodAccessor getX;
    private static final IReflection.MethodAccessor getY;
    private static final IReflection.MethodAccessor getZ;
    private static final IReflection.FieldAccessor<?> packetLines;

    static {
        packetClass = IReflection.getClass(IReflection.ServerPacket.PACKETS, "PacketPlayInUpdateSign");

        if (Version.get().isBiggerThan(8))
            updatePacket = IReflection.getClass(IReflection.ServerPacket.PACKETS, "PacketPlayOutTileEntityData");
        else updatePacket = IReflection.getClass(IReflection.ServerPacket.PACKETS, "PacketPlayOutUpdateSign");

        pos = IReflection.getField(updatePacket, PacketUtils.BlockPositionClass, 0);
        baseBlockPosition = IReflection.getClass(IReflection.ServerPacket.CORE, Version.choose("BaseBlockPosition", 20.5, "Vec3i"));

        getX = IReflection.getMethod(baseBlockPosition, Version.choose("getX", 18, "u", 20.5, "getX"), int.class, new Class[0]);
        getY = IReflection.getMethod(baseBlockPosition, Version.choose("getY", 18, "v", 20.5, "getY"), int.class, new Class[0]);
        getZ = IReflection.getMethod(baseBlockPosition, Version.choose("getZ", 18, "w", 20.5, "getZ"), int.class, new Class[0]);


        if (Version.atLeast(9))
            packetLines = IReflection.getField(PacketUtils.PacketPlayInUpdateSignClass, String[].class, 0);
        else packetLines = IReflection.getField(PacketUtils.PacketPlayInUpdateSignClass, "b");
    }

    private final Player player;
    private final JavaPlugin plugin;
    private final Sign sign;
    private final String[] lines;

    //used for instant opening -> open directly after sending the sign update packet
    private Location signLocation = null;
    private Runnable waiting = null;

    @NmsLoader
    private SignGUI() {
        player = null;
        plugin = null;
        sign = null;
        lines = null;
    }

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

        if (this.sign != null) signLocation = sign.getLocation();
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

        //inject before updating sign to save time between setting and removing the sign
        injectPacketReader();

        Runnable runnable = () -> {
            if (sign != null) prepareSignEditing(sign);
            openEditor(signLocation);
            waiting = null;
        };

        boolean needsTempSign = sign == null;
        if (needsTempSign) {
            waiting = runnable;

            signLocation = getTemporarySignLocation();
            prepareTemporarySign(XMaterial.OAK_SIGN, signLocation);
        } else Bukkit.getScheduler().runTask(plugin, runnable);
    }

    private void injectPacketReader() {
        new PacketReader(this.player, "SignEditor", this.plugin) {
            @Override
            public boolean readPacket(Object packet) {
                if (packet.getClass().equals(packetClass)) {
                    String[] lines;

                    if (Version.atLeast(9)) {
                        lines = (String[]) packetLines.get(packet);
                    } else {
                        lines = sign == null ? new String[4] : sign.getLines();

                        Object[] data = (Object[]) packetLines.get(packet);

                        assert PacketUtils.IChatBaseComponentClass != null;
                        IReflection.MethodAccessor getText = IReflection.getMethod(PacketUtils.IChatBaseComponentClass, "getText", String.class, new Class[]{});
                        IReflection.MethodAccessor getSiblings = IReflection.getMethod(PacketUtils.IChatBaseComponentClass, "a", List.class, new Class[]{});

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

                    Bukkit.getScheduler().runTask(plugin, () -> {
                        onSignChangeEvent(lines);
                        revertTempSignBlock();
                    });
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

                    if (waiting != null && signLocation != null &&
                            signLocation.getBlockX() == x &&
                            signLocation.getBlockY() == y &&
                            signLocation.getBlockZ() == z) {
                        Bukkit.getScheduler().runTask(plugin, waiting);
                        waiting = null;
                    }
                }

                return false;
            }
        }.inject();
    }

    private @NotNull Location getTemporarySignLocation() {
        // max distance is 7
        return player.getLocation().clone().subtract(0, 7, 0);
    }

    private void prepareTemporarySign(@NotNull XMaterial material, @NotNull Location tempSign) {
        PacketUtils.sendBlockChange(player, tempSign, material);
        if (this.lines != null) sendLinesChange(tempSign, material);
    }

    private void revertTempSignBlock() {
        // do we have a temporary sign?
        if (sign != null) return;

        Block b = signLocation.getBlock();
        PacketUtils.sendBlockChange(player, signLocation, b);
    }

    private void sendLinesChange(@NotNull Location tempSign, @NotNull XMaterial material) {
        IReflection.ConstructorAccessor con;
        Class<?> iChatBaseComponentArrayClass = Array.newInstance(PacketUtils.IChatBaseComponentClass, 0).getClass();

        Class<?> craftSign = IReflection.getClass(IReflection.ServerPacket.CRAFTBUKKIT_BLOCK, "CraftSign");
        IReflection.MethodAccessor sanitizeLines = IReflection.getMethod(craftSign, iChatBaseComponentArrayClass, new Class[]{String[].class});
        Object[] lines = (Object[]) sanitizeLines.invoke(null, new Object[]{this.lines});

        Object blockPos = PacketUtils.getBlockPosition(tempSign);

        // create tile entity instance
        Class<?> tileEntitySignClass = IReflection.getClass(IReflection.ServerPacket.BLOCK_ENTITY, "TileEntitySign");
        Object tileEntity;
        if (Version.atLeast(17)) {
            con = IReflection.getConstructor(tileEntitySignClass, PacketUtils.BlockPositionClass, PacketUtils.IBlockDataClass);
            if (con == null)
                throw new NullPointerException("Cannot prepare temporary sign: Could not find TileEntitySign constructor.");
            tileEntity = con.newInstance(PacketUtils.getBlockPosition(tempSign), PacketUtils.getIBlockData(material));
        } else {
            con = IReflection.getConstructor(tileEntitySignClass);
            if (con == null)
                throw new NullPointerException("Cannot prepare temporary sign: Could not find TileEntitySign constructor.");
            tileEntity = con.newInstance();
            IReflection.FieldAccessor<?> setBlockPos = IReflection.getField(tileEntitySignClass, PacketUtils.BlockPositionClass, 0);
            setBlockPos.set(tileEntity, blockPos);
        }

        // write line contents
        if (Version.atLeast(20)) {
            Class<?> signTextClass = IReflection.getClass(IReflection.ServerPacket.BLOCK_ENTITY, "SignText");
            Class<?> enumColorClass = IReflection.getClass(IReflection.ServerPacket.WORLD_ITEM, "EnumColor");
            con = IReflection.getConstructor(signTextClass, iChatBaseComponentArrayClass, iChatBaseComponentArrayClass, enumColorClass, boolean.class);
            if (con == null)
                throw new NullPointerException("Cannot prepare temporary sign: Could not find SignText constructor.");

            Object blackColor = enumColorClass.getEnumConstants()[enumColorClass.getEnumConstants().length - 1];
            Object signText = con.newInstance(lines, lines, blackColor, false);

            IReflection.MethodAccessor applyText = IReflection.getMethod(tileEntitySignClass, boolean.class, new Class[]{signTextClass, boolean.class});

            boolean front = true;
            applyText.invoke(tileEntity, signText, front);
        } else if (Version.atLeast(13)) {
            IReflection.MethodAccessor applyLine = IReflection.getMethod(tileEntity.getClass(), new Class[]{int.class, PacketUtils.IChatBaseComponentClass});

            for (int i = 0; i < lines.length; i++) {
                applyLine.invoke(tileEntity, i, lines[i]);
            }
        } else {
            IReflection.FieldAccessor<?> linesField = IReflection.getField(tileEntitySignClass, Array.newInstance(PacketUtils.IChatBaseComponentClass, 0).getClass(), 0);
            linesField.set(tileEntity, lines);
        }

        // send content to player
        Object packet;
        if (Version.atLeast(9)) {
            IReflection.MethodAccessor createUpdatePacket = IReflection.getMethod(
                    tileEntity.getClass(),
                    IReflection.getClass(IReflection.ServerPacket.PACKETS, "PacketPlayOutTileEntityData"),
                    new Class[0]
            );
            packet = createUpdatePacket.invoke(tileEntity);
        } else {
            IReflection.MethodAccessor createUpdatePacket = IReflection.getMethod(
                    tileEntity.getClass(),
                    IReflection.getClass(IReflection.ServerPacket.PACKETS, "Packet"),
                    new Class[0]
            );
            packet = createUpdatePacket.invoke(tileEntity);
        }

        PacketUtils.sendPacket(this.player, packet);
    }

    private void prepareSignEditing(@Nullable Sign sign) {
        if (sign != null) {
            Object tileEntity;

            if (Version.get().isBiggerThan(Version.v1_11)) {
                IReflection.MethodAccessor getTileEntity = IReflection.getMethod(sign.getClass(), "getTileEntity");
                tileEntity = getTileEntity.invoke(sign);
            } else tileEntity = IReflection.getField(sign.getClass(), "sign").get(sign);

            IReflection.FieldAccessor<Boolean> editable = IReflection.getNonStaticField(PacketUtils.TileEntitySignClass, boolean.class, 0);
            editable.set(tileEntity, true);

            if (Version.atLeast(17)) {
                IReflection.FieldAccessor<UUID> id = IReflection.getNonStaticField(PacketUtils.TileEntitySignClass, UUID.class, 0);
                id.set(tileEntity, player.getUniqueId());
            } else {
                IReflection.FieldAccessor<?> owner = IReflection.getField(PacketUtils.TileEntitySignClass, Version.since(13, "h", "g", "j", "c"));
                owner.set(tileEntity, PacketUtils.getEntityPlayer(this.player));
            }
        }
    }

    private void openEditor(@NotNull Location signLocation) {
        Packet packet = new Packet(PacketUtils.PacketPlayOutOpenSignEditorClass, player);

        Object location = PacketUtils.getBlockPosition(signLocation);
        packet.initialize(location, Version.since(20, Packet.IGNORE, true));
        packet.send();
    }

    public void close() {
        close(null);
    }

    public void close(@Nullable Call call) {
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

    /**
     * Wrapper to instantiate the SignGUI class for the NMS check.
     */
    public static class NmsWrapper extends SignGUI {
        @NmsLoader
        private NmsWrapper() {
            super();
        }

        @Override
        public void onSignChangeEvent(String[] lines) {
        }
    }
}
