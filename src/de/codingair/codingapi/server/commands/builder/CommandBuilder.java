package de.codingair.codingapi.server.commands.builder;

import de.codingair.codingapi.API;
import de.codingair.codingapi.server.Version;
import de.codingair.codingapi.server.commands.CommandDispatcher;
import de.codingair.codingapi.server.reflections.IReflection;
import org.bukkit.Bukkit;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.plugin.SimplePluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.Field;
import java.util.*;

public class CommandBuilder implements CommandExecutor, TabCompleter {
    private static final HashMap<String, CommandBuilder> REGISTERED = new HashMap<>();
    private static Listener listener;
    private CommandBackup backup = null;

    private static void registerListener(JavaPlugin plugin) {
        if(listener != null) return;

        Bukkit.getPluginManager().registerEvents(listener = new Listener() {

            @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
            public void onPreProcess(PlayerCommandPreprocessEvent e) {
                String label = e.getMessage().split(" ")[0].replaceFirst("/", "");
                Command command = API.getPluginCommand(label);

                if(command == null || command.getName() == null) return;

                CommandBuilder builder = REGISTERED.get(command.getName().toLowerCase());

                if(builder == null) return;
                if(!builder.isHighestPriority()) return;

                e.setCancelled(true);
                builder.onCommand(e.getPlayer(), command, label, e.getMessage().replaceFirst("/" + label + " ", "").split(" "));
            }

        }, plugin);
    }

    private String name;
    private String description;
    private List<String> aliases;

    private BaseComponent baseComponent;
    private TabCompleter ownTabCompleter = null;
    private boolean tabCompleter;
    private boolean highestPriority = false;

    public CommandBuilder(String name, BaseComponent baseComponent, boolean tabCompleter) {
        this(name, null, baseComponent, tabCompleter, null);
    }

    public CommandBuilder(String name, String description, BaseComponent baseComponent, boolean tabCompleter, String... aliases) {
        this.name = name;
        this.description = description;
        this.baseComponent = baseComponent;
        this.tabCompleter = tabCompleter;
        this.aliases = aliases == null ? new ArrayList<>() : Arrays.asList(aliases);
    }

    public void register(JavaPlugin plugin) {
        if(isRegistered()) return;

        List<String> names = new ArrayList<>(aliases);
        names.add(0, this.name);

        PluginCommand command = Bukkit.getPluginCommand(this.name);
        if(command != null && !command.getName().equalsIgnoreCase(this.name)) command = null;

        PluginCommand main;
        PluginCommand pluginC = plugin.getCommand(this.name);
        if(pluginC != null && !pluginC.getName().equalsIgnoreCase(this.name)) pluginC = null;

        if(pluginC == null) {
            //Create PluginCommand using CustomCommand.class
            main = new CustomCommand(plugin, this.name, this.description).invoke();

            if(command == null) {
                //Register command in SimpleCommandMap.class
                SimplePluginManager spm = (SimplePluginManager) Bukkit.getPluginManager();
                IReflection.FieldAccessor commandMap = IReflection.getField(SimplePluginManager.class, "commandMap");
                SimpleCommandMap scm = (SimpleCommandMap) commandMap.get(spm);

                if(tabCompleter) main.setTabCompleter(this);
                main.setExecutor(this);
                main.setAliases(aliases);
                main.setPermission(this.baseComponent.getPermission());

                scm.register(plugin.getDescription().getName(), main);

                CommandDispatcher.addCommand(plugin.getName().toLowerCase(Locale.ENGLISH).trim() + ":" + this.name.toLowerCase(Locale.ENGLISH).trim());
                CommandDispatcher.addCommand(this.name.toLowerCase(Locale.ENGLISH).trim());
            }
        } else {
            main = plugin.getCommand(this.name);
        }

        for(String s : names) {
            command = Bukkit.getPluginCommand(s);

            if(command != null && (!API.getInstance().getPlugins().contains(command.getPlugin()) || (plugin.getName().equals(command.getPlugin().getName()) && command.getName().equalsIgnoreCase(this.name)))) {
                if(command.getExecutor().equals(this)) continue;

                if(highestPriority) {
                    backup = new CommandBackup(command);

                    try {
                        //1.9+
                        command.setName(main.getName());
                    } catch(Throwable ignored) {
                    }

                    command.setExecutor(this);
                    command.setTabCompleter(this);
                    command.setDescription(main.getDescription());
                    command.setAliases(main.getAliases());
                    command.setPermission(main.getPermission());
                    command.setUsage(main.getUsage());

                    try {
                        final Field owningPlugin = PluginCommand.class.getDeclaredField("owningPlugin");
                        owningPlugin.setAccessible(true);
                        owningPlugin.set(command, plugin);
                    } catch(NoSuchFieldException | IllegalAccessException ignored) {
                    }
                } else if(command.getPlugin().getName().equals(plugin.getName())) {
                    command.setExecutor(this);
                    command.setTabCompleter(this);

                    try {
                        final Field owningPlugin = PluginCommand.class.getDeclaredField("owningPlugin");
                        owningPlugin.setAccessible(true);
                        owningPlugin.set(command, plugin);
                    } catch(NoSuchFieldException | IllegalAccessException ignored) {
                    }
                }
            }
        }

        main.setExecutor(this);
        if(tabCompleter) main.setTabCompleter(this);

        REGISTERED.put(this.name.toLowerCase(), this);

        registerListener(plugin);
    }

