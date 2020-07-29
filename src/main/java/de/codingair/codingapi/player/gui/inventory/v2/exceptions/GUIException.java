package de.codingair.codingapi.player.gui.inventory.v2.exceptions;

public class GUIException extends Exception {
    public GUIException() {
    }

    public GUIException(String message) {
        super(message);
    }

    public GUIException(String message, Throwable cause) {
        super(message, cause);
    }
}
