package de.codingair.codingapi.server.commands.dispatcher;

import de.codingair.codingapi.server.Version;
import de.codingair.codingapi.server.commands.builder.CommandBuilder;
import de.codingair.codingapi.server.reflections.IReflection;
import de.codingair.codingapi.server.reflections.PacketUtils;

import java.util.Locale;
import java.util.Map;

public class CommandDispatcher {
    private static Map<String, ?> CACHE = null;

    public static boolean removeCommand(CommandBuilder command) {
        if(command == null || command.getMain() == null || command.getMain().getPlugin() == null) return false;
        return removeCommand(command.getMain().getPlugin().getName().toLowerCase(Locale.ENGLISH).trim() + ":" + command.getName().toLowerCase(Locale.ENGLISH).trim())
                & removeCommand(command.getName().toLowerCase(Locale.ENGLISH).trim());
    }

    private static boolean removeCommand(String command) {
        if(Version.getVersion().isBiggerThan(Version.v1_12)) {
            try {
                if(CACHE == null) {
                    Class<?> commandNode = Class.forName("com.mojang.brigadier.tree.CommandNode");
                    Class<?> rootCommandNodeClass = IReflection.getClass("com.mojang.brigadier.tree.RootCommandNode");
                    Class<?> commandDispatcherBrigadierClass = IReflection.getClass("com.mojang.brigadier.CommandDispatcher");
                    IReflection.MethodAccessor getRoot = IReflection.getMethod(commandDispatcherBrigadierClass, "getRoot", rootCommandNodeClass, new Class[] {});

                    IReflection.FieldAccessor<Map<String, ?>> children = IReflection.getField(commandNode, "children");
                    CACHE = children.get(getRoot.invoke(dispatcher()));
                }

                return CACHE.remove(command) != null;
            } catch(ClassNotFoundException e) {
                e.printStackTrace();
                return true;
            }
        } else return true;
    }

    public static boolean addCommand(CommandBuilder command) {
        if(Version.getVersion().isBiggerThan(Version.v1_12)) {
            Class<?> commandDispatcherBrigadierClass = IReflection.getClass("com.mojang.brigadier.CommandDispatcher");
            Object dispatcher = dispatcher();
            Class<?> lArgBuilder = IReflection.getClass("com.mojang.brigadier.builder.LiteralArgumentBuilder");
            Class<?> lCommandNode = IReflection.getClass("com.mojang.brigadier.tree.LiteralCommandNode");

            IReflection.MethodAccessor register = IReflection.getMethod(commandDispatcherBrigadierClass, "register", lCommandNode, new Class[] {lArgBuilder});

            return register.invoke(dispatcher, command.getBaseComponent().buildLiteralArgument(command.getMain().getPlugin().getName().toLowerCase(Locale.ENGLISH).trim() + ":" + command.getName().toLowerCase(Locale.ENGLISH).trim())) != null
                    && register.invoke(dispatcher, command.getBaseComponent().buildLiteralArgument(command.getName().toLowerCase(Locale.ENGLISH).trim())) != null;
        } else return true;
    }

    private static Object dispatcher() {
        IReflection.MethodAccessor getCommandDispatcher = IReflection.getMethod(PacketUtils.MinecraftServerClass, "getCommandDispatcher");
        Class<?> commandDispatcherClass = IReflection.getClass(IReflection.ServerPacket.MINECRAFT_PACKAGE, "CommandDispatcher");
        Class<?> commandDispatcherBrigadierClass = IReflection.getClass("com.mojang.brigadier.CommandDispatcher");

        IReflection.MethodAccessor a = IReflection.getMethod(commandDispatcherClass, "a", commandDispatcherBrigadierClass, new Class[] {});
        return a.invoke(getCommandDispatcher.invoke(PacketUtils.getMinecraftServer()));
    }
}
