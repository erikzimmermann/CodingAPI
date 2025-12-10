package de.codingair.codingapi.server.commands.builder.brigadier;

import com.mojang.brigadier.context.CommandContext;
import de.codingair.codingapi.server.commands.builder.CommandBuilder;
import de.codingair.codingapi.server.reflections.IReflection;
import de.codingair.codingapi.server.specification.Version;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.List;

public class CommandListenerWrapper {
    private static IReflection.MethodAccessor getBukkitSender;
    private final CommandBuilder builder;

    public CommandListenerWrapper(CommandBuilder builder) {
        this.builder = builder;

        if (getBukkitSender == null) {
            Class<?> commandListenerWrapperClass = IReflection.getClass(IReflection.ServerPacket.MINECRAFT_PACKAGE("net.minecraft.commands"), Version.choose("CommandListenerWrapper", 21.11, "CommandSourceStack"));
            getBukkitSender = IReflection.getMethod(commandListenerWrapperClass, "getBukkitSender", CommandSender.class, new Class[]{});
        }
    }

    public CommandSender getBukkitSender(Object instance) {
        return (CommandSender) getBukkitSender.invoke(instance);
    }

    public List<String> tabComplete(CommandContext<Object> context, String input) {
        String label = input.substring(
                input.startsWith("/") ? 1 : 0,
                input.contains(" ") ? input.indexOf(" ") : input.length()
        ).toLowerCase();
        if (!label.equals(builder.getName()) && !builder.getAliases().contains(label)) {
            int idx = input.indexOf(builder.getName());

            if (idx == -1) {
                for (String alias : builder.getAliases()) {
                    idx = input.indexOf(alias);
                    if (idx != -1) break;
                }
            }

            if (idx == -1) return new ArrayList<>();
            input = "/" + input.substring(idx);
        }

        label = input.substring(
                input.startsWith("/") ? 1 : 0,
                input.contains(" ") ? input.indexOf(" ") : input.length()
        ).toLowerCase();

        String cut_input = input.substring((input.startsWith("/") ? 1 : 0) + label.length());
        while (cut_input.startsWith(" ") && cut_input.length() > 1)
            cut_input = cut_input.substring(1);
        while (cut_input.endsWith(" "))
            cut_input = cut_input.substring(0, cut_input.length() - 1);

        return builder.onTabComplete(getBukkitSender(context.getSource()), builder.getMain(), label, cut_input.split(" "));
    }
}