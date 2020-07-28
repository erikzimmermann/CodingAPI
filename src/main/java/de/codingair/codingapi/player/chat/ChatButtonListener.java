package de.codingair.codingapi.player.chat;

import org.bukkit.entity.Player;

import java.util.UUID;

public interface ChatButtonListener {
    void onForeignClick(Player player, UUID id, String type);
}
