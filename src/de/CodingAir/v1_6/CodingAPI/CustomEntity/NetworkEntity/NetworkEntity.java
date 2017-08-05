package de.CodingAir.v1_6.CodingAPI.CustomEntity.NetworkEntity;

import de.CodingAir.v1_6.CodingAPI.API;
import de.CodingAir.v1_6.CodingAPI.CustomEntity.FakePlayer.Extras.AnimationType;
import de.CodingAir.v1_6.CodingAPI.Particles.Particle;
import de.CodingAir.v1_6.CodingAPI.Player.Data.PacketReader;
import de.CodingAir.v1_6.CodingAPI.Server.Environment;
import de.CodingAir.v1_6.CodingAPI.Server.Reflections.IReflection;
import de.CodingAir.v1_6.CodingAPI.Server.Reflections.Packet;
import de.CodingAir.v1_6.CodingAPI.Server.Reflections.PacketUtils;
import de.CodingAir.v1_6.CodingAPI.Server.Version;
import de.CodingAir.v1_6.CodingAPI.Utils.Removable;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * Removing of this disclaimer is forbidden.
 *
 * @author CodingAir
 * @verions: 1.0.0
 **/

public class NetworkEntity implements Removable {
	private static final IReflection.MethodAccessor setPosition = IReflection.getMethod(PacketUtils.EntityClass, "setPosition", new Class[]{double.class, double.class, double.class});
	private static final IReflection.MethodAccessor setCustomName = IReflection.getMethod(PacketUtils.EntityClass, "setCustomName", new Class[]{String.class});
	private static final IReflection.MethodAccessor setCustomNameVisible = IReflection.getMethod(PacketUtils.EntityClass, "setCustomNameVisible", new Class[]{boolean.class});
	private static final IReflection.MethodAccessor getCustomNameVisible = IReflection.getMethod(PacketUtils.EntityClass, "getCustomNameVisible", boolean.class, new Class[]{});
	private static final IReflection.MethodAccessor getCustomName = IReflection.getMethod(PacketUtils.EntityClass, "getCustomName", String.class, new Class[]{});
	private static final IReflection.MethodAccessor getDataWatcher = IReflection.getMethod(PacketUtils.EntityClass, "getDataWatcher", PacketUtils.DataWatcherClass, new Class[]{});
	private static final IReflection.MethodAccessor setOnFire = IReflection.getMethod(PacketUtils.EntityClass, "setOnFire", new Class[]{int.class});
	private static final IReflection.MethodAccessor getHeadHeight = IReflection.getMethod(PacketUtils.EntityClass, "getHeadHeight", float.class, new Class[]{});
	private static final IReflection.MethodAccessor setEquipment = IReflection.getMethod(PacketUtils.EntityClass, "setEquipment", new Class[]{int.class, PacketUtils.ItemStackClass});
	private static final IReflection.MethodAccessor getEquipment = IReflection.getMethod(PacketUtils.EntityClass, "getEquipment", Array.newInstance(PacketUtils.ItemStackClass, 0).getClass(), new Class[]{});
	private static final IReflection.MethodAccessor isBurning = IReflection.getMethod(PacketUtils.EntityClass, "isBurning", boolean.class, new Class[]{});
	private static final IReflection.MethodAccessor setHealth = IReflection.getMethod(PacketUtils.EntityLivingClass, "setHealth", new Class[]{float.class});
	private static final IReflection.MethodAccessor getHealth = IReflection.getMethod(PacketUtils.EntityLivingClass, "getHealth", float.class, new Class[]{});
	private static final IReflection.MethodAccessor getMaxHealth = IReflection.getMethod(PacketUtils.EntityLivingClass, "getMaxHealth", float.class, new Class[]{});
	private static final IReflection.MethodAccessor setInvisible = IReflection.getMethod(PacketUtils.EntityClass, "setInvisible", new Class[]{boolean.class});
	private static final IReflection.MethodAccessor isInvisible = IReflection.getMethod(PacketUtils.EntityClass, "isInvisible", boolean.class, new Class[]{});
	
