package de.codingair.codingapi.player.gui.book;

import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.entity.Player;

import java.util.UUID;

public abstract class Button {
    private final UUID uniqueId = UUID.randomUUID();
    private String text;
    private String hover;

    public Button(String text) {
        this(text, null);
    }

    public Button(String text, String hover) {
        this.text = text;
        this.hover = hover;
    }

    TextComponent getComponent() {
        TextComponent textComponent = new TextComponent(this.text);

        if(this.hover != null) {
            textComponent.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new BaseComponent[] {new TextComponent(this.hover)}));
        }

        textComponent.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "CodingAPI|BookAPI|Button|" + this.uniqueId.toString()));

        return textComponent;
    }

    /**
     * @param player Player
     * @return true, to reopen the GUI
     */
    public abstract boolean onClick(Player player);

    public UUID getUniqueId() {
        return uniqueId;
    }

    public String getText() {
        return text;
    }

    public String getHover() {
        return hover;
    }
}
