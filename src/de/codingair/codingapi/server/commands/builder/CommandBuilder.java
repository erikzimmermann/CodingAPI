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
    public static final HashMap<String, CommandBuilder> REGISTERED = new HashMap<>();
    private static Listener listener;
    private List<CommandBackup> backups = new ArrayList<>();
    private PluginCommand main;

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

        main = plugin.getCommand(this.name);
        //alias of other command blocks this command > create command and block alias
        if(main != null && !main.getName().equalsIgnoreCase(this.name)) {
            if(command != null && command.equals(main)) command = null;
            main = null;
        }

        if(main == null) {
            //unregister existing command
            if(command != null) {
                //remove from SimpleCommandMap
                SimplePluginManager spm = (SimplePluginManager) Bukkit.getPluginManager();
                IReflection.FieldAccessor<?> commandMap = IReflection.getField(SimplePluginManager.class, "commandMap");
                SimpleCommandMap scm = (SimpleCommandMap) commandMap.get(spm);

                IReflection.FieldAccessor<?> knownCommands = IReflection.getField(SimpleCommandMap.class, "knownCommands");
                Map<String, Command> commands = (Map<String, Command>) knownCommands.get(scm);
                commands.remove(command.getName().toLowerCase(Locale.ENGLISH).trim());
                commands.remove(command.getPlugin().getName().toLowerCase(Locale.ENGLISH).trim() + ":" + command.getName().toLowerCase(Locale.ENGLISH).trim());

                //Remove from CommandDispatcher
                CommandDispatcher.removeCommand(command.getPlugin().getName().toLowerCase(Locale.ENGLISH).trim() + ":" + command.getName().toLowerCase(Locale.ENGLISH).trim());
                CommandDispatcher.removeCommand(command.getName().toLowerCase(Locale.ENGLISH).trim());
            }

            //Create PluginCommand using CustomCommand.class
            main = new CustomCommand(plugin, this.name, this.description).invoke();

            //Register command in SimpleCommandMap.class
            SimplePluginManager spm = (SimplePluginManager) Bukkit.getPluginManager();
            IReflection.FieldAccessor<?> commandMap = IReflection.getField(SimplePluginManager.class, "commandMap");
            SimpleCommandMap scm = (SimpleCommandMap) commandMap.get(spm);

            if(tabCompleter) main.setTabCompleter(this);
            main.setExecutor(this);
            main.setAliases(aliases);
            main.setPermission(null);

            scm.register(plugin.getDescription().getName(), main);

            //Add to CommandDispatcher
            CommandDispatcher.addCommand(plugin.getName().toLowerCase(Locale.ENGLISH).trim() + ":" + this.name.toLowerCase(Locale.ENGLISH).trim());
            CommandDispatcher.addCommand(this.name.toLowerCase(Locale.ENGLISH).trim());
        }

        for(String s : names) {
            command = Bukkit.getPluginCommand(s);

            if(command != null && (!API.getInstance().getPlugins().contains(command.getPlugin()) || (plugin.getName().equals(command.getPlugin().getName()) && command.getName().equalsIgnoreCase(this.name)))) {
                //alias matches original command > continue
                if(command.getExecutor().equals(this)) continue;

                if(highestPriority) {
                    //going to overwrite existing command > create backup
                    backups.add(new CommandBackup(command));

                    try {
                        //1.9+
                        command.setName(main.getName());
                    } catch(Throwable ignored) {
                    }

                    command.setExecutor(this);
                    command.setTabCompleter(this);
                    command.setDescription(main.getDescription());
                    command.setAliases(main.getAliases());
                    command.setPermission(null);
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

        names.clear();
        main.setExecutor(this);
        if(tabCompleter) main.setTabCompleter(this);

        REGISTERED.put(this.name.toLowerCase(), this);

        registerListener(plugin);
    }

    public void unregister(JavaPlugin plugin) {
        if(!isRegistered()) return;

        List<String> names = new ArrayList<>(aliases);
        names.add(0, this.name);

        this.backups.forEach(CommandBackup::restore);
        this.backups.clear();

        for(String s : names) {
            PluginCommand command = plugin.getCommand(s);

            //command backup was restored > command is being owned by another plugin
            if(command == null) continue;

            //remove executor
            command.setExecutor(null);
            command.setTabCompleter(null);

            //remove from SimpleCommandMap
            SimplePluginManager spm = (SimplePluginManager) Bukkit.getPluginManager();
            IReflection.FieldAccessor<?> commandMap = IReflection.getField(SimplePluginManager.class, "commandMap");
            SimpleCommandMap scm = (SimpleCommandMap) commandMap.get(spm);

            IReflection.FieldAccessor<Map<String, Command>> knownCommands = IReflection.getField(SimpleCommandMap.class, "knownCommands");
            Map<String, Command> commands = knownCommands.get(scm);
            commands.remove(s.toLowerCase(Locale.ENGLISH).trim());
            commands.remove(plugin.getName().toLowerCase(Locale.ENGLISH).trim() + ":" + s.toLowerCase(Locale.ENGLISH).trim());

            //1.13+
            //Remove from CommandDispatcher
            if(Version.getVersion().isBiggerThan(Version.v1_12)) {
                CommandDispatcher.removeCommand(plugin.getName().toLowerCase(Locale.ENGLISH).trim() + ":" + s.toLowerCase(Locale.ENGLISH).trim());
                CommandDispatcher.removeCommand(s.toLowerCase(Locale.ENGLISH).trim());
            }
        }

        names.clear();
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

    public PluginCommand getMain() {
        return main;
    }
}
