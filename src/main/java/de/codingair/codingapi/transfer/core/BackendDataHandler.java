package de.codingair.codingapi.transfer.core;

import de.codingair.codingapi.tools.Callback;
import de.codingair.codingapi.tools.time.TimeList;
import de.codingair.codingapi.transfer.packets.utils.AnswerPacket;
import de.codingair.codingapi.transfer.packets.utils.AssignedPacket;
import de.codingair.codingapi.transfer.packets.utils.Packet;
import de.codingair.codingapi.transfer.packets.utils.RequestPacket;
import org.bukkit.entity.Player;

import java.util.Optional;
import java.util.UUID;

public abstract class BackendDataHandler<P> extends DataHandler<P> {
    protected final TimeList<UUID> timeOut = new TimeList<UUID>() {
        @Override
        public void timeout(UUID item) {
            Callback<?> callback = callbacks.remove(item);
            if(callback != null) callback.accept(null);
        }
    };

    public BackendDataHandler(String pluginName) {
        super(pluginName);
    }

    public abstract Optional<P> getRandomPlayer();

    public void send(P player, Packet packet) {
        send(player, packet, -1);
    }

    public void send(P player, Packet packet, int timeOut) {
        P sender;
        if(player == null) {
            Optional<P> opt = getRandomPlayer();
            if(!opt.isPresent()) return;
            sender = opt.get();
        } else sender = player;

        if(packet instanceof RequestPacket && timeOut > 0 && ((RequestPacket<?>) packet).getCallback() != null) {
            this.timeOut.add(((RequestPacket<?>) packet).getUniqueId(), timeOut);
        }

        processPacket(packet).ifPresent(data -> send(data, sender));
    }

    @Override
    public void onReceive(Packet packet, P sender) {
        super.onReceive(packet, sender);

        if(packet instanceof AnswerPacket) {
            UUID uniqueId = ((AssignedPacket) packet).getUniqueId();
            this.timeOut.remove(uniqueId);
        }
    }
}
