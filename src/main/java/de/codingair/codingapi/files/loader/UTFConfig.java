package de.codingair.codingapi.files.loader;

import com.google.common.base.Charsets;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.io.Files;
import de.codingair.codingapi.server.specification.Version;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class UTFConfig extends YamlConfiguration {
    private static final String COMMENT = "# ";
    private final List<Extra> extras = new ArrayList<>();
    private final Cache<String, String> caseSensitive = CacheBuilder.newBuilder().expireAfterAccess(5, TimeUnit.MINUTES).build();
    private boolean deployedExtras = false;

    private UTFConfig() {
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
            loader.load(new InputStreamReader(stream, Charsets.UTF_8));
        } catch (Exception e) {
            e.printStackTrace();
        }

        return loader;
    }

    public UTFConfig copy() {
        UTFConfig config = new UTFConfig();
        config.map.putAll(map);
        return config;
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
        //noinspection UnstableApiUsage
        Files.createParentDirs(file);

        String data;
        if (Version.atLeast(18.1)) data = this.saveToString();
        else data = writeExtras(this.saveToString());

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

    protected @NotNull String parseHeader(@NotNull String input) {
        return "";
    }

    @Override
    protected @NotNull String buildHeader() {
        return "";
    }

    @Override
    public void loadFromString(@NotNull String contents) throws InvalidConfigurationException {
        super.loadFromString(contents);
        if (Version.before(18.1)) loadExtras(contents);
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
                if (!(old instanceof ConfigurationSection) && current instanceof ConfigurationSection)
                    toRemove.add(key);
                else if (!(current instanceof ConfigurationSection) && old instanceof ConfigurationSection)
                    toRemove.add(key);
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