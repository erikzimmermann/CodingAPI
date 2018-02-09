package de.codingair.codingapi.bungeecord.commands;

import net.md_5.bungee.api.plugin.Plugin;

import java.util.ArrayList;
import java.util.List;

public class CommandManager {
    private static List<CommandExecutor> commandExecutors = new ArrayList<>();
    private static CommandListener listener;

    public static void enable(Plugin plugin) {
        if(listener == null) listener = new CommandListener(plugin);
    }

    public static void registerCommand(CommandExecutor commandExecutor, Plugin plugin) {
        enable(plugin);
        if(!existsCommand(commandExecutor.getLabel())) commandExecutors.add(commandExecutor);
    }

    public static void unregisterCommand(CommandExecutor commandExecutor) {
        if(existsCommand(commandExecutor.getLabel())) commandExecutors.remove(getExecutor(commandExecutor.getLabel()));
    }

    public static boolean existsCommand(String label) {
        return getExecutor(label) != null;
    }

    public static CommandExecutor getExecutor(String label) {
        for(CommandExecutor commandExecutor : commandExecutors) {
            if(commandExecutor.getLabel().equalsIgnoreCase(label)) return commandExecutor;
            for(String a : commandExecutor.getAliases()) {
                if(a.equalsIgnoreCase(label)) return commandExecutor;
            }
        }

        return null;
    }
}
