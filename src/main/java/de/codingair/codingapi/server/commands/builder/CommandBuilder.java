package de.codingair.codingapi.server.commands.builder;

import de.codingair.codingapi.API;
import de.codingair.codingapi.server.commands.builder.special.MultiCommandComponent;
import de.codingair.codingapi.server.commands.builder.special.NaturalCommandComponent;
import de.codingair.codingapi.server.reflections.IReflection;
import de.codingair.codingapi.server.specification.Version;
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
    private static Class<?> wrapper = null;
    private static IReflection.MethodAccessor register = null;
    private static IReflection.MethodAccessor unregister = null;

    private final HashMap<String, Command> fallback = new HashMap<>();
    private final UUID uniqueId = UUID.randomUUID();
    private final JavaPlugin plugin;
    private final String name;
    private final String description;
    private final String[] importantAliases;
    private final List<String> aliases;
    private final BaseComponent baseComponent;
    private final boolean tabCompleter;
    private Object wrapperInstance = null;
    private PluginCommand main;
    private TabCompleter ownTabCompleter = null;
    private boolean mergeSpaceArguments = true;

    public CommandBuilder(JavaPlugin plugin, String name, BaseComponent baseComponent, boolean tabCompleter) {
        this(plugin, name, null, baseComponent, tabCompleter, (String[]) null);
    }

    public CommandBuilder(JavaPlugin plugin, String name, String description, BaseComponent baseComponent, boolean tabCompleter, String... aliases) {
        this(plugin, name, description, baseComponent, tabCompleter, null, aliases);
    }

    public CommandBuilder(JavaPlugin plugin, String name, String description, BaseComponent baseComponent, boolean tabCompleter, String[] importantAliases, String... aliases) {
        this.plugin = plugin;
        this.name = name.toLowerCase(Locale.ENGLISH).trim();
        this.description = description;
        this.baseComponent = baseComponent;
        this.baseComponent.setBuilder(this);
        this.tabCompleter = tabCompleter;

        this.aliases = new ArrayList<>();
        if (importantAliases == null) this.importantAliases = new String[0];
        else {
            this.importantAliases = new String[importantAliases.length];
            for (int i = 0; i < importantAliases.length; i++) {
                String s = importantAliases[i].toLowerCase(Locale.ENGLISH).trim();
                this.importantAliases[i] = s;
                this.aliases.add(s);
            }
        }

        if (aliases != null)
            for (String alias : aliases) {
                this.aliases.add(alias.toLowerCase(Locale.ENGLISH).trim());
            }

        if (Version.atLeast(13) && wrapper == null) {
            String path = CommandBuilder.class.getName();
            path = path.substring(0, path.lastIndexOf("."));

            try {
                // use package name in case of relocation
                wrapper = Class.forName(path + ".CommandWrapper");
                register = IReflection.getMethod(wrapper, "register", wrapper, new Class[]{CommandBuilder.class});
                unregister = IReflection.getMethod(wrapper, "unregister");
            } catch (ClassNotFoundException ex) {
                ex.printStackTrace();
            }
        }
    }

    public static Command getCommand(String name) {
        if (name.startsWith("/")) return getKnownCommands().get(name.toLowerCase().substring(1));
        else return getKnownCommands().get(name.toLowerCase());
    }

    public static boolean exists(String name) {
        return getCommand(name) != null;
    }

    public static Map<String, Command> getKnownCommands() {
        if (knownCommands == null) {
            try {
                Field knownCommands = SimpleCommandMap.class.getDeclaredField("knownCommands");
                knownCommands.setAccessible(true);

                CommandBuilder.knownCommands = (Map<String, Command>) knownCommands.get(simpleCommandMap());
            } catch (NoSuchFieldException | IllegalAccessException e) {
                e.printStackTrace();
                return new HashMap<>();
            }
        }

        return knownCommands;
    }

    public static SimpleCommandMap simpleCommandMap() {
        if (simpleCommandMap == null) {
            SimplePluginManager spm = (SimplePluginManager) Bukkit.getPluginManager();

            try {
                Field commandMap = SimplePluginManager.class.getDeclaredField("commandMap");
                Field knownCommands = SimpleCommandMap.class.getDeclaredField("knownCommands");

                commandMap.setAccessible(true);
                knownCommands.setAccessible(true);

                simpleCommandMap = (SimpleCommandMap) commandMap.get(spm);
            } catch (NoSuchFieldException | IllegalAccessException e) {
                e.printStackTrace();
            }
        }

        return simpleCommandMap;
    }

    @Override
    public void destroy() {
        unregister();
    }

    public void register() {
        if (main != null) return;
        API.addRemovable(this);

        //unregister foreign main command
        Command c = getKnownCommands().remove(this.name);
        if (c != null) {
            //add to fallback commands
            fallback.put(this.name, c);
        }

        //unregister foreign commands which block important aliases
        for (String importantAlias : importantAliases) {
            c = getKnownCommands().remove(importantAlias);
            if (c != null) {
                //add to fallback commands
                fallback.put(importantAlias, c);
            }
        }

        main = new CustomCommand(plugin, name, description).invoke();
        main.setTabCompleter(this.tabCompleter ? this : null);
        main.setExecutor(this);
        main.setAliases(aliases);
        main.setPermission(null);
        main.setLabel(name);

        //Register main command in SimpleCommandMap.class
        simpleCommandMap().register(plugin.getDescription().getName(), main);

        //Add to CommandDispatcher
        if (Version.atLeast(13)) wrapperInstance = register.invoke(null, this);
    }

    public void unregister() {
        if (main == null) return;

        //Remove from CommandDispatcher
        if (Version.atLeast(13)) unregister.invoke(wrapperInstance);

        unregister(name);
        for (String alias : aliases) {
            unregister(alias);
        }

        //revive overwritten commands to SimpleCommandMap
        fallback.forEach((key, command) -> getKnownCommands().put(key, command));
        fallback.clear();

        main = null;
        API.removeRemovable(this);
    }

    private void unregister(String label) {
        //remove from SimpleCommandMap
        label = label.toLowerCase(Locale.ENGLISH).trim();

        Map<String, Command> commands = getKnownCommands();
        Command c = commands.get(label);

        if (c instanceof PluginCommand && ((PluginCommand) c).getPlugin().getName().equals(plugin.getName()))
            commands.remove(label);
        commands.remove(main.getPlugin().getName().toLowerCase(Locale.ENGLISH).trim() + ":" + label);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        args = repairArgs(args);

        CommandComponent component = (args.length == 1 && args[0].equals("/" + label)) || baseComponent.getChildren().isEmpty() ? getBaseComponent() : getComponent(args);

        if (component == null) {
            if (baseComponent.isOnlyConsole() && sender instanceof Player) {
                this.baseComponent.onlyFor(false, sender, label, component);
                return true;
            }

            if (baseComponent.isOnlyPlayers() && !(sender instanceof Player)) {
                this.baseComponent.onlyFor(true, sender, label, component);
                return true;
            }

            if (this.baseComponent.hasPermission(sender)) {
                this.baseComponent.unknownSubCommand(sender, label, args);
                return true;
            }

            this.baseComponent.noPermission(sender, label, component);
            return true;
        }

        if (component.isOnlyConsole() && sender instanceof Player) {
            this.baseComponent.onlyFor(false, sender, label, component);
            return true;
        }

        if (component.isOnlyPlayers() && !(sender instanceof Player)) {
            this.baseComponent.onlyFor(true, sender, label, component);
            return true;
        }

        if (component.hasPermission(sender)) {
            return component.runCommand(sender, label, args);
        }

        this.baseComponent.noPermission(sender, label, component);
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        args = repairArgs(args);
        List<String> sug = new ArrayList<>();

        if (this.ownTabCompleter != null) {
            List<String> apply = this.ownTabCompleter.onTabComplete(sender, command, label, args);
            if (apply != null) {
                for (String s : apply) {
                    if (s.contains(" ") && mergeSpaceArguments) s = "\"" + s + "\"";
                    sug.add(s);
                }
            }
            return sug;
        }

        HashMap<CommandComponent, List<String>> sub = new HashMap<>();

        if (args.length == 0) return sug;

        String lastArg = args[args.length - 1];
        if (lastArg == null) lastArg = "";
        else lastArg = lastArg.toLowerCase();

        args[args.length - 1] = "";
        CommandComponent component = getComponent(args);

        if (component == null) return sug;

        args[args.length - 1] = lastArg;

        if (!(component instanceof NaturalCommandComponent)) {
            for (CommandComponent child : component.getChildren()) {
                if (child.hasPermission(sender)) {
                    if (child.useInTabCompleter(sender, label, args)) {
                        List<String> suggestion = new ArrayList<>();

                        if (child instanceof MultiCommandComponent)
                            ((MultiCommandComponent) child).addArguments(sender, args, suggestion);
                        else suggestion.add(child.getArgument());

                        if (!suggestion.isEmpty()) sub.put(child, suggestion);
                    }
                }
            }

            for (CommandComponent c : sub.keySet()) {
                List<String> suggestions = sub.get(c);

                for (String subCommand : suggestions) {
                    if (subCommand.contains(" ") && mergeSpaceArguments) {
                        String modSC = "\"" + subCommand + "\"";

                        if (c.matchTabComplete(sender, modSC, lastArg)) {
                            sug.add(modSC);
                            continue;
                        }

                        if (lastArg.isEmpty() || modSC.toLowerCase().startsWith(lastArg)) {
                            sug.add(modSC);
                            continue;
                        }
                    }

                    if (c.matchTabComplete(sender, subCommand, lastArg)) {
                        if (subCommand.contains(" ") && mergeSpaceArguments) sug.add("\"" + subCommand + "\"");
                        else sug.add(subCommand);
                        continue;
                    }

                    if (lastArg.isEmpty() || subCommand.toLowerCase().startsWith(lastArg)) {
                        if (subCommand.contains(" ") && mergeSpaceArguments) sug.add("\"" + subCommand + "\"");
                        else sug.add(subCommand);
                    }
                }

                suggestions.clear();
            }

            sub.clear();
        } else {
            NaturalCommandComponent ncc = (NaturalCommandComponent) component;
            if (ncc.hasPermission(sender)) {
                List<String> list = ncc.onTabComplete(sender, command, label, args);
                if (list != null) {
                    for (String s : list) {
                        if (s.contains(" ") && mergeSpaceArguments) s = "\"" + s + "\"";
                        sug.add(s);
                    }

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
        if (args.length == 0) return this.baseComponent;
        return getComponent(Arrays.asList(args));
    }

    public CommandComponent getComponent(List<String> s) {
        if (s.isEmpty()) return this.baseComponent;

        CommandComponent current = this.baseComponent;

        for (String value : s) {
            if (current == null || current instanceof NaturalCommandComponent) break;
            CommandComponent cc = current.getChild(value);

            if ((value != null && value.isEmpty()) && !(cc instanceof NaturalCommandComponent)) break;
            current = cc;
        }

        return current;
    }

    private String[] repairArgs(String[] args) {
        if (args == null || args.length == 0) return args;

        StringBuilder b = new StringBuilder();
        for (String s : args) {
            b.append(s);
            b.append(" ");
        }

        String s = b.toString();
        s = s.substring(0, s.length() - 1);

        boolean endingSpace = s.endsWith(" ");
        s = b.toString().trim().replaceAll(" {2,}?", "") + (endingSpace ? " " : "");

        if (s.isEmpty()) {
            return new String[]{""};
        }

        List<String> nArgs = new ArrayList<>();

        int parse = 0;
        char[] cA = s.toCharArray();
        for (int i = 0; i < cA.length; i++) {
            char c = cA[i];

            if (c == '"') {
                //search for correct usage
                for (int j = i + 1; j < cA.length; j++) {
                    char c1 = cA[j];

                    if (c1 == '"') {
                        //success
                        if (parse < i) {
                            //parse prepending remaining chars
                            nArgs.add(s.substring(parse, i));
                            parse = i;
                        }

                        nArgs.add(s.substring(parse + 1, j));

                        i = parse = j + 1;
                        break;
                    }
                }
            } else if (c == ' ') {
                nArgs.add(s.substring(parse, i));
                parse = i + 1;
            }
        }

        if (parse < cA.length) {
            nArgs.add(s.substring(parse, cA.length));
        }

        if (cA[cA.length - 1] == ' ') nArgs.add("");
        return nArgs.toArray(new String[0]);
    }

    public String getName() {
        return name;
    }

    public BaseComponent getBaseComponent() {
        return baseComponent;
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

    public String[] getImportantAliases() {
        return importantAliases;
    }

    public List<String> getAliases() {
        return aliases;
    }

    public boolean isMergeSpaceArguments() {
        return mergeSpaceArguments;
    }

    public void setMergeSpaceArguments(boolean mergeSpaceArguments) {
        this.mergeSpaceArguments = mergeSpaceArguments;
    }
}
