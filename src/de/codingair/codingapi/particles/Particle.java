package de.codingair.codingapi.particles;

import de.codingair.codingapi.server.Sound;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.awt.*;
import java.util.Arrays;
import java.util.List;

/**
 * Removing of this disclaimer is forbidden.
 *
 * @author codingair
 * @verions: 1.0.0
 **/

public enum Particle {
    BARRIER("barrier", "barrier", 35),
    BLOCK_CRACK("blockcrack", "block", 37, ParticleProperty.REQUIRES_DATA),
    BLOCK_DUST("blockdust", "block", 38, ParticleProperty.REQUIRES_DATA),
    CLOUD("cloud", "cloud", 29),
    CRIT("crit", "crit", 9),
    CRIT_MAGIC("magicCrit", "enchanted_hit", 10),
    DAMAGE_INDICATOR("damageIndicator", "damage_indicator", 44),
    DRAGON_BREATH("dragonbreath", "dragon_breath", 42),
    DRIP_LAVA("dripLava", "dripping_lava", 19),
    DRIP_WATER("dripWater", "dripping_water", 18),
    ENCHANTMENT_TABLE("enchantmenttable", "enchant", 25),
    END_ROD("endRod", "end_rod", 43),
    EXPLOSION_HUGE("hugeexplosion", "explosion_emitter", 2),
    EXPLOSION_LARGE("largeexplode", "explosion", 1),
    EXPLOSION_NORMAL("explode", "poof", 0),
    FALLING_DUST("fallingdust", "falling_dust", 46),
    FIREWORKS_SPARK("fireworksSpark", "firework", 3),
    FLAME("flame", "flame", 26),
    FOOTSTEP("footstep", 28),
    HEART("heart", "heart", 34),
    ITEM_CRACK("iconcrack", "item", 36, ParticleProperty.REQUIRES_DATA),
    ITEM_TAKE("take", 40),
    LAVA("lava", "lava", 27),
    MOB_APPEARANCE("mobappearance", "elder_guardian", 41),
    NOTE("note", "note", 23, ParticleProperty.COLORABLE),
    PORTAL("portal", "portal", 24),
    REDSTONE("reddust", "dust", 30, ParticleProperty.COLORABLE),
    SLIME("slime", "item_slime", 33),
    SMOKE_LARGE("largesmoke", "large_smoke", 12),
    SMOKE_NORMAL("smoke", "smoke", 11),
    SNOW_SHOVEL("snowshovel", "item_snowball", 32),
    SNOWBALL("snowballpoof", "item_snowball", 31),
    SPELL("spell", "effect", 13),
    SPELL_INSTANT("instantSpell", "instant_effect", 14),
    SPELL_MOB("mobSpell", "entity_effect", 15, ParticleProperty.COLORABLE),
    SPELL_MOB_AMBIENT("mobSpellAmbient", "ambient_entity_effect", 16, ParticleProperty.COLORABLE),
    SPELL_WITCH("witchMagic", "witch", 17),
    SUSPENDED("suspended", "underwater", 7, ParticleProperty.REQUIRES_WATER),
    SUSPENDED_DEPTH("depthsuspend", "underwater", 8),
    SWEEP_ATTACK("sweepAttack", "sweep_attack", 45),
    TOWN_AURA("townaura", "mycelium", 22),
    VILLAGER_ANGRY("angryVillager", "angry_villager", 20),
    VILLAGER_HAPPY("happyVillager", "happy_villager", 21),
    WATER_BUBBLE("bubble", "bubble", 4, ParticleProperty.REQUIRES_WATER),
    WATER_DROP("droplet", "rain", 39),
    WATER_SPLASH("splash", "splash", 5),
    WATER_WAKE("wake", "fishing", 6);

    private String name;
    private String name_v1_13;
    private int id;
    private List<ParticleProperty> properties = null;

    Particle(String name, int id) {
        this.name = name;
        this.name_v1_13 = null;
        this.id = id;
    }

    Particle(String name, int id, ParticleProperty... property) {
        this.name = name;
        this.name_v1_13 = null;
        this.id = id;
        properties = Arrays.asList(property);
    }

    Particle(String name, String name_v1_13, int id) {
        this.name = name;
        this.name_v1_13 = name_v1_13;
        this.id = id;
    }

    Particle(String name, String name_v1_13, int id, ParticleProperty... property) {
        this.name = name;
        this.name_v1_13 = name_v1_13;
        this.id = id;
        properties = Arrays.asList(property);
    }

    public boolean isColorable() {
        if(properties == null) return false;
        return properties.contains(ParticleProperty.COLORABLE);
    }

    public boolean requiresData() {
        if(properties == null) return false;
        return properties.contains(ParticleProperty.REQUIRES_DATA);
    }

