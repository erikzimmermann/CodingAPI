package de.codingair.codingapi.player.chat;

import org.bukkit.entity.Player;

import java.util.UUID;

public interface ChatButtonListener {
    /**
     * @param player The player that clicked on this button.
     * @param id     The id of the chat button.
     * @param type   The type of the chat button.
     * @return True if this click was used. False if this click can be forwarded to other packet readers.
     */
    boolean onAsyncClick(Player player, UUID id, String type);
}
