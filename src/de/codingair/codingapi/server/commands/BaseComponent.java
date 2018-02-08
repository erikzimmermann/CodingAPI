package de.codingair.codingapi.server.commands;

import org.bukkit.command.CommandSender;

public abstract class BaseComponent extends CommandComponent {
    public BaseComponent() {
        super(null);
    }
    public BaseComponent(String permission) {
        super(null, permission);
    }

    public abstract void noPermission(CommandSender sender, String label, CommandComponent child);

    public abstract void onlyFor(boolean player, CommandSender sender, String label, CommandComponent child);

    public abstract void unknownSubCommand(CommandSender sender, String label, String[] args);

    @Override
    public BaseComponent setOnlyPlayers(boolean onlyPlayers) {
        return (BaseComponent) super.setOnlyPlayers(onlyPlayers);
    }

    @Override
    public BaseComponent setOnlyConsole(boolean onlyConsole) {
        return (BaseComponent) super.setOnlyConsole(onlyConsole);
    }
}
