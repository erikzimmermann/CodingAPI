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
import org.bukkit.Bukkit;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;

public class CommandWrapper implements Predicate<Object>, Command<Object>, SuggestionProvider<Object> {
    private static CommandDispatcher<Object> dispatcher = null;
    private static Map<String, CommandNode<?>> CHILDREN = null;
    private static Map<String, CommandNode<?>> LITERALS = null;
    private static Map<String, CommandNode<?>> ARGUMENTS = null;
    private final CommandListenerWrapper wrapper;

    private final CommandBuilder builder;

    private CommandWrapper(CommandBuilder builder) {
        this.builder = builder;
        this.wrapper = new CommandListenerWrapper();
    }

    public static CommandWrapper a(CommandBuilder builder) {
        return new CommandWrapper(builder).register();
    }

    private CommandWrapper register() {
        CommandDispatcher<Object> dispatcher = dispatcher();
        LiteralArgumentBuilder<Object> l = LiteralArgumentBuilder.literal(builder.getName());
        l.requires(this).executes(this);
        RequiredArgumentBuilder<Object, ?> r = RequiredArgumentBuilder.argument("args", StringArgumentType.greedyString());
        l.then(r.suggests(this).executes(this));

        dispatcher.register(l);
        return this;
    }

    public void unregister() {
        removeCommand(builder.getName(), false);

        for(String alias : builder.getMain().getAliases()) {
            removeCommand(alias, false);
        }
    }

    public boolean test(Object context) {
        return true;
    }

    public int run(CommandContext<Object> context) {
        return Bukkit.getServer().dispatchCommand(wrapper.getBukkitSender(context.getSource()), context.getInput()) ? 1 : 0;
    }

    public CompletableFuture<Suggestions> getSuggestions(CommandContext<Object> context, SuggestionsBuilder builder) {
        List<String> results = wrapper.tabComplete(Bukkit.getServer(), context, builder.getInput());
        builder = builder.createOffset(builder.getInput().lastIndexOf(32) + 1);

        for(String s : results) {
            builder.suggest(s);
        }

        return builder.buildFuture();
    }

    private Backup removeCommand(String command, boolean backup) {
        if(CHILDREN == null) {
            RootCommandNode<?> root = dispatcher().getRoot();

            IReflection.FieldAccessor<Map<String, CommandNode<?>>> children = IReflection.getField(CommandNode.class, "children");
            CHILDREN = children.get(root);

            IReflection.FieldAccessor<Map<String, CommandNode<?>>> literals = IReflection.getField(CommandNode.class, "literals");
            LITERALS = literals.get(root);

            IReflection.FieldAccessor<Map<String, CommandNode<?>>> arguments = IReflection.getField(CommandNode.class, "arguments");
            ARGUMENTS = arguments.get(root);
        }

        if(!backup) return null;
        return new Backup(new CommandNode[] {CHILDREN.remove(command), LITERALS.remove(command), ARGUMENTS.remove(command)}, command);
    }

    public static CommandDispatcher<Object> dispatcher() {
        if(dispatcher == null) {
            IReflection.MethodAccessor getCommandDispatcher = IReflection.getMethod(PacketUtils.MinecraftServerClass, "getCommandDispatcher");
            Class<?> commandDispatcherClass = IReflection.getClass(IReflection.ServerPacket.MINECRAFT_PACKAGE("net.minecraft.commands"), "CommandDispatcher");
            Class<?> commandDispatcherBrigadierClass = IReflection.getClass("com.mojang.brigadier.CommandDispatcher");

            IReflection.MethodAccessor a = IReflection.getMethod(commandDispatcherClass, "a", commandDispatcherBrigadierClass, new Class[] {});
            dispatcher = (CommandDispatcher<Object>) a.invoke(getCommandDispatcher.invoke(PacketUtils.getMinecraftServer()));
        }

        return dispatcher;
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
