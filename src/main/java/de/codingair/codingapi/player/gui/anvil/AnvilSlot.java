package de.codingair.codingapi.player.gui.anvil;

public enum AnvilSlot {
    INPUT_LEFT(0),
    INPUT_RIGHT(1),
    OUTPUT(2),
    NONE(-999);

    private final int slot;

    AnvilSlot(int slot) {
        this.slot = slot;
    }

    public static AnvilSlot bySlot(int slot) {
        for (AnvilSlot anvilSlot : values()) {
            if (anvilSlot.getSlot() == slot) {
                return anvilSlot;
            }
        }

        return NONE;
    }

    public int getSlot() {
        return slot;
    }
}
