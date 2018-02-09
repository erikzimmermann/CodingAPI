package de.codingair.codingapi.bungeecord.commands;

import net.md_5.bungee.api.connection.Connection;

/**
 * This class uses a method to use same command-label on bungeecord and spigot.
 * Furthermore you can use aliases.
 */
public abstract class CommandExecutor {
    private String label;
    private String[] aliases = null;

    public CommandExecutor(String label) {
        this.label = label;
    }

    public CommandExecutor(String label, String... aliases) {
        this.label = label;
        this.aliases = aliases;
    }

    /**
     * @param sender CommandSender
     * @param label CommandLabel
     * @param args CommandArgs
     * @return false, if the message should be sent to the spigot servers : true, if the message should be cancelled
     */
    public abstract boolean onCommand(Connection sender, String label, String[] args);

    public String getLabel() {
        return label;
    }

    public String[] getAliases() {
        return aliases;
    }
}
