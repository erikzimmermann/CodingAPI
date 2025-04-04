package de.codingair.codingapi.particles;

import de.codingair.codingapi.nms.NmsLoader;
import de.codingair.codingapi.server.reflections.IReflection;
import de.codingair.codingapi.server.reflections.PacketUtils;
import de.codingair.codingapi.server.specification.Version;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.lang.reflect.Constructor;

public class ParticlePacket {

    private static final Class<?> packetPlayOutWorldParticles;
    private static final Class<?> particleParam;
    private static final Class<?> craftParticle;
    private static final Class<?> dustOptions;
    private static final IReflection.MethodAccessor toNMS;
    private static final IReflection.ConstructorAccessor particlePacketConstructor;

    static {
        if (Version.atLeast(13)) {
            packetPlayOutWorldParticles = IReflection.getClass(IReflection.ServerPacket.PACKETS, "PacketPlayOutWorldParticles");
            particleParam = IReflection.getClass(IReflection.ServerPacket.PARTICLES, "ParticleParam");
            craftParticle = IReflection.getClass(IReflection.ServerPacket.CRAFTBUKKIT_PACKAGE, "CraftParticle");
            dustOptions = IReflection.getClass(IReflection.ServerPacket.BUKKIT_PACKET, "Particle$DustOptions");
            toNMS = IReflection.getMethod(craftParticle, particleParam, new Class[]{org.bukkit.Particle.class, Object.class});

            if (Version.atLeast(21.4)) {
                particlePacketConstructor = IReflection.getConstructor(packetPlayOutWorldParticles, particleParam, boolean.class, boolean.class, double.class, double.class, double.class, float.class, float.class, float.class, float.class, int.class);
            } else if (Version.atLeast(17)) {
                particlePacketConstructor = IReflection.getConstructor(packetPlayOutWorldParticles, particleParam, boolean.class, double.class, double.class, double.class, float.class, float.class, float.class, float.class, int.class);
            } else {
                particlePacketConstructor = IReflection.getConstructor(packetPlayOutWorldParticles);
            }
            if (particlePacketConstructor == null) throw new NullPointerException("Cannot find particle constructor");
        } else {
            packetPlayOutWorldParticles = null;
            particleParam = null;
            craftParticle = null;
            dustOptions = null;
            toNMS = null;
            particlePacketConstructor = null;
        }
    }

    private final Particle particle;
    private Object packet;
    private Color color = null;
    private boolean longDistance = false;
    private Location location;
    private double maxDistance = 0;
    private int noteId = 0;

    @NmsLoader
    private ParticlePacket() {
        this.particle = null;
    }

    public ParticlePacket(Particle particle) {
        this.particle = particle;
    }

    public ParticlePacket(Particle particle, Color color, boolean longDistance) {
        this.particle = particle;
        this.color = color;
        this.longDistance = longDistance;
    }

