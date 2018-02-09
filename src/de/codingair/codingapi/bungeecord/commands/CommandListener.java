package de.codingair.codingapi.bungeecord.commands;

import net.md_5.bungee.api.event.ChatEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.event.EventHandler;

public class CommandListener implements Listener {
    private static CommandListener listener = null;

    public CommandListener(Plugin plugin) {
        if(getListener() == null) plugin.getProxy().getPluginManager().registerListener(plugin, this);
        listener = this;
    }

    @EventHandler
    public void onChat(ChatEvent e) {
        if(e.getMessage().startsWith("/")) {
            String command = e.getMessage().contains(" ") ? e.getMessage().split(" ")[0] : e.getMessage();
            command = command.replaceFirst("/", "");

            String[] args = e.getMessage().contains(" ") ? e.getMessage().replaceFirst("/" + command + " ", "").split(" ") : new String[0];

            CommandExecutor executor = CommandManager.getExecutor(command);
            if(executor != null)
                e.setCancelled(executor.onCommand(e.getSender(), command, args));
        }
    }

    public static CommandListener getListener() {
        return listener;
    }
}
