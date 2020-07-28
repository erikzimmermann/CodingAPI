package de.codingair.codingapi.particles;

import de.codingair.codingapi.server.reflections.IReflection;
import de.codingair.codingapi.server.reflections.PacketUtils;
import de.codingair.codingapi.server.specification.Version;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.awt.*;
import java.lang.reflect.Constructor;

/**
 * Removing of this disclaimer is forbidden.
 *
 * @author codingair
 * @verions: 1.0.0
 **/

public class ParticlePacket {
    private final Particle particle;
    private Object packet;
    private Color color = null;
    private boolean longDistance = false;
    private Location location;
    private double maxDistance = 0;
    private int noteId = 0;

    public ParticlePacket(Particle particle) {
        this.particle = particle;
    }

    public ParticlePacket(Particle particle, Color color, boolean longDistance) {
        this.particle = particle;
        this.color = color;
        this.longDistance = longDistance;
    }

    public ParticlePacket initialize(Location loc) {
        if(!available()) return this;
        this.location = loc;

        Class<?> packetClass = IReflection.getClass(IReflection.ServerPacket.MINECRAFT_PACKAGE, "PacketPlayOutWorldParticles");
        Constructor packetConstructor = IReflection.getConstructor(packetClass).getConstructor();

        if(Version.get().isBiggerThan(Version.v1_12)) {
            Class<?> packetPlayOutWorldParticles = IReflection.getClass(IReflection.ServerPacket.MINECRAFT_PACKAGE, "PacketPlayOutWorldParticles");
            Class<?> particleParam = IReflection.getClass(IReflection.ServerPacket.MINECRAFT_PACKAGE, "ParticleParam");
            Class<?> craftParticle = IReflection.getClass(IReflection.ServerPacket.CRAFTBUKKIT_PACKAGE, "CraftParticle");
            Class<?> dustOptions = IReflection.getClass(IReflection.ServerPacket.BUKKIT_PACKET, "Particle$DustOptions");
            IReflection.ConstructorAccessor packetCon = IReflection.getConstructor(packetPlayOutWorldParticles);
            IReflection.MethodAccessor toNMS = IReflection.getMethod(craftParticle, "toNMS", particleParam, new Class[] {org.bukkit.Particle.class, Object.class});

            Object data = null;
            float offsetX = 0, offsetY = 0, offsetZ = 0, extra = 0;
            int count = 1;
            if(this.color != null) {
                if(particle == Particle.REDSTONE)
                    data = IReflection.getConstructor(dustOptions, org.bukkit.Color.class, float.class).newInstance(org.bukkit.Color.fromRGB(this.color.getRed(), this.color.getGreen(), this.color.getBlue()), 1);
                else if(particle == Particle.NOTE) {
                    count = 0;
                    offsetX = noteId / 24F;
                    extra = 1F;
                } else if(particle == Particle.SPELL_MOB || particle == Particle.SPELL_MOB_AMBIENT) {
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
            } catch(Exception ex) {
                return this;
            }

            packet = packetCon.newInstance();

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
            } catch(IllegalAccessException | NoSuchFieldException e1) {
                e1.printStackTrace();
            }
        } else {
            Class<?> enumParticle = IReflection.getClass(IReflection.ServerPacket.MINECRAFT_PACKAGE, "EnumParticle");

            ParticleData data = null;

            if(particle.requiresData()) {
                Location below = loc.clone();
                below.setY(loc.getBlockY() - 0.49);

                //noinspection deprecation
                data = new ParticleData(below.getBlock().getType(), below.getBlock().getData());
            }

            if(particle.requiresWater() && !loc.getBlock().getType().equals(Material.WATER) && !loc.getBlock().getType().equals(Material.valueOf("STATIONARY_WATER"))) return this;

            float e = 0, f = 0, g = 0, h = 0;
            int i = 1;
            if(particle.isColorable() && this.color != null) {
                e = (float) this.color.getRed() / 255F;

                if(e == 0) e = 0.003921569F;

                f = (float) this.color.getGreen() / 255F;
                g = (float) this.color.getBlue() / 255F;
                h = 1F;
                i = 0;
            }

            try {
                packet = packetConstructor.newInstance();

                IReflection.setValue(packet, "a", enumParticle.getEnumConstants()[particle.getId()]);
                IReflection.setValue(packet, "j", this.longDistance);

                if(data != null) {
                    int[] packetData = data.getPacketData();
                    IReflection.setValue(packet, "k", particle == Particle.ITEM_CRACK ? packetData : new int[] {packetData[0] | (packetData[1] << 12)});
                }

                IReflection.setValue(packet, "b", (float) this.location.getX());
                IReflection.setValue(packet, "c", (float) this.location.getY());
                IReflection.setValue(packet, "d", (float) this.location.getZ());
                IReflection.setValue(packet, "e", e);
                IReflection.setValue(packet, "f", f);
                IReflection.setValue(packet, "g", g);
                IReflection.setValue(packet, "h", h);
                IReflection.setValue(packet, "i", i);
            } catch(Exception exception) {
                exception.printStackTrace();
            }
        }

        return this;
    }

    public boolean available() {
        if(Version.get().isBiggerThan(Version.v1_12)) return this.particle != null && this.particle.getName_v1_13() != null;
        Class<?> enumParticle = IReflection.getClass(IReflection.ServerPacket.MINECRAFT_PACKAGE, "EnumParticle");
        return enumParticle.getEnumConstants().length - 1 >= this.particle.getId();
    }

    public void send(Player... p) {
        if(packet == null || location == null) return;

        for(Player player : p) {
            if(player.getWorld() == this.location.getWorld() && (this.maxDistance <= 0 || this.location.distance(player.getLocation()) <= maxDistance)) {
                PacketUtils.sendPacket(packet, player);
            }
        }
    }

    public void send() {
        if(packet == null || location == null) return;
        send(Bukkit.getOnlinePlayers().toArray(new Player[0]));
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
