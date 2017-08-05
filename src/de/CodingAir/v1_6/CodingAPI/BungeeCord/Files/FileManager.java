package de.CodingAir.v1_6.CodingAPI.BungeeCord.Files;

import net.md_5.bungee.api.plugin.Plugin;

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
        this.configList.add(new ConfigFile(name, path, plugin));
    }

    public void reloadAll() {
        for (ConfigFile file : this.configList) {
            file.reload();
        }
    }
}