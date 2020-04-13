package de.codingair.codingapi.player.chat;

import de.codingair.codingapi.API;
import de.codingair.codingapi.player.data.PacketReader;
import de.codingair.codingapi.server.reflections.IReflection;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.List;
import java.util.UUID;

public class ChatListener implements Listener {
    public ChatListener() {
        for(Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            inject(onlinePlayer);
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        inject(e.getPlayer());
    }

    private void inject(Player player) {
        new PacketReader(player, "WARPSYSTEM_CHAT_BUTTON_LISTENER", API.getInstance().getMainPlugin()) {
            @Override
            public boolean readPacket(Object packet) {
                if(packet.getClass().getSimpleName().equals("PacketPlayInChat")) {
                    IReflection.FieldAccessor<String> aField = IReflection.getField(packet.getClass(), "a");
                    String msg = aField.get(packet);
                    
                    if(msg == null || !msg.startsWith(ChatButton.PREFIX)) return false;
                    String type = null;
                    UUID uniqueId;

                    if(msg.contains("#")) {
                        String[] a = msg.split("#");
                        uniqueId = UUID.fromString(a[0].replace(ChatButton.PREFIX, ""));
                        type = a[1];
                    } else uniqueId = UUID.fromString(msg.replace(ChatButton.PREFIX, ""));

                    List<SimpleMessage> messageList = API.getRemovables(SimpleMessage.class);

                    String finalType = type;
                    if(!messageList.isEmpty()) {
                        Bukkit.getScheduler().runTask(API.getInstance().getMainPlugin(), () -> {
                            boolean clicked = false;
                            for(SimpleMessage message : messageList) {
                                ChatButton button = message.getButton(uniqueId);
                                if(button != null) {
                                    button.onClick(player);
                                    clicked = true;
                                    break;
                                }
                            }

                            if(!clicked) {
                                ChatButtonManager.onInteract(l -> l.onForeignClick(player, uniqueId, finalType));
                            }

                            messageList.clear();
                        });
                    } else ChatButtonManager.onInteract(l -> l.onForeignClick(player, uniqueId, finalType));

                    return true;
                }
                
                return false;
            }

            @Override
            public boolean writePacket(Object packet) {
                return false;
            }
        }.inject();
    }
}
