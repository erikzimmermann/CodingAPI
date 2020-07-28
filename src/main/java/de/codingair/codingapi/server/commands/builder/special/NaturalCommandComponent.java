package de.codingair.codingapi.server.commands.builder.special;

import de.codingair.codingapi.server.commands.builder.CommandComponent;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.util.List;

public abstract class NaturalCommandComponent extends SpecialCommandComponent {
    public NaturalCommandComponent() {
        super(null);
    }

    public NaturalCommandComponent(String permission) {
        super(null, permission);
    }

    public abstract List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args);

    @Override
    public CommandComponent getChild(String arg) {
        return this;
    }
}
