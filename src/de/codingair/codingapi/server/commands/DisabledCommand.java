package de.codingair.codingapi.server.commands;

import org.bukkit.command.CommandSender;

public class DisabledCommand extends CommandBuilder {
    public DisabledCommand(String name) {
        super(name, new BaseComponent() {
            @Override
            public void noPermission(CommandSender sender, String label, CommandComponent child) {

            }

            @Override
            public void onlyFor(boolean player, CommandSender sender, String label, CommandComponent child) {

            }

            @Override
            public void unknownSubCommand(CommandSender sender, String label, String[] args) {

            }

            @Override
            public boolean runCommand(CommandSender sender, String label, String[] args) {
                sender.sendMessage(org.spigotmc.SpigotConfig.unknownCommandMessage);
                return false;
            }
        }, false);
    }
}
