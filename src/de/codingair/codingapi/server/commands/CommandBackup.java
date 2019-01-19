package de.codingair.codingapi.server.commands;

import org.bukkit.command.CommandExecutor;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.TabCompleter;

import java.util.List;

public class CommandBackup {
    private PluginCommand command;
    private CommandExecutor executor;
    private TabCompleter tabCompleter;
    private String name;
    private String description;
    private List<String> aliases;
    private String permission;
    private String usage;

    public CommandBackup(PluginCommand command) {
        this.command = command;
        this.executor = command.getExecutor();
        this.tabCompleter = command.getTabCompleter();
        this.name = command.getName();
        this.description = command.getDescription();
        this.aliases = command.getAliases();
        this.permission = command.getPermission();
        this.usage = command.getUsage();
    }

    public void restore() {
        this.command.setExecutor(this.executor);
        this.command.setTabCompleter(this.tabCompleter);
        this.command.setName(this.name);
        this.command.setDescription(this.description);
        this.command.setAliases(this.aliases);
        this.command.setPermission(this.permission);
        this.command.setUsage(this.usage);
    }

    public TabCompleter getTabCompleter() {
        return tabCompleter;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public List<String> getAliases() {
        return aliases;
    }

    public String getPermission() {
        return permission;
    }

    public String getUsage() {
        return usage;
    }
}
