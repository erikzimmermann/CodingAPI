package de.CodingAir.v1_6.CodingAPI.CustomEntity.FakePlayer.Extras.Modules;

import de.CodingAir.v1_6.CodingAPI.CustomEntity.FakePlayer.Extras.Modules.Utils.Module;
import de.CodingAir.v1_6.CodingAPI.CustomEntity.FakePlayer.Extras.Modules.Utils.Type;
import de.CodingAir.v1_6.CodingAPI.CustomEntity.FakePlayer.FakePlayer;
import de.CodingAir.v1_6.CodingAPI.Particles.Particle;

/**
 * Removing of this disclaimer is forbidden.
 *
 * @author CodingAir
 * @verions: 1.0.0
 **/

public class ParticleModule extends Module {
	
	public ParticleModule(FakePlayer player) {
		super(player, Type.ParticleModule);
	}
	
	@Override
	public void onEvent() {
		if(getPlayer().isOnGround() && getPlayer().isMoving() && getPlayer().isSprinting()) {
			getPlayer().sendPacket(Particle.BLOCK_CRACK.getParticlePacket(getPlayer().getOldLocation()).getPacket());
		}
	}
}
