package de.CodingAir.v1_6.CodingAPI.Game;

import de.CodingAir.v1_6.CodingAPI.Game.Utils.GameState;
import de.CodingAir.v1_6.CodingAPI.Game.Utils.Team;
import de.CodingAir.v1_6.CodingAPI.Server.Reflections.IReflection;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.*;
import org.bukkit.event.player.*;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.projectiles.ProjectileSource;

/**
 * Removing of this disclaimer is forbidden.
 *
 * @author CodingAir
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
	
	@EventHandler
	public void EventHandler_onJoin(PlayerJoinEvent e) {
		Player p = e.getPlayer();
		
		if(game.joinOnServerJoin()) this.game.join(p);
	}
	
	@EventHandler
	public void EventHandler_onQuit(PlayerQuitEvent e) {
		Player p = e.getPlayer();
		
		if(this.game.isPlaying(p)) this.game.quit(p);
	}
	
	@EventHandler
	public void EventHandler_onKill(PlayerDeathEvent e) {
		Player p = e.getEntity().getPlayer();
		Player killer = e.getEntity().getKiller();
		
		if(!this.game.ready()) return;
		if(this.game.isPlaying(p) && (killer == null || this.game.isPlaying(killer))) onDeath(p, killer);
	}
	
	@EventHandler
	public void EventHandler_onRespawn(PlayerRespawnEvent e) {
		Player p = e.getPlayer();
		
		System.out.println("Respawn");
		
		if(!this.game.ready()) return;
		System.out.println("isReady");
		if(this.game.isPlaying(p)) {
			System.out.println("isPlaying");
			Location location = onRespawn(p);
			
			if(location != null) {
				e.setRespawnLocation(location);
				System.out.println("Respawn at new Location");
			} else {
				System.out.println("Location is null");
			}
		}
	}
	
	@EventHandler
	public void EventHandler_onHit(PotionSplashEvent e) {
		if(this.game.getGameState().equals(GameState.WAITING)) e.setCancelled(true);
		
		if(!this.game.ready()) return;
		
		for(LivingEntity entity : e.getAffectedEntities()) {
			if(entity instanceof Player && e.getPotion().getShooter() instanceof Player) {
				Player p = (Player) entity;
				Player damager = (Player) e.getPotion().getShooter();
				
				if(!p.getName().equals(damager.getName())) {
					boolean friendly = true;
					
					for(PotionEffect effect : e.getPotion().getEffects()) {
						PotionEffectType type = effect.getType();
						
						if(type.equals(PotionEffectType.HUNGER) || type.equals(PotionEffectType.POISON) || type.equals(PotionEffectType.WITHER) || type.equals(PotionEffectType.WEAKNESS) ||
								type.equals(PotionEffectType.HARM) || type.equals(PotionEffectType.SLOW) || type.equals(PotionEffectType.SLOW_DIGGING) || type.equals(PotionEffectType.CONFUSION) ||
								type.equals(PotionEffectType.BLINDNESS) || type.equals(PotionEffectType.GLOWING) || type.equals(PotionEffectType.UNLUCK)) friendly = false;
					}
					
					if(!friendly) {
						if(this.game.isPlaying(p) && this.game.isPlaying(damager)) {
							if(this.game.getCurrentMap().getTeam(p).isMember(damager) && !this.game.getCurrentMap().isFriendlyFire()) {
								e.getAffectedEntities().remove(p);
							} else if(!this.onHit(p, damager, true)) {
								e.getAffectedEntities().remove(p);
							}
						}
					}
				}
			}
		}
	}
	
	@EventHandler
	public void EventHandler_onHit(EntityDamageEvent e) {
		Entity en = e.getEntity();
		
		if(en instanceof Player) {
			if(this.game.getGameState().equals(GameState.WAITING)) {
				e.setCancelled(true);
				return;
			}
			
			e.setCancelled(!onHit((Player) en, null, false));
		}
	}
	
	@EventHandler
	public void EventHandler_onHit(EntityDamageByEntityEvent e) {
		if(this.game.getGameState().equals(GameState.WAITING)) {
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
			
			if(damager instanceof Player) d = (Player) damager;
			
			Entity projectile = null;
			if(damager.getName().equals("Arrow") && d == null) {
				projectile = damager;
				
				Class<?> craftArrow = IReflection.getClass(IReflection.ServerPacket.CRAFTBUKKIT_PACKAGE, "entity.CraftArrow");
				IReflection.MethodAccessor getShooter = IReflection.getMethod(craftArrow, "getShooter", ProjectileSource.class, new Class[]{});
				
				Object arrow = craftArrow.cast(projectile);
				Object shooter = getShooter.invoke(arrow);
				
				if(shooter instanceof Player) d = (Player) shooter;
			}
			
			if(damager.getType().name().equals("SNOWBALL") && d == null) {
				projectile = damager;
				
				Class<?> craftProjectile = IReflection.getClass(IReflection.ServerPacket.CRAFTBUKKIT_PACKAGE, "entity.CraftProjectile");
				IReflection.MethodAccessor getShooter = IReflection.getMethod(craftProjectile, "getShooter", ProjectileSource.class, new Class[]{});
				
				Object arrow = craftProjectile.cast(projectile);
				Object shooter = getShooter.invoke(arrow);
				
				if(shooter instanceof Player) d = (Player) shooter;
			}
			
			if(damager.getType().name().equals("FISHING_HOOK") && d == null) {
				projectile = damager;
				
				Class<?> fishingHook = IReflection.getClass(IReflection.ServerPacket.CRAFTBUKKIT_PACKAGE, "entity.CraftFish");
				IReflection.MethodAccessor getShooter = IReflection.getMethod(fishingHook, "getShooter", ProjectileSource.class, new Class[]{});
				
				Object hook = fishingHook.cast(projectile);
				Object shooter = getShooter.invoke(hook);
				
				if(shooter instanceof Player) d = (Player) shooter;
			}
			
			if(damager.getType().name().equals("SPLASH_POTION") && d == null) {
				return;
			}
			
			if(d != null) {
				if(this.game.isPlaying(d) && this.game.getCurrentMap() != null && this.game.getCurrentMap().getTeam(p) != null) {
					if(this.game.getCurrentMap().getTeam(p).isMember(d) && !this.game.getCurrentMap().isFriendlyFire()) {
						e.setCancelled(true);
						
						if(projectile != null) {
							projectile.remove();
							if(p.getFireTicks() == 100) p.setFireTicks(0);
						}
						
						return;
					}
					
					if(!this.onHit(p, d, projectile != null)) {
						e.setCancelled(true);
						
						if(projectile != null) {
							projectile.remove();
							if(p.getFireTicks() == 100) p.setFireTicks(0);
						}
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
		
		if(result > 0.05){
			if(!onWalk(p, e.getFrom(), e.getTo())) {
				Location from = e.getFrom().clone();
				from.setYaw(e.getTo().getYaw());
				from.setPitch(e.getTo().getPitch());
				
				e.setTo(from);
			}
		}
	}
	
	@EventHandler
	public void EventHandler_onProtect(BlockBreakEvent e) {
		if(this.game.getGameState().equals(GameState.WAITING)) {
			e.setCancelled(true);
			return;
		}
		
		Player p = e.getPlayer();
		
		if(!this.game.isPlaying(p)) return;
		
		if(!onBlockBreak(p, e.getBlock())) {
			e.setCancelled(true);
		}
	}
	
	@EventHandler
	public void EventHandler_onProtect(BlockPlaceEvent e) {
		if(this.game.getGameState().equals(GameState.WAITING)) {
			e.setCancelled(true);
			return;
		}
		
		Player p = e.getPlayer();
		
		if(!this.game.isPlaying(p)) return;
		
		if(!onBlockPlace(p, e.getBlock())) {
			e.setCancelled(true);
			e.setBuild(false);
		}
	}
	
	@EventHandler
	public void EventHandler_onInteract(PlayerInteractAtEntityEvent e) {
		if(this.game.getGameState().equals(GameState.WAITING)) {
			e.setCancelled(true);
			return;
		}
		
		Player p = e.getPlayer();
		
		if(!this.game.isPlaying(p)) return;
		
		if(!onInteractAt(p, e.getRightClicked())) {
			e.setCancelled(true);
		}
	}
	
	@EventHandler
	public void EventHandler_onInteract(PlayerInteractEvent e) {
		if(this.game.getGameState().equals(GameState.WAITING)) {
			e.setCancelled(true);
			return;
		}
		
		Player p = e.getPlayer();
		
		if(!this.game.isPlaying(p)) return;
		
		if(!onInteract(p, e.getClickedBlock())) {
			e.setCancelled(true);
		}
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
		
		e.setFoodLevel(onFoodLevelChange(p, e.getFoodLevel()));
	}
	
	/**
	 * Runs every tick
	 */
	public void onTick() {
	
	}
	
	/**
	 * @param p Player
	 * @return If 'true' the player can build, if 'false' the block will removed.
	 */
	public boolean onBlockBreak(Player p, Block b) {
		switch(this.game.getGameState()) {
			case STOPPED:
			case STOPPING:
			case NOT_PLAYABLE:
			case WAITING:
			case STARTING: return false;
			
			case RUNNING:
			default: return true;
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
			case STARTING: return false;
			
			case RUNNING:
			default: return true;
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
			case STARTING: return false;
			
			case RUNNING:
			default: return true;
		}
	}
	
	/**
	 * @param p Player
	 * @return If 'true' the player can interact, if 'false' the interaction will interrupted.
	 */
	public boolean onInteract(Player p, Block b) {
		switch(this.game.getGameState()) {
			case STOPPED:
			case STOPPING:
			case NOT_PLAYABLE:
			case WAITING:
			case STARTING: return false;
			
			case RUNNING:
			default: return true;
		}
	}
	
	public void onChat(Player p, AsyncPlayerChatEvent e) {
	}
	
	public void onGameStateChange(GameState gameState) {
	}
	
	public void onDeath(Player player, Player killer) {
	}
	
	/**
	 * @param player Player
	 * @return The respawn-location
	 */
	public Location onRespawn(Player player) {
		return null;
	}
	
	/**
	 * @param player Player
	 * @param damager Player
	 * @param projectile boolean
	 * @return If 'true' the player can hit, if 'false' the hit will interrupted.
	 */
	public boolean onHit(Player player, Player damager, boolean projectile) {
		return true;
	}
	
	/**
	 *
	 * @param player PLayer
	 * @param from Location
	 * @param to Location
	 * @return If 'true' the player can walk, if 'false' he will teleported back.
	 */
	public boolean onWalk(Player player, Location from, Location to) {
		return true;
	}
	
	/**
	 *
	 * @param player PLayer
	 * @param newFoodLevel Double
	 * @return Returns the new food level.
	 */
	public int onFoodLevelChange(Player player, double newFoodLevel) {
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
	
	public void onTeamChangeCooldownInterruption(Player player) {
	}
}
