package de.CodingAir.v1_6.CodingAPI.Files;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
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
		
		this.reloadConfig();
		this.config.options().copyDefaults(true);
		this.config.options().copyHeader(true);
		this.saveConfig();
	}
	
	public void reloadConfig() {
		
		if (configFile == null) {
			configFile = new File(this.plugin.getDataFolder(), this.path + this.name + ".yml");
		}
		
		config = YamlConfiguration.loadConfiguration(configFile);
		
		Reader defConfigStream = null;
		try {
			defConfigStream = new InputStreamReader(this.plugin.getResource(name + ".yml"), "UTF8");
		} catch (Exception e) {
		}
		
		if (defConfigStream != null) {
			YamlConfiguration GGmapsdefConfig = YamlConfiguration.loadConfiguration(defConfigStream);
			config.setDefaults(GGmapsdefConfig);
		}
	}
	
	public FileConfiguration getConfig() {
		if (config == null) {
			reloadConfig();
		}
		
		return config;
	}
	
	public void saveConfig() {
		if (config == null || configFile == null) {
			return;
		}
		try {
			getConfig().save(configFile);
			this.reloadConfig();
		} catch (IOException ex) {
			this.plugin.getLogger().log(Level.SEVERE, "Could not save config to " + configFile, ex);
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