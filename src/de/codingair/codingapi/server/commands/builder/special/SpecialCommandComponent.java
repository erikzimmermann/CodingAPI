package de.codingair.codingapi.server.commands.builder.special;

import de.codingair.codingapi.server.commands.builder.CommandComponent;

public abstract class SpecialCommandComponent extends CommandComponent {
    public SpecialCommandComponent(String argument) {
        super(argument);
    }

    public SpecialCommandComponent(String argument, String permission) {
        super(argument, permission);
    }
}
