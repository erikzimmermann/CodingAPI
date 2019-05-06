package de.codingair.codingapi.player.chat;

import de.codingair.codingapi.API;
import de.codingair.codingapi.utils.Removable;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class SimpleMessage implements Removable {
    private final UUID uniqueId = UUID.randomUUID();
    private List<Object> components = new ArrayList<>();
    private HoverEvent hoverEvent;
    private ClickEvent clickEvent;
    private JavaPlugin plugin;
    private boolean sent = false;
    private int timeOut = -1;
    private BukkitRunnable runnable = null;

    public SimpleMessage(JavaPlugin plugin) {
        this.plugin = plugin;
        API.addRemovable(this);
    }

    public SimpleMessage(String base, JavaPlugin plugin) {
        this(plugin);
        add(base);
    }

    public SimpleMessage(TextComponent base, JavaPlugin plugin) {
        this(plugin);
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

    public SimpleMessage add(ChatButton button) {
        this.components.add(button);
        return this;
    }

    public SimpleMessage add(int index, String s) {
        this.components.add(index, new TextComponent(s));
        return this;
    }

    public SimpleMessage add(int index, TextComponent messageComponent) {
        this.components.add(index, messageComponent);
        return this;
    }

    private SimpleMessage add(int index, Object messageComponent) {
        this.components.add(index, messageComponent);
        return this;
    }

    public SimpleMessage add(int index, ChatButton button) {
        this.components.add(index, button);
        return this;
    }

    public SimpleMessage addComponent(int index, String message, boolean hover, boolean click) {
        return addComponent(index, new TextComponent(message), hover, click);
    }

    public SimpleMessage addComponent(String message, boolean hover, boolean click) {
        return addComponent(new TextComponent(message), hover, click);
    }

    public SimpleMessage addComponent(int index, TextComponent messageComponent, boolean hover, boolean click) {
        if(hover) messageComponent.setHoverEvent(this.hoverEvent);
        if(click) messageComponent.setClickEvent(this.clickEvent);

        if(index >= 0) this.components.add(index, messageComponent);
        else this.components.add(messageComponent);
        return this;
    }

    public SimpleMessage addComponent(TextComponent messageComponent, boolean hover, boolean click) {
        return addComponent(-1, messageComponent, hover, click);
    }

    private TextComponent getTextComponent() {
        TextComponent base = null;

        for(Object tc : this.components) {
            if(base == null) base = tc instanceof ChatButton ? ((ChatButton) tc).build() : (TextComponent) tc;
            else base.addExtra(tc instanceof ChatButton ? ((ChatButton) tc).build() : (TextComponent) tc);
        }

        return base;
    }

    public void send(Player player) {
        player.spigot().sendMessage(getTextComponent());
        sent = true;

        if(timeOut > 0) {
            this.runnable = new BukkitRunnable() {
                @Override
                public void run() {
                    timeOut--;

                    if(timeOut == 0) {
                        destroy();
                    }
                }
            };

            this.runnable.runTaskTimer(this.plugin, 20, 20);
        }
    }

    public List<ChatButton> getButtons() {
        List<ChatButton> buttons = new ArrayList<>();

        for(Object c : this.components) {
            if(c instanceof ChatButton) buttons.add((ChatButton) c);
        }

        return buttons;
    }

    public ChatButton getButton(UUID uniqueId) {
        for(Object c : this.components) {
            if(c instanceof ChatButton && ((ChatButton) c).getUniqueId().equals(uniqueId)) return (ChatButton) c;
        }

        return null;
    }

    public boolean replace(String toReplaced, ChatButton replacement) {
        return replace(toReplaced, (Object) replacement);
    }

    public boolean replace(String toReplaced, TextComponent replacement) {
        return replace(toReplaced, (Object) replacement);
    }

    private boolean replace(String toReplaced, Object replacement) {
        boolean foundSth = false;

        int i = 0;
        List<Object> components = new ArrayList<>(this.components);

        for(Object o : components) {
            TextComponent c = o instanceof ChatButton ? ((ChatButton) o).build() : (TextComponent) o;

            if(c.getText().contains(toReplaced)) {
                this.components.remove(c);
                foundSth = true;
            } else {
                i++;
                continue;
            }

            int j = 0;
            String[] a = c.getText().split(toReplaced, -1);
            for(int k = 0; k < a.length; k++) {
                String s = a[k];

                if(s != null && !s.isEmpty()) {
                    TextComponent tc = new TextComponent(s);
                    tc.setClickEvent(c.getClickEvent());
                    tc.setHoverEvent(c.getHoverEvent());
                    tc.setBold(c.isBold());
                    tc.setColor(c.getColor());
                    tc.setItalic(c.isItalic());
                    tc.setObfuscated(c.isObfuscated());
                    tc.setStrikethrough(c.isStrikethrough());
                    tc.setUnderlined(c.isUnderlined());

                    add(i + j++, tc);
                }

                if(k < a.length - 1) add(i + j++, replacement);
            }

            i++;
        }

        components.clear();

        return foundSth;
    }

    @Override
    public void destroy() {
        API.removeRemovable(this);
        this.components.clear();
        if(this.runnable != null) {
            this.runnable.cancel();
            this.runnable = null;
        }
    }

    @Override
    public Player getPlayer() {
        return null;
    }

    @Override
    public Class<? extends Removable> getAbstractClass() {
        return SimpleMessage.class;
    }

    @Override
    public UUID getUniqueId() {
        return this.uniqueId;
    }

    @Override
    public JavaPlugin getPlugin() {
        return this.plugin;
    }

    public boolean isSent() {
        return sent;
    }

    public int getTimeOut() {
        return timeOut;
    }

    public void setTimeOut(int timeOut) {
        if(sent) return;
        this.timeOut = timeOut;
    }

    public void clearTimeOut() {
        this.sent = false;
    }
}
