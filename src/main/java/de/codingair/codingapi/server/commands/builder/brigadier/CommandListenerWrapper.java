package de.codingair.codingapi.server.commands.builder.brigadier;

import com.mojang.brigadier.context.CommandContext;
import de.codingair.codingapi.server.reflections.IReflection;
import de.codingair.codingapi.server.reflections.PacketUtils;
import org.bukkit.command.CommandSender;

import java.util.List;

public class CommandListenerWrapper {
    private static IReflection.MethodAccessor getBukkitSender;
    private static IReflection.FieldAccessor<?> getWorld;
    private static IReflection.FieldAccessor<?> getPosition;
    private static IReflection.MethodAccessor tabComplete;

    public CommandListenerWrapper() {
        if(getBukkitSender == null) {
            Class<?> commandListenerWrapperClass = IReflection.getClass(IReflection.ServerPacket.MINECRAFT_PACKAGE("net.minecraft.commands"), "CommandListenerWrapper");

            Class<?> vec3DClass = IReflection.getClass(IReflection.ServerPacket.MINECRAFT_PACKAGE("net.minecraft.world.phys"), "Vec3D");
            getBukkitSender = IReflection.getMethod(commandListenerWrapperClass, "getBukkitSender", CommandSender.class, new Class[]{});

            getWorld = IReflection.getField(commandListenerWrapperClass, PacketUtils.WorldServerClass, 0);
            getPosition = IReflection.getField(commandListenerWrapperClass, vec3DClass, 0);
            tabComplete = IReflection.getMethod(PacketUtils.CraftServerClass, "tabComplete", List.class, new Class[]{CommandSender.class, String.class, PacketUtils.WorldServerClass, vec3DClass, boolean.class});
        }
    }

    public CommandSender getBukkitSender(Object instance) {
        return (CommandSender) getBukkitSender.invoke(instance);
    }

    private Object getWorld(Object instance) {
        return getWorld.get(instance);
    }

    private Object getPosition(Object instance) {
        return getPosition.get(instance);
    }

    public List<String> tabComplete(Object server, CommandContext<Object> context, String input) {
        return (List<String>) tabComplete.invoke(server, getBukkitSender(context.getSource()), input, getWorld(context.getSource()), getPosition(context.getSource()), true);
    }
}