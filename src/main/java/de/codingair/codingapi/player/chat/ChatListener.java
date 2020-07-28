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
    private Class<?> chatPacket = null;

    public ChatListener() {
        try {
            chatPacket = IReflection.getSaveClass(IReflection.ServerPacket.MINECRAFT_PACKAGE, "PacketPlayInChat");
        } catch(ClassNotFoundException ignored) {
        }

        for(Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            inject(onlinePlayer);
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        inject(e.getPlayer());
    }

    private void inject(Player player) {
        new PacketReader(player, "CHAT_BUTTON_LISTENER", API.getInstance().getMainPlugin()) {
            @Override
            public boolean readPacket(Object packet) {
                if(packet.getClass().equals(chatPacket)) {
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

                    List<SimpleMessage> messageList = API.getRemovables(null, SimpleMessage.class);

                    String finalType = type;
                    if(!messageList.isEmpty()) {
                        Bukkit.getScheduler().runTask(API.getInstance().getMainPlugin(), () -> {
                            boolean clicked = false;
                            for(SimpleMessage message : messageList) {
                                ChatButton button = message.getButton(uniqueId);
                                if(button != null) {
                                    if(button.canClick()) {
                                        if(button.getSound() != null) button.getSound().play(player);
                                        button.onClick(player);
                                    }

                                    clicked = true;
                                    break;
                                }
                            }
                            messageList.clear();

                            if(!clicked) callForeignClick(player, uniqueId, finalType);
                        });
                    } else callForeignClick(player, uniqueId, finalType);

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

    private void callForeignClick(Player player, UUID uniqueId, String type) {
        Bukkit.getScheduler().runTask(API.getInstance().getMainPlugin(), () -> {
            ChatButtonManager.onInteract(l -> l.onForeignClick(player, uniqueId, type));
        });
    }
}
