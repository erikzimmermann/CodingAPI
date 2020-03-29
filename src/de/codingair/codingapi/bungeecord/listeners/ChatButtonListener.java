package de.codingair.codingapi.bungeecord.listeners;

import de.codingair.codingapi.player.chat.ChatButton;
import net.md_5.bungee.api.connection.Connection;
import net.md_5.bungee.api.event.ChatEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

import java.util.HashMap;
import java.util.UUID;

public class ChatButtonListener implements Listener {
    private HashMap<Connection, String> ids = new HashMap<>();

    @EventHandler(priority = -64)
    public void beforeChat(ChatEvent e) {
        if(e.getMessage() == null || !e.getMessage().startsWith(ChatButton.PREFIX)) return;
        String id = e.getMessage().replace(ChatButton.PREFIX, "");
        ids.put(e.getSender(), id);

        e.setCancelled(true);
        e.setMessage("");
    }

    @EventHandler(priority = 64)
    public void afterChat(ChatEvent e) {
        String id = ids.remove(e.getSender());
        if(id != null)  {
            e.setCancelled(false);
            e.setMessage(ChatButton.PREFIX + id);
        }
    }

}
