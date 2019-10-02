package de.codingair.codingapi.server.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.tree.CommandNode;
import de.codingair.codingapi.server.Version;
import de.codingair.codingapi.server.commands.builder.CommandBuilder;
import de.codingair.codingapi.server.reflections.IReflection;
import de.codingair.codingapi.server.reflections.PacketUtils;

import java.util.Map;

public class CommandDispatcher {
    public static boolean removeCommand(String command) {
        if(Version.getVersion().isBiggerThan(Version.v1_12)) {
            IReflection.FieldAccessor children = IReflection.getField(CommandNode.class, "children");
            Map<String, CommandNode> childrenMap = (Map<String, CommandNode>) children.get(dispatcher().getRoot());
            return childrenMap.remove(command) != null;
        } else return true;
    }

    public static boolean addCommand(String command, CommandBuilder builder) {
        if(Version.getVersion().isBiggerThan(Version.v1_12)) {
//            Class<?> ArgumentChatClass = IReflection.getClass(IReflection.ServerPacket.MINECRAFT_PACKAGE, "ArgumentChat");
//
//            dispatcher().getRoot().addChild(RequiredArgumentBuilder.argument(command, (ArgumentType) IReflection.getConstructor(ArgumentChatClass).newInstance())
//                    .suggests(new SuggestionProvider() {
//                        @Override
//                        public CompletableFuture<Suggestions> getSuggestions(CommandContext commandContext, SuggestionsBuilder suggestionsBuilder) throws CommandSyntaxException {
//                            if(!(commandContext.getSource() instanceof CommandListenerWrapper)) return suggestionsBuilder.buildFuture();
//
//                            CommandListenerWrapper l = (CommandListenerWrapper) commandContext.getSource();
//                            String label = commandContext.getInput().substring(1);
//                            if(label.contains(" ")) label = label.split(" ")[0];
//                            String[] args = commandContext.getInput().contains(" ") ? commandContext.getInput().substring(2 + label.length()).split(" ") : new String[0];
//
//                            List<String> suggestions = builder.onTabComplete(l.getBukkitSender(), builder.getMain(), label, args);
//
//                            System.out.println(suggestions.size());
//                            if(suggestions == null) return null;
//                            for(String suggestion : suggestions) {
//                                System.out.println("adding: '"+suggestion+"'");
//                                suggestionsBuilder.suggest(suggestion);
//                            }
//
//                            return suggestionsBuilder.buildFuture();
//                        }
//                    })
//                    .build());

            return dispatcher().register(LiteralArgumentBuilder.literal(command)) != null;
        } else return true;
    }

    private static com.mojang.brigadier.CommandDispatcher dispatcher() {
        IReflection.MethodAccessor getCommandDispatcher = IReflection.getMethod(PacketUtils.MinecraftServerClass, "getCommandDispatcher");
        Class<?> commandDispatcherClass = IReflection.getClass(IReflection.ServerPacket.MINECRAFT_PACKAGE, "CommandDispatcher");
        IReflection.MethodAccessor a = IReflection.getMethod(commandDispatcherClass, "a", com.mojang.brigadier.CommandDispatcher.class, new Class[] {});
        return (com.mojang.brigadier.CommandDispatcher) a.invoke(getCommandDispatcher.invoke(PacketUtils.getMinecraftServer()));
    }
}
