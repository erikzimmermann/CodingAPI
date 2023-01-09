package de.codingair.codingapi.files;

import com.google.common.base.Charsets;
import de.codingair.codingapi.files.loader.UTFConfig;
import de.codingair.codingapi.server.specification.Version;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.*;
import java.util.logging.Level;

public class ConfigFile {
    private YamlConfiguration config = null;
    private File configFile = null;
    private final JavaPlugin plugin;
    private final String name;
    private String path;
    private String srcPath;
    private final boolean raw;

    public ConfigFile(JavaPlugin plugin, String name, String path) {
        this(plugin, name, path, true);
    }

    public ConfigFile(JavaPlugin plugin, String name, String path, boolean removeUnused) {
        this(plugin, name, path, null, removeUnused);
    }

    public ConfigFile(JavaPlugin plugin, String name, String path, String srcPath) {
        this(plugin, name, path, srcPath, true);
    }

    public ConfigFile(JavaPlugin plugin, String name, String path, String srcPath, boolean removeUnused) {
        this(plugin, name, path, srcPath, removeUnused, false);
    }

    public ConfigFile(JavaPlugin plugin, String name, String path, String srcPath, boolean removeUnused, boolean raw) {
        this.plugin = plugin;
        this.name = name;
        this.path = path;
        this.srcPath = srcPath;
        this.raw = raw;
        if (this.srcPath != null) {
            if (this.srcPath.startsWith("/")) this.srcPath = this.srcPath.replaceFirst("/", "");
            if (!this.srcPath.endsWith("/")) this.srcPath += "/";
        }

        this.loadConfig();

        InputStream in = plugin.getResource((this.srcPath == null ? "" : this.srcPath) + this.name + ".yml");
        if (in != null) {
            InputStreamReader reader = new InputStreamReader(in, Charsets.UTF_8);

            BufferedReader input = new BufferedReader(reader);
            StringBuilder builder = new StringBuilder();

            String line;
            try {
                try {
                    while ((line = input.readLine()) != null) {
                        builder.append(line);
                        builder.append('\n');
                    }
                } finally {
                    input.close();
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }

            String read;
            if (builder.toString().startsWith("~Config\n")) read = builder.toString().replaceFirst("~Config\n", "");
            else read = builder.toString();

            if (config instanceof UTFConfig) {
                ((UTFConfig) config).deployExtras(read);
            }

            if (removeUnused) {
                in = plugin.getResource((this.srcPath == null ? "" : this.srcPath) + this.name + ".yml");
                if (in != null) {
                    if (config instanceof UTFConfig) ((UTFConfig) config).removeUnused(UTFConfig.loadConf(in));
                }
            }

            this.config.options().copyDefaults(true);
            this.config.options().copyHeader(true);
            this.saveConfig();
        }
    }

    private void mkDir(File file) {
        if (!file.getParentFile().exists()) mkDir(file.getParentFile());
        if (!file.exists()) {
            try {
                file.mkdir();
            } catch (SecurityException ex) {
                throw new IllegalArgumentException("Plugin is not permitted to create a folder!");
            }
        }
    }

    public void loadConfig() {
        try {
            File folder = plugin.getDataFolder();
            if (!folder.exists()) mkDir(folder);

            if (!this.path.startsWith("/")) this.path = "/" + this.path;
            if (!this.path.endsWith("/")) this.path = this.path + "/";

            folder = new File(this.plugin.getDataFolder() + this.path);
            if (!this.path.isEmpty() && !this.path.equals("/") && !folder.exists()) mkDir(folder);

            configFile = new File(this.plugin.getDataFolder() + this.path, this.name + ".yml");

            if (!configFile.exists()) {
                configFile.createNewFile();
                try (InputStream in = plugin.getResource((srcPath == null ? "" : srcPath) + this.name + ".yml");
                     OutputStream out = new FileOutputStream(configFile)) {
                    copy(in, out);
                }
            }

            InputStream reader = raw ? null : plugin.getResource((srcPath == null ? "" : srcPath) + this.name + ".yml");
            if (reader != null) {
                if (Version.atLeast(19.3)) {
                    config = YamlConfiguration.loadConfiguration(new InputStreamReader(reader, Charsets.UTF_8));
                } else config = UTFConfig.loadConf(reader);

                mergeConfigFile();
            } else {
                config = UTFConfig.loadConf(configFile);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void mergeConfigFile() {
        YamlConfiguration c = YamlConfiguration.loadConfiguration(configFile);

        // just apply all values; this is only important for new entries from the plugin resource
        for (String key : config.getKeys(true)) {
            Object current = config.get(key);
            Object fileValue = c.get(key);

            if (current != null && fileValue != null && current.getClass() == fileValue.getClass() && !(current instanceof ConfigurationSection)) {
                config.set(key, fileValue);
            }
        }
    }

    private long copy(InputStream from, OutputStream to) throws IOException {
        if (from == null) return -1;
        if (to == null) throw new NullPointerException();

        byte[] buf = new byte[4096];
        long total = 0L;

        while (true) {
            int r = from.read(buf);
            if (r == -1) {
                return total;
            }

            to.write(buf, 0, r);
            total += r;
        }
    }

    public void reloadConfig() {
        saveConfig();
        loadConfig();
    }

    public YamlConfiguration getConfig() {
        if (config == null) reloadConfig();

        return config;
    }

    public void saveConfig() {
        saveConfig(false);
    }

    public void destroy() {
        if (config instanceof UTFConfig) {
            ((UTFConfig) config).destroy();
        }
    }

    public void saveConfig(boolean destroy) {
        if (config == null || configFile == null) {
            return;
        }
        try {
            getConfig().save(configFile);
            if (destroy) destroy();
        } catch (IOException ex) {
            this.plugin.getLogger().log(Level.SEVERE, "Could not save config to " + configFile, ex);
        }
    }

    public void clearConfig() {
        if (this.config == null) return;

        for (String key : this.config.getKeys(false)) {
            this.config.set(key, null);
        }
    }

    public void delete() {
        if (this.configFile == null) return;

        if (!this.configFile.delete()) this.configFile.deleteOnExit();
    }

    public String getName() {
        return name;
    }

    public String getPath() {
        return path;
    }

    public File getConfigFile() {
        return configFile;
    }

    public JavaPlugin getPlugin() {
        return plugin;
    }
}