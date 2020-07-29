package de.codingair.codingapi.player.gui.inventory.v2.exceptions;

public class IsNotWaitingException extends GUIException {
    public IsNotWaitingException() {
    }

    public IsNotWaitingException(String message) {
        super(message);
    }

    public IsNotWaitingException(String message, Throwable cause) {
        super(message, cause);
    }
}
