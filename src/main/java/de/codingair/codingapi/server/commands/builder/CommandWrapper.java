package de.codingair.codingapi.server.commands.builder;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.RootCommandNode;
import de.codingair.codingapi.server.commands.builder.brigadier.CommandListenerWrapper;
import de.codingair.codingapi.server.reflections.IReflection;
import de.codingair.codingapi.server.reflections.PacketUtils;
import de.codingair.codingapi.server.specification.Version;
import org.bukkit.Bukkit;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;

public class CommandWrapper implements Predicate<Object>, Command<Object>, SuggestionProvider<Object> {
    private final CommandListenerWrapper wrapper;

    private final CommandBuilder builder;

    private CommandWrapper(CommandBuilder builder) {
        this.builder = builder;
        this.wrapper = new CommandListenerWrapper(builder);
    }

    public static CommandWrapper register(CommandBuilder builder) {
        return new CommandWrapper(builder).register();
    }

    public static CommandDispatcher<Object> dispatcher() {
        Class<?> commandDispatcherClass = IReflection.getClass(IReflection.ServerPacket.COMMANDS, "CommandDispatcher");

        Object commandDispatcher;
        if (Version.atLeast(19.3)) {
            IReflection.MethodAccessor getCommandDispatcher = IReflection.getMethod(PacketUtils.MinecraftServerClass, commandDispatcherClass, new Class[0]);
            commandDispatcher = getCommandDispatcher.invoke(PacketUtils.getMinecraftServer());
        } else if (Version.atLeast(18)) {
            IReflection.FieldAccessor<?> vanillaCommandDispatcher = IReflection.getField(PacketUtils.MinecraftServerClass, "vanillaCommandDispatcher");
            commandDispatcher = vanillaCommandDispatcher.get(PacketUtils.getMinecraftServer());
        } else {
            IReflection.MethodAccessor getCommandDispatcher = IReflection.getMethod(PacketUtils.MinecraftServerClass, "getCommandDispatcher");
            commandDispatcher = getCommandDispatcher.invoke(PacketUtils.getMinecraftServer());
        }

        Class<?> commandDispatcherBrigadierClass = IReflection.getClass("com.mojang.brigadier.CommandDispatcher");
        IReflection.MethodAccessor a = IReflection.getMethod(commandDispatcherClass, commandDispatcherBrigadierClass, new Class[]{});
        //noinspection unchecked
        return (CommandDispatcher<Object>) a.invoke(commandDispatcher);
    }

    private CommandWrapper register() {
        registerCommand(builder.getName());
        for (String alias : builder.getMain().getAliases()) {
            registerCommand(alias);
        }
        return this;
    }

    public void unregister() {
        unregisterCommand(builder.getName());
        for (String alias : builder.getMain().getAliases()) {
            unregisterCommand(alias);
        }
    }

    private void registerCommand(String name) {
        CommandDispatcher<Object> dispatcher = dispatcher();
        LiteralArgumentBuilder<Object> l = LiteralArgumentBuilder.literal(name);
        l.requires(this).executes(this);
        RequiredArgumentBuilder<Object, ?> r = RequiredArgumentBuilder.argument("args", StringArgumentType.greedyString());
        l.then(r.suggests(this).executes(this));

        dispatcher.register(l);
    }

    public boolean test(Object context) {
        return true;
    }

    public int run(CommandContext<Object> context) {
        String args = "";

        try {
            args = context.getArgument("args", String.class);
        } catch (IllegalArgumentException ignored) {
        }
        
        return this.builder.onCommand(wrapper.getBukkitSender(context.getSource()), builder.getMain(), builder.getName(), args.split(" ")) ? 1 : 0;
    }

    public CompletableFuture<Suggestions> getSuggestions(CommandContext<Object> context, SuggestionsBuilder builder) {
        List<String> results = wrapper.tabComplete(context, builder.getInput());
        builder = builder.createOffset(builder.getInput().lastIndexOf(32) + 1);

        for (String s : results) {
            builder.suggest(s);
        }

        return builder.buildFuture();
    }

    private void unregisterCommand(String name) {
        RootCommandNode<?> root = dispatcher().getRoot();

        IReflection.FieldAccessor<Map<String, CommandNode<?>>> children = IReflection.getField(CommandNode.class, "children");
        children.get(root).remove(name);

        IReflection.FieldAccessor<Map<String, CommandNode<?>>> literals = IReflection.getField(CommandNode.class, "literals");
        literals.get(root).remove(name);

        IReflection.FieldAccessor<Map<String, CommandNode<?>>> arguments = IReflection.getField(CommandNode.class, "arguments");
        arguments.get(root).remove(name);
    }

    protected static class Backup {
        protected CommandNode<?>[] commands;
        protected String label;

        public Backup(CommandNode<?>[] commands, String label) {
            this.commands = commands;
            this.label = label;
        }
    }
}
