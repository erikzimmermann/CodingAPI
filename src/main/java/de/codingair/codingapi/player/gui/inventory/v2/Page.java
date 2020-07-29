package de.codingair.codingapi.player.gui.inventory.v2;

import de.codingair.codingapi.player.gui.inventory.v2.buttons.Button;

import java.util.HashMap;

public abstract class Page {
    protected final GUI gui;
    protected final Page basic;
    private final HashMap<Integer, Button> items;

    public Page(GUI gui, Page basic) {
        this.gui = gui;
        this.basic = basic;
        this.items = new HashMap<>();
    }

    public abstract void buildItems();

    public void apply() {
        apply(true);
    }

    public void apply(boolean basic) {
        buildItems();
        deploy(basic);
    }

    private void deploy(boolean basic) {
        items.forEach((slot, item) -> gui.setItem(slot, item.buildItem()));
        if(basic && this.basic != null) this.basic.apply(basic);
    }

    public void rebuild() {
        rebuild(true);
    }

    /**
     * Clears and rebuilds buttons & updates inventory
     * @param basic Predecessor page
     */
    public void rebuild(boolean basic) {
        clear(basic);
        items.clear();
        buildItems();
        deploy(basic);
    }

    public void updateItems() {
        updateItems(true);
    }

    public void updateItems(boolean basic) {
        deploy(basic);
    }

    public void clear() {
        clear(true);
    }

    public void clear(boolean basic) {
        gui.clear(items.keySet());
        if(basic && this.basic != null) this.basic.clear(basic);
    }

    public Button getButtonAt(int slot) {
        Button b = items.get(slot);

        if(b == null && this.basic != null) return this.basic.getButtonAt(slot);
        else return b;
    }

    public Button getButtonAt(int x, int y) {
        return getButtonAt(x + y * 9);
    }

    public Page(GUI gui) {
        this(gui, null);
    }

    public void addButton(int slot, Button button) {
        items.put(slot, button);
    }

    public void addButton(int x, int y, Button button) {
        items.put(x + y * 9, button);
    }

    public void destroy() {
        this.items.clear();
    }

    public Page getBasic() {
        return basic;
    }
}
