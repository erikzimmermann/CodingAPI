package de.codingair.codingapi.server.sounds;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.Objects;

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

    public void stop(Player player) {
        this.sound.stopSound(player);
    }

    public SoundData clone() {
        return new SoundData(this.sound, volume, pitch);
    }

    @Override
    public boolean equals(Object o) {
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;
        SoundData soundData = (SoundData) o;
        return Float.compare(soundData.volume, volume) == 0 &&
                Float.compare(soundData.pitch, pitch) == 0 &&
                sound == soundData.sound;
    }

    @Override
    public int hashCode() {
        return Objects.hash(sound, volume, pitch);
    }
}
