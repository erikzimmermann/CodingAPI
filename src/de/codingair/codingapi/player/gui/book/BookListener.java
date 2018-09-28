package de.codingair.codingapi.player.gui.book;

import de.codingair.codingapi.API;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.List;
import java.util.UUID;

public class BookListener implements Listener {

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onCommandPreProcess(AsyncPlayerChatEvent e) {
        if(e.getMessage() == null || !e.getMessage().startsWith("CodingAPI|BookAPI|Button|")) return;
        UUID uniqueId = UUID.fromString(e.getMessage().replace("CodingAPI|BookAPI|Button|", ""));

        e.setCancelled(true);
        e.setMessage(null);
        e.getRecipients().clear();

        List<Book> bookList = API.getRemovables(Book.class);

        for(Book book : bookList) {
            Button button = book.getButton(uniqueId);
            if(button != null) {
                if(button.onClick(e.getPlayer())) {
                    book.update();
                }
            }
        }
    }

}
