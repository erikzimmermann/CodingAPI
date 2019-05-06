package de.codingair.codingapi.server.commands;

import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class CommandComponent {
    private CommandComponent parent;
    private List<CommandComponent> children = new ArrayList<>();
    private String argument;
    private String permission;
    private boolean onlyPlayers = false;
    private boolean onlyConsole = false;

    public CommandComponent(String argument) {
        this.argument = argument;
        this.permission = null;
    }

    public CommandComponent(String argument, String permission) {
        this.argument = argument;
        this.permission = permission;
    }

    public boolean useInTabCompleter(CommandSender sender, String label, String[] args) {
        return true;
    }

    public abstract boolean runCommand(CommandSender sender, String label, String[] args);

    private void setParent(CommandComponent parent) {
        this.parent = parent;
    }

    public CommandComponent getParent() {
        return parent;
    }

    public List<CommandComponent> getChildren() {
        return Collections.unmodifiableList(children);
    }

    public void addChild(CommandComponent child) {
        if(child instanceof MultiCommandComponent && this.getChild(null) != null) throw new IllegalStateException("There is already a MultiCommandComponent!");
        child.setParent(this);
        this.children.add(child);
    }

    public CommandComponent getChild(String arg) {
        List<CommandComponent> children = new ArrayList<>(this.children);
        CommandComponent child = null;
        CommandComponent multi = null;

        for(CommandComponent c : children) {
            if(c instanceof MultiCommandComponent) {
                multi = c;
                continue;
            }

            if(arg == null || arg.isEmpty()) continue;

            if(c.getArgument().equalsIgnoreCase(arg)) {
                child = c;
                break;
            }
        }

        children.clear();

        return child == null ? multi : child;
    }

    public boolean removeChild(CommandComponent child) {
        if(this.children.remove(child)) {
            child.setParent(null);
            return true;
        } else return false;
    }

    public boolean removeChild(String arg) {
        CommandComponent cc = getChild(arg);
        if(cc == null) return false;
        else return removeChild(cc);
    }

    public String getArgument() {
        return argument;
    }

    public String getPermission() {
        return permission;
    }

    public boolean hasPermission(CommandSender sender) {
        return (this.permission == null || sender.hasPermission(this.permission)) && (parent == null || parent.hasPermission(sender));
    }

    public List<String> getArgs() {
        List<String> s = new ArrayList<>();
        addArgs(s);
        return s;
    }

    private void addArgs(List<String> s) {
        if(this.parent == null) s.add(this.argument);
        else {
            this.parent.addArgs(s);
            s.add(this.argument);
        }
    }

    public boolean isOnlyPlayers() {
        if(onlyPlayers) return true;

        if(this.parent == null) return onlyPlayers;
        else return this.parent.isOnlyPlayers();
    }

    public CommandComponent setOnlyPlayers(boolean onlyPlayers) {
        this.onlyPlayers = onlyPlayers;
        return this;
    }

    public boolean isOnlyConsole() {
        if(onlyConsole) return true;

        if(this.parent == null) return onlyConsole;
        else return this.parent.isOnlyConsole();
    }

    public CommandComponent setOnlyConsole(boolean onlyConsole) {
        this.onlyConsole = onlyConsole;
        return this;
    }
}
