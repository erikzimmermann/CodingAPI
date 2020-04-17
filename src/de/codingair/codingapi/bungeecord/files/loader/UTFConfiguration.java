package de.codingair.codingapi.bungeecord.files.loader;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import de.codingair.codingapi.files.loader.Extra;
import de.codingair.codingapi.server.reflections.IReflection;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.representer.Representer;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class UTFConfiguration extends ConfigurationProvider {
    private static final String COMMENT = "#";
    private List<Extra> extras = new ArrayList<>();
    private boolean deployedExtras = false;
    private Configuration config;

    private final ThreadLocal<Yaml> yaml = new ThreadLocal<Yaml>() {
        protected Yaml initialValue() {
            Representer representer = new Representer() {{
                this.representers.put(Configuration.class, data -> represent(getSelf((Configuration) data)));
            }};
            DumperOptions options = new DumperOptions();
            options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
            return new Yaml(new Constructor(), representer, options);
        }
    };

    public void destroy() {
        this.extras.clear();
    }

    private Map<String, Object> getSelf(Configuration config) {
        IReflection.FieldAccessor<Map<String, Object>> self = IReflection.getField(Configuration.class, "self");
        return self.get(config);
    }

    private Configuration createConfiguration(Map<?, ?> map, Configuration def) {
        if(map == null) return new Configuration(def);
        return (Configuration) IReflection.getConstructor(Configuration.class, Map.class, Configuration.class).newInstance(map, def);
    }

    public static void notNull(Object object) {
        notNull(object, "The validated object is null");
    }

    public static void notNull(Object object, String message) {
        if(object == null) {
            throw new IllegalArgumentException(message);
        }
    }

    public void save(Configuration config, File file) throws IOException {
        notNull(file, "File cannot be null");
        Files.createParentDirs(file);

        Writer writer = new OutputStreamWriter(new FileOutputStream(file), Charsets.UTF_8);
        save(config, writer);
    }

    @Override
    public void save(Configuration config, Writer writer) {
        String data = writeExtras(this.saveToString(config));

        try {
            writer.write(data);
        } catch(IOException e) {
            e.printStackTrace();
        } finally {
            try {
                writer.close();
            } catch(IOException e) {
                e.printStackTrace();
            }
        }
    }

    private String saveToString(Configuration config) {
        String dump = this.yaml.get().dump(getSelf(config));
        if(dump.equals("{}\n")) dump = "";
        return dump;
    }

    private Configuration loadFromString(String contents, Configuration def) {
        notNull(contents, "Contents cannot be null");
        loadExtras(contents);
        contents = contents.replaceFirst("~Config\n", "");

        Map<String, Object> input = this.yaml.get().load(contents);
        config = createConfiguration(input, def);

        removeUnused(def);

        return config;
    }

    public Configuration loadByString(Reader reader, Configuration def) throws IOException {
        BufferedReader input = reader instanceof BufferedReader ? (BufferedReader) reader : new BufferedReader(reader);
        StringBuilder builder = new StringBuilder();

        String line;
        try {
            while((line = input.readLine()) != null) {
                builder.append(line);
                builder.append('\n');
            }
        } finally {
            input.close();
        }

        return this.loadFromString(builder.toString(), def);
    }

    public Configuration load(File file) throws IOException {
        notNull(file, "File cannot be null");
        return this.load(new InputStreamReader(new FileInputStream(file), Charsets.UTF_8));
    }

    public Configuration load(File file, Configuration defaults) throws IOException {
        notNull(file, "File cannot be null");
        return this.load(new InputStreamReader(new FileInputStream(file), Charsets.UTF_8), defaults);
    }

    public Configuration load(Reader reader) {
        try {
            return loadByString(reader, null);
        } catch(IOException e) {
            e.printStackTrace();
            return config = new Configuration();
        }
    }

    public Configuration load(Reader reader, Configuration defaults) {
        try {
            return loadByString(reader, defaults);
        } catch(IOException e) {
            e.printStackTrace();
            return config = new Configuration();
        }
    }

    public Configuration load(InputStream is) {
        return this.load(is, null);
    }

    public Configuration load(InputStream is, Configuration defaults) {
        try {
            return this.loadByString(new InputStreamReader(is), defaults);
        } catch(IOException e) {
            e.printStackTrace();
            return config = new Configuration();
        }
    }

    public Configuration load(String string) {
        return this.load(string, null);
    }

    public Configuration load(String string, Configuration defaults) {
        return this.loadFromString(string, defaults);
    }


    //--------------------------------------


    public void removeUnused(Configuration origin) {
        if(origin == null) return;
        removeUnused("", config, origin);
    }

    private void removeUnused(String prefix, Configuration config, Configuration defaults) {
        Map<String, Object> data = getSelf(config);
        Map<String, Object> def = getSelf(defaults);

        List<String> toRemove = new ArrayList<>();

        for(String key : data.keySet()) {
            if(def.containsKey(key)) {
                Object baseValue = data.get(key);
                Object dataValue = def.get(key);

                if(dataValue instanceof Configuration && baseValue instanceof Configuration) {
                    removeUnused(prefix + key + ".", (Configuration) baseValue, (Configuration) dataValue);
                }
            } else toRemove.add(prefix + key);
        }

        for(String key : toRemove) {
            getConfig().set(key, null);
        }

        toRemove.clear();
    }

    public void deployExtras(String contents) {
        this.loadExtras(contents);
        this.deployedExtras = true;
    }

    private void loadExtras(String contents) {
        if(this.deployedExtras) return;
        this.extras.clear();

        int line = 0;

        String[] a = contents.split("\n", -1);

        int list = -1;
        for(int i = 0; i < a.length - 1; i++) {
            String s = a[i];

            int empty = 0;
            while(s.startsWith(" ")) {
                s = s.substring(1);
                empty++;
            }

            if(list > -1) {
                list = list < empty ? list : -1;
                if(list > -1) continue;
            }

            if(s.startsWith("-")) {
                list = empty;
                continue;
            }

            if(isComment(a[i]) || isEmpty(a[i])) extras.add(new Extra(a[i], line));
            line++;
        }
    }

    private boolean isComment(String s) {
        while(s.startsWith(" ")) s = s.replaceFirst(" ", "");
        return s.startsWith(COMMENT);
    }

    private boolean isEmpty(String s) {
        while(s.startsWith(" ")) s = s.replaceFirst(" ", "");
        return s.isEmpty() || s.equals("\n");
    }

    private String writeExtras(String contents) {
        List<String> lines = new ArrayList<>(Arrays.asList(contents.split("\n", -1)));

        final int size = lines.size() + this.extras.size();
        int e = 0;
        int listItems = 0;
        int list = -1;
        for(int i = 0; i < size; i++) {
            if(this.extras.size() == e) break;

            if(lines.size() == i) {
                lines.add(this.extras.get(e++).getText());
                continue;
            }

            String s = lines.get(i);
            int empty = 0;
            while(s.startsWith(" ")) {
                s = s.substring(1);
                empty++;
            }

            if(list > -1) {
                list = list < empty ? list : -1;
                if(list > -1) {
                    listItems++;
                    continue;
                }
            }

            if(s.startsWith("-")) {
                listItems++;
                list = empty;
                continue;
            }

            if(this.extras.get(e).getLine() == (i - listItems)) {
                lines.add(i, extras.get(e++).getText());
            }
        }

        StringBuilder builder = new StringBuilder();

        for(int j = 0; j < lines.size(); j++) {
            builder.append(lines.get(j));
            if(j < lines.size() - 1) builder.append("\n");
        }

        return builder.toString();
    }

    public Configuration getConfig() {
        return config;
    }
}
