package de.codingair.codingapi.server.commands.builder.special;

import de.codingair.codingapi.server.commands.builder.CommandComponent;
import org.bukkit.command.CommandSender;

public abstract class MultiTextCommandComponent extends MultiCommandComponent {
    public MultiTextCommandComponent() {
    }

    public MultiTextCommandComponent(String permission) {
        super(permission);
    }

    @Override
    public boolean runCommand(CommandSender sender, String label, String[] args) {
        int before = 0;
        CommandComponent cc = getParent();
        while(cc != null) {
            before++;
            cc = cc.getParent();
        }

        StringBuilder builder = new StringBuilder();
        for(int i = before - 1; i < args.length; i++) {
            builder.append(args[i]).append(" ");
        }

        String arg = builder.toString();
        return runCommand(sender, label, arg.substring(0, arg.length() - 1), args);
    }

    @Override
    public CommandComponent getChild(String arg) {
        return this;
    }
}
