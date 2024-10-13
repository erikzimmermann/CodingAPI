package de.codingair.codingapi.player.gui.anvil;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class AnvilCloseEvent extends Event {
    public static HandlerList handlers = new HandlerList();
    private final Player player;
    private final AnvilGUI anvil;
    private boolean cancelled = false;
    private Runnable post = null;
    private String submittedText;
    private boolean submitted;

    public AnvilCloseEvent(Player player, AnvilGUI anvil) {
        this.player = player;
        this.anvil = anvil;
    }

    public AnvilCloseEvent(Player player, AnvilGUI anvil, boolean submitted, String submittedText) {
        this.player = player;
        this.anvil = anvil;
        this.submitted = submitted;
        this.submittedText = submittedText;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public AnvilGUI getAnvil() {
        return anvil;
    }

    public boolean isCancelled() {
        return cancelled;
    }

    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    public Player getPlayer() {
        return player;
    }

    public Runnable getPost() {
        return post;
    }

    public void setPost(Runnable post) {
        this.post = post;
    }

    public boolean isSubmitted() {
        return submitted;
    }

    public String getSubmittedText() {
        return submittedText;
    }
}
