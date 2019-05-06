package de.codingair.codingapi.player.gui.inventory.gui.simple;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

public abstract class Layout {
    private ItemStack[] content;

    public Layout(int size) {
        if(size > 54) throw new IllegalArgumentException("Layout size cannot be bigger than 54!");
        else if(size % 9 != 0) throw new IllegalArgumentException("Layout size have to be divisible by 9!");

        this.content = new ItemStack[size];

        initialize();
    }

    public abstract void initialize();
    
    public boolean initialize(SimpleGUI gui) {
        boolean haveToBeReopened = false;

        if(gui.getSize() != getSize()) {
            gui.setSize(getSize());
            haveToBeReopened = true;
        }

        for(int i = 0; i < getSize(); i++) {
            ItemStack item = getItem(i);

            if(item == null) continue;

            gui.setItem(i, item);
        }

        return haveToBeReopened;
    }

    public int getSize() {
        return this.content.length;
    }

    public ItemStack[] getContent() {
        return this.content.clone();
    }

    public Layout setItem(int slot, ItemStack item) {
        if(item == null) throw new IllegalArgumentException("Item cannot be null!");

        this.content[slot] = item.clone();
        return this;
    }

    public Layout setItem(int x, int y, ItemStack item) {
        return setItem(x + y * 9, item);
    }

    public ItemStack getItem(int slot) {
        return this.content[slot] == null ? null : this.content[slot].clone();
    }

    public ItemStack getItem(int x, int y) {
        return getItem(x + y * 9);
    }

    public Layout addLine(int x0, int y0, int x1, int y1, ItemStack item, boolean override) {
        if(item == null) throw new IllegalArgumentException("Item cannot be null!");

        double cX = (double) x0, cY = (double) y0;
        Vector v = new Vector(x1, y1, 0).subtract(new Vector(x0, y0, 0)).normalize();

        do {
            if(override || getItem((int) cX, (int) cY) == null || getItem((int) cX, (int) cY).getType() == Material.AIR) setItem((int) cX, (int) cY, item.clone());
            cX += v.getX();
            cY += v.getY();
        } while((int) cX != x1 || (int) cY != y1);

        if(override || getItem((int) cX, (int) cY) == null || getItem((int) cX, (int) cY).getType() == Material.AIR) setItem((int) cX, (int) cY, item.clone());

        return this;
    }
}
