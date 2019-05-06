package de.codingair.codingapi.bungeecord.files;

import net.md_5.bungee.api.plugin.Plugin;

import java.io.IOException;
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

    public void loadFile(String name, String path, String srcPath) {
        this.configList.add(new ConfigFile(name, path, srcPath, plugin));
    }

    public void reloadAll() {
        for (ConfigFile file : this.configList) {
            try {
                file.load();
            } catch(IOException e) {
                e.printStackTrace();
            }
        }
    }
}