    public ParticlePacket initialize(Location loc) {
        if (!available()) return this;
        this.location = loc;

        if (Version.atLeast(13)) {
            Object data = null;
            float offsetX = 0, offsetY = 0, offsetZ = 0, extra = 0;
            int count = 1;
            if (this.color != null) {
                if (particle == Particle.REDSTONE)
                    data = IReflection.getConstructor(dustOptions, org.bukkit.Color.class, float.class).newInstance(org.bukkit.Color.fromRGB(this.color.getRed(), this.color.getGreen(), this.color.getBlue()), 1);
                else if (particle == Particle.NOTE) {
                    count = 0;
                    offsetX = noteId / 24F;
                    extra = 1F;
                } else if (particle == Particle.SPELL_MOB || particle == Particle.SPELL_MOB_AMBIENT) {
                    count = 0;
                    offsetX = color.getRed() / 255F;
                    offsetY = color.getGreen() / 255F;
                    offsetZ = color.getBlue() / 255F;
                    extra = 1F;
                }
            }

            Object particle;
            try {
                particle = toNMS.invoke(null, org.bukkit.Particle.valueOf(this.particle.name()), data);
            } catch (Exception ex) {
                return this;
            }

            if (Version.atLeast(21.4)) {
                packet = particlePacketConstructor.newInstance(particle,
                        false, // force: false by default
                        this.longDistance,
                        this.location.getX(),
                        this.location.getY(),
                        this.location.getZ(),
                        offsetX,
                        offsetY,
                        offsetZ,
                        extra,
                        count);
            } else if (Version.atLeast(17)) {
                packet = particlePacketConstructor.newInstance(particle,
                        this.longDistance,
                        this.location.getX(),
                        this.location.getY(),
                        this.location.getZ(),
                        offsetX,
                        offsetY,
                        offsetZ,
                        extra,
                        count);
            } else {
                packet = particlePacketConstructor.newInstance();
                try {
                    IReflection.setValue(packet, "a", (float) this.location.getX());      //x
                    IReflection.setValue(packet, "b", (float) this.location.getY());      //y
                    IReflection.setValue(packet, "c", (float) this.location.getZ());      //z
                    IReflection.setValue(packet, "d", offsetX);                           //offset x
                    IReflection.setValue(packet, "e", offsetY);                           //offset y
                    IReflection.setValue(packet, "f", offsetZ);                           //offset z
                    IReflection.setValue(packet, "g", extra);                             //extra
                    IReflection.setValue(packet, "h", count);                             //count
                    IReflection.setValue(packet, "i", this.longDistance);
                    IReflection.setValue(packet, "j", particle);
                } catch (IllegalAccessException | NoSuchFieldException e1) {
                    e1.printStackTrace();
                }
            }
        } else {
            Class<?> packetClass = IReflection.getClass(IReflection.ServerPacket.PACKETS, "PacketPlayOutWorldParticles");
            Constructor<?> packetConstructor = IReflection.getConstructor(packetClass).getConstructor();

            Class<?> enumParticle = IReflection.getClass(IReflection.ServerPacket.MINECRAFT_PACKAGE, "EnumParticle");

            ParticleData data = null;

            if (particle.requiresData()) {
                Location below = location.clone();
                below.setY(location.getBlockY() - 0.49);

                //noinspection deprecation
                data = new ParticleData(below.getBlock().getType(), below.getBlock().getData());
            }

            if (particle.requiresWater() && !location.getBlock().getType().equals(Material.WATER) && !location.getBlock().getType().equals(Material.valueOf("STATIONARY_WATER")))
                return this;

            float e = 0, f = 0, g = 0, h = 0;
            int i = 1;
            if (particle.isColorable() && this.color != null) {
                e = (float) this.color.getRed() / 255F;

                if (e == 0) e = 0.003921569F;

                f = (float) this.color.getGreen() / 255F;
                g = (float) this.color.getBlue() / 255F;
                h = 1F;
                i = 0;
            }

            try {
                packet = packetConstructor.newInstance();

                IReflection.setValue(packet, "a", enumParticle.getEnumConstants()[particle.getId()]);
                IReflection.setValue(packet, "j", this.longDistance);

                if (data != null) {
                    int[] packetData = data.getPacketData();
                    IReflection.setValue(packet, "k", particle == Particle.ITEM_CRACK ? packetData : new int[]{packetData[0] | (packetData[1] << 12)});
                }

                IReflection.setValue(packet, "b", (float) this.location.getX());
                IReflection.setValue(packet, "c", (float) this.location.getY());
                IReflection.setValue(packet, "d", (float) this.location.getZ());
                IReflection.setValue(packet, "e", e);
                IReflection.setValue(packet, "f", f);
                IReflection.setValue(packet, "g", g);
                IReflection.setValue(packet, "h", h);
                IReflection.setValue(packet, "i", i);
            } catch (Exception exception) {
                exception.printStackTrace();
            }
        }

        return this;
    }

    public boolean available() {
        if (Version.atLeast(13)) return this.particle != null && this.particle.getName_v1_13() != null;
        Class<?> enumParticle = IReflection.getClass(IReflection.ServerPacket.MINECRAFT_PACKAGE, "EnumParticle");
        return enumParticle.getEnumConstants().length - 1 >= this.particle.getId();
    }

    public void send(@NotNull Player player) {
        if (packet == null || location == null) return;

        if (player.getWorld() == this.location.getWorld() && (this.maxDistance <= 0 || this.location.distance(player.getLocation()) <= maxDistance)) {
            PacketUtils.sendPacket(packet, player);
        }
    }

    public void send() {
        if (packet == null || location == null) return;
        Bukkit.getOnlinePlayers().forEach(this::send);
    }

    public Particle getParticle() {
        return particle;
    }

    public Object getPacket() {
        return packet;
    }

    public Color getColor() {
        return color;
    }

    public ParticlePacket setColor(Color color) {
        this.color = color;
        return this;
    }

    @Override
    public ParticlePacket clone() {
        ParticlePacket packet = new ParticlePacket(this.particle);
        packet.setColor(this.color);
        packet.setLongDistance(this.longDistance);
        return packet;
    }

    public boolean isLongDistance() {
        return longDistance;
    }

    public void setLongDistance(boolean longDistance) {
        this.longDistance = longDistance;
    }

    public double getMaxDistance() {
        return maxDistance;
    }

    public void setMaxDistance(double maxDistance) {
        this.maxDistance = maxDistance;
    }

    public int getNoteId() {
        return noteId;
    }

    public void setNoteId(int noteId) {
        this.noteId = noteId;
    }
}