    public void unregister(JavaPlugin plugin) {
        if(!isRegistered()) return;

        plugin.getCommand(this.name).setExecutor(null);
        plugin.getCommand(this.name).setTabCompleter(null);

        if(this.backup != null) {
            this.backup.restore();
        }

        PluginCommand command = Bukkit.getPluginCommand(this.name);
        if(command.getExecutor() == command.getPlugin() && API.getInstance().getPlugins().contains(command.getPlugin())) {
            //remove from SimpleCommandMap
            SimplePluginManager spm = (SimplePluginManager) Bukkit.getPluginManager();
            IReflection.FieldAccessor commandMap = IReflection.getField(SimplePluginManager.class, "commandMap");
            SimpleCommandMap scm = (SimpleCommandMap) commandMap.get(spm);

            IReflection.FieldAccessor knownCommands = IReflection.getField(SimpleCommandMap.class, "knownCommands");
            Map<String, Command> commands = (Map<String, Command>) knownCommands.get(scm);
            commands.remove(this.name.toLowerCase(Locale.ENGLISH).trim());
            commands.remove(plugin.getName().toLowerCase(Locale.ENGLISH).trim() + ":" + this.name.toLowerCase(Locale.ENGLISH).trim());

            //1.13+
            //Remove from CommandDispatcher
            if(Version.getVersion().isBiggerThan(Version.v1_12)) {
                CommandDispatcher.removeCommand(plugin.getName().toLowerCase(Locale.ENGLISH).trim() + ":" + this.name.toLowerCase(Locale.ENGLISH).trim());
                CommandDispatcher.removeCommand(this.name.toLowerCase(Locale.ENGLISH).trim());
            }
        }

        REGISTERED.remove(this.name.toLowerCase());
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
        if(this.ownTabCompleter != null) return this.ownTabCompleter.onTabComplete(sender, command, label, args);

        List<String> sub = new ArrayList<>();
        List<String> sug = new ArrayList<>();

        if(args.length == 0) return sug;

        String lastArg = args[args.length - 1];
        args[args.length - 1] = "";
        CommandComponent component = getComponent(args);

        if(component == null) return sug;

        for(CommandComponent child : component.getChildren()) {
            if(child.hasPermission(sender)) {
                if(child instanceof MultiCommandComponent && child.useInTabCompleter(sender, label, args)) ((MultiCommandComponent) child).addArguments(sender, args, sub);
                else if(child.useInTabCompleter(sender, label, args)) sub.add(child.getArgument());
            }
        }

        for(String subCommand : sub) {
            if(subCommand.toLowerCase().startsWith(lastArg.toLowerCase())) {
                sug.add(subCommand);
            }
        }

        if(sug.isEmpty()) return sub;
        else sub.clear();
        return sug;
    }

    public CommandComponent getComponent(String... args) {
        if(args.length == 0) return this.baseComponent;
        return getComponent(Arrays.asList(args));
    }

    public CommandComponent getComponent(List<String> s) {
        if(s.isEmpty()) return this.baseComponent;

        CommandComponent current = this.baseComponent;

        for(String value : s) {
            if(current == null || (value != null && value.isEmpty())) break;
            current = current.getChild(value);
        }

        return current;
    }

    public String getName() {
        return name;
    }

    public BaseComponent getBaseComponent() {
        return baseComponent;
    }

    public boolean isTabCompleter() {
        return tabCompleter;
    }

    public boolean isRegistered() {
        return REGISTERED.containsKey(this.name.toLowerCase());
    }

    public boolean isHighestPriority() {
        return highestPriority;
    }

    public CommandBuilder setHighestPriority(boolean highestPriority) {
        this.highestPriority = highestPriority;
        return this;
    }

    public TabCompleter getOwnTabCompleter() {
        return ownTabCompleter;
    }

    public void setOwnTabCompleter(TabCompleter ownTabCompleter) {
        this.ownTabCompleter = ownTabCompleter;
    }
}
