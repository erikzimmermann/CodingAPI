package de.codingair.codingapi.server.commands.builder;

import de.codingair.codingapi.API;
import de.codingair.codingapi.server.Version;
import de.codingair.codingapi.server.commands.builder.special.MultiCommandComponent;
import de.codingair.codingapi.server.commands.builder.special.NaturalCommandComponent;
import de.codingair.codingapi.server.commands.dispatcher.CommandDispatcher;
import de.codingair.codingapi.utils.Removable;
import org.bukkit.Bukkit;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.bukkit.plugin.SimplePluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.Field;
import java.util.*;

public class CommandBuilder implements CommandExecutor, TabCompleter, Removable {
    private static SimpleCommandMap simpleCommandMap = null;
    private static Map<String, Command> knownCommands = null;

    private PluginCommand fallback = null;
    private PluginCommand main;
    private final UUID uniqueId = UUID.randomUUID();
    private final JavaPlugin plugin;
    private boolean registered = false;

    private final String name;
    private final String description;
    private final List<String> aliases;

    private final BaseComponent baseComponent;
    private TabCompleter ownTabCompleter = null;
    private final boolean tabCompleter;

    public CommandBuilder(JavaPlugin plugin, String name, BaseComponent baseComponent, boolean tabCompleter) {
        this(plugin, name, null, baseComponent, tabCompleter, (String[]) null);
    }

    public CommandBuilder(JavaPlugin plugin, String name, String description, BaseComponent baseComponent, boolean tabCompleter, String... aliases) {
        this.plugin = plugin;
        this.name = name.toLowerCase();
        this.description = description;
        this.baseComponent = baseComponent;
        this.tabCompleter = tabCompleter;

        this.aliases = new ArrayList<>();
        if(aliases != null)
            for(String alias : aliases) {
                this.aliases.add(alias.toLowerCase());
            }
    }

    @Override
    public void destroy() {
        unregister();
    }

    public void register() {
        if(isRegistered()) return;
        registered = true;
        API.addRemovable(this);

        //register just one command
        fallback = Bukkit.getPluginCommand(this.name);
        if(fallback != null) {
            //remove from SimpleCommandMap
            getKnownCommands().remove(fallback.getName().toLowerCase(Locale.ENGLISH).trim());
        }

        main = new CustomCommand(plugin, name, description).invoke();
        main.setTabCompleter(this.tabCompleter ? this : null);
        main.setExecutor(this);
        main.setAliases(aliases);
        main.setPermission(null);

        //Register command in SimpleCommandMap.class
        simpleCommandMap().register(plugin.getDescription().getName(), main);

        //Add to CommandDispatcher
        if(Version.getVersion().isBiggerThan(Version.v1_12)) CommandDispatcher.addCommand(this);
    }

    public void unregister() {
        if(!isRegistered()) return;

        //Remove from CommandDispatcher
        if(Version.getVersion().isBiggerThan(Version.v1_12)) CommandDispatcher.removeCommand(this);

        unregister(name);
        for(String alias : aliases) {
            unregister(alias);
        }

        if(fallback != null) {
            //add to SimpleCommandMap
            getKnownCommands().put(fallback.getName().toLowerCase(Locale.ENGLISH).trim(), fallback);
        }

        main = null;
        registered = false;
        API.removeRemovable(this);
    }

    private void unregister(String label) {
        //remove from SimpleCommandMap
        label = label.toLowerCase(Locale.ENGLISH).trim();

        Map<String, Command> commands = getKnownCommands();
        Command c = commands.get(label);

        if(c instanceof PluginCommand && ((PluginCommand) c).getPlugin().getName().equals(plugin.getName())) commands.remove(label);
        commands.remove(main.getPlugin().getName().toLowerCase(Locale.ENGLISH).trim() + ":" + label);
    }

    public static Command getCommand(String name) {
        if(name.startsWith("/")) return getKnownCommands().get(name.toLowerCase().substring(1));
        else return getKnownCommands().get(name.toLowerCase());
    }

    public static boolean exists(String name) {
        return getCommand(name) != null;
    }

    public static Map<String, Command> getKnownCommands() {
        if(knownCommands == null) {
            try {
                Field knownCommands = SimpleCommandMap.class.getDeclaredField("knownCommands");
                knownCommands.setAccessible(true);

                CommandBuilder.knownCommands = (Map<String, Command>) knownCommands.get(simpleCommandMap());
            } catch(NoSuchFieldException | IllegalAccessException e) {
                e.printStackTrace();
                return new HashMap<>();
            }
        }

        return knownCommands;
    }

