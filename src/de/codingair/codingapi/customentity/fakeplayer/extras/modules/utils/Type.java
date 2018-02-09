package de.codingair.codingapi.customentity.fakeplayer.extras.modules.utils;

/**
 * Removing of this disclaimer is forbidden.
 *
 * @author codingair
 * @verions: 1.0.0
 **/

public enum Type {
	FollowModule(de.codingair.codingapi.customentity.fakeplayer.extras.modules.FollowModule.class),
	GravityModule(de.codingair.codingapi.customentity.fakeplayer.extras.modules.GravityModule.class),
	InteractModule(de.codingair.codingapi.customentity.fakeplayer.extras.modules.InteractModule.class),
	JumpModule(de.codingair.codingapi.customentity.fakeplayer.extras.modules.JumpModule.class),
	ParticleModule(de.codingair.codingapi.customentity.fakeplayer.extras.modules.ParticleModule.class),
	TargetModule(de.codingair.codingapi.customentity.fakeplayer.extras.modules.TargetModule.class);

	private Class<? extends Module> clazz;

	Type(Class<? extends Module> clazz) {
		this.clazz = clazz;
	}

	public Class<? extends Module> getClazz() {
		return clazz;
	}
}
