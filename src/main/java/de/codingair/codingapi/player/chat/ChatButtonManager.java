package de.codingair.codingapi.player.chat;

import java.util.ArrayList;
import java.util.List;

public class ChatButtonManager {
    private static ChatButtonManager instance;
    private final List<ChatButtonListener> listeners = new ArrayList<>();

    static boolean onAsyncInteract(Event e) {
        if (!hasInstance()) return false;

        List<ChatButtonListener> listeners = new ArrayList<>(getInstance().listeners);

        for (ChatButtonListener listener : listeners) {
            if (e.onInteract(listener)) return true;
        }

        listeners.clear();
        return false;
    }

    public static ChatButtonManager getInstance() {
        if (instance == null) instance = new ChatButtonManager();
        return instance;
    }

    public static boolean hasInstance() {
        return instance != null;
    }

    public boolean addListener(ChatButtonListener l) {
        return this.listeners.add(l);
    }

    public boolean removeListener(ChatButtonListener l) {
        return this.listeners.remove(l);
    }

    public interface Event {
        boolean onInteract(ChatButtonListener l);
    }
}
