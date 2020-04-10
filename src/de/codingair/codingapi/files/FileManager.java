package de.codingair.codingapi.files;

import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.List;

public class FileManager {
    private Plugin plugin;
    private List<ConfigFile> configList = new ArrayList<>();

    public FileManager(Plugin plugin) {
        this.plugin = plugin;
    }

    public ConfigFile getFile(String name) {
        return getFile(name, null);
    }

    public ConfigFile getFile(String name, String path) {
        for (ConfigFile file : configList) {
            if (file.getName().equalsIgnoreCase(name) && (path == null || path.equalsIgnoreCase(file.getPath()))) return file;
        }

        return null;
    }

    public void unloadFile(ConfigFile file) {
        this.configList.remove(file);
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
        ConfigFile cf = getFile(name, path);
        if(cf != null) return cf;

        cf = new ConfigFile(plugin, name, path, srcPath, removeUnused);
        this.configList.add(cf);
        return cf;
    }

    public void reloadAll() {
        for (ConfigFile file : this.configList) {
            file.reloadConfig();
        }
    }

    public void loadAll() {
        for (ConfigFile file : this.configList) {
            file.loadConfig();
        }
    }

    public void saveAll() {
        for (ConfigFile file : this.configList) {
            file.saveConfig();
        }
    }
}