	private static final double MOVE_TICKS = 0.429D;
	private static final double JUMP_TICKS = 3.0D;
	private static final double MOVE_SPEED = 2.3D / 20;
	private static final double FALLING_ACCELERATION = 0.05D;
	private static final double JUMP_HEIGHT = 1.3D;
	private static final double DISTANCE_TO_BLOCK = 1.8D;
	private static final double DISTANCE_TO_BLOCK_SPRINTING = 3.0D;
	
	private UUID uniqueId = UUID.randomUUID();
	private NetworkEntityType type;
	private List<Player> players;
	
	private Class<?> nmsClass;
	private Object entity = null;
	
	private List<PacketReader> packetReaders = new ArrayList<>();
	private NetworkEntityListener listener = null;
	
	private List<Entity> passengers = new ArrayList<>();
	private Location location;
	private boolean damageable = false;
	private boolean steerable = false;
	private boolean gravity = true;
	private boolean sprinting = false;
	
	private double fallingAcceleration = 0;
	
	private int moveTicks = 0;
	private int jumpTicks = 0;
	
	public NetworkEntity(NetworkEntityType type, Location location, Player... players) {
		this.type = type;
		this.location = location;
		this.players = players.length == 0 ? null : Arrays.asList(players);
		this.nmsClass = IReflection.getClass(IReflection.ServerPacket.MINECRAFT_PACKAGE, type.getNmsClassName());
		
		API.addRemovable(this);
		
		for(Player p : players) {
			packetReaders.add(getPacketReader(p));
		}
	}
	
	@Override
	public void destroy() {
		API.removeRemovable(this);
		if(!isSpawned()) return;
		
		PacketUtils.EntityPackets.destroyEntity(this.entity, getPlayers());
		this.entity = null;
		
		this.packetReaders.forEach(PacketReader::unInject);
	}
	
	@Override
	public Player getPlayer() {
		return null;
	}
	
	@Override
	public Class<? extends Removable> getAbstractClass() {
		return NetworkEntity.class;
	}
	
	@Override
	public UUID getUniqueId() {
		return this.uniqueId;
	}
	
	public void onTick() {
		if(!isSpawned()) return;
		
		if(moveTicks > 0) moveTicks--;
		if(jumpTicks > 0) jumpTicks--;
		
		checkGravity();
	}
	
	private void checkGravity() {
		if(isJumping() || !gravity) return;
		
		if(!isOnGround()) fallingAcceleration += FALLING_ACCELERATION;
		else {
			fallingAcceleration = 0;
			return;
		}
		
		Location loc = getLocation();
		while(!Environment.isBlock(loc.getBlock())) {
			loc.setY(loc.getBlockY() - 1);
		}
		
		loc.setY(loc.getBlockY() + Environment.getBlockHeight(loc.getBlock()));
		double distance = getLocation().getY() - loc.getY();
		
		if(distance < fallingAcceleration) {
			if(distance != 0) move(new Vector(0, -Math.abs(distance), 0));
			fallingAcceleration = 0;
		} else {
			move(new Vector(0, -fallingAcceleration, 0));
		}
	}
	
	private void checkMovement(Location to) {
		Vector vector = getJumpVector(to);
		if(vector != null) move(vector);
	}
	
	public Vector getJumpVector(Location to) {
		Location old = this.location;
		
		double x = (to.getX() - old.getX()) * (sprinting ? DISTANCE_TO_BLOCK_SPRINTING : DISTANCE_TO_BLOCK);
		double z = (to.getZ() - old.getZ()) * (sprinting ? DISTANCE_TO_BLOCK_SPRINTING : DISTANCE_TO_BLOCK);
		
		to.add(x, 0, z);
		
		if(Environment.isBlock(to.getBlock()) && !Environment.isPassableDoor(to.getBlock())) {
			if(Environment.isSlab(to.getBlock())) {
				to.add(0, 0.5, 0);
				if(Environment.isBlock(to.getBlock())) {
					return new Vector(x, 0.5, z);
				}
			} else {
				return new Vector(x, JUMP_HEIGHT, z);
			}
		}
		
		return null;
	}
	
