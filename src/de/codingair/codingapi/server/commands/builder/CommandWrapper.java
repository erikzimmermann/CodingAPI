package de.codingair.codingapi.server.commands.builder;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import de.codingair.codingapi.server.commands.builder.brigadier.CommandListenerWrapper;
import de.codingair.codingapi.server.commands.builder.special.SpecialCommandComponent;
import org.bukkit.Bukkit;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;

public class CommandWrapper implements Predicate<Object>, Command<Object>, SuggestionProvider<Object> {
    private final CommandListenerWrapper wrapper;
    private final CommandBuilder builder;

    private CommandWrapper(CommandBuilder builder) {
        this.builder = builder;
        this.wrapper = new CommandListenerWrapper();
    }

    public static void a(CommandBuilder builder) {
        new CommandWrapper(builder).register();
    }

    private void register() {
        CommandDispatcher<Object> dispatcher = (CommandDispatcher<Object>) de.codingair.codingapi.server.commands.dispatcher.CommandDispatcher.dispatcher();
        LiteralArgumentBuilder<Object> l = LiteralArgumentBuilder.literal(builder.getName());
        l.requires(this).executes(this);
        RequiredArgumentBuilder<Object, ?> r = RequiredArgumentBuilder.argument("args", StringArgumentType.greedyString());
        l.then(r.suggests(this).executes(this));

        dispatcher.register(l);
    }

    public static void unregister(CommandBuilder builder) {
        de.codingair.codingapi.server.commands.dispatcher.CommandDispatcher.removeCommand(builder.getName());
    }

    private void then(ArgumentBuilder<Object, ?> builder, CommandComponent c) {
        //does not work properly
        if(c instanceof SpecialCommandComponent) {
            RequiredArgumentBuilder<Object, ?> r = RequiredArgumentBuilder.argument("args", StringArgumentType.greedyString());
            builder.then(r.suggests(this).executes(this));
        } else {
            LiteralArgumentBuilder<Object> l = LiteralArgumentBuilder.literal(c.getArgument());
            builder.then(l.requires(this).executes(this));
        }

        for(CommandComponent child : c.getChildren()) {
            then(builder, child);
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
}
