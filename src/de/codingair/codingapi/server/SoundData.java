package de.codingair.codingapi.server;

import org.bukkit.Location;
import org.bukkit.entity.Player;

public class SoundData {
    private Sound sound;
    private float volume;
    private float pitch;

    public SoundData(Sound sound, float volume, float pitch) {
        this.sound = sound;
        this.volume = volume;
        this.pitch = pitch;
    }

    public Sound getSound() {
        return sound;
    }

    public void setSound(Sound sound) {
        this.sound = sound;
    }

    public float getVolume() {
        return volume;
    }

    public void setVolume(float volume) {
        this.volume = volume;
    }

    public float getPitch() {
        return pitch;
    }

    public void setPitch(float pitch) {
        this.pitch = pitch;
    }

    public void play(Player player) {
        this.sound.playSound(player, this.volume, this.pitch);
    }

    public void play(Player player, Location location) {
        this.sound.playSound(player, location == null ? player.getLocation() : location, this.volume, this.pitch);
    }

    public SoundData clone() {
        return new SoundData(this.sound, volume, pitch);
    }
}
