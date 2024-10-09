package de.codingair.codingapi.player.gui.sign;

import com.github.Anon8281.universalScheduler.UniversalRunnable;
import com.github.Anon8281.universalScheduler.UniversalScheduler;
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
import org.bukkit.Location;
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
    private static final Class<?> playInUpdateSignClass;
    private static final Class<?> playOutTileEntityData;
    private static final Class<?> baseBlockPosition;
    private static final Class<?> playOutBlockChangeClass;
    private static final IReflection.FieldAccessor<?> playOutBlockChangeClass$BlockPosition;
    private static final IReflection.FieldAccessor<?> playOutTileEntityData$BlockPosition;
    private static final IReflection.FieldAccessor<Integer> getX;
    private static final IReflection.FieldAccessor<Integer> getY;
    private static final IReflection.FieldAccessor<Integer> getZ;
    private static final IReflection.FieldAccessor<?> packetLines;

    private static final IReflection.MethodAccessor setWorld;

    static {
        playInUpdateSignClass = IReflection.getClass(IReflection.ServerPacket.PACKETS, "PacketPlayInUpdateSign");
        playOutBlockChangeClass = IReflection.getClass(IReflection.ServerPacket.PACKETS, "PacketPlayOutBlockChange");

        if (Version.atLeast(9))
            playOutTileEntityData = IReflection.getClass(IReflection.ServerPacket.PACKETS, "PacketPlayOutTileEntityData");
        else playOutTileEntityData = IReflection.getClass(IReflection.ServerPacket.PACKETS, "PacketPlayOutUpdateSign");

        playOutBlockChangeClass$BlockPosition = IReflection.getField(playOutBlockChangeClass, PacketUtils.BlockPositionClass, 0);
        playOutTileEntityData$BlockPosition = IReflection.getField(playOutTileEntityData, PacketUtils.BlockPositionClass, 0);
        baseBlockPosition = IReflection.getClass(IReflection.ServerPacket.CORE, Version.choose("Vec3i", "BaseBlockPosition"));

        getX = IReflection.getNonStaticField(baseBlockPosition, int.class, 0);
        getY = IReflection.getNonStaticField(baseBlockPosition, int.class, 1);
        getZ = IReflection.getNonStaticField(baseBlockPosition, int.class, 2);

        if (Version.atLeast(9))
            packetLines = IReflection.getField(PacketUtils.PacketPlayInUpdateSignClass, String[].class, 0);
        else packetLines = IReflection.getField(PacketUtils.PacketPlayInUpdateSignClass, "b");

        if (Version.atLeast(20.6)) {
            setWorld = IReflection.getMethod(PacketUtils.TileEntityClass, new Class[]{PacketUtils.WorldClass});
        } else setWorld = null;
    }

    private final Player player;
    private final JavaPlugin plugin;
    private final Sign sign;
    private final String[] lines;

    //used for instant opening -> open directly after sending the sign update packet
    private Location signLocation = null;
    private Runnable waiting = null;
    private boolean ignoreFutureBlockChanges = true;
    private boolean ignoreBlockChanges = false;

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
        if (Version.before(8)) {
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
        } else UniversalScheduler.getScheduler(plugin).runTask(runnable);
    }

    private void injectPacketReader() {
        new PacketReader(this.player, "SignEditor", this.plugin) {
            @Override
            public boolean readPacket(Object packet) {
                if (packet.getClass().equals(playInUpdateSignClass)) {
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

                    UniversalScheduler.getScheduler(plugin).runTask(() -> {
                        onSignChangeEvent(lines);
                        close();
                    });
                    return true;
                }

                return false;
            }

            @Override
            public boolean writePacket(Object packet) {
                if (signLocation == null) return false;

                Object position;
                if (packet.getClass().equals(playOutBlockChangeClass))
                    position = playOutBlockChangeClass$BlockPosition.get(packet);
                else if (packet.getClass().equals(playOutTileEntityData))
                    position = playOutTileEntityData$BlockPosition.get(packet);
                else return false;

                int x = getX.get(position);
                int y = getY.get(position);
                int z = getZ.get(position);

                if (signLocation.getBlockX() != x || signLocation.getBlockY() != y || signLocation.getBlockZ() != z)
                    return false;

                if (packet.getClass().equals(playOutBlockChangeClass)) {
                    // ignore changes during editing
                    if (ignoreBlockChanges) return true;

                    if (ignoreFutureBlockChanges) {
                        // The first block change needs to be passed through to allow us to set the sign
                        ignoreFutureBlockChanges = false;
                        ignoreBlockChanges = true;
                    }
                }

                if (waiting != null && packet.getClass().equals(playOutTileEntityData)) {
                    UniversalScheduler.getScheduler(plugin).runTask(waiting);
                    waiting = null;
                }

                return false;
            }
        }.inject();
    }

    private @NotNull Location getTemporarySignLocation() {
        // max distance is 7
        return player.getLocation().getBlock().getLocation().subtract(0, 7, 0);
    }

    private void prepareTemporarySign(@NotNull XMaterial material, @NotNull Location tempSign) {
        Object updatePacket = null;

        ignoreFutureBlockChanges = true;
        PacketUtils.sendBlockChange(player, tempSign, material);

        if (this.lines != null) updatePacket = createUpdatePacket(tempSign, material);
        if (updatePacket != null) PacketUtils.sendPacket(player, updatePacket);
    }

    private void revertTempSignBlock() {
        // do we have a temporary sign?
        if (sign != null) return;

        ignoreBlockChanges = false;
        PacketUtils.sendBlockChange(player, signLocation, signLocation.getBlock());
    }

    @NotNull
    private Object createUpdatePacket(@NotNull Location tempSign, @NotNull XMaterial material) {
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

        if (Version.atLeast(20.6)) {
            // Fix: Since 20.6, the PacketPlayOutTileEntityData requires the world to be set.
            setWorld.invoke(tileEntity, PacketUtils.getWorldServer(tempSign.getWorld()));
        }

        // write line contents
        if (Version.atLeast(20)) {
            Class<?> signTextClass = IReflection.getClass(IReflection.ServerPacket.BLOCK_ENTITY, "SignText");
            Class<?> enumColorClass = IReflection.getClass(IReflection.ServerPacket.WORLD_ITEM, "EnumColor");
            con = IReflection.getConstructor(signTextClass, iChatBaseComponentArrayClass, iChatBaseComponentArrayClass, enumColorClass, boolean.class);
            if (con == null)
                throw new NullPointerException("Cannot prepare temporary sign: Could not find SignText constructor.");

            Object blackColor = enumColorClass.getEnumConstants()[enumColorClass.getEnumConstants().length - 1];
            Object signText = con.newInstance(lines, lines, blackColor, false /*glow*/);

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

        return packet;
    }

    private void prepareSignEditing(@Nullable Sign sign) {
        if (sign != null) {
            Object tileEntity;

            if (Version.atLeast(12)) {
                IReflection.MethodAccessor getTileEntity = IReflection.getMethod(sign.getClass(), "getTileEntity");
                tileEntity = getTileEntity.invoke(sign);
            } else tileEntity = IReflection.getField(sign.getClass(), "sign").get(sign);

            IReflection.FieldAccessor<Boolean> editable = IReflection.getNonStaticField(PacketUtils.TileEntitySignClass, boolean.class, 0);
            editable.set(tileEntity, true);

            if (Version.atLeast(17)) {
                IReflection.FieldAccessor<UUID> id = IReflection.getNonStaticField(PacketUtils.TileEntitySignClass, UUID.class, 0);
                id.set(tileEntity, player.getUniqueId());
            } else {
                IReflection.FieldAccessor<?> owner = IReflection.getField(PacketUtils.TileEntitySignClass, Version.choose("h", 13, "g", 14, "j", 15, "c"));
                owner.set(tileEntity, PacketUtils.getEntityPlayer(this.player));
            }
        }
    }

    private void openEditor(@NotNull Location signLocation) {
        Packet packet = new Packet(PacketUtils.PacketPlayOutOpenSignEditorClass, player);

        Object location = PacketUtils.getBlockPosition(signLocation);
        packet.initialize(location, Version.choose(Packet.IGNORE, 20, true));
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
        UniversalRunnable runnable = new UniversalRunnable() {
            @Override
            public void run() {
                player.closeInventory();
                if (call != null) call.proceed();
            }
        };
        AsyncCatcher.runSync(plugin, runnable, player.getLocation());
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