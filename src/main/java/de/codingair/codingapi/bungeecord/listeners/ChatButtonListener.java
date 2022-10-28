package de.codingair.codingapi.bungeecord.listeners;

import de.codingair.codingapi.player.chat.ChatButton;
import net.md_5.bungee.api.connection.Connection;
import net.md_5.bungee.api.event.ChatEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

import java.util.HashMap;

public class ChatButtonListener implements Listener {
    private final HashMap<Connection, String> ids = new HashMap<>();

    @EventHandler (priority = -100)
    public void beforeChat(ChatEvent e) {
        if (!ChatButton.isChatButton(e.getMessage())) return;
        String id = e.getMessage();
        ids.put(e.getSender(), id);

        e.setCancelled(true);
        e.setMessage("");
    }

    @EventHandler (priority = 100)
    public void afterChat(ChatEvent e) {
        String id = ids.remove(e.getSender());
        if (id != null) {
            e.setCancelled(false);
            e.setMessage(id);
        }
    }

}