	public void spawn() {
		if(type.getReleased().isBiggerThan(Version.getVersion()))
			throw new IllegalStateException("The current version of your server does not support the entity \"" + type.getNmsClassName() + "\"!");
		
		if(isSpawned()) throw new IllegalStateException("The entity has already been spawned!");
		
		this.entity = IReflection.getConstructor(this.nmsClass, PacketUtils.WorldServerClass).newInstance(PacketUtils.getWorldServer(this.location.getWorld()));
		setPosition(this.location);
		
		this.packetReaders.forEach(PacketReader::inject);
		
		for(Player p : this.players) {
			spawn(p);
		}
	}
	
	private void spawn(Player player) {
		if(this.type.isLivingEntity()) {
			Packet packet = new Packet(PacketUtils.PacketPlayOutSpawnEntityLivingClass, player);
			packet.initialize(this.entity);
			packet.send();
		} else {
			PacketUtils.EntityPackets.spawnEntity(this.entity, this.type.getId(), player);
		}
	}
	
	public void teleport(Location location) {
		if(!isSpawned()) throw new IllegalStateException("The entity has not been spawned!");
		
		Object packet = PacketUtils.EntityPackets.getTeleportPacket(this.entity, location);
		PacketUtils.sendPacket(packet, getPlayers());
		
		this.location = location;
	}
	
	public void jump() {
		move(new Vector(0, JUMP_HEIGHT, 0));
	}
	
	public void move(Vector vector) {
		if(!isSpawned()) throw new IllegalStateException("The entity has not been spawned!");
		
		double x = vector.getX();
		double y = vector.getY();
		double z = vector.getZ();
		double preX = this.location.getX();
		double preY = this.location.getY();
		double preZ = this.location.getZ();
		
		x *= MOVE_SPEED;
		z *= MOVE_SPEED;
		
		boolean old = true;
		IReflection.ConstructorAccessor con = IReflection.getConstructor(PacketUtils.PacketPlayOutEntity$PacketPlayOutRelEntityMoveLookClass, int.class, byte.class, byte.class, byte.class, byte.class, byte.class, boolean.class);
		
		if(con == null) {
			con = IReflection.getConstructor(PacketUtils.PacketPlayOutEntity$PacketPlayOutRelEntityMoveLookClass, int.class, long.class, long.class, long.class, byte.class, byte.class, boolean.class);
			old = false;
		}
		
		Object packet;
		
		double changeX, changeY, changeZ;
		
		if(old) {
			packet = con.newInstance(getEntityId(), (byte) toFixedPointNumber(x), (byte) toFixedPointNumber(y), (byte) toFixedPointNumber(z), toAngle(this.location.getYaw()), toAngle(this.location.getPitch()), true);
			
			changeX = toFixedPointNumber(x) / 32D;
			changeY = toFixedPointNumber(y) / 32D;
			changeZ = toFixedPointNumber(z) / 32D;
		} else {
			packet = con.newInstance(getEntityId(), toFixedPointNumber(preX + x, preX), toFixedPointNumber(preY + y, preY), toFixedPointNumber(preZ + z, preZ), toAngle(this.location.getYaw()), toAngle(this.location.getPitch()), true);
			
			changeX = toFixedPointNumber(preX + x, preX) / 4096D;
			changeY = toFixedPointNumber(preY + y, preY) / 4096D;
			changeZ = toFixedPointNumber(preZ + z, preZ) / 4096D;
		}
		
		checkMovement(this.location.clone().add(changeX, changeY, changeZ));
		
		this.location.add(changeX, changeY, changeZ);
		
		int moveTicks = Math.round((float) ((Math.abs(x) + Math.abs(y) + Math.abs(z)) / MOVE_TICKS));
		if(moveTicks == 0) moveTicks = 1;
		
		int jumpTicks = Math.round((float) (y * JUMP_TICKS));
		if(jumpTicks == 0) jumpTicks = 1;
		
		if(y > 0) this.jumpTicks = jumpTicks;
		this.moveTicks = moveTicks;
		
		PacketUtils.sendPacket(packet, getPlayers());
	}
	
