package de.codingair.codingapi.server.sounds;

import de.codingair.codingapi.server.specification.Version;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class SoundData {
    private Sound sound;
    private String soundName;
    private float volume;
    private float pitch;

    public SoundData(@NotNull Sound sound, float volume, float pitch) {
        this.sound = sound;
        this.volume = volume;
        this.pitch = pitch;
    }

    public SoundData(@NotNull String soundName, float volume, float pitch) {
        this.soundName = soundName;
        this.volume = volume;
        this.pitch = pitch;
    }

    @Nullable
    public Sound getSound() {
        return sound;
    }

    public void setSound(@Nullable Sound sound) {
        this.sound = sound;
    }

    @Nullable
    public String getSoundName() {
        return soundName;
    }

    public void setSoundName(@Nullable String soundName) {
        this.soundName = soundName;
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

    public void play(@NotNull Player player) {
        if (sound != null) {
            if (!sound.isSupported()) return;
            this.sound.playSound(player, this.volume, this.pitch);
        } else if (soundName != null) {
            player.playSound(player.getLocation(), this.soundName, this.volume, this.pitch);
        }
    }

    public void stop(@NotNull Player player) {
        if (Version.before(9)) return;

        if (sound != null) this.sound.stopSound(player);
        else if (soundName != null) player.stopSound(this.soundName);
    }

    @NotNull
    public SoundData clone() {
        return new SoundData(this.sound, volume, pitch);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SoundData soundData = (SoundData) o;
        return Float.compare(soundData.volume, volume) == 0 &&
                Float.compare(soundData.pitch, pitch) == 0 &&
                Objects.equals(soundName, soundData.soundName) &&
                sound == soundData.sound;
    }

    @Override
    public int hashCode() {
        return Objects.hash(sound, soundName, volume, pitch);
    }
}
