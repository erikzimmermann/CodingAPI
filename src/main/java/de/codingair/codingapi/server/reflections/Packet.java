package de.codingair.codingapi.server.reflections;

import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.HashMap;

public class Packet {
    public static final Object IGNORE = new Object();
    private final Class<?> clazz;
    private final HashMap<String, Object> fields = new HashMap<>();
    private Object packet;
    private Player[] players;
    private boolean initialized = false;

    public Packet(Class<?> packet) {
        this.clazz = packet;
        this.players = null;
    }

    public Packet(Class<?> packet, Player... players) {
        this.clazz = packet;
        this.players = players == null ? null : (players.length == 1 && players[0] == null ? null : players);
    }

    public void initialize(Object... parameters) {
        parameters = Arrays.stream(parameters).filter(o -> o == null || !o.equals(IGNORE)).toArray();

        Class<?>[] clazzes = new Class<?>[parameters.length];

        for (int i = 0; i < parameters.length; i++) {
            clazzes[i] = parameters[i].getClass();
        }

        initialize(clazzes, parameters);
    }

    public void initialize(Class<?>[] parameterTypes, Object... parameters) {
        this.packet = IReflection.getConstructor(this.clazz, parameterTypes).newInstance(parameters);
        setFields();

        this.initialized = true;
    }

    private void setFields() {
        if (this.packet == null) return;

        for (String name : this.fields.keySet()) {
            Object value = this.fields.get(name);

            IReflection.FieldAccessor field = IReflection.getField(this.clazz, name);
            field.set(this.packet, value);
        }

        this.fields.clear();
    }

    public void editField(String name, Object value) {
        this.fields.put(name, value);
    }

    public Object getPacket() {
        return packet;
    }

    public Player[] getPlayers() {
        return players;
    }

    public Packet setPlayers(Player... players) {
        this.players = players;
        return this;
    }

    public boolean isInitialized() {
        return initialized;
    }

    public void send() {
        if (!this.isInitialized()) return;

        if (this.players == null || this.players.length == 0) PacketUtils.sendPacketToAll(this.packet);
        else PacketUtils.sendPacket(this.packet, players);
    }
}
