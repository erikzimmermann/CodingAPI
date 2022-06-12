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
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.BiConsumer;

public class SimpleMessage implements Removable {
    private final UUID uniqueId = UUID.randomUUID();
    private final List<Object> components = new ArrayList<>();
    private final Player player;
    private HoverEvent hoverEvent;
    private ClickEvent clickEvent;
    private final JavaPlugin plugin;
    private boolean sent = false;
    private int timeOut = -1, cachedSize;
    private BukkitRunnable runnable = null;

    public SimpleMessage(JavaPlugin plugin) {
        this((Player) null, plugin);
    }

    public SimpleMessage(String base, JavaPlugin plugin) {
        this(null, base, plugin);
    }

    public SimpleMessage(TextComponent base, JavaPlugin plugin) {
        this(null, base, plugin);
    }

    public SimpleMessage(Player player, JavaPlugin plugin) {
        this.plugin = plugin;
        this.player = player;
        API.addRemovable(this);
    }

    public SimpleMessage(Player player, String base, JavaPlugin plugin) {
        this(player, plugin);
        add(base);
    }

    public SimpleMessage(Player player, TextComponent base, JavaPlugin plugin) {
        this(player, plugin);
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
        s = s.replace("\\n", "\n");
        this.components.add(convert(s));
        cachedSize = -1;
        return this;
    }

    @NotNull
    private TextComponent convert(String s) {
        return new TextComponent(TextComponent.fromLegacyText(s));
    }

    public SimpleMessage add(TextComponent messageComponent) {
        this.components.add(messageComponent);
        cachedSize = -1;
        return this;
    }

    public SimpleMessage add(ChatButton button) {
        this.components.add(button);
        cachedSize = -1;
        return this;
    }

    public SimpleMessage add(int index, String s) {
        s = s.replace("\\n", "\n");
        this.components.add(index, convert(s));
        cachedSize = -1;
        return this;
    }

    public SimpleMessage add(int index, TextComponent messageComponent) {
        this.components.add(index, messageComponent);
        cachedSize = -1;
        return this;
    }

    private SimpleMessage add(int index, Object messageComponent) {
        if(messageComponent instanceof String) messageComponent = ((String) messageComponent).replace("\\n", "\n");
        this.components.add(index, messageComponent);
        cachedSize = -1;
        return this;
    }

    public SimpleMessage add(int index, ChatButton button) {
        this.components.add(index, button);
        cachedSize = -1;
        return this;
    }

    public SimpleMessage addComponent(int index, String message, boolean hover, boolean click) {
        message = message.replace("\\n", "\n");
        return addComponent(index, convert(message), hover, click);
    }

    public SimpleMessage addComponent(String message, boolean hover, boolean click) {
        message = message.replace("\\n", "\n");
        return addComponent(convert(message), hover, click);
    }

    public SimpleMessage addComponent(int index, TextComponent messageComponent, boolean hover, boolean click) {
        if(hover) messageComponent.setHoverEvent(this.hoverEvent);
        if(click) messageComponent.setClickEvent(this.clickEvent);

        if(index >= 0) this.components.add(index, messageComponent);
        else this.components.add(messageComponent);
        cachedSize = -1;
        return this;
    }

    public SimpleMessage addComponent(TextComponent messageComponent, boolean hover, boolean click) {
        return addComponent(-1, messageComponent, hover, click);
    }

    private TextComponent getTextComponent() {
        TextComponent base = null;

        for(Object tc : this.components) {
            TextComponent current = tc instanceof ChatButton ? ((ChatButton) tc).build() : new TextComponent((TextComponent) tc);
            if(base == null) base = current;
            else base.addExtra(current);
        }

        return base;
    }

    public void send() {
        if(player == null) throw new NullPointerException("The message receiver is not defined!");
        send(player);
    }

    public void send(Player sender) {
        send(sender, (p, tc) -> p.spigot().sendMessage(tc));
    }

    public void send(Player sender, BiConsumer<Player, TextComponent> sending) {
        sending.accept(sender, getTextComponent());
        sent = true;

        if(this.runnable == null && timeOut > 0) {
            this.runnable = new BukkitRunnable() {
                @Override
                public void run() {
                    timeOut--;

                    if(timeOut == 0) {
                        onTimeOut();
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

            if(c.toLegacyText().contains(toReplaced)) {
                this.components.remove(c);
                foundSth = true;
            } else {
                i++;
                continue;
            }

            int j = 0;
            String[] a = c.toLegacyText().split(toReplaced, -1);
            for(int k = 0; k < a.length; k++) {
                String s = a[k];

                if(s != null && !s.isEmpty()) {
                    TextComponent tc = convert(s);
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
        for (Object component : this.components) {
            if (component instanceof ChatButton) ChatListener.DEAD_BUTTONS.add(((ChatButton) component).getUniqueId());
        }

        API.removeRemovable(this);
        this.components.clear();
        if(this.runnable != null) {
            this.runnable.cancel();
            this.runnable = null;
        }
    }

    @Override
    public Player getPlayer() {
        return player;
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

    public void onTimeOut() {

    }

    public void clearTimeOut() {
        this.sent = false;
    }

    public int size() {
        if(cachedSize == -1) {
            cachedSize = 0;
            int from = 0;
            for(Object c : this.components) {
                String s = c.toString();
                while((from = s.indexOf("\n", from)) != -1) {
                    from += 1;
                    cachedSize++;
                }
            }
        }

        return cachedSize;
    }
}