	public void look(float yaw, float pitch) {
		if(!isSpawned()) throw new IllegalStateException("The entity has not been spawned!");
		
		Packet packet = new Packet(PacketUtils.PacketPlayOutEntityLookClass, getPlayers());
		packet.initialize(new Class[]{int.class, byte.class, byte.class, boolean.class}, getEntityId(), toAngle(yaw), toAngle(pitch), false);
		packet.send();
		
		packet = new Packet(PacketUtils.PacketPlayOutEntityHeadRotationClass, getPlayers());
		packet.initialize(new Class[]{PacketUtils.EntityClass, byte.class}, this.entity, toAngle(yaw));
		packet.send();
		
		this.location.setYaw(yaw);
		this.location.setPitch(pitch);
	}
	
	private void updateMetaData() {
		if(!isSpawned()) throw new IllegalStateException("The entity has not been spawned!");
		
		Packet packet = new Packet(PacketUtils.PacketPlayOutEntityMetadataClass, getPlayers());
		packet.initialize(getEntityId(), getDataWatcher.invoke(this.entity), true);
		packet.send();
	}
	
	private void setPosition(Location location) {
		if(!this.isSpawned()) return;
		
		setPosition.invoke(this.entity, location.getX(), location.getY(), location.getZ());
	}
	
	public int getEntityId() {
		if(!isSpawned()) throw new IllegalStateException("The entity has not been spawned!");
		return PacketUtils.EntityPackets.getId(this.entity);
	}
	
	public String getCustomName() {
		if(!isSpawned()) throw new IllegalStateException("The entity has not been spawned!");
		return (String) getCustomName.invoke(this.entity);
	}
	
	public void setCustomName(String customName) {
		if(!isSpawned()) throw new IllegalStateException("The entity has not been spawned!");
		setCustomName.invoke(this.entity, customName);
		updateMetaData();
	}
	
	public void setCustomNameVisible(boolean visible) {
		if(!isSpawned()) throw new IllegalStateException("The entity has not been spawned!");
		setCustomNameVisible.invoke(this.entity, visible);
		updateMetaData();
	}
	
	public boolean getCustomNameVisible() {
		if(!isSpawned()) throw new IllegalStateException("The entity has not been spawned!");
		return (boolean) getCustomNameVisible.invoke(this.entity);
	}
	
	public void setOnFire(boolean onFire) {
		if(!isSpawned()) throw new IllegalStateException("The entity has not been spawned!");
		setOnFire.invoke(this.entity, onFire ? 1 : 0);
		updateMetaData();
	}
	
	public boolean isOnFire() {
		if(!isSpawned()) throw new IllegalStateException("The entity has not been spawned!");
		return (boolean) isBurning.invoke(this.entity);
	}
	
	public double getHeadHeight() {
		if(!isSpawned()) throw new IllegalStateException("The entity has not been spawned!");
		return (double) getHeadHeight.invoke(this.entity);
	}
	
	public void setEquipment(int slot, ItemStack item) {
		if(!isSpawned()) throw new IllegalStateException("The entity has not been spawned!");
		setEquipment.invoke(this.entity, slot, PacketUtils.getItemStack(item));
		updateMetaData();
	}
	
	public ItemStack[] getEquipment() {
		if(!isSpawned()) throw new IllegalStateException("The entity has not been spawned!");
		List<ItemStack> items = new ArrayList<>();
		IReflection.MethodAccessor asBukkitCopy = IReflection.getMethod(PacketUtils.CraftItemStackClass, "asBukkitCopy", ItemStack.class, new Class[]{PacketUtils.ItemStackClass});
		
		for(Object o : ((Object[]) getEquipment.invoke(this.entity))) {
			items.add((ItemStack) asBukkitCopy.invoke(null, o));
		}
		
		return items.toArray(new ItemStack[items.size()]);
	}
	
