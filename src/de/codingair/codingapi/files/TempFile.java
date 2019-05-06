package de.codingair.codingapi.files;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import de.codingair.codingapi.API;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Removing of this disclaimer is forbidden.
 *
 * @author codingair
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
        return getFile().getSimpleName() + "_" + id;
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

    public static void saveTempFiles(Plugin plugin, String path, Class<?> fileType) throws Exception {
        List<String> data = new ArrayList<>();

        for(TempFile tempFile : tempFiles) {
            if(tempFile.getFile().getSimpleName().equalsIgnoreCase(fileType.getSimpleName()) && tempFile.getSavingFile() != null) {
                data.add(tempFile.gson.toJson(tempFile.getSavingFile()));
            }
        }

        ConfigFile configFile = new ConfigFile(plugin, "TempFiles-" + fileType.getSimpleName(), path);
        configFile.getConfig().set("Data", data);
        configFile.saveConfig();
    }

    public static <T> List<T> loadTempFiles(JavaPlugin plugin, String path, Class<? extends T> fileType, TypeAdapter typeAdapter, boolean delete) throws Exception {
        ConfigFile configFile = new ConfigFile(plugin, "TempFiles-" + fileType.getSimpleName(), path);
        List<String> data = configFile.getConfig().getStringList("Data");
        List<T> files = new ArrayList<>();

        Gson gson = new GsonBuilder().registerTypeAdapter(fileType, typeAdapter).create();

        for(String datum : data) {
            files.add(gson.fromJson(datum, fileType));
        }

        if(delete) {
            configFile.delete();
            File file = new File(plugin.getDataFolder() + path);
            if(!file.delete()) file.deleteOnExit();
        }

        return files;
    }
}
