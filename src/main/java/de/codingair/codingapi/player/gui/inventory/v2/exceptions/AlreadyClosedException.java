package de.codingair.codingapi.player.gui.inventory.v2.exceptions;

public class AlreadyClosedException extends GUIException {
    public AlreadyClosedException() {
    }

    public AlreadyClosedException(String message) {
        super(message);
    }

    public AlreadyClosedException(String message, Throwable cause) {
        super(message, cause);
    }
}
