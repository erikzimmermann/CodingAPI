package de.codingair.codingapi.player.gui.inventory.gui.simple;

import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public abstract class Page {
    private String title;
    private Layout layout;
    private List<Button> buttons = new ArrayList<>();
    private SimpleGUI last;

    public Page(Player p, String title, Layout layout) {
        this(p, title, layout, true);
    }

    public Page(Player p, String title) {
        this(p, title, null, true);
    }

    public Page(Player p, String title, boolean preInitialize) {
        this(p, title, null, preInitialize);
    }

    public Page(Player p, String title, Layout layout, boolean preInitialize) {
        this.title = title;
        this.layout = layout;

        if(preInitialize) initialize(p);
    }

    public abstract void initialize(Player p);

    public boolean initialize(SimpleGUI gui) {
        this.last = gui;
        boolean haveToBeReopened = false;

        if(this.title != null && !gui.getTitle().equals(this.title)) {
            gui.setTitle(this.title, false);
            haveToBeReopened = gui.isOpen();
        }

        gui.clear();

        if(gui.getLayout() != null) {
            if(gui.getLayout().initialize(gui)) haveToBeReopened = true;
        }

        if(this.layout != null) {
            if(this.layout.initialize(gui)) haveToBeReopened = true;
        }

        for(Button button : this.buttons) {
            gui.addButton(button);
        }

        return haveToBeReopened;
    }

    public Button getButton(int slot) {
        for(Button button : this.buttons) {
            if(button.getSlot() == slot) return button;
        }

        return null;
    }

    public void close() {
        if(this.last != null) this.last.close();
    }

    public Button getButton(int x, int y) {
        return getButton(x + y * 9);
    }

    public Button removeButton(int slot) {
        Button button = getButton(slot);
        if(button != null) this.buttons.remove(button);
        return button;
    }

    public Button removeButton(int x, int y) {
        return removeButton(x + y * 9);
    }

    public void addButton(Button button) {
        removeButton(button.getSlot());
        this.buttons.add(button);
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title, boolean reopen) {
        this.title = title;
        if(getLast() != null) {
            if(reopen) {
                if(initialize(getLast())) getLast().updateTitle();
            }
        }
    }

    public Layout getLayout() {
        return layout;
    }

    public void updatePage() {
        for(Button button : this.buttons) {
            if(button instanceof SyncButton) ((SyncButton) button).update();
        }
    }

    public void onExitByPlayer() {
    }

    public SimpleGUI getLast() {
        return last;
    }
}
