package de.codingair.codingapi.files.loader;

import com.google.common.base.Charsets;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.io.Files;
import de.codingair.codingapi.server.reflections.IReflection;
import org.apache.commons.lang.Validate;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.error.YAMLException;
import org.yaml.snakeyaml.representer.Representer;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class UTFConfig extends YamlConfiguration {
    private static final String COMMENT = "#";
    private final List<Extra> extras = new ArrayList<>();
    private final Cache<String, String> caseSensitive = CacheBuilder.newBuilder().expireAfterAccess(5, TimeUnit.MINUTES).build();
    private boolean deployedExtras = false;

    private UTFConfig() {
    }

    public UTFConfig copy() {
        UTFConfig config = new UTFConfig();
        config.map.putAll(map);
        return config;
    }

    public static UTFConfig loadConf(File file) {
        UTFConfig loader = new UTFConfig();

        try {
            loader.load(file);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return loader;
    }

    public static UTFConfig loadConf(InputStream stream) {
        UTFConfig loader = new UTFConfig();

        try {
            Validate.notNull(stream, "File cannot be null");
            loader.load(new InputStreamReader(stream, Charsets.UTF_8));
        } catch (Exception e) {
            e.printStackTrace();
        }

        return loader;
    }

    public void destroy() {
        this.extras.clear();
    }

    @Override
    public Object get(@NotNull String path, Object def) {
        Object value = super.get(path, null);

        if (value == null) {
            //search for case sensitive
            path = path.toLowerCase();
            String key = caseSensitive.getIfPresent(path);

            if (key == null) {
                for (String s : getKeys(true)) {
                    if (s.toLowerCase().equals(path)) {
                        key = s;
                        caseSensitive.put(path, s);
                        break;
                    }
                }
            }

            if (key != null) value = super.get(key, def);
        }

        return value;
    }

    @Override
    public void save(@NotNull File file) throws IOException {
        Validate.notNull(file, "File cannot be null");
        //noinspection UnstableApiUsage
        Files.createParentDirs(file);
        String data = writeExtras(this.saveToString());

        BufferedReader input = new BufferedReader(new InputStreamReader(new FileInputStream(file), Charsets.UTF_8));
        StringBuilder builder = new StringBuilder();

        String line;
        try {
            while ((line = input.readLine()) != null) {
                builder.append(line);
                builder.append('\n');
            }
        } finally {
            input.close();
        }

        String old = builder.toString();
        if (data.equals(old)) return;

        try (Writer writer = new OutputStreamWriter(new FileOutputStream(file), Charsets.UTF_8)) {
            writer.write(data);
        }
    }

    @Override
    public @NotNull String saveToString() {
        try {
            IReflection.FieldAccessor<DumperOptions> fy = IReflection.getField(getClass(), DumperOptions.class, 0);
            IReflection.FieldAccessor<Representer> fr = IReflection.getField(getClass(), Representer.class, 0);

            DumperOptions yamlOptions = fy.get(this);
            Representer yamlRepresenter = fr.get(this);
            DumperOptions.FlowStyle fs = DumperOptions.FlowStyle.BLOCK;

            yamlOptions.setIndent(this.options().indent());
            yamlOptions.setDefaultFlowStyle(fs);
            yamlOptions.setAllowUnicode(true);
            yamlRepresenter.setDefaultFlowStyle(fs);

            String dump = getYaml().dump(this.getValues(false));
            if (dump.equals("{}\n")) dump = "";

            return dump;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return "Error while running this#saveToString()";
    }

    private Yaml getYaml() {
        IReflection.FieldAccessor<Yaml> fYaml = IReflection.getField(getClass(), "yaml");
        return fYaml.get(this);
    }

    @Override
    protected @NotNull String parseHeader(@NotNull String input) {
        return "";
    }

    @Override
    protected @NotNull String buildHeader() {
        return "";
    }

    @Override
    public void load(@NotNull File file) throws IOException, InvalidConfigurationException {
        Validate.notNull(file, "File cannot be null");
        this.load(new InputStreamReader(new FileInputStream(file), Charsets.UTF_8));
    }

    @Override
    public void loadFromString(@NotNull String contents) throws InvalidConfigurationException {
        Validate.notNull(contents, "Contents cannot be null");
        loadExtras(contents);
        if (contents.startsWith("~Config\n")) contents = contents.replaceFirst("~Config\n", "");

        Map<?, ?> input;
        try {
            //setMaxAliasesForCollections is not available?
            //loaderOptions.setMaxAliasesForCollections(Integer.MAX_VALUE); // SPIGOT-5881: Not ideal, but was default pre SnakeYAML 1.26
            input = getYaml().load(contents);
        } catch (YAMLException var4) {
            throw new InvalidConfigurationException(var4);
        } catch (ClassCastException var5) {
            throw new InvalidConfigurationException("Top level is not a Map.");
        }

        String header = this.parseHeader(contents);
        if (header.length() > 0) this.options().header(header);
        if (input != null) this.convertMapsToSections(input, this);
    }

    @Override
    public void convertMapsToSections(Map<?, ?> input, @NotNull ConfigurationSection section) {
        for (Map.Entry<?, ?> entry : input.entrySet()) {
            String key = entry.getKey().toString();
            Object value = entry.getValue();

            if (value instanceof Map) {
                ConfigurationSection deep = section.getConfigurationSection(key);
                this.convertMapsToSections((Map<?, ?>) value, deep == null ? section.createSection(key) : deep);
            } else {
                section.set(key, value);
            }
        }
    }

    public void removeUnused(UTFConfig origin) {
        List<String> toRemove = new ArrayList<>();

        for (String key : getKeys(true)) {
            if (!origin.contains(key)) toRemove.add(key);
            else {
                Object current = get(key);
                Object old = origin.get(key);

                if (!(old instanceof Map) && current instanceof Map) toRemove.add(key);
                else if (!(current instanceof Map) && old instanceof Map) toRemove.add(key);
                if (!(old instanceof ConfigurationSection) && current instanceof ConfigurationSection) toRemove.add(key);
                else if (!(current instanceof ConfigurationSection) && old instanceof ConfigurationSection) toRemove.add(key);
            }
        }

        for (String key : toRemove) {
            set(key, null);
        }
    }

    public void deployExtras(String contents) {
        this.loadExtras(contents);
        this.deployedExtras = true;
    }

    private void loadExtras(String contents) {
        if (this.deployedExtras) return;
        this.extras.clear();

        int line = 0;

        String[] a = contents.split("\n", -1);

        int list = -1;
        for (int i = 0; i < a.length - 1; i++) {
            String s = a[i];

            int empty = 0;
            while (s.startsWith(" ")) {
                s = s.substring(1);
                empty++;
            }

            if (list > -1) {
                list = list < empty ? list : -1;
                if (list > -1) continue;
            }

            if (s.startsWith("-")) {
                list = empty;
                continue;
            }

            if (isComment(a[i]) || isEmpty(a[i])) extras.add(new Extra(a[i], line));
            line++;
        }
    }

    private boolean isComment(String s) {
        while (s.startsWith(" ")) s = s.replaceFirst(" ", "");
        return s.startsWith(COMMENT);
    }

    private boolean isEmpty(String s) {
        while (s.startsWith(" ")) s = s.replaceFirst(" ", "");
        return s.isEmpty() || s.equals("\n");
    }

    private String writeExtras(String contents) {
        List<String> lines = new ArrayList<>(Arrays.asList(contents.split("\n", -1)));

        final int size = lines.size() + this.extras.size();
        int e = 0;
        int listItems = 0;
        int list = -1;
        for (int i = 0; i < size; i++) {
            if (this.extras.size() == e) break;

            if (lines.size() == i) {
                lines.add(this.extras.get(e++).getText());
                continue;
            }

            String s = lines.get(i);
            int empty = 0;
            while (s.startsWith(" ")) {
                s = s.substring(1);
                empty++;
            }

            if (list > -1) {
                list = list < empty ? list : -1;
                if (list > -1) {
                    listItems++;
                    continue;
                }
            }

            if (s.startsWith("-")) {
                listItems++;
                list = empty;
                continue;
            }

            if (this.extras.get(e).getLine() == (i - listItems)) {
                lines.add(i, extras.get(e++).getText());
            }
        }

        StringBuilder builder = new StringBuilder();

        for (int j = 0; j < lines.size(); j++) {
            builder.append(lines.get(j));
            if (j < lines.size() - 1) builder.append("\n");
        }

        return builder.toString();
    }

    public void setDefaults(InputStream stream) {
        super.setDefaults(loadConf(stream));
    }
}