package de.codingair.codingapi.server.commands.builder;

import com.mojang.brigadier.builder.ArgumentBuilder;
import de.codingair.codingapi.server.reflections.IReflection;
import org.bukkit.command.CommandSender;

import java.util.Map;

public abstract class BaseComponent extends CommandComponent {
    public BaseComponent() {
        super(null);
    }

    public BaseComponent(String permission) {
        super(null, permission);
    }

    public abstract void noPermission(CommandSender sender, String label, CommandComponent child);

    public abstract void onlyFor(boolean player, CommandSender sender, String label, CommandComponent child);

    public abstract void unknownSubCommand(CommandSender sender, String label, String[] args);

    @Override
    public BaseComponent setOnlyPlayers(boolean onlyPlayers) {
        return (BaseComponent) super.setOnlyPlayers(onlyPlayers);
    }

    @Override
    public BaseComponent setOnlyConsole(boolean onlyConsole) {
        return (BaseComponent) super.setOnlyConsole(onlyConsole);
    }

    public Object buildLiteralArgument(String argument) {
        try {
            Class<?> lArgBuilder = Class.forName("com.mojang.brigadier.builder.LiteralArgumentBuilder");
            Class<?> argBuilder = Class.forName("com.mojang.brigadier.builder.ArgumentBuilder");
            IReflection.MethodAccessor literal = IReflection.getMethod(lArgBuilder, "literal", lArgBuilder, new Class[] {String.class});
            IReflection.MethodAccessor then = IReflection.getMethod(argBuilder, "then", argBuilder, new Class[] {argBuilder});

            Object l = literal.invoke(null, argument);

            for(CommandComponent child : getChildren()) {
                if(child instanceof MultiCommandComponent) continue;
                then.invoke(l, child.buildLiteralArgument());
            }

            return l;
        } catch(ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }
}
