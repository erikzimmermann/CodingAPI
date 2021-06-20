package de.codingair.codingapi.player.chat;

import de.codingair.codingapi.API;
import de.codingair.codingapi.player.data.PacketReader;
import de.codingair.codingapi.server.reflections.IReflection;
import de.codingair.codingapi.server.specification.Version;
import net.minecraft.network.protocol.game.PacketPlayInChat;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.List;
import java.util.UUID;

public class ChatListener implements Listener {
    private static final Class<?> chatPacket;
    private static final IReflection.FieldAccessor<String> text;

    static {
        chatPacket = IReflection.getClass(IReflection.ServerPacket.PACKETS, "PacketPlayInChat");
        text = IReflection.getField(chatPacket, Version.since(17, "a", "b"));
    }

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
        new PacketReader(player, "CHAT_BUTTON_LISTENER", API.getInstance().getMainPlugin()) {
            @Override
            public boolean readPacket(Object packet) {
                if(packet.getClass().equals(chatPacket)) {
                    String msg = text.get(packet);

                    if(msg == null || !msg.startsWith(ChatButton.PREFIX)) return false;
                    String type = null;
                    UUID uniqueId;

                    if(msg.contains("#")) {
                        String[] a = msg.split("#");
                        uniqueId = UUID.fromString(a[0].replace(ChatButton.PREFIX, ""));
                        type = a[1];
                    } else uniqueId = UUID.fromString(msg.replace(ChatButton.PREFIX, ""));

                    List<SimpleMessage> messageList = API.getRemovables(null, SimpleMessage.class);
                    handleSimpleMessages(type, uniqueId, player, messageList);

                    messageList = API.getRemovables(player, SimpleMessage.class);
                    handleSimpleMessages(type, uniqueId, player, messageList);
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

    private void handleSimpleMessages(String type, UUID uniqueId, Player player, List<SimpleMessage> messageList) {
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

                if(!clicked) callForeignClick(player, uniqueId, type);
            });
        } else callForeignClick(player, uniqueId, type);
    }

    private void callForeignClick(Player player, UUID uniqueId, String type) {
        Bukkit.getScheduler().runTask(API.getInstance().getMainPlugin(), () -> {
            ChatButtonManager.onInteract(l -> l.onForeignClick(player, uniqueId, type));
        });
    }
}
