package de.codingair.codingapi.transfer;

import com.google.common.collect.HashBiMap;
import de.codingair.codingapi.tools.Callback;
import de.codingair.codingapi.transfer.packets.utils.PacketType;
import de.codingair.codingapi.transfer.utils.PacketListener;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public abstract class DataHandler {
    private final HashBiMap<Class<?>, Integer> register = HashBiMap.create();
    private int ID = 0;

    protected final String getChannel, requestChannel;
    protected final HashMap<UUID, Callback<?>> callbacks = new HashMap<>();
    protected final Set<PacketListener> listeners = new HashSet<>();

    public DataHandler(String pluginName) {
        getChannel = pluginName + ":get";
        requestChannel = pluginName + ":request";

        for(PacketType value : PacketType.VALUES) {
            registerPacket(value.getPacket());
        }

        registering();
        ID = -1;
    }

    public abstract void registering();

    public abstract void onEnable();

    public abstract void onDisable();

    public void register(PacketListener listener) {
        this.listeners.add(listener);
    }

    public void registerPacket(Class<?> c) {
        if(ID == -1) throw new IllegalStateException("Packet classes cannot be registered on runtime!");
        register.put(c, ID++);
    }

    public int getId(Class<?> c) {
        Integer id = register.get(c);
        if(id != null) return id;
        else return -1;
    }

    public Class<?> byId(int id) {
        if(id < 0) return null;
        return register.inverse().get(id);
    }

    public <T> T produce(int id) {
        Class<?> c = byId(id);

        if(c == null) return null;

        try {
            return (T) c.newInstance();
        } catch(InstantiationException | IllegalAccessException e) {
            e.printStackTrace();
            return null;
        }
    }

    public String getRequestChannel() {
        return requestChannel;
    }

    public String getGetChannel() {
        return getChannel;
    }

    public void unregister(PacketListener listener) {
        this.listeners.remove(listener);
    }
}
