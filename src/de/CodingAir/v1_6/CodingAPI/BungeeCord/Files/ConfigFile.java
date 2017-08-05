package de.CodingAir.v1_6.CodingAPI.BungeeCord.Files;

import com.google.common.io.ByteStreams;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;

import java.io.*;

/**
 * Removing of this disclaimer is forbidden.
 *
 * @author CodingAir
 * @verions: 1.0.0
 **/

public class ConfigFile {
	private String name;
	private String path;
	private Plugin plugin;
	
	private Configuration config;
	
	public ConfigFile(String name, String path, Plugin plugin) {
		this.name = name;
		this.path = path;
		this.plugin = plugin;
	}
	
	public String getName() {
		return name;
	}
	
	public String getPath() {
		return path;
	}
	
	public Plugin getPlugin() {
		return plugin;
	}
	
	public void reload() {
		if(!plugin.getDataFolder().exists()) plugin.getDataFolder().mkdir();
		
		File file = new File(plugin.getDataFolder() + this.path, this.name + ".yml");
		
		if(!file.exists()) {
			try {
				file.createNewFile();
				
				InputStream is = plugin.getResourceAsStream(this.name + ".yml");
				OutputStream os = new FileOutputStream(file);
				ByteStreams.copy(is, os);
			} catch(IOException e) {
				e.printStackTrace();
			}
		}
		
		try {
			config = ConfigurationProvider.getProvider(YamlConfiguration.class).load(file);
		} catch(IOException e) {
			e.printStackTrace();
		}
	}
	
	public void save() {
		try {
			ConfigurationProvider.getProvider(YamlConfiguration.class).save(config, new File(plugin.getDataFolder(), this.name + ".yml"));
		} catch(IOException e) {
			e.printStackTrace();
		}
	}
	
	public Configuration getConfig() {
		if(config == null) this.reload();
		return config;
	}
}
