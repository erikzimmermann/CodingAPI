package de.codingair.codingapi.server.fancymessages;

import de.codingair.codingapi.server.DefaultFontInfo;
import de.codingair.codingapi.utils.TextAlignment;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Removing of this disclaimer is forbidden.
 *
 * @author BubbleEgg
 * @verions: 1.0.0
 **/

public class FancyMessage {
    private Player player;
    private List<String> messages = new ArrayList<>();
    private MessageTypes type;
    private boolean autoSize = true;
    private TextAlignment alignment = TextAlignment.LEFT;
    private boolean centered = false;

    public FancyMessage() {

    }

    public FancyMessage(Player p, MessageTypes type, boolean autoSize, String... messages) {
        this.player = p;
        this.type = type;
        this.autoSize = autoSize;
        this.messages.addAll(Arrays.asList(messages));
    }

    public FancyMessage(MessageTypes type, boolean autoSize, String... messages) {
        this.type = type;
        this.autoSize = autoSize;
        this.messages.addAll(Arrays.asList(messages));
    }

    public void send() {
        send(this.player);
    }

    public void send(Player p) {
        switch(this.type) {
            case INFO_MESSAGE: {
                sendInfoMessage((this.player = p) == null);
                break;
            }
        }
    }

    public void addMessages(String... messages) {
        this.messages.addAll(Arrays.asList(messages));
    }

    public void setMessages(String... messages) {
        this.messages.clear();
        this.messages.addAll(Arrays.asList(messages));
    }

    public List<String> getMessages() {
        return this.messages;
    }

    public void setType(MessageTypes type) {
        this.type = type;
    }

    public MessageTypes getType() {
        return this.type;
    }

    public void setAutoSize(boolean autoSize) {
        this.autoSize = autoSize;
    }

    private void checkTestAlignments() {
        List<String> temp = new ArrayList<>();

        if(centered) {
            int largest = 0;
            for(String message : this.messages) {
                int length = DefaultFontInfo.getExactLength(message);
                if(length > largest) largest = length;
            }

            double spaces = (DefaultFontInfo.CHAT.getLength() / 2 - largest / 2) / (DefaultFontInfo.SPACE.getLength() + 0.5);

            for(String s : this.messages) {
                for(int i = 0; i < spaces; i++) {
                    s = " " + s;
                }

                temp.add(s);
            }

            this.messages.clear();
            this.messages.addAll(temp);
            temp.clear();
        }


        temp = this.alignment.apply(this.messages);
        this.messages.clear();
        this.messages.addAll(temp);
        temp.clear();
    }

    private void sendInfoMessage(boolean broadcast) {
        checkTestAlignments();

        if(broadcast) {
            Bukkit.broadcastMessage(ChatColor.COLOR_CHAR + "7" + ChatColor.COLOR_CHAR + "m" + ChatColor.COLOR_CHAR + "l↔↔↔↔↔↔↔↔↔↔↔↔↔↔↔↔↔↔↔↔↔↔↔↔↔↔↔↔↔↔↔↔↔↔↔↔↔↔↔↔↔↔↔↔↔↔↔↔↔↔↔↔↔");

            if(this.messages.size() <= 7) {
                Bukkit.broadcastMessage("");
            }

            for(String message : this.messages) {
                Bukkit.broadcastMessage(message);
            }

            if(autoSize) {
                for(int i = 0; i < 6 - messages.size(); i++) {
                    Bukkit.broadcastMessage("");
                }
            }

            if(this.messages.size() <= 6) {
                Bukkit.broadcastMessage("");
            }

            Bukkit.broadcastMessage(ChatColor.COLOR_CHAR + "7" + ChatColor.COLOR_CHAR + "m" + ChatColor.COLOR_CHAR + "l↔↔↔↔↔↔↔↔↔↔↔↔↔↔↔↔↔↔↔↔↔↔↔↔↔↔↔↔↔↔↔↔↔↔↔↔↔↔↔↔↔↔↔↔↔↔↔↔↔↔↔↔↔");
        } else {
            this.player.sendMessage(ChatColor.COLOR_CHAR + "7" + ChatColor.COLOR_CHAR + "m" + ChatColor.COLOR_CHAR + "l↔↔↔↔↔↔↔↔↔↔↔↔↔↔↔↔↔↔↔↔↔↔↔↔↔↔↔↔↔↔↔↔↔↔↔↔↔↔↔↔↔↔↔↔↔↔↔↔↔↔↔↔↔");

            if(this.messages.size() <= 7) {
                this.player.sendMessage("");
            }

            for(String message : this.messages) {
                this.player.sendMessage(message);
            }

            if(autoSize) {
                for(int i = 0; i < 6 - messages.size(); i++) {
                    this.player.sendMessage("");
                }
            }

            if(this.messages.size() <= 6) {
                this.player.sendMessage("");
            }

            this.player.sendMessage(ChatColor.COLOR_CHAR + "7" + ChatColor.COLOR_CHAR + "m" + ChatColor.COLOR_CHAR + "l↔↔↔↔↔↔↔↔↔↔↔↔↔↔↔↔↔↔↔↔↔↔↔↔↔↔↔↔↔↔↔↔↔↔↔↔↔↔↔↔↔↔↔↔↔↔↔↔↔↔↔↔↔");
        }
    }

    public TextAlignment getAlignment() {
        return alignment;
    }

    public void setAlignment(TextAlignment alignment) {
        this.alignment = alignment;
    }

    public boolean isCentered() {
        return centered;
    }

    public void setCentered(boolean centered) {
        this.centered = centered;
    }
}
