package de.codingair.codingapi.player.title;

import de.codingair.codingapi.API;
import de.codingair.codingapi.player.MessageAPI;
import de.codingair.codingapi.utils.Removable;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.UUID;

public class Title implements Removable {
    private UUID uuid = UUID.randomUUID();
    private JavaPlugin plugin;
    private Player player;
    private String title;
    private int maxTime;
    private int fadeIn;
    private int fadeOut;
    private boolean sent = false;

    public Title(JavaPlugin plugin, Player player, String title, int maxTime, int fadeIn, int fadeOut) {
        this.plugin = plugin;
        this.player = player;
        this.title = title;
        this.maxTime = maxTime + fadeIn + fadeOut;
        this.fadeIn = fadeIn;
        this.fadeOut = fadeOut;
    }

    @Override
    public void destroy() {
        MessageAPI.sendTitle(player, "", "", 0, 0, 0);
        API.removeRemovable(this);
    }

    @Override
    public Class<? extends Removable> getAbstractClass() {
        return Title.class;
    }

    @Override
    public UUID getUniqueId() {
        return this.uuid;
    }

    @Override
    public JavaPlugin getPlugin() {
        return this.plugin;
    }
    
    public void update(String message) {
        if(!sent) {
            API.addRemovable(this);
            MessageAPI.sendTitle(player, this.title, message, this.fadeIn, this.maxTime, this.fadeOut, false, false, true);
            sent = true;

            Bukkit.getScheduler().runTaskLater(this.plugin, () -> {
                sent = false;
                API.removeRemovable(Title.this);
            }, this.maxTime * 20);
        } else {
            MessageAPI.sendTitle(player, null, message, 0, this.maxTime, 0, false, false, false);
        }
    }

    public Player getPlayer() {
        return player;
    }

    public String getTitle() {
        return title;
    }

    public int getMaxTime() {
        return maxTime;
    }

    public int getFadeIn() {
        return fadeIn;
    }

    public int getFadeOut() {
        return fadeOut;
    }
}
