package de.codingair.codingapi.files;

import com.google.common.base.Preconditions;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;

public class FileManager {
    private final JavaPlugin plugin;
    private final HashMap<String, ConfigFile> cache = new HashMap<>();

    public FileManager(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    private String key(ConfigFile file) {
        return key(file.getName());
    }

    private String key(String name) {
        Preconditions.checkNotNull(name);
        return name.toLowerCase().trim();
    }

    public ConfigFile getFile(String name) {
        return cache.get(key(name));
    }

    public void unloadFile(ConfigFile file) {
        this.cache.remove(key(file));
        file.destroy();
    }

    public ConfigFile loadFile(String name, String path) {
        return loadFile(name, path, true);
    }

    public ConfigFile loadFile(String name, String path, boolean removeUnused) {
        return loadFile(name, path, null, removeUnused);
    }

    public ConfigFile loadFile(String name, String path, String srcPath) {
        return this.loadFile(name, path, srcPath, true);
    }

    public ConfigFile loadFile(String name, String path, String srcPath, boolean removeUnused) {
        ConfigFile c = getFile(name);
        if(c != null) return c;

        c = new ConfigFile(plugin, name, path, srcPath, removeUnused);
        this.cache.put(key(c), c);
        return c;
    }

    public void destroy() {
        this.cache.values().forEach(ConfigFile::destroy);
        this.cache.clear();
    }
}