package de.codingair.codingapi.player.gui.inventory.v2.exceptions;

public class PageAlreadyOpenedException extends GUIException {
    public PageAlreadyOpenedException() {
    }

    public PageAlreadyOpenedException(String message) {
        super(message);
    }

    public PageAlreadyOpenedException(String message, Throwable cause) {
        super(message, cause);
    }
}
