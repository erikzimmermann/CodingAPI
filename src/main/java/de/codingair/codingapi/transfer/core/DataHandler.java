package de.codingair.codingapi.transfer.core;

import com.google.common.collect.HashBiMap;
import de.codingair.codingapi.tools.Callback;
import de.codingair.codingapi.transfer.packets.utils.*;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public abstract class DataHandler<P> {
    private final HashBiMap<Class<?>, Integer> register = HashBiMap.create();
    private int ID = 0;

    protected final String channelBackend, channelProxy;
    protected final ConcurrentHashMap<UUID, Callback<?>> callbacks = new ConcurrentHashMap<>();
    protected final Set<PacketListener<P>> listeners = new HashSet<>();

    public DataHandler(String pluginName) {
        channelBackend = pluginName + ":backend";
        channelProxy = pluginName + ":proxy";

        for(PacketType value : PacketType.VALUES) {
            registerPacket(value.getPacket());
        }

        registering();
        ID = -1;
    }

    public abstract void registering();

    public abstract void onEnable();

    public abstract void onDisable();

    public abstract void send(byte[] data, P sender);

    public void send(Packet packet, P sender) {
        processPacket(packet).ifPresent(data -> send(data, sender));
    }

    public void register(PacketListener<P> listener) {
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

    public <T> T formPacket(int id) {
        Class<?> c = byId(id);

        if(c == null) return null;

        try {
            return (T) c.newInstance();
        } catch(InstantiationException | IllegalAccessException e) {
            e.printStackTrace();
            return null;
        }
    }

    public Optional<Packet> translateBytes(byte[] bytes) {
        DataInputStream in = new DataInputStream(new ByteArrayInputStream(bytes));

        try {
            Packet packet = formPacket(in.readUnsignedShort());
            if(packet == null) return Optional.empty();

            packet.read(in);
            return Optional.of(packet);
        } catch(IOException e1) {
            e1.printStackTrace();
            return Optional.empty();
        }
    }

    public Optional<byte[]> processPacket(Packet packet) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(stream);

        int id = getId(packet.getClass());
        if(id == -1) throw new IllegalStateException(packet.getClass() + " is not registered!");

        if(packet instanceof RequestPacket && ((RequestPacket<?>) packet).getCallback() != null) {
            RequestPacket<?> rp = (RequestPacket<?>) packet;
            
            if(callbacks.get(rp.getUniqueId()) != null) rp.checkUUID(this.callbacks.keySet());
            callbacks.put(rp.getUniqueId(), rp.getCallback());

            processRequestPacket(rp, rp.getCallback());
        }

        try {
            out.writeShort(id);
            packet.write(out);
        } catch(IOException e) {
            e.printStackTrace();
        }

        for(PacketListener<P> listener : listeners) {
            if(listener.onSend(packet)) return Optional.empty();
        }

        return Optional.of(stream.toByteArray());
    }

    public void onReceive(byte[] data, P sender) {
        Optional<Packet> opt = translateBytes(data);
        opt.ifPresent(p -> onReceive(opt.get(), sender));
    }

    protected void onReceive(Packet packet, P sender) {
        if(packet instanceof AnswerPacket) {
            UUID uniqueId = ((AssignedPacket) packet).getUniqueId();
            Callback callback;
            if((callback = this.callbacks.remove(uniqueId)) == null) return;
            callback.accept(((AnswerPacket<?>) packet).getValue());
            
            processAnswerPacket((AnswerPacket<?>) packet, sender, callback);
        }

        for(PacketListener<P> listener : listeners) {
            listener.onReceive(packet, sender);
        }
    }

    protected void processRequestPacket(RequestPacket<?> packet, @Nullable Callback<?> callback) {
    }

    protected void processAnswerPacket(AnswerPacket<?> packet, P player, @Nullable Callback<?> callback) {
        
    }

    public String getChannelProxy() {
        return channelProxy;
    }

    public String getChannelBackend() {
        return channelBackend;
    }

    public void unregister(PacketListener listener) {
        this.listeners.remove(listener);
    }
}
