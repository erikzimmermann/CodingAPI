package de.codingair.codingapi.server.commands.builder;

import de.codingair.codingapi.server.reflections.IReflection;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.TabCompleter;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.List;

public class CustomCommand {
    private final Plugin owner;
    private final String name;
    protected String description;
    private final List<String> aliases;

    private CommandExecutor executor;
    private TabCompleter tabCompleter;

    public CustomCommand(Plugin owner, String name) {
        this(owner, name, null);
    }

    public CustomCommand(Plugin owner, String name, String description) {
        this(owner, name, description, null);
    }

    public CustomCommand(Plugin owner, String name, String description, List<String> aliases) {
        this.owner = owner;
        this.name = name;
        this.description = description == null ? "An automatically created command by CodingAir" : description;
        this.aliases = aliases == null ? new ArrayList<>() : aliases;
    }

    public PluginCommand invoke() {
        IReflection.ConstructorAccessor pluginCommand = IReflection.getConstructor(PluginCommand.class, String.class, Plugin.class);
        PluginCommand command = (PluginCommand) pluginCommand.newInstance(this.name, this.owner);

        command.setDescription(this.description);
        command.setAliases(this.aliases);

        command.setExecutor(this.executor);
        command.setTabCompleter(this.tabCompleter);

        return command;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<String> getAliases() {
        return aliases;
    }

    public CommandExecutor getExecutor() {
        return executor;
    }

    public void setExecutor(CommandExecutor executor) {
        this.executor = executor;
    }

    public TabCompleter getTabCompleter() {
        return tabCompleter;
    }

    public void setTabCompleter(TabCompleter tabCompleter) {
        this.tabCompleter = tabCompleter;
    }
}
