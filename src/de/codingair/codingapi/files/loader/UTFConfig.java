package de.codingair.codingapi.files.loader;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import de.codingair.codingapi.server.reflections.IReflection;
import org.apache.commons.lang.Validate;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.error.YAMLException;
import org.yaml.snakeyaml.representer.Representer;

import java.io.*;
import java.util.*;

public class UTFConfig extends YamlConfiguration {
    private static final String COMMENT = "#";
    private static final String CONFIG_TAG = "~Config\n";
    private List<Extra> extras = new ArrayList<>();
    private boolean loadExtras = false;
    private boolean deployedExtras = false;

    private UTFConfig() {
    }

    public void destroy() {
        this.extras.clear();
    }

    @Override
    public void save(File file) throws IOException {
        Validate.notNull(file, "File cannot be null");
        Files.createParentDirs(file);
        String data = this.saveToString();
        if(this.loadExtras) {
            data = writeExtras(CONFIG_TAG + data);
        }

        Writer writer = new OutputStreamWriter(new FileOutputStream(file), Charsets.UTF_8);

        try {
            writer.write(data);
        } finally {
            writer.close();
        }
    }

    @Override
    public String saveToString() {
        try {
            IReflection.FieldAccessor fy = IReflection.getField(getClass(), "yamlOptions");
            IReflection.FieldAccessor fr = IReflection.getField(getClass(), "yamlRepresenter");
            IReflection.FieldAccessor fYaml = IReflection.getField(getClass(), "yaml");

            DumperOptions yamlOptions = (DumperOptions) fy.get(this);
            Representer yamlRepresenter = (Representer) fr.get(this);
            Yaml yaml = (Yaml) fYaml.get(this);
            DumperOptions.FlowStyle fs = DumperOptions.FlowStyle.BLOCK;

            yamlOptions.setIndent(this.options().indent());
            yamlOptions.setDefaultFlowStyle(fs);
            yamlOptions.setAllowUnicode(true);
            yamlRepresenter.setDefaultFlowStyle(fs);

            String header = this.buildHeader();
            String dump = yaml.dump(this.getValues(false));
            if(dump.equals("{}\n")) dump = "";

            return header + dump;
        } catch(Exception ex) {
            ex.printStackTrace();
        }
        return "Error while running this#saveToString()";
    }

    @Override
    protected String parseHeader(String input) {
        if(loadExtras) return "";

        String[] lines = input.split("\r?\n", -1);
        StringBuilder result = new StringBuilder();
        boolean readingHeader = true;
        boolean foundHeader = false;

        for(int i = 0; i < lines.length && readingHeader; ++i) {
            String line = lines[i];
            if(line.startsWith(COMMENT)) {
                if(i > 0) {
                    result.append("\n");
                }

                result.append(line.substring(COMMENT.length()));
                foundHeader = true;
            } else if(foundHeader && line.length() == 0) {
                result.append("\n");
            } else if(foundHeader) {
                readingHeader = false;
            }
        }

        return result.toString();
    }

    @Override
    protected String buildHeader() {
        if(loadExtras) return "";

        String header = this.options().header();
        if(this.options().copyHeader()) {
            Configuration def = this.getDefaults();
            if(def instanceof UTFConfig) {
                UTFConfig fileDefaults = (UTFConfig) def;
                String defaultsHeader = fileDefaults.buildHeader();
                if(defaultsHeader != null && defaultsHeader.length() > 0) {
                    return defaultsHeader;
                }
            }
        }

        if(header == null) {
            return "";
        } else {
            StringBuilder builder = new StringBuilder();
            String[] lines = header.split("\r?\n", -1);
            boolean startedHeader = false;

            for(int i = lines.length - 1; i >= 0; --i) {
                builder.insert(0, "\n");
                if(startedHeader || lines[i].length() != 0) {
                    builder.insert(0, lines[i]);
                    builder.insert(0, COMMENT);
                    startedHeader = true;
                }
            }

            return builder.toString();
        }
    }

    @Override
    public void load(File file) throws IOException, InvalidConfigurationException {
        Validate.notNull(file, "File cannot be null");
        this.load(new InputStreamReader(new FileInputStream(file), Charsets.UTF_8));
    }

    @Override
    public void loadFromString(String contents) throws InvalidConfigurationException {
        if(contents.startsWith(CONFIG_TAG)) {
            this.loadExtras = true;
            loadExtras(contents);
            contents = contents.replaceFirst(CONFIG_TAG, "");
        }

        super.loadFromString(contents);
    }

    @Override
    public void convertMapsToSections(Map<?, ?> input, ConfigurationSection section) {
        Iterator var3 = input.entrySet().iterator();

        while(var3.hasNext()) {
            Map.Entry<?, ?> entry = (Map.Entry) var3.next();
            String key = entry.getKey().toString();
            Object value = entry.getValue();
            if(value instanceof Map) {
                this.convertMapsToSections((Map) value, section.getConfigurationSection(key) == null ? section.createSection(key) : section.getConfigurationSection(key));
            } else {
                section.set(key, value);
            }
        }
    }

    public void removeUnused(UTFConfig origin) {
        List<String> toRemove = new ArrayList<>();

        for(String key : getKeys(true)) {
            if(!origin.contains(key)) toRemove.add(key);
        }

        for(String key : toRemove) {
            set(key, null);
        }
    }

    public void deployExtras(String contents) {
        if(contents.startsWith(CONFIG_TAG)) {
            this.loadExtras = true;
            this.loadExtras(contents);
            this.deployedExtras = true;
        }
    }

    private void loadExtras(String contents) {
        if(this.deployedExtras) return;
        this.extras.clear();

        int line = 0;

        String[] a = contents.split("\n", -1);

        for(int i = 0; i < a.length - 1; i++) {
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

        for(Extra c : this.extras) {
            if(c.getLine() >= lines.size()) lines.add(c.getText());
            else lines.add(c.getLine(), c.getText());
        }

        StringBuilder builder = new StringBuilder();

        for(int i = 0; i < lines.size(); i++) {
            builder.append(lines.get(i));
            if(i < lines.size() - 1) builder.append("\n");
        }

        return builder.toString();
    }

    public void setDefaults(InputStream stream) {
        super.setDefaults(loadConf(stream));
    }

    public static UTFConfig loadConf(File file) {
        UTFConfig loader = new UTFConfig();

        try {
            loader.load(file);
        } catch(Exception e) {
            e.printStackTrace();
        }

        return loader;
    }

    public static UTFConfig loadConf(InputStream stream) {
        UTFConfig loader = new UTFConfig();

        try {
            Validate.notNull(stream, "File cannot be null");
            loader.load(new InputStreamReader(stream, Charsets.UTF_8));
        } catch(Exception e) {
            e.printStackTrace();
        }

        return loader;
    }
}