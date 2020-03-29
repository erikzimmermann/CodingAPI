package de.codingair.codingapi.player.chat;

import de.codingair.codingapi.API;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.List;
import java.util.UUID;

public class ChatListener implements Listener {

    @EventHandler(priority = EventPriority.LOWEST)
    public void onCommandPreProcess(AsyncPlayerChatEvent e) {
        if(e.getMessage() == null || !e.getMessage().startsWith(ChatButton.PREFIX)) return;
        String type = null;
        UUID uniqueId;

        if(e.getMessage().contains("#")) {
            String[] a = e.getMessage().split("#");
            uniqueId = UUID.fromString(a[0].replace(ChatButton.PREFIX, ""));
            type = a[1];
        } else uniqueId = UUID.fromString(e.getMessage().replace(ChatButton.PREFIX, ""));

        e.setCancelled(true);
        e.setMessage(null);
        e.getRecipients().clear();

        List<SimpleMessage> messageList = API.getRemovables(SimpleMessage.class);

        String finalType = type;
        if(!messageList.isEmpty()) {
            Bukkit.getScheduler().runTask(API.getInstance().getMainPlugin(), () -> {
                boolean clicked = false;
                for(SimpleMessage message : messageList) {
                    ChatButton button = message.getButton(uniqueId);
                    if(button != null) {
                        button.onClick(e.getPlayer());
                        clicked = true;
                        break;
                    }
                }

                if(!clicked) {
                    ChatButtonManager.onInteract(l -> {
                        l.onForeignClick(e.getPlayer(), uniqueId, finalType);
                    });
                }

                messageList.clear();
            });
        } else {
            ChatButtonManager.onInteract(l -> {
                l.onForeignClick(e.getPlayer(), uniqueId, finalType);
            });
        }
    }

}
