package de.codingair.codingapi.server.commands.dispatcher;

import com.mojang.brigadier.tree.CommandNode;
import de.codingair.codingapi.server.Version;
import de.codingair.codingapi.server.commands.builder.CommandBuilder;
import de.codingair.codingapi.server.reflections.IReflection;
import de.codingair.codingapi.server.reflections.PacketUtils;

import java.util.Locale;
import java.util.Map;

public class CommandDispatcher {
    public static boolean removeCommand(CommandBuilder command) {
        if(command == null || command.getMain() == null || command.getMain().getPlugin() == null) return false;
        return removeCommand(command.getMain().getPlugin().getName().toLowerCase(Locale.ENGLISH).trim() + ":" + command.getName().toLowerCase(Locale.ENGLISH).trim())
                & removeCommand(command.getName().toLowerCase(Locale.ENGLISH).trim());
    }

    private static boolean removeCommand(String command) {
        if(Version.getVersion().isBiggerThan(Version.v1_12)) {
            IReflection.FieldAccessor<Map<String, CommandNode<?>>> children = IReflection.getField(CommandNode.class, "children");
            Map<String, CommandNode<?>> childrenMap = children.get(dispatcher().getRoot());
            return childrenMap.remove(command) != null;
        } else return true;
    }

    public static boolean addCommand(CommandBuilder command) {
        if(Version.getVersion().isBiggerThan(Version.v1_12)) {
            return dispatcher().register(command.getBaseComponent().buildLiteralArgument(command.getMain().getPlugin().getName().toLowerCase(Locale.ENGLISH).trim() + ":" + command.getName().toLowerCase(Locale.ENGLISH).trim())) != null
                    & dispatcher().register(command.getBaseComponent().buildLiteralArgument(command.getName().toLowerCase(Locale.ENGLISH).trim())) != null;
        } else return true;
    }

    private static com.mojang.brigadier.CommandDispatcher<?> dispatcher() {
        IReflection.MethodAccessor getCommandDispatcher = IReflection.getMethod(PacketUtils.MinecraftServerClass, "getCommandDispatcher");
        Class<?> commandDispatcherClass = IReflection.getClass(IReflection.ServerPacket.MINECRAFT_PACKAGE, "CommandDispatcher");
        IReflection.MethodAccessor a = IReflection.getMethod(commandDispatcherClass, "a", com.mojang.brigadier.CommandDispatcher.class, new Class[] {});
        return (com.mojang.brigadier.CommandDispatcher<?>) a.invoke(getCommandDispatcher.invoke(PacketUtils.getMinecraftServer()));
    }
}
