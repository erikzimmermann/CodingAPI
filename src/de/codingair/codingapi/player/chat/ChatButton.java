package de.codingair.codingapi.player.chat;

import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public abstract class ChatButton {
    public static final String PREFIX = "CodingAPI|ChatAPI|Button|";
    private final UUID uniqueId = UUID.randomUUID();
    private String text, type;
    private List<String> hover;

    public ChatButton(String text) {
        this.text = text;
    }

    public ChatButton(String text, String hover) {
        this(text);
        setHover(hover);
    }

    public ChatButton(String text, List<String> hover) {
        this(text);
        setHover(hover);
    }

    TextComponent build() {
        TextComponent component = new TextComponent(this.text);

        List<BaseComponent> lore = new ArrayList<>();
        if(hover != null) {
            for(String s : hover) {
                lore.add(new TextComponent(s));
            }
        }
        BaseComponent[] components = lore.toArray(new BaseComponent[0]);

        if(this.hover != null && !this.hover.isEmpty()) component.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, components));
        component.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, PREFIX + this.uniqueId.toString() + (type == null ? "" : "#" + type)));

        return component;
    }

    public ChatButton setHover(String hover) {
        this.hover = new ArrayList<String>() {{
            add(hover);
        }};
        return this;
    }

    public ChatButton setHover(List<String> hover) {
        this.hover = new ArrayList<>();

        for(int i = 0; i < hover.size(); i++) {
            String s = hover.get(i);
            this.hover.add(s + (i + 1 < hover.size() ? "\n" : ""));
        }

        return this;
    }

    public UUID getUniqueId() {
        return uniqueId;
    }

    public abstract void onClick(Player player);

    public String getType() {
        return type;
    }

    public ChatButton setType(String type) {
        this.type = type;
        return this;
    }
}
