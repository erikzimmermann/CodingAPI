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

    public static boolean addCommand(String command) {
        if(Version.getVersion().isBiggerThan(Version.v1_12)) {
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
