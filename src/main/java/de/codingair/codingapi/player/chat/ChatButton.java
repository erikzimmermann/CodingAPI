package de.codingair.codingapi.player.chat;

import de.codingair.codingapi.server.sounds.SoundData;
import de.codingair.codingapi.server.specification.Version;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;

public abstract class ChatButton {
    public static final String PREFIX = "CodingAPI|ChatAPI|Button|";
    private final UUID uniqueId = ChatListener.getRandom();
    private final String text;
    private String type;
    private BaseComponent[] hover;
    private SoundData sound = null;

    public static boolean isChatButton(@Nullable String message) {
        if (message == null) return false;

        if (message.startsWith("/")) message = message.substring(1);
        return message.startsWith(ChatButton.PREFIX);
    }

    @Contract("null -> null")
    public static String removePrefix(@Nullable String message) {
        if (message == null) return null;
        return message.substring((message.startsWith("/") ? 1 : 0) + PREFIX.length());
    }

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

    public ChatButton(String text, BaseComponent hover) {
        this(text);
        setHover(hover);
    }

    public ChatButton(String text, BaseComponent... hover) {
        this(text);
        setHover(hover);
    }

    public boolean canClick() {
        return true;
    }

    TextComponent build() {
        TextComponent component = new TextComponent(TextComponent.fromLegacyText(this.text));

        if (this.hover != null && this.hover.length > 0) {
            component.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, hover));
        }
        component.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, Version.since(19, "", "/") + PREFIX + this.uniqueId + (type == null ? "" : "#" + type)));

        return component;
    }

    public ChatButton setHover(String hover) {
        this.hover = TextComponent.fromLegacyText(hover);
        return this;
    }

    public ChatButton setHover(BaseComponent hover) {
        this.hover = new BaseComponent[] {hover};
        return this;
    }

    public ChatButton setHover(BaseComponent... hover) {
        this.hover = hover;
        return this;
    }

    public ChatButton setHover(List<String> hover) {
        this.hover = new BaseComponent[hover.size()];

        for (int i = 0; i < hover.size(); i++) {
            String s = hover.get(i);
            this.hover[i] = new TextComponent(TextComponent.fromLegacyText(s + (i + 1 < hover.size() ? "\n" : "")));
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
