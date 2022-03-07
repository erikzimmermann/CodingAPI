package de.codingair.codingapi.particles;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.awt.*;
import java.util.Arrays;
import java.util.List;

@Deprecated
public enum Particle {
    BARRIER("barrier", "barrier", 35),
    BLOCK_CRACK("blockcrack", "block", 37, ParticleProperty.REQUIRES_DATA, ParticleProperty.NO_ANIMATION_PURPOSE),
    BLOCK_DUST("blockdust", "block", 38, ParticleProperty.REQUIRES_DATA, ParticleProperty.NO_ANIMATION_PURPOSE),
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
    FALLING_DUST("fallingdust", "falling_dust", 46, ParticleProperty.NO_ANIMATION_PURPOSE),
    FIREWORKS_SPARK("fireworksSpark", "firework", 3),
    FLAME("flame", "flame", 26),
    FOOTSTEP("footstep", 28, ParticleProperty.NO_ANIMATION_PURPOSE),
    HEART("heart", "heart", 34),
    ITEM_CRACK("iconcrack", "item", 36, ParticleProperty.REQUIRES_DATA, ParticleProperty.NO_ANIMATION_PURPOSE),
    ITEM_TAKE("take", 40, ParticleProperty.NO_ANIMATION_PURPOSE),
    LAVA("lava", "lava", 27),
    MOB_APPEARANCE("mobappearance", "elder_guardian", 41, ParticleProperty.NO_ANIMATION_PURPOSE),
    NOTE("note", "note", 23, ParticleProperty.COLORABLE),
    PORTAL("portal", "portal", 24),
    REDSTONE("reddust", "dust", 30, ParticleProperty.COLORABLE),
    SLIME("slime", "item_slime", 33),
    SMOKE_LARGE("largesmoke", "large_smoke", 12),
    SMOKE_NORMAL("smoke", "smoke", 11),
    SNOW_SHOVEL("snowshovel", "item_snowball", 32, ParticleProperty.NO_ANIMATION_PURPOSE),
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

    private static final Particle[] values = Particle.values();
    private final String name;
    private final String name_v1_13;
    private final int id;
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

    public static Particle getById(int id) {
        for (Particle particle : Particle.values()) {
            if (particle.getId() == id) return particle;
        }

        return null;
    }

    public static Particle next(int id, boolean checkAnimationPurpose) {
        return next(id, false, checkAnimationPurpose);
    }

    public static Particle next(int id, boolean skipCharacter, boolean checkAnimationPurpose) {
        Particle p = getById(id);

        if (skipCharacter) {
            for (int i = p.ordinal() + 1; i < values.length; i++) {
                if (values[i].name().charAt(0) != p.name().charAt(0)) {
                    Particle next = values[i];
                    if (!checkAnimationPurpose || !next.noAnimationPurpose()) return next;
                }
            }
        } else {
            for (int i = p.ordinal() + 1; i < values.length; i++) {
                Particle next = values[i];
                if (!checkAnimationPurpose || !next.noAnimationPurpose()) return next;
            }
        }

        return values[0];
    }

    public static Particle previous(int id, boolean checkAnimationPurpose) {
        return previous(id, false, checkAnimationPurpose);
    }

    public static Particle previous(int id, boolean skipCharacter, boolean checkAnimationPurpose) {
        Particle p = getById(id);

        if (skipCharacter) {
            for (int i = p.ordinal() - 1; i >= 0; i--) {
                if (values[i].name().charAt(0) != p.name().charAt(0)) {
                    Particle next = values[i];
                    if (!checkAnimationPurpose || !next.noAnimationPurpose()) return next;
                }
            }
        } else {
            for (int i = p.ordinal() - 1; i >= 0; i--) {
                Particle previous = values[i];
                if (!checkAnimationPurpose || !previous.noAnimationPurpose()) return previous;
            }
        }

        return values[values.length - 1];
    }

    public boolean isColorable() {
        if (properties == null) return false;
        return properties.contains(ParticleProperty.COLORABLE);
    }

    public boolean noAnimationPurpose() {
        if (properties == null) return false;
        return properties.contains(ParticleProperty.NO_ANIMATION_PURPOSE);
    }

    public boolean requiresData() {
        if (properties == null) return false;
        return properties.contains(ParticleProperty.REQUIRES_DATA);
    }

    public boolean requiresWater() {
        if (properties == null) return false;
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
        if (packet.available()) {
            packet.initialize(loc);
            packet.send();
        }
    }

    public void send(Location loc, Player... players) {
        ParticlePacket packet = new ParticlePacket(this);
        if (packet.available()) {
            packet.initialize(loc);

            for (Player player : players) {
                packet.send(player);
            }
        }
    }

