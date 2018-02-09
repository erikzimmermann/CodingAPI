package de.codingair.codingapi.customentity.fakeplayer.extras.modules;

import de.codingair.codingapi.customentity.fakeplayer.extras.modules.utils.Type;
import de.codingair.codingapi.customentity.fakeplayer.extras.modules.utils.Module;
import de.codingair.codingapi.customentity.fakeplayer.FakePlayer;
import de.codingair.codingapi.particles.Particle;

/**
 * Removing of this disclaimer is forbidden.
 *
 * @author codingair
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
