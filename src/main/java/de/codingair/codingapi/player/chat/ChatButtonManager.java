package de.codingair.codingapi.player.chat;

import java.util.ArrayList;
import java.util.List;

public class ChatButtonManager {
    private static ChatButtonManager instance;
    private final List<ChatButtonListener> listeners = new ArrayList<>();

    public boolean addListener(ChatButtonListener l) {
        return this.listeners.add(l);
    }

    public boolean removeListener(ChatButtonListener l) {
        return this.listeners.remove(l);
    }

    static void onInteract(Event e) {
        if(!hasInstance()) return;

        List<ChatButtonListener> listeners = new ArrayList<>(getInstance().listeners);

        for(ChatButtonListener listener : listeners) {
            e.onInteract(listener);
        }

        listeners.clear();
    }

    public static ChatButtonManager getInstance() {
        if(instance == null) instance = new ChatButtonManager();
        return instance;
    }

    public static boolean hasInstance() {
        return instance != null;
    }

    public interface Event {
        void onInteract(ChatButtonListener l);
    }
}