	public void playAnimation(AnimationType type) {
		if(!isSpawned()) throw new IllegalStateException("The entity has not been spawned!");
		
		Packet packet = new Packet(PacketUtils.PacketPlayOutAnimationClass, getPlayers());
		packet.initialize(this.entity, type.getId());
		packet.send();
	}
	
	public void mount(Entity entity) {
		if(!isSpawned()) throw new IllegalStateException("The entity has not been spawned!");
		if(entity == null) return;
		
		if(Version.getVersion().isBiggerThan(Version.v1_8)) {
			this.passengers.add(entity);
		} else {
			if(this.passengers.size() != 0) this.passengers.set(0, entity);
			else this.passengers.add(entity);
		}
		
		mount(PacketUtils.getEntity(entity));
	}
	
	public void mount(NetworkEntity entity) {
		if(!isSpawned()) throw new IllegalStateException("The entity has not been spawned!");
		if(entity == null) return;
		
		if(Version.getVersion().isBiggerThan(Version.v1_8)) {
			this.passengers.add(PacketUtils.getBukkitEntity(entity.getEntity()));
		} else {
			if(this.passengers.size() != 0) this.passengers.set(0, PacketUtils.getBukkitEntity(entity.getEntity()));
			else this.passengers.add(PacketUtils.getBukkitEntity(entity.getEntity()));
		}
		
		mount(entity.getEntity());
	}
	
	public void eject() {
		if(!isSpawned()) throw new IllegalStateException("The entity has not been spawned!");
		
		Packet packet = PacketUtils.EntityPackets.getEjectPacket(this.entity);
		packet.setPlayers(this.players.toArray(new Player[this.players.size()]));
		packet.send();
		
		this.passengers.clear();
		
		updateMetaData();
	}
	
	private void mount(Object entity) {
		if(!isSpawned()) throw new IllegalStateException("The entity has not been spawned!");
		
		Packet packet;
		
		if(entity == null) {
			packet = PacketUtils.EntityPackets.getEjectPacket(this.entity);
		} else {
			packet = PacketUtils.EntityPackets.getPassengerPacket(this.entity, entity);
		}
		
		packet.setPlayers(this.players.toArray(new Player[this.players.size()]));
		packet.send();
		
		updateMetaData();
	}
	
	public void setHealth(float health) {
		if(!isSpawned()) throw new IllegalStateException("The entity has not been spawned!");
		if(!this.type.isLivingEntity()) throw new IllegalStateException("The entity is not a living entity!");
		
		setHealth.invoke(this.entity, health);
		updateMetaData();
	}
	
	public float getHealth() {
		if(!isSpawned()) throw new IllegalStateException("The entity has not been spawned!");
		if(!this.type.isLivingEntity()) throw new IllegalStateException("The entity is not a living entity!");
		
		return (float) getHealth.invoke(this.entity);
	}
	
	public float getMaxHealth() {
		if(!isSpawned()) throw new IllegalStateException("The entity has not been spawned!");
		if(!this.type.isLivingEntity()) throw new IllegalStateException("The entity is not a living entity!");
		
		return (float) getMaxHealth.invoke(this.entity);
	}
	
	public void setInvisible(boolean invisible) {
		if(!isSpawned()) throw new IllegalStateException("The entity has not been spawned!");
		
		setInvisible.invoke(this.entity, invisible);
		updateMetaData();
	}
	
	public boolean isInvisible() {
		if(!isSpawned()) throw new IllegalStateException("The entity has not been spawned!");
		
		return (boolean) isInvisible.invoke(this.entity);
	}
	
	public List<Entity> getPassengers() {
		if(!isSpawned()) throw new IllegalStateException("The entity has not been spawned!");
		
		return this.passengers;
	}
	
	public boolean hasPassengers() {
		return getPassengers().size() != 0;
	}
	
	public boolean isSpawned() {
		return this.entity != null;
	}
	
	public Player[] getPlayers() {
		return this.players == null || this.players.size() == 0 ? Bukkit.getOnlinePlayers().toArray(new Player[Bukkit.getOnlinePlayers().size()]) : this.players.toArray(new Player[this.players.size()]);
	}
	
	public boolean isDamageable() {
		return damageable;
	}
	
