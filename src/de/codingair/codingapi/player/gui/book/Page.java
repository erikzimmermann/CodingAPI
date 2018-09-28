package de.codingair.codingapi.player.gui.book;

import de.codingair.codingapi.server.reflections.IReflection;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.chat.ComponentSerializer;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Page {
    private List<BaseComponent> data = new ArrayList<>();
    private List<Button> buttons = new ArrayList<>();

    public Page() {
    }

    public Page(BaseComponent... baseComponents) {
        for(BaseComponent baseComponent : baseComponents) {
            if(baseComponent == null) this.data.add(new TextComponent("\n"));
            else this.data.add(baseComponent);
        }
    }

    public Page add(BaseComponent baseComponent) {
        return this.add(baseComponent, false);
    }

    public Page add(BaseComponent baseComponent, boolean newLine) {
        if(baseComponent == null) baseComponent = new TextComponent("\n");
        else if(newLine) this.data.get(this.data.size() - 1).addExtra("\n");

        this.data.add(baseComponent);
        return this;
    }
    
    public Page addButton(Button button, boolean newLine) {
        if(this.buttons.contains(button)) throw new IllegalArgumentException("Cannot add buttons twice!");
        
        this.buttons.add(button);
        return add(button.getComponent(), newLine);
    }

    public List<BaseComponent> getData() {
        return this.data;
    }

    public Object getFinal() {
        Class<?> IChatBaseComponentClass = IReflection.getClass(IReflection.ServerPacket.MINECRAFT_PACKAGE, "IChatBaseComponent");
        Class<?> ChatSerializerClass = IReflection.getClass(IReflection.ServerPacket.MINECRAFT_PACKAGE, "IChatBaseComponent$ChatSerializer");

        IReflection.MethodAccessor a = IReflection.getMethod(ChatSerializerClass, "a", IChatBaseComponentClass, new Class[] {String.class});
        return a.invoke(null, ComponentSerializer.toString(this.data.toArray(new BaseComponent[0])));
    }

    public List<Button> getButtons() {
        return buttons;
    }
    
    public Button getButton(UUID uniqueId) {
        for(Button button : this.buttons) {
            if(button.getUniqueId().equals(uniqueId)) return button;
        }

        return null;
    }
}