    public boolean requiresWater() {
        if(properties == null) return false;
        return properties.contains(ParticleProperty.REQUIRES_WATER);
    }

    public String getName() {
        return name;
    }

    public int getId() {
        return id;
    }

    public List<ParticleProperty> getProperties() {
        return properties;
    }

    public void send(Location loc) {
        ParticlePacket packet = new ParticlePacket(this);
        if(packet.available()) {
            packet.initialize(loc);
            packet.send();
        }
    }

    public void send(Location loc, Player... players) {
        ParticlePacket packet = new ParticlePacket(this);
        if(packet.available()) {
            packet.initialize(loc);

            for(Player player : players) {
                packet.send(player);
            }
        }
    }

    public void send(Location loc, boolean longDistance) {
        ParticlePacket packet = new ParticlePacket(this);
        if(packet.available()) {
            packet.setLongDistance(longDistance);
            packet.initialize(loc);
            packet.send();
        }
    }

    public void send(Location loc, double maxDistance) {
        ParticlePacket packet = new ParticlePacket(this);
        if(packet.available()) {
            packet.setLongDistance(true);
            packet.setMaxDistance(maxDistance);
            packet.initialize(loc);
            packet.send();
        }
    }

    public void send(Location loc, boolean longDistance, Player... players) {
        ParticlePacket packet = new ParticlePacket(this);
        if(packet.available()) {
            packet.setLongDistance(longDistance);
            packet.initialize(loc);

            for(Player player : players) {
                packet.send(player);
            }
        }
    }

    public void send(Location loc, Color color) {
        ParticlePacket packet = new ParticlePacket(this);
        if(packet.available()) {
            packet.setColor(color);
            packet.initialize(loc);
            packet.send();
        }
    }

    public void send(Location loc, Color color, boolean longDistance) {
        ParticlePacket packet = new ParticlePacket(this);
        if(packet.available()) {
            packet.setLongDistance(longDistance);
            packet.setColor(color);
            packet.initialize(loc);
            packet.send();
        }
    }

    public void send(Location loc, Color color, Player... players) {
        ParticlePacket packet = new ParticlePacket(this);
        if(packet.available()) {
            packet.setColor(color);
            packet.initialize(loc);

            for(Player player : players) {
                packet.send(player);
            }
        }
    }

    public void send(Location loc, Color color, boolean longDistance, Player... players) {
        ParticlePacket packet = new ParticlePacket(this);
        if(packet.available()) {
            packet.setLongDistance(longDistance);
            if(color != null) packet.setColor(color);
            packet.initialize(loc);

            for(Player player : players) {
                packet.send(player);
            }
        }
    }

    public ParticlePacket getParticlePacket(Location loc) {
        ParticlePacket packet = new ParticlePacket(this);
        packet.initialize(loc);
        return packet;
    }

    public static Particle getById(int id) {
        for(Particle particle : Particle.values()) {
            if(particle.getId() == id) return particle;
        }

        return null;
    }

    public String getName_v1_13() {
        return name_v1_13;
    }

    public Particle next() {
        return next(false);
    }

    public Particle next(boolean skipCharacter) {
        return next(id, skipCharacter);
    }

    public Particle previous() {
        return previous(false);
    }

    public Particle previous(boolean skipCharacter) {
        return previous(id, skipCharacter);
    }

    public static Particle next(int id) {
        return next(id, false);
    }

    public static Particle next(int id, boolean skipCharacter) {
        if(skipCharacter) {
            Particle p = getById(id);

            int last = -1;
            for(int i = 0; i < Sound.values().length; i++) {
                if(values()[i] == p) {
                    last = i;
                } else if(last >= 0 && values()[i].name().charAt(0) != p.name().charAt(0)) {
                    return values()[i];
                }
            }

            return values()[0];
        } else {
            for(int i = 0; i < values().length; i++) {
                if(values()[i].getId() == id) return i + 1 == values().length ? values()[0] : values()[i + 1];
            }

            throw new IllegalArgumentException("Couldn't found AnimationType with id=" + id);
        }
    }

    public static Particle previous(int id) {
        return previous(id, false);
    }

    public static Particle previous(int id, boolean skipCharacter) {
        if(skipCharacter) {
            int last = -1;
            Particle p = getById(id);

            for(int i = 0; i < values().length; i++) {
                if(values()[i].name().charAt(0) == p.name().charAt(0)) {
                    return last == -1 ? values()[values().length - 1] : values()[last];
                } else last = i;
            }

            throw new IllegalArgumentException("Couldn't found AnimationType with id=" + id);
        } else {
            for(int i = 0; i < values().length; i++) {
                if(values()[i].getId() == id) {
                    return i - 1 < 0 ? values()[values().length - 1] : values()[i - 1];
                }
            }

            throw new IllegalArgumentException("Couldn't found AnimationType with id=" + id);
        }
    }
}