	public void setDamageable(boolean damageable) {
		this.damageable = damageable;
	}
	
	public NetworkEntityListener getListener() {
		return listener;
	}
	
	public void setListener(NetworkEntityListener listener) {
		this.listener = listener;
	}
	
	public boolean hasListener() {
		return this.listener != null;
	}
	
	public void addPlayer(Player player) {
		if(this.players.contains(player)) return;
		
		this.players.add(player);
		this.packetReaders.add(getPacketReader(player));
		getPacketReader(player).inject();
		
		if(isSpawned()) {
			spawn(player);
		}
	}
	
	public void removePlayer(Player player) {
		if(this.players.contains(player)) this.players.remove(player);
	}
	
	public boolean isSteerable() {
		return steerable;
	}
	
	public void setSteerable(boolean steerable) {
		this.steerable = steerable;
	}
	
	private PacketReader getPacketReader(Player player) {
		for(PacketReader reader : this.packetReaders) {
			if(reader.getPlayer().getName().equals(player)) return reader;
		}
		
		return new PacketReader(player, "PacketReader_" + player.getName() + "_NetworkEntity_" + uniqueId.toString()) {
			@Override
			public boolean readPacket(Object packet) {
				if(packet.getClass().getSimpleName().equals("PacketPlayInSteerVehicle") && isSteerable()) {
					float sideways = (float) IReflection.getField(packet.getClass(), "a").get(packet);
					float forward = (float) IReflection.getField(packet.getClass(), "b").get(packet);
					boolean jump = (boolean) IReflection.getField(packet.getClass(), "c").get(packet);
					boolean eject = (boolean) IReflection.getField(packet.getClass(), "d").get(packet);
					Location loc = player.getLocation();
					double multiply = 5D;
					
					Vector vector = loc.getDirection();
					vector.multiply(multiply).multiply(forward);
					vector.setY(0);
					
					Vector orth = new Vector(-loc.getDirection().getZ(), 0, loc.getDirection().getX());
					orth.multiply(multiply).multiply(sideways);
					
					vector = vector.subtract(orth);
					
					if(jump && isOnGround() && !isJumping()) {
						jump();
					}
					
					if(eject) {
						eject();
						return false;
					}
					
					look(loc.getYaw(), loc.getPitch());
					move(vector);
				}
				
				if(packet.getClass().getSimpleName().equals("PacketPlayInUseEntity") && (int) IReflection.getField(packet.getClass(), "a").get(packet) == getEntityId()) {
					IReflection.FieldAccessor playerAction = IReflection.getField(packet.getClass(), "action");
					
					if(hasListener()) {
						if(playerAction.get(packet).equals(PacketUtils.EnumEntityUseActionClass.getEnumConstants()[0]))
							getListener().onInteract(player);
						else if(playerAction.get(packet).equals(PacketUtils.EnumEntityUseActionClass.getEnumConstants()[1]))
							getListener().onHit(player);
					}
				}
				
				return false;
			}
		};
	}
	
	public boolean isOnGround() {
		Location loc = this.location.clone();
		loc.setY(loc.getY() - 0.0001);
		Block b = loc.getBlock();
		
		double height = Environment.getBlockHeight(loc.getBlock());
		loc.setY(loc.getY() - height);
		
		return Environment.isBlock(b) && !b.equals(loc.getBlock());
	}
	
	public boolean isJumping() {
		return this.jumpTicks > 0;
	}
	
	public boolean hasGravity() {
		return gravity;
	}
	
	public void setGravity(boolean gravity) {
		this.gravity = gravity;
	}
	
	public Location getLocation() {
		return location.clone();
	}
	
	private Object getEntity() {
		return entity;
	}
	
	private byte toAngle(float value) {
		return (byte) ((int) (value * 256.0F / 360.0F));
	}
	
	private int toFixedPointNumber(double value) {
		return (int) Math.floor(value * 32D);
	}
	
	private long toFixedPointNumber(double current, double pre) {
		return (long) (((current * 32D) - (pre * 32D)) * 128D);
	}
}
