package de.codingair.codingapi.files;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.*;
import java.util.logging.Level;

public class ConfigFile {
    private FileConfiguration config = null;
    private File configFile = null;
    private Plugin plugin;
    private String name;
    private String path;

    public ConfigFile(Plugin plugin, String name, String path) {
        this.plugin = plugin;
        this.name = name;
        this.path = path;

        this.loadConfig();
//        this.config.options().copyDefaults(true);
//        this.config.options().copyHeader(true);
        this.saveConfig();
    }

    public void loadConfig() {
        File folder = plugin.getDataFolder();
        if(!folder.exists()) folder.mkdir();

        configFile = new File(this.plugin.getDataFolder(), this.path + this.name + ".yml");

        try {
            if(!configFile.exists()) {
                configFile.createNewFile();
                try(InputStream in = plugin.getResource(this.name + ".yml");
                    OutputStream out = new FileOutputStream(configFile)) {
                    copy(in, out);
                }
            }
        } catch(Exception e) {
            e.printStackTrace();
        }

        config = YamlConfiguration.loadConfiguration(configFile);
    }

    private long copy(InputStream from, OutputStream to) throws IOException {
        if(from == null) return -1;
        if(to == null) throw new NullPointerException();

        byte[] buf = new byte[4096];
        long total = 0L;

        while(true) {
            int r = from.read(buf);
            if(r == -1) {
                return total;
            }

            to.write(buf, 0, r);
            total += (long) r;
        }
    }

    public void reloadConfig() {
        saveConfig();
        loadConfig();
    }

    public FileConfiguration getConfig() {
        if(config == null) reloadConfig();

        return config;
    }

    public void saveConfig() {
        if(config == null || configFile == null) {
            return;
        }
        try {
            getConfig().save(configFile);
            this.loadConfig();
        } catch(IOException ex) {
            this.plugin.getLogger().log(Level.SEVERE, "Could not save config to " + configFile, ex);
        }
    }

    public void clearConfig() {
        if(this.config == null) return;

        for(String key : this.config.getKeys(false)) {
            this.config.set(key, null);
        }
    }

    public void delete() {
        if(this.configFile == null) return;

        if(!this.configFile.delete()) this.configFile.deleteOnExit();
    }

    public String getName() {
        return name;
    }

    public String getPath() {
        return path;
    }
}