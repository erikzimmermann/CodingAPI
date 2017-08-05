package de.CodingAir.v1_6.CodingAPI.Files;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Removing of this disclaimer is forbidden.
 *
 * @author CodingAir
 * @verions: 1.0.0
 **/

public class TempFile<T> {
	private Gson gson;
	
	private static List<TempFile> tempFiles = new ArrayList<>();
	private static int ID = 0;
	
	private Class<? extends T> file;
	private Object savingFile;
	private boolean save;
	private int id = ID++;
	
	public TempFile(Class<? extends T> file, TypeAdapter typeAdapter, boolean save) {
		this.file = file;
		this.save = save;
		this.gson = new GsonBuilder().registerTypeAdapter(file, typeAdapter).create();
	}
	
	public boolean hasToSave() {
		return save;
	}
	
	public Class<? extends T> getFile() {
		return file;
	}
	
	public String getId() {
		return getFile().getSimpleName()+"_"+id;
	}
	
	public Object getSavingFile() {
		return savingFile;
	}
	
	public void setSavingFile(Object savingFile) {
		this.savingFile = savingFile;
		if(savingFile != null && !tempFiles.contains(this)) tempFiles.add(this);
	}
	
	public static List<TempFile> getTempFiles() {
		return tempFiles;
	}
	
	public void remove() {
		tempFiles.remove(this);
	}
	
	public static void saveTempFiles(Plugin plugin, String path, Class<?> fileType) {
		List<String> data = new ArrayList<>();
		
		for(TempFile tempFile : tempFiles) {
			if(tempFile.getFile().getSimpleName().equalsIgnoreCase(fileType.getSimpleName()) && tempFile.getSavingFile() != null) {
				data.add(tempFile.gson.toJson(tempFile.getSavingFile()));
			}
		}
		
		ConfigFile configFile = new ConfigFile(plugin, "TempFiles-"+fileType.getSimpleName(), path);
		configFile.getConfig().set("Data", data);
		configFile.saveConfig();
	}
	
	public static <T> List<T> loadTempFiles(Plugin plugin, String path, Class<? extends T> fileType, TypeAdapter typeAdapter, boolean delete) {
		ConfigFile configFile = new ConfigFile(plugin, "TempFiles-"+fileType.getSimpleName(), path);
		List<String> data = configFile.getConfig().getStringList("Data");
		List<T> files = new ArrayList<>();
		
		Gson gson = new GsonBuilder().registerTypeAdapter(fileType, typeAdapter).create();
		
		for(String datum : data) {
			files.add(gson.fromJson(datum, fileType));
		}
		
		if(delete) {
			configFile.delete();
			File file = new File(path);
			if(!file.delete()) file.deleteOnExit();
		}
		
		return files;
	}
}
