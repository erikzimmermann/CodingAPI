package de.codingair.codingapi.bungeecord.files;

import com.google.common.base.Charsets;
import de.codingair.codingapi.bungeecord.files.loader.UTFConfiguration;
import de.codingair.codingapi.server.reflections.IReflection;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.config.Configuration;

import java.io.*;
import java.util.Map;

/**
 * Removing of this disclaimer is forbidden.
 *
 * @author codingair
 * @verions: 1.0.0
 **/

public class ConfigFile {
    private final UTFConfiguration configuration;
    private final String name;
    private final String path;
    private final String srcPath;
    private final Plugin plugin;

    public ConfigFile(String name, String path, String srcPath, Plugin plugin) {
        configuration = new UTFConfiguration();
        this.name = name;
        this.path = path;
        this.srcPath = srcPath == null ? "" : srcPath;
        this.plugin = plugin;

        try {
            this.load();
            this.save();
        } catch(IOException e) {
            e.printStackTrace();
        }
    }

    public ConfigFile(String name, String path, Plugin plugin) {
        this(name, path, "", plugin);
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

    private File getDataFolder() {
        return plugin.getDataFolder();
    }

    public void load() throws IOException {
        if(getDataFolder() == null || !getDataFolder().exists()) getDataFolder().mkdir();

        File file = new File(getDataFolder() + this.path, this.name + ".yml");

        InputStream in = plugin.getResourceAsStream(srcPath + this.name + ".yml");

        if(!file.exists()) {
            file.createNewFile();

            if(in != null) {
                OutputStream out = new FileOutputStream(file);
                copy(in, out);
                out.close();
            }
        }


        in = plugin.getResourceAsStream(srcPath + this.name + ".yml");
        Configuration defaults = in == null ? null : configuration.load(in);
        configuration.load(file, defaults);

        if(defaults != null) {
            configuration.removeUnused(defaults);
            merge(configuration.getConfig(), defaults);
        }

        in = plugin.getResourceAsStream(srcPath + this.name + ".yml");
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

            if(builder.toString().startsWith("~Config\n")) {
                configuration.deployExtras(builder.toString());
            }
        }

        if(in != null) in.close();
    }

    private void merge(Configuration config, Configuration defaults) {
        Map<String, Object> data = getSelf(config);
        Map<String, Object> def = getSelf(defaults);

        for(String key : def.keySet()) {
            if(data.containsKey(key)) {
                Object baseValue = data.get(key);
                Object dataValue = def.get(key);

                if(dataValue instanceof Configuration) {
                    if(baseValue instanceof Configuration) {
                        merge((Configuration) baseValue, (Configuration) dataValue);
                    } else data.put(key, dataValue);
                }
            } else {
                data.put(key, def.get(key));
            }
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
            total += r;
        }
    }

    public void save() {
        try {
            configuration.save(configuration.getConfig(), new File(getDataFolder(), path + this.name + ".yml"));
        } catch(IOException e) {
            e.printStackTrace();
        }
    }

    private Map<String, Object> getSelf(Configuration config) {
        IReflection.FieldAccessor<Map<String, Object>> self = IReflection.getField(Configuration.class, "self");
        return self.get(config);
    }

    public Configuration getConfig() {
        if(configuration.getConfig() == null) {
            try {
                this.load();
            } catch(IOException e) {
                e.printStackTrace();
            }
        }
        return configuration.getConfig();
    }

    public void clearConfig() {
        for(String key : getConfig().getKeys()) {
            getConfig().set(key, null);
        }
    }
}
