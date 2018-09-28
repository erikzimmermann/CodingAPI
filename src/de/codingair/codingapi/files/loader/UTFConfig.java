package de.codingair.codingapi.files.loader;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import de.codingair.codingapi.server.reflections.IReflection;
import org.apache.commons.lang.Validate;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.representer.Representer;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class UTFConfig extends YamlConfiguration {
    private static final String COMMENT = "#";
    private List<Extra> extras = new ArrayList<>();

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
        data = writeExtras(data);

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

            String dump = yaml.dump(this.getValues(false));

            if(dump.equals("{}\n"))
                dump = "";

            return dump;
        } catch(Exception ex) {
            ex.printStackTrace();
        }
        return "Error while running this#saveToString()";
    }

    @Override
    public void load(File file) throws IOException, InvalidConfigurationException {
        Validate.notNull(file, "File cannot be null");
        this.load(new InputStreamReader(new FileInputStream(file), Charsets.UTF_8));
    }

    @Override
    public void loadFromString(String contents) throws InvalidConfigurationException {
        loadExtras(contents);
        super.loadFromString(contents);
    }

    private void loadExtras(String contents) {
        this.extras.clear();

        if(contents.contains("\n")) {
            int line = 0;
            for(String s : contents.split("\n")) {
                if(isComment(s) || isEmpty(s)) extras.add(new Extra(s, line));

                line++;
            }
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
        List<String> lines = contents.contains("\n") ? new ArrayList<>(Arrays.asList(contents.split("\n"))) : new ArrayList<>();

        for(Extra c : this.extras) {
            if(c.getLine() >= lines.size()) lines.add(c.getText());
            else lines.add(c.getLine(), c.getText());
        }

        StringBuilder builder = new StringBuilder();

        for(String line : lines) {
            builder.append(line).append("\n");
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