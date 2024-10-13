package de.codingair.codingapi.server.commands.builder.special;

import de.codingair.codingapi.server.commands.builder.CommandComponent;
import org.bukkit.command.CommandSender;

import java.util.List;

public abstract class MultiCommandComponent extends SpecialCommandComponent {
    public MultiCommandComponent() {
        super(null);
    }

    public MultiCommandComponent(String permission) {
        super(null, permission);
    }

    public abstract void addArguments(CommandSender sender, String[] args, List<String> suggestions);

    @Override
    public boolean runCommand(CommandSender sender, String label, String[] args) {
        int before = 0;
        CommandComponent cc = getParent();
        while (cc != null) {
            before++;
            cc = cc.getParent();
        }

        return runCommand(sender, label, args[before - 1], args);
    }

    public abstract boolean runCommand(CommandSender sender, String label, String argument, String[] args);
}
