package de.CodingAir.v1_6.CodingAPI.Files;

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
        for (ConfigFile file : configList) {
            if (file.getName().equalsIgnoreCase(name)) return file;
        }

        return null;
    }

    public void loadFile(String name, String path) {
        this.configList.add(new ConfigFile(plugin, name, path));
    }

    public void reloadAll() {
        for (ConfigFile file : this.configList) {
            file.reloadConfig();
        }
    }
}