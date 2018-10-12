package de.codingair.codingapi.player.chat;

import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.entity.Player;

import java.util.UUID;

public abstract class ChatButton extends TextComponent {
    private final UUID uniqueId = UUID.randomUUID();
    private String text;
    private String hover;

    public ChatButton(String text) {
        this.text = text;
    }

    public ChatButton(String text, String hover) {
        this(text);
        setHover(hover);
    }

    public TextComponent build() {
        TextComponent component = new TextComponent(this.text);

        if(this.hover != null && !this.hover.isEmpty()) component.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new BaseComponent[] {new TextComponent(hover)}));
        component.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "CodingAPI|ChatAPI|Button|" + this.uniqueId.toString()));

        return component;
    }

    public void setHover(String hover) {
        this.hover = hover;
    }

    public UUID getUniqueId() {
        return uniqueId;
    }

    public abstract void onClick(Player player);
}
