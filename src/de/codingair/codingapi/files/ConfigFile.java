package de.codingair.codingapi.files;

import com.google.common.base.Charsets;
import de.codingair.codingapi.files.loader.UTFConfig;
import org.bukkit.plugin.Plugin;

import java.io.*;
import java.util.logging.Level;

public class ConfigFile {
    private UTFConfig config = null;
    private File configFile = null;
    private Plugin plugin;
    private String name;
    private String path;

    public ConfigFile(Plugin plugin, String name, String path) {
        this.plugin = plugin;
        this.name = name;
        this.path = path;

        this.loadConfig();

        InputStream in = plugin.getResource(this.name + ".yml");
        if(in != null) {
            InputStreamReader reader = new InputStreamReader(in, Charsets.UTF_8);

            BufferedReader input = new BufferedReader(reader);
            StringBuilder builder = new StringBuilder();

            String line;
            try {
                try {
                    while((line = input.readLine()) != null) {
                        builder.append(line);
                        builder.append('\n');
                    }
                } finally {
                    input.close();
                }
            } catch(IOException ex) {
                ex.printStackTrace();
            }

            this.config.deployExtras(builder.toString());
        }

        this.config.options().copyDefaults(true);
        this.config.options().copyHeader(true);
        this.saveConfig();
    }

    private void mkDir(File file) {
        if(!file.getParentFile().exists()) mkDir(file.getParentFile());
        if(!file.exists()) {
            try {
                file.mkdir();
            } catch(SecurityException ex) {
                throw new IllegalArgumentException("Plugin is not permitted to create a folder!");
            }
        }
    }

    public void loadConfig() {
        try {
            File folder = plugin.getDataFolder();
            if(!folder.exists()) mkDir(folder);

            if(!this.path.startsWith("/")) this.path = "/" + this.path;
            if(!this.path.endsWith("/")) this.path = this.path + "/";

            folder = new File(this.plugin.getDataFolder() + this.path);
            if(!this.path.isEmpty() && !this.path.equals("/") && !folder.exists()) mkDir(folder);

            configFile = new File(this.plugin.getDataFolder() + this.path, this.name + ".yml");

            if(!configFile.exists()) {
                configFile.createNewFile();
                try(InputStream in = plugin.getResource(this.name + ".yml");
                    OutputStream out = new FileOutputStream(configFile)) {
                    copy(in, out);
                }
            }

            config = UTFConfig.loadConf(configFile);

            InputStream reader = plugin.getResource(this.name + ".yml");
            if(reader != null) config.setDefaults(UTFConfig.loadConf(reader));
        } catch(Exception e) {
            e.printStackTrace();
        }
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

    public UTFConfig getConfig() {
        if(config == null) reloadConfig();

        return config;
    }

    public void saveConfig() {
        saveConfig(false);
    }

    public void destroy() {
        getConfig().destroy();
    }

    public void saveConfig(boolean destroy) {
        if(config == null || configFile == null) {
            return;
        }
        try {
            getConfig().save(configFile);
            if(destroy) destroy();
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