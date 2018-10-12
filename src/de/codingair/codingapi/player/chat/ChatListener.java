package de.codingair.codingapi.player.chat;

import de.codingair.codingapi.API;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.List;
import java.util.UUID;

public class ChatListener implements Listener {

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onCommandPreProcess(AsyncPlayerChatEvent e) {
        if(e.getMessage() == null || !e.getMessage().startsWith("CodingAPI|ChatAPI|Button|")) return;
        UUID uniqueId = UUID.fromString(e.getMessage().replace("CodingAPI|ChatAPI|Button|", ""));

        e.setCancelled(true);
        e.setMessage(null);
        e.getRecipients().clear();

        List<SimpleMessage> messageList = API.getRemovables(SimpleMessage.class);

        for(SimpleMessage message : messageList) {
            ChatButton button = message.getButton(uniqueId);
            if(button != null) {
                button.onClick(e.getPlayer());
            }
        }
    }

}
