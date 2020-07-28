package de.codingair.codingapi.server.commands.builder.special;

import de.codingair.codingapi.server.commands.builder.CommandComponent;

public abstract class SpecialCommandComponent extends CommandComponent {
    protected final String type;

    public SpecialCommandComponent(String argument) {
        this(argument, null);
    }

    public SpecialCommandComponent(String argument, String permission) {
        this(argument, permission, "arg");
    }

    public SpecialCommandComponent(String argument, String permission, String type) {
        super(argument, permission);
        this.type = type;
    }

    public String getType() {
        return type;
    }
}
