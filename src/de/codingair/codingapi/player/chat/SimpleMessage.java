package de.codingair.codingapi.player.chat;

import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;

import java.util.ArrayList;
import java.util.List;

public class SimpleMessage {
    private List<TextComponent> components = new ArrayList<>();
    private HoverEvent hoverEvent;
    private ClickEvent clickEvent;

    public SimpleMessage() {
    }

    public SimpleMessage(String base) {
        add(base);
    }

    public SimpleMessage(TextComponent base) {
        add(base);
    }

    public SimpleMessage setHoverEvent(HoverEvent.Action action, BaseComponent... value) {
        this.hoverEvent = new HoverEvent(action, value);
        return this;
    }

    public SimpleMessage setClickEvent(ClickEvent.Action action, String value) {
        this.clickEvent = new ClickEvent(action, value);
        return this;
    }

    public SimpleMessage add(String s) {
        this.components.add(new TextComponent(s));
        return this;
    }

    public SimpleMessage add(TextComponent messageComponent) {
        this.components.add(messageComponent);
        return this;
    }

    public SimpleMessage addComponent(TextComponent messageComponent, boolean hover, boolean click) {
        if(hover) messageComponent.setHoverEvent(this.hoverEvent);
        if(click) messageComponent.setClickEvent(this.clickEvent);
        this.components.add(messageComponent);
        return this;
    }

    public TextComponent getTextComponent() {
        TextComponent base = null;

        for(TextComponent tc : this.components) {
            if(base == null) base = tc;
            else base.addExtra(tc);
        }

        return base;
    }
}