    public void send(Location loc, boolean longDistance) {
        ParticlePacket packet = new ParticlePacket(this);
        if (packet.available()) {
            packet.setLongDistance(longDistance);
            packet.initialize(loc);
            packet.send();
        }
    }

    public void send(Location loc, double maxDistance) {
        ParticlePacket packet = new ParticlePacket(this);
        if (packet.available()) {
            packet.setLongDistance(true);
            packet.setMaxDistance(maxDistance);
            packet.initialize(loc);
            packet.send();
        }
    }

    public void send(Location loc, boolean longDistance, Player... players) {
        ParticlePacket packet = new ParticlePacket(this);
        if (packet.available()) {
            packet.setLongDistance(longDistance);
            packet.initialize(loc);

            for (Player player : players) {
                packet.send(player);
            }
        }
    }

    public void send(Location loc, Color color) {
        ParticlePacket packet = new ParticlePacket(this);
        if (packet.available()) {
            packet.setColor(color);
            packet.initialize(loc);
            packet.send();
        }
    }

    public void send(Location loc, Color color, boolean longDistance) {
        ParticlePacket packet = new ParticlePacket(this);
        if (packet.available()) {
            packet.setLongDistance(longDistance);
            packet.setColor(color);
            packet.initialize(loc);
            packet.send();
        }
    }

    public void send(Location loc, Color color, int noteColor, boolean longDistance) {
        ParticlePacket packet = new ParticlePacket(this);
        if (packet.available()) {
            packet.setLongDistance(longDistance);
            packet.setColor(color);
            packet.setNoteId(noteColor);
            packet.initialize(loc);
            packet.send();
        }
    }

    public void send(Location loc, Color color, int noteColor, boolean longDistance, double maxDistance) {
        ParticlePacket packet = new ParticlePacket(this);
        if (packet.available()) {
            packet.setLongDistance(longDistance);
            packet.setColor(color);
            packet.setNoteId(noteColor);
            packet.setMaxDistance(maxDistance);
            packet.initialize(loc);
            packet.send();
        }
    }

    public void send(Location loc, Color color, Player... players) {
        ParticlePacket packet = new ParticlePacket(this);
        if (packet.available()) {
            packet.setColor(color);
            packet.initialize(loc);

            for (Player player : players) {
                packet.send(player);
            }
        }
    }

    public void send(Location loc, Color color, int noteColor, Player... players) {
        ParticlePacket packet = new ParticlePacket(this);
        if (packet.available()) {
            packet.setColor(color);
            packet.setNoteId(noteColor);
            packet.initialize(loc);

            for (Player player : players) {
                packet.send(player);
            }
        }
    }

    public void send(Location loc, Color color, boolean longDistance, Player... players) {
        ParticlePacket packet = new ParticlePacket(this);
        if (packet.available()) {
            packet.setLongDistance(longDistance);
            if (color != null) packet.setColor(color);
            packet.initialize(loc);

            for (Player player : players) {
                packet.send(player);
            }
        }
    }

    public void send(Location loc, Color color, int noteColor, boolean longDistance, Player... players) {
        ParticlePacket packet = new ParticlePacket(this);
        if (packet.available()) {
            packet.setLongDistance(longDistance);
            if (color != null) packet.setColor(color);
            packet.setNoteId(noteColor);
            packet.initialize(loc);

            for (Player player : players) {
                packet.send(player);
            }
        }
    }

    public void send(Location loc, Color color, int noteColor, boolean longDistance, double maxDistance, Player... players) {
        ParticlePacket packet = new ParticlePacket(this);
        if (packet.available()) {
            packet.setLongDistance(longDistance);
            packet.setMaxDistance(maxDistance);
            if (color != null) packet.setColor(color);
            packet.setNoteId(noteColor);
            packet.initialize(loc);

            for (Player player : players) {
                packet.send(player);
            }
        }
    }

    public ParticlePacket getParticlePacket(Location loc) {
        ParticlePacket packet = new ParticlePacket(this);
        packet.initialize(loc);
        return packet;
    }

    public String getName_v1_13() {
        return name_v1_13;
    }

    public Particle next(boolean checkAnimationPurpose) {
        return next(false, checkAnimationPurpose);
    }

    public Particle next(boolean skipCharacter, boolean checkAnimationPurpose) {
        return next(id, skipCharacter, checkAnimationPurpose);
    }

    public Particle previous(boolean checkAnimationPurpose) {
        return previous(false, checkAnimationPurpose);
    }

    public Particle previous(boolean skipCharacter, boolean checkAnimationPurpose) {
        return previous(id, skipCharacter, checkAnimationPurpose);
    }
}
