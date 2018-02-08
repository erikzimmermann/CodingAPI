package de.codingair.codingapi.game;

import de.codingair.codingapi.game.utils.GameState;
import de.codingair.codingapi.game.utils.Team;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.*;
import org.bukkit.event.player.*;
import org.bukkit.potion.Potion;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

/**
 * Removing of this disclaimer is forbidden.
 *
 * @author codingair
 * @verions: 1.0.0
 **/

public class GameListener implements Listener {
    private Game game;

    public GameListener(Game game) {
        this.game = game;
    }

    public Game getGame() {
        return game;
    }

    @EventHandler(priority = EventPriority.LOW)
    public void EventHandler_onJoin(PlayerJoinEvent e) {
        Player p = e.getPlayer();

        if(game.joinOnServerJoin()) {
            this.game.join(p);
            e.setJoinMessage(null);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void EventHandler_onQuit(PlayerQuitEvent e) {
        Player p = e.getPlayer();

        if(this.game.isPlaying(p)) {
            this.game.quit(p);
            e.setQuitMessage(null);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void EventHandler_onKill(PlayerDeathEvent e) {
        Player p = e.getEntity().getPlayer();
        Player killer = e.getEntity().getKiller();

        if(!this.game.ready()) return;
        if(this.game.isPlaying(p) && (killer == null || this.game.isPlaying(killer))) {
            e.setDeathMessage(null);
            onDeath(p, killer, e);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void EventHandler_onTNT(EntityExplodeEvent e) {
        if(!this.game.isExplodeProtection()) return;
        e.blockList().clear();
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void EventHandler_onRespawn(PlayerRespawnEvent e) {
        Player p = e.getPlayer();

        if(!this.game.ready()) return;
        if(this.game.isPlaying(p)) {
            Location location = onRespawn(p);

            if(location != null) {
                e.setRespawnLocation(location);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void EventHandler_onHit(PotionSplashEvent e) {
        if(!this.game.getGameState().equals(GameState.RUNNING)) {
            e.setCancelled(true);
            return;
        }

        if(!this.game.ready()) return;

        for(LivingEntity entity : e.getAffectedEntities()) {
            if(entity instanceof Player && e.getPotion().getShooter() instanceof Player) {
                Player p = (Player) entity;
                Player damager = (Player) e.getPotion().getShooter();

                if(!p.getName().equals(damager.getName())) {
                    if(this.game.isSpectator(p)) {
                        Location location = p.getLocation().add(0, 5, 0);
                        while(!location.getBlock().getType().equals(Material.AIR)) location.add(0, 1, 0);

                        p.setFlying(true);
                        p.teleport(location);
                        p.setFlying(true);

                        ThrownPotion nextProjectile = damager.launchProjectile(e.getPotion().getClass());
                        nextProjectile.setShooter(damager);
                        nextProjectile.teleport(e.getPotion().getLocation().add(0, 0.2, 0));
                        nextProjectile.setVelocity(e.getPotion().getVelocity());
                        nextProjectile.setBounce(false);
                        nextProjectile.setItem(Potion.fromItemStack(e.getPotion().getItem()).toItemStack(1));

                        e.setCancelled(true);
                        e.getPotion().teleport(new Location(e.getPotion().getWorld(), 0, 0, 0));
                        return;
                    }

                    boolean friendly = true;

                    for(PotionEffect effect : e.getPotion().getEffects()) {
                        PotionEffectType type = effect.getType();

                        switch(type.getId()) {
                            case 4:
                            case 2:
                            case 7:
                            case 9:
                            case 15:
                            case 17:
                            case 18:
                            case 19:
                            case 20:
                            case 24:
                            case 27:
                                friendly = false;
                        }
                    }

                    if(!friendly) {
                        if(this.game.isPlaying(p) && this.game.isPlaying(damager)) {
                            if(this.game.isSpectator(p)) {
                                e.getAffectedEntities().remove(p);
                            } else if(this.game.getCurrentMap().getTeam(p).isMember(damager) && !this.game.getCurrentMap().isFriendlyFire()) {
                                e.getAffectedEntities().remove(p);
                            } else if(!this.onHit(p, damager, e.getPotion(), null)) {
                                e.getAffectedEntities().remove(p);
                            }
                        }
                    }
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void EventHandler_onHit(EntityDamageEvent e) {
        if(!this.game.getGameState().equals(GameState.RUNNING)) {
            e.setCancelled(true);
            return;
        }

        Entity en = e.getEntity();

        if(en instanceof Player) {
            if(this.game.getGameState().equals(GameState.WAITING) || this.game.isSpectator((Player) en)) {
                e.setCancelled(true);
                return;
            }

            e.setCancelled(!this.onHit((Player) en, null, null, e.getCause()));
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void EventHandler_onHit(EntityDamageByEntityEvent e) {
        if(!this.game.getGameState().equals(GameState.RUNNING)) {
            e.setCancelled(true);
            return;
        }

        Entity en = e.getEntity();
        Entity damager = e.getDamager();

        if(!this.game.ready()) return;

        if(en instanceof Player) {

            Player p = (Player) en;
            Player d = null;

            if(!this.game.isPlaying(p)) return;
            if(this.game.isSpectator(p)) e.setCancelled(true);

            if(damager instanceof Player) d = (Player) damager;

            Projectile projectile = null;
            if(damager instanceof Projectile) {
                projectile = (Projectile) damager;
                if(projectile.getShooter() instanceof Player) d = (Player) projectile.getShooter();
            }

            if(d != null) {
                if(this.game.isPlaying(d) && this.game.getCurrentMap() != null) {
                    if(this.game.getCurrentMap().getTeam(p) != null) {
                        if((this.game.getTeam(p).isMember(d) && !this.game.getCurrentMap().isFriendlyFire()) || this.game.isSpectator(d)) {
                            e.setCancelled(true);
                            e.setDamage(0);

                            if(projectile != null) {
                                projectile.remove();
                                if(p.getFireTicks() == 100) p.setFireTicks(0);
                            }

                            return;
                        }

                        if(!this.onHit(p, d, projectile, e.getCause())) {
                            e.setCancelled(true);

                            if(projectile != null) {
                                projectile.setBounce(false);
                                projectile.setVelocity(new Vector(0, 0, 0));
                                projectile.remove();
                                if(p.getFireTicks() == 100) p.setFireTicks(0);
                            }
                        }
                    } else if(this.game.isSpectator(p)) {
                        if(projectile instanceof ThrownPotion) return;

                        Location location = p.getLocation().add(0, 5, 0);
                        while(!location.getBlock().getType().equals(Material.AIR)) location.add(0, 1, 0);

                        p.setFlying(true);
                        p.teleport(location);
                        p.setFlying(true);

                        Projectile nextProjectile = d.launchProjectile(((Projectile) projectile).getClass());
                        nextProjectile.setShooter(d);
                        nextProjectile.teleport(projectile);
                        nextProjectile.setVelocity(projectile.getVelocity());
                        nextProjectile.setBounce(false);

                        projectile.remove();
                        e.setCancelled(true);
                    }
                }
            }
        }
    }

    @EventHandler
    public void EventHandler_onMove(PlayerMoveEvent e) {
        Player p = e.getPlayer();

        if(!this.game.ready()) return;
        if(!this.game.isPlaying(p)) return;

        double x = e.getFrom().getX() - e.getTo().getX();
        double y = e.getFrom().getY() - e.getTo().getY();
        double z = e.getFrom().getZ() - e.getTo().getZ();

        if(x < 0) x *= -1;
        if(y < 0) y *= -1;
        if(z < 0) z *= -1;

        double result = x + y + z;

        if(result > 0.05) {
            if(!onWalk(p, e.getFrom(), e.getTo())) {
                Location from = e.getFrom().clone();
                from.setYaw(e.getTo().getYaw());
                from.setPitch(e.getTo().getPitch());

                e.setTo(from);
                return;
            }

            if(getGame().isSpectator(p)) {
                for(Player other : getGame().getPlayers()) {
                    if(getGame().isSpectator(other)) continue;

                    if(other.getLocation().getWorld().getName().equals(p.getLocation().getWorld().getName()) && other.getLocation().distance(p.getLocation()) < getGame().getDistanceToInfluence()) {
                        Vector v = p.getLocation().toVector().subtract(other.getLocation().toVector());
                        v.normalize();

                        if(v.length() == 0) v.setY(0.5);

                        p.setVelocity(v);
                    }
                }
            } else {
                for(Player other : getGame().getSpectator().getMembers()) {
                    if(other.getLocation().getWorld().getName().equals(p.getLocation().getWorld().getName()) && other.getLocation().distance(p.getLocation()) < getGame().getDistanceToInfluence()) {
                        Vector v = other.getLocation().toVector().subtract(p.getLocation().toVector());
                        v.normalize();

                        if(v.length() == 0) v.setY(0.5);

                        other.setVelocity(v);
                    }
                }
            }
        }
    }

    @EventHandler
    public void EventHandler_onProtect(BlockBreakEvent e) {
        Player p = e.getPlayer();

        if(!this.game.isPlaying(p)) return;

        if(getGame().isSpectator(p)) {
            e.setCancelled(true);
            return;
        }

        if(!onBlockBreak(p, e.getBlock())) e.setCancelled(true);
    }

    @EventHandler
    public void EventHandler_onProtect(BlockPlaceEvent e) {
        Player p = e.getPlayer();

        if(!this.game.isPlaying(p)) return;

        if(getGame().isSpectator(p)) {
            e.setCancelled(true);
            return;
        }

        if(!onBlockPlace(p, e.getBlock())) {
            e.setCancelled(true);
            e.setBuild(false);
        }
    }

    @EventHandler
    public void EventHandler_onPickUp(PlayerPickupItemEvent e) {
        Player p = e.getPlayer();

        if(!this.game.isPlaying(p)) return;

        if(getGame().isSpectator(p)) {
            e.setCancelled(true);
            return;
        }

        if(!onPickUp(e.getPlayer(), e.getItem())) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void EventHandler_onInteract(PlayerInteractAtEntityEvent e) {
        Player p = e.getPlayer();

        if(!this.game.isPlaying(p)) return;

        if(getGame().isSpectator(p)) {
            e.setCancelled(true);
            return;
        }

        if(!onInteractAt(p, e.getRightClicked())) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void EventHandler_onInteract(PlayerInteractEvent e) {
        Player p = e.getPlayer();

        if(!this.game.isPlaying(p)) return;

        if(getGame().isSpectator(p)) {
            e.setCancelled(true);
            return;
        }

        if(!onInteract(p, e.getClickedBlock(), e.getAction().name().toLowerCase().contains("right")))
            e.setCancelled(true);
    }

    @EventHandler
    public void EventHandler_onChat(AsyncPlayerChatEvent e) {
        Player p = e.getPlayer();

        if(!this.game.isPlaying(p)) return;

        onChat(p, e);
    }

    @EventHandler
    public void EventHandler_onFoodLevelChange(FoodLevelChangeEvent e) {
        if(!(e.getEntity() instanceof Player)) return;

        if(this.game.getGameState().equals(GameState.WAITING)) {
            e.setCancelled(true);
            e.setFoodLevel(20);
            return;
        }

        Player p = (Player) e.getEntity();

        if(!this.game.isPlaying(p)) return;

        if(getGame().isSpectator(p)) {
            e.setFoodLevel(20);
            e.setCancelled(true);
        } else e.setFoodLevel(onFoodLevelChange(p, e.getFoodLevel()));
    }

    /**
     * Runs every tick
     */
    public void onTick() {

    }

    /**
     * @param p Player
     * @return If 'true' the player can build, if 'false' the block will not be removed.
     */
    public boolean onBlockBreak(Player p, Block b) {
        switch(this.game.getGameState()) {
            case STOPPED:
            case STOPPING:
            case NOT_PLAYABLE:
            case WAITING:
            case STARTING:
                return false;

            case RUNNING:
            default:
                return !getGame().isSpectator(p);
        }
    }

    /**
     * @param p Player
     * @return If 'true' the player can build, if 'false' the block will removed.
     */
    public boolean onBlockPlace(Player p, Block b) {
        switch(this.game.getGameState()) {
            case STOPPED:
            case STOPPING:
            case NOT_PLAYABLE:
            case WAITING:
            case STARTING:
                return false;

            case RUNNING:
            default:
                return !getGame().isSpectator(p);
        }
    }

    public boolean onPickUp(Player p, Item item) {
        switch(this.game.getGameState()) {
            case STOPPED:
            case STOPPING:
            case NOT_PLAYABLE:
            case WAITING:
            case STARTING:
                return false;

            case RUNNING:
            default:
                return !getGame().isSpectator(p);
        }
    }

    /**
     * @param p Player
     * @return If 'true' the player can interact, if 'false' the interaction will interrupted.
     */
    public boolean onInteractAt(Player p, Entity entity) {
        switch(this.game.getGameState()) {
            case STOPPED:
            case STOPPING:
            case NOT_PLAYABLE:
            case WAITING:
            case STARTING:
                return false;

            case RUNNING:
            default:
                return !getGame().isSpectator(p);
        }
    }

    /**
     * @param p Player
     * @return If 'true' the player can interact, if 'false' the interaction will interrupted.
     */
    public boolean onInteract(Player p, Block b, boolean rightClick) {
        switch(this.game.getGameState()) {
            case STOPPED:
            case STOPPING:
            case NOT_PLAYABLE:
            case WAITING:
            case STARTING:
                return false;

            case RUNNING:
            default:
                return !getGame().isSpectator(p);
        }
    }

    public void onChat(Player p, AsyncPlayerChatEvent e) {
    }

    public void onGameStateChange(GameState gameState) {
    }

    public void onDeath(Player player, Player killer, PlayerDeathEvent e) {
    }

    /**
     * @param player Player
     * @return The respawn-location
     */
    public Location onRespawn(Player player) {
        return null;
    }

    /**
     * @param player     Player
     * @param damager    Player
     * @param projectile boolean
     * @return If 'true' the player can be damaged, if 'false' the hit will be interrupted.
     */
    public boolean onHit(Player player, Player damager, Projectile projectile, EntityDamageEvent.DamageCause cause) {
        return getGame().getGameState().equals(GameState.RUNNING);
    }

    /**
     * @param player Player
     * @param from   Location
     * @param to     Location
     * @return If 'true' the player can walk, if 'false' he will teleported back.
     */
    public boolean onWalk(Player player, Location from, Location to) {
        return true;
    }

    /**
     * @param player       PLayer
     * @param newFoodLevel Double
     * @return Returns the new food level.
     */
    public int onFoodLevelChange(Player player, int newFoodLevel) {
        return 20;
    }

    public void onJoin(Player player) {
    }

    public void onQuit(Player player) {
    }

    public void onTick(int time, boolean isTimeLeft) {
    }

    public void onStartCountdownTick(int timeLeft) {
    }

    public void onKickCountdownTick(int timeLeft) {
    }

    public void onTeamJoin(Player player, Team team) {
    }

    public void onTeamQuit(Player player, Team team) {
    }

    public void onTeamChange(Player player, Team oldTeam, Team newTeam) {
    }

    public void onTeamChangeErrorIsFull(Player player, Team team) {
    }

    public void onTeamChangeCooldownInterruption(Player player) {
    }
}
