package de.codingair.codingapi.server.commands;

import org.bukkit.command.CommandExecutor;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.TabCompleter;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.Field;
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
    private Plugin owningPlugin;

    public CommandBackup(PluginCommand command) {
        this.command = command;
        this.executor = command.getExecutor();
        this.tabCompleter = command.getTabCompleter();
        this.name = command.getName();
        this.description = command.getDescription();
        this.aliases = command.getAliases();
        this.permission = command.getPermission();
        this.usage = command.getUsage();

        try {
            final Field owningPlugin = PluginCommand.class.getDeclaredField("owningPlugin");
            owningPlugin.setAccessible(true);
            this.owningPlugin = (Plugin) owningPlugin.get(command);
        } catch(NoSuchFieldException | IllegalAccessException ignored) {
            this.owningPlugin = null;
        }
    }

    public void restore() {
        this.command.setExecutor(this.executor);
        this.command.setTabCompleter(this.tabCompleter);
        this.command.setName(this.name);
        this.command.setDescription(this.description);
        this.command.setAliases(this.aliases);
        this.command.setPermission(this.permission);
        this.command.setUsage(this.usage);

        if(this.owningPlugin != null) {
            try {
                final Field owningPlugin = PluginCommand.class.getDeclaredField("owningPlugin");
                owningPlugin.setAccessible(true);
                owningPlugin.set(command, this.owningPlugin);
            } catch(NoSuchFieldException | IllegalAccessException ignored) {
            }
        }
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