    public static SimpleCommandMap simpleCommandMap() {
        if(simpleCommandMap == null) {
            SimplePluginManager spm = (SimplePluginManager) Bukkit.getPluginManager();

            try {
                Field commandMap = SimplePluginManager.class.getDeclaredField("commandMap");
                Field knownCommands = SimpleCommandMap.class.getDeclaredField("knownCommands");

                commandMap.setAccessible(true);
                knownCommands.setAccessible(true);

                simpleCommandMap = (SimpleCommandMap) commandMap.get(spm);
            } catch(NoSuchFieldException | IllegalAccessException e) {
                e.printStackTrace();
            }
        }

        return simpleCommandMap;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        CommandComponent component = (args.length == 1 && args[0].equals("/" + label)) || baseComponent.getChildren().isEmpty() ? getBaseComponent() : getComponent(args);

        if(component == null) {
            this.baseComponent.unknownSubCommand(sender, label, args);
            return false;
        }

        if(component.isOnlyConsole() && sender instanceof Player) {
            this.baseComponent.onlyFor(false, sender, label, component);
            return false;
        }

        if(component.isOnlyPlayers() && !(sender instanceof Player)) {
            this.baseComponent.onlyFor(true, sender, label, component);
            return false;
        }

        if(component.hasPermission(sender)) {
            return component.runCommand(sender, label, args);
        }

        this.baseComponent.noPermission(sender, label, component);
        return false;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        List<String> sug = new ArrayList<>();
        if(this.ownTabCompleter != null) {
            List<String> apply = this.ownTabCompleter.onTabComplete(sender, command, label, args);
            if(apply != null) sug.addAll(apply);
            return sug;
        }

        HashMap<CommandComponent, List<String>> sub = new HashMap<>();

        if(args.length == 0) return sug;

        String lastArg = args[args.length - 1];
        if(lastArg == null) lastArg = "";
        args[args.length - 1] = "";
        CommandComponent component = getComponent(args);

        if(component == null) return sug;

        args[args.length - 1] = lastArg;

        if(!(component instanceof NaturalCommandComponent)) {
            for(CommandComponent child : component.getChildren()) {
                if(child.hasPermission(sender)) {
                    if(child.useInTabCompleter(sender, label, args)) {
                        List<String> suggestion = new ArrayList<>();

                        if(child instanceof MultiCommandComponent) ((MultiCommandComponent) child).addArguments(sender, args, suggestion);
                        else suggestion.add(child.getArgument());

                        if(!suggestion.isEmpty()) sub.put(child, suggestion);
                    }
                }
            }

            for(CommandComponent c : sub.keySet()) {
                List<String> suggestions = sub.get(c);

                for(String subCommand : suggestions) {
                    if(sug.contains(subCommand)) continue;

                    if(c.matchTabComplete(sender, subCommand, lastArg.toLowerCase())) {
                        sug.add(subCommand);
                        continue;
                    }

                    if(lastArg.isEmpty() || subCommand.toLowerCase().startsWith(lastArg.toLowerCase())) {
                        sug.add(subCommand);
                    }
                }

                suggestions.clear();
            }

            sub.clear();
        } else {
            NaturalCommandComponent ncc = (NaturalCommandComponent) component;
            if(ncc.hasPermission(sender)) {
                List<String> list = ncc.onTabComplete(sender, command, label, args);
                if(list != null) {
                    sug.addAll(list);
                    list.clear();
                }
            }
        }

        sug.sort(Comparator.naturalOrder());
        return sug;
    }

    @Override
    public Player getPlayer() {
        return null;
    }

    @Override
    public UUID getUniqueId() {
        return uniqueId;
    }

    @Override
    public JavaPlugin getPlugin() {
        return plugin;
    }

    public CommandComponent getComponent(String... args) {
        if(args.length == 0) return this.baseComponent;
        return getComponent(Arrays.asList(args));
    }

    public CommandComponent getComponent(List<String> s) {
        if(s.isEmpty()) return this.baseComponent;

        CommandComponent current = this.baseComponent;

        for(String value : s) {
            if(current == null || current instanceof NaturalCommandComponent) break;
            CommandComponent cc = current.getChild(value);

            if((value != null && value.isEmpty()) && !(cc instanceof NaturalCommandComponent)) break;
            current = cc;
        }

        return current;
    }

    public String getName() {
        return name;
    }

    public BaseComponent getBaseComponent() {
        return baseComponent;
    }

    public boolean isRegistered() {
        return registered;
    }

    public TabCompleter getOwnTabCompleter() {
        return ownTabCompleter;
    }

    public void setOwnTabCompleter(TabCompleter ownTabCompleter) {
        this.ownTabCompleter = ownTabCompleter;
    }

    public PluginCommand getMain() {
        return main;
    }
}
