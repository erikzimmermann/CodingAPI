package de.codingair.codingapi.server.events;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

public class WalkListener implements Listener {
    public WalkListener() {
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onWalk(PlayerMoveEvent e) {
        Player p = e.getPlayer();

        double x = e.getFrom().getX() - e.getTo().getX();
        double y = e.getFrom().getY() - e.getTo().getY();
        double z = e.getFrom().getZ() - e.getTo().getZ();

        if(Math.abs(x) + Math.abs(y) + Math.abs(z) > 0.05) {
            PlayerWalkEvent event = new PlayerWalkEvent(p, e.getFrom().clone(), e.getTo().clone());
            Bukkit.getPluginManager().callEvent(event);
            if(event.isCancelled()) e.setCancelled(event.isCancelled());
            e.setTo(event.getTo());
            e.setFrom(event.getFrom());
        }
    }

}
