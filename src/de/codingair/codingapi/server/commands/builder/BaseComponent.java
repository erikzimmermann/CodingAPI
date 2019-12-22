package de.codingair.codingapi.server.commands.builder;

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

    public com.mojang.brigadier.builder.LiteralArgumentBuilder buildLiteralArgument(String argument) {
        com.mojang.brigadier.builder.LiteralArgumentBuilder l = com.mojang.brigadier.builder.LiteralArgumentBuilder.literal(argument);

        for(CommandComponent child : getChildren()) {
            if(child instanceof MultiCommandComponent) continue;
            l.then(child.buildLiteralArgument());
        }

        return l;
    }
}
