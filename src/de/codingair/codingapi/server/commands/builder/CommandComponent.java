package de.codingair.codingapi.server.commands.builder;

import de.codingair.codingapi.server.commands.builder.special.SpecialCommandComponent;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class CommandComponent {
    protected CommandComponent parent;
    protected final List<CommandComponent> children = new ArrayList<>();
    protected final String argument;
    protected final String permission;
    protected Boolean onlyPlayers = null;
    protected Boolean onlyConsole = null;

    public CommandComponent(String argument) {
        this(argument, null);
    }

    public CommandComponent(String argument, String permission) {
        this.argument = argument;
        this.permission = permission;
    }

    public boolean useInTabCompleter(CommandSender sender, String label, String[] args) {
        return true;
    }

    public boolean matchTabComplete(CommandSender sender, String suggestion, String argument) {
        return false;
    }

    public abstract boolean runCommand(CommandSender sender, String label, String[] args);

    private void setParent(CommandComponent parent) {
        this.parent = parent;
    }

    public CommandComponent getParent() {
        return parent;
    }

    public BaseComponent getBase() {
        if(this instanceof BaseComponent) return (BaseComponent) this;
        else if(parent instanceof BaseComponent) return (BaseComponent) parent;
        return parent == null ? null : parent.getBase();
    }

    public List<CommandComponent> getChildren() {
        return Collections.unmodifiableList(children);
    }

    public CommandComponent addChild(CommandComponent child) {
        if(child instanceof SpecialCommandComponent && this.getChild(null) != null) throw new IllegalStateException("There is already a SpecialCommandComponent!");
        child.setParent(this);
        this.children.add(child);
        return this;
    }

    public Object buildArgument() {
//        try {
//            Object l;
//            if(this instanceof SpecialCommandComponent) {
//                Class<?> rArgBuilder = Class.forName("com.mojang.brigadier.builder.RequiredArgumentBuilder");
//                Class<?> argType = Class.forName("com.mojang.brigadier.arguments.ArgumentType");
//                Class<?> argChat = IReflection.getSaveClass(IReflection.ServerPacket.MINECRAFT_PACKAGE, "ArgumentChatComponent");
//
//                IReflection.MethodAccessor required = IReflection.getMethod(rArgBuilder, "argument", rArgBuilder, new Class[] {String.class, argType});
//                Object o = IReflection.getConstructor(argChat).newInstance();
//                l = required.invoke(null, ((SpecialCommandComponent) this).getType(), o);
//
//                IReflection.FieldAccessor<?> suggestionsProvider = IReflection.getField(rArgBuilder, "suggestionsProvider");
//                suggestionsProvider.set(l, getBukkitCommandWrapper());
//            } else {
//                Class<?> lArgBuilder = Class.forName("com.mojang.brigadier.builder.LiteralArgumentBuilder");
//                IReflection.MethodAccessor literal = IReflection.getMethod(lArgBuilder, "literal", lArgBuilder, new Class[] {String.class});
//                l = literal.invoke(null, argument);
//            }
//
//            Class<?> argBuilder = Class.forName("com.mojang.brigadier.builder.ArgumentBuilder");
//            Class<?> command = Class.forName("com.mojang.brigadier.Command");
//            IReflection.MethodAccessor then = IReflection.getMethod(argBuilder, "then", argBuilder, new Class[] {argBuilder});
//            IReflection.MethodAccessor executes = IReflection.getMethod(argBuilder, "executes", argBuilder, new Class[] {command});
//            executes.invoke(l, getBukkitCommandWrapper());
//
//            for(CommandComponent child : getChildren()) {
//                Object o = child.buildArgument();
//                if(o != null) then.invoke(l, o);
//            }
//
//            return l;
//        } catch(ClassNotFoundException e) {
//            e.printStackTrace();
            return null;
//        }
    }

    public CommandComponent getChild(String arg) {
        List<CommandComponent> children = new ArrayList<>(this.children);
        CommandComponent child = null;
        CommandComponent special = null;

        for(CommandComponent c : children) {
            if(c instanceof SpecialCommandComponent) {
                special = c;
                continue;
            }

            if(arg == null || arg.isEmpty()) continue;

            if(c.getArgument().equalsIgnoreCase(arg)) {
                child = c;
                break;
            }
        }

        children.clear();

        return child == null ? special : child;
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
        if(onlyPlayers != null) return onlyPlayers;

        if(this.parent == null) return false;
        else return this.parent.isOnlyPlayers();
    }

    public CommandComponent setOnlyPlayers(boolean onlyPlayers) {
        this.onlyPlayers = onlyPlayers;
        return this;
    }

    public boolean isOnlyConsole() {
        if(onlyConsole != null) return onlyConsole;

        if(this.parent == null) return false;
        else return this.parent.isOnlyConsole();
    }

    public CommandComponent setOnlyConsole(boolean onlyConsole) {
        this.onlyConsole = onlyConsole;
        return this;
    }
}
