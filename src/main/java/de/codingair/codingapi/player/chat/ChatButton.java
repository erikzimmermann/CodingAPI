package de.codingair.codingapi.player.chat;

import de.codingair.codingapi.server.sounds.SoundData;
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
    private final String text;
    private String type;
    private List<TextComponent> hover;
    private SoundData sound = null;

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

    @Deprecated
    public ChatButton(String text, TextComponent hover) {
        this(text);
        setHover(hover);
    }

    public boolean canClick() {
        return true;
    }

    TextComponent build() {
        TextComponent component = new TextComponent(this.text);

        if(this.hover != null && !this.hover.isEmpty()) {
            component.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, hover.toArray(new BaseComponent[0])));
        }
        component.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, PREFIX + this.uniqueId + (type == null ? "" : "#" + type)));

        return component;
    }

    public ChatButton setHover(String hover) {
        this.hover = new ArrayList<TextComponent>() {{
            add(new TextComponent(hover));
        }};
        return this;
    }

    public ChatButton setHover(TextComponent hover) {
        this.hover = new ArrayList<TextComponent>() {{
            add(hover);
        }};
        return this;
    }

    public ChatButton setHover(List<String> hover) {
        this.hover = new ArrayList<>();

        for(int i = 0; i < hover.size(); i++) {
            String s = hover.get(i);
            this.hover.add(new TextComponent(s + (i + 1 < hover.size() ? "\n" : "")));
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

    public SoundData getSound() {
        return sound;
    }

    public void setSound(SoundData sound) {
        this.sound = sound;
    }
}
