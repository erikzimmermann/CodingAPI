package de.codingair.codingapi.player.gui.inventory.v2.exceptions;

public class AlreadyOpenedException extends GUIException {
    public AlreadyOpenedException() {
    }

    public AlreadyOpenedException(String message) {
        super(message);
    }

    public AlreadyOpenedException(String message, Throwable cause) {
        super(message, cause);
    }
}
