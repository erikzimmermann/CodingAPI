package de.codingair.codingapi.tools.io;

import de.codingair.codingapi.bungeecord.files.ConfigFile;
import de.codingair.codingapi.tools.io.JSON.JSON;
import de.codingair.codingapi.tools.io.JSON.JSONParser;
import de.codingair.codingapi.tools.io.lib.JSONArray;
import de.codingair.codingapi.tools.io.lib.ParseException;
import de.codingair.codingapi.tools.io.utils.DataMask;
import de.codingair.codingapi.tools.io.utils.Serializable;
import net.md_5.bungee.config.Configuration;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class BungeeConfigMask implements DataMask {
    private String prefix;
    private final ConfigFile file;

    public BungeeConfigMask(ConfigFile file) {
        this(file, null);
    }

    public BungeeConfigMask(ConfigFile file, String prefix) {
        this.file = file;
        this.prefix = prefix == null ? "" : prefix;
    }

    private Configuration c() {
        return file.getConfig();
    }

    private String k(String key) {
        return (prefix.isEmpty() ? "" : prefix + ".") + key;
    }

    public ConfigFile getFile() {
        return this.file;
    }

    public String getPrefix() {
        return prefix;
    }

    @Override
    public Set<String> keySet(boolean depth) {
        Collection<String> o = c().getKeys();
        Set<String> data = new HashSet<>();

        if(depth) {
            for(String s : o) {
                if(prefix.isEmpty()) data.add(s);
                else if(s.startsWith(prefix + ".")) data.add(s.replace(prefix + ".", ""));
            }
        } else {
            for(String s : o) {
                if(prefix.isEmpty()) data.add(s.split("\\.")[0]);
                else if(s.startsWith(prefix + ".")) data.add(s.replace(prefix + ".", "").split("\\.")[0]);
            }
        }

        return data;
    }

    public void write(Serializable s, String key) {
        String oldPrefix = prefix;
        prefix = k(key);
        s.write(this);
        prefix = oldPrefix;
    }

    public void read(Serializable s, String key) throws Exception {
        String oldPrefix = prefix;
        prefix = k(key);
        s.read(this);
        prefix = oldPrefix;
    }

    @Override
    public Object finalCommit(String key, Object value) {
        Object prev;

        if(value instanceof Serializable) {
            write((Serializable) value, key);
            prev = null;
        } else {
            prev = c().get(k(key));
            c().set(k(key), value);
        }

        return prev;
    }

    @Override
    public Object remove(String key) {
        Object prev = c().get(k(key));
        c().set(k(key), null);
        return prev;
    }

    @Override
    public <T extends Serializable> T getSerializable(String key, Serializable serializable) {
        try {
            read(serializable, key);
        } catch(Exception e) {
            e.printStackTrace();
        }

        return (T) serializable;
    }

    @Override
    public Boolean getBoolean(String key, Boolean def) {
        return c().getBoolean(k(key), def);
    }

    @Override
    public Integer getInteger(String key, Integer def) {
        return c().getInt(k(key), def);
    }

    @Override
    public JSONArray getList(String key) {
        JSONArray array = new JSONArray();
        List l = c().getList(k(key));

        if(l == null) return array;

        array.addAll(l);
        return array;
    }

    @Override
    public Long getLong(String key, Long def) {
        return c().getLong(k(key), def);
    }

    @Override
    public Double getDouble(String key, Double def) {
        return c().getDouble(k(key), def);
    }

    @Override
    public Float getFloat(String key, Float def) {
        Double d = getDouble(key);
        return d == null ? def : (Float) d.floatValue();
    }

    @Override
    public <T> T get(String key, T def, boolean raw) {
        Object o = c().get(k(key));

        if(!raw) {
            if(o instanceof Long) {
                long l = (long) o;
                if(l <= Integer.MAX_VALUE && l >= Integer.MIN_VALUE) return (T) (Object) Math.toIntExact(l);
            }

            if(o instanceof String) {
                try {
                    Object result = new JSONParser().parse((String) o);
                    if(result != null) o = result;
                } catch(ParseException ignored) {
                }
            }

            if(o instanceof org.json.simple.JSONObject) return (T) new JSON((org.json.simple.JSONObject) o);
        }

        return o == null ? def : (T) o;
    }
}
