package com.example.gojospells;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/** The gameplay behind each spell. {@code power} is the spell's scaled power from Iron's Spells. */
public final class GojoEffects {
	/** Hollow Purple carves a tunnel through terrain. Set to false to only damage entities. */
	private static final boolean PURPLE_BREAKS_BLOCKS = true;
	/** Domain also freezes other players in place (otherwise they only get potion effects). */
	private static final boolean DOMAIN_FREEZES_PLAYERS = false;

	private GojoEffects() {
	}

	// ---------------------------------------------------------------- helpers

	private static Vec3 aimPoint(ServerLevel level, LivingEntity caster, double range) {
		Vec3 eye = caster.getEyePosition();
		Vec3 end = eye.add(caster.getLookAngle().scale(range));
		BlockHitResult hit = level.clip(new ClipContext(eye, end, ClipContext.Block.COLLIDE, ClipContext.Fluid.NONE, caster));
		return hit.getType() == HitResult.Type.MISS ? end : hit.getLocation();
	}

	private static List<LivingEntity> entitiesNear(ServerLevel level, LivingEntity caster, Vec3 center, double radius) {
		AABB box = new AABB(center.x - radius, center.y - radius, center.z - radius,
				center.x + radius, center.y + radius, center.z + radius);
		return level.getEntitiesOfClass(LivingEntity.class, box,
				e -> e != caster && e.isAlive() && e.position().add(0, e.getBbHeight() / 2, 0).distanceTo(center) <= radius);
	}

	private static void hurt(ServerLevel level, LivingEntity caster, LivingEntity target, float amount) {
		target.hurt(level.damageSources().indirectMagic(caster, caster), amount);
	}

	private static void ring(ServerLevel level, ParticleOptions particle, Vec3 c, double radius, int points) {
		double offset = level.getRandom().nextDouble() * Math.PI * 2;
		for (int i = 0; i < points; i++) {
			double a = offset + (Math.PI * 2 * i) / points;
			double x = Math.cos(a) * radius;
			double z = Math.sin(a) * radius;
			level.sendParticles(particle, c.x + x, c.y, c.z + z, 1, 0, 0, 0, 0);
			level.sendParticles(particle, c.x + x, c.y + z, c.z, 1, 0, 0, 0, 0);
			level.sendParticles(particle, c.x, c.y + x, c.z + z, 1, 0, 0, 0, 0);
		}
	}

	private static void sound(ServerLevel level, Vec3 p, SoundEvent event, float volume, float pitch) {
		level.playSound(null, p.x, p.y, p.z, event, SoundSource.PLAYERS, volume, pitch);
	}

	private static Vec3 randomUnit(ServerLevel level) {
		double theta = level.getRandom().nextDouble() * Math.PI * 2;
		double z = level.getRandom().nextDouble() * 2 - 1;
		double r = Math.sqrt(1 - z * z);
		return new Vec3(r * Math.cos(theta), z, r * Math.sin(theta));
	}

	// ---------------------------------------------------------------- Blue

	/** Blue: a point of attraction that drags nearby entities in and grinds them down. */
	public static void blue(ServerLevel level, LivingEntity caster, float power) {
		Vec3 center = aimPoint(level, caster, 10);
		sound(level, center, SoundEvents.BEACON_ACTIVATE, 1.5f, 1.8f);

		Scheduler.repeat(60, tick -> {
			ring(level, ParticleTypes.SOUL_FIRE_FLAME, center, 1.2, 10);
			level.sendParticles(ParticleTypes.ELECTRIC_SPARK, center.x, center.y, center.z, 4, 0.4, 0.4, 0.4, 0.3);

			for (LivingEntity e : entitiesNear(level, caster, center, 7)) {
				Vec3 pull = center.subtract(e.position().add(0, e.getBbHeight() / 2, 0));
				double dist = Math.max(pull.length(), 0.5);
				e.setDeltaMovement(pull.normalize().scale(Math.min(1.2, 3.0 / dist)));
				e.hurtMarked = true;
				if (tick % 10 == 0 && dist < 2.5) {
					hurt(level, caster, e, power * 0.5f);
				}
			}
		});
	}

	// ---------------------------------------------------------------- Red

	/** Red: an instant repulsive blast at the aim point. */
	public static void red(ServerLevel level, LivingEntity caster, float power) {
		Vec3 center = aimPoint(level, caster, 12);
		sound(level, center, SoundEvents.LIGHTNING_BOLT_THUNDER, 1.2f, 1.6f);

		level.sendParticles(ParticleTypes.FLAME, center.x, center.y, center.z, 80, 0.8, 0.8, 0.8, 0.25);
		level.sendParticles(ParticleTypes.LAVA, center.x, center.y, center.z, 30, 1.0, 1.0, 1.0, 0.0);
		ring(level, ParticleTypes.FLAME, center, 2.5, 24);

		for (LivingEntity e : entitiesNear(level, caster, center, 5)) {
			Vec3 push = e.position().add(0, e.getBbHeight() / 2, 0).subtract(center);
			if (push.lengthSqr() < 1.0E-4) {
				push = caster.getLookAngle();
			}
			e.setDeltaMovement(push.normalize().scale(2.2).add(0, 0.5, 0));
			e.hurtMarked = true;
			hurt(level, caster, e, power * 1.5f);
		}
	}

	// ---------------------------------------------------------------- Hollow Purple

	/** Hollow Purple: charge (the cast time), then a beam that erases everything along its path. */
	public static void purple(ServerLevel level, LivingEntity caster, float power) {
		final Vec3 eye = caster.getEyePosition();
		final Vec3 dir = caster.getLookAngle().normalize();
		final Vec3 start = eye.add(dir.scale(3));
		sound(level, start, SoundEvents.END_PORTAL_SPAWN, 2.0f, 1.4f);

		Set<UUID> alreadyHit = new HashSet<>();
		Scheduler.repeat(10, i -> {
			for (int s = 0; s < 4; s++) {
				double dist = 3 + i * 4 + s;
				Vec3 pt = eye.add(dir.scale(dist));

				level.sendParticles(ParticleTypes.END_ROD, pt.x, pt.y, pt.z, 6, 0.6, 0.6, 0.6, 0.02);
				level.sendParticles(ParticleTypes.PORTAL, pt.x, pt.y, pt.z, 10, 1.0, 1.0, 1.0, 0.3);

				for (LivingEntity e : entitiesNear(level, caster, pt, 3.5)) {
					if (alreadyHit.add(e.getUUID())) {
						hurt(level, caster, e, power * 4f);
					}
				}

				if (PURPLE_BREAKS_BLOCKS) {
					BlockPos origin = BlockPos.containing(pt);
					for (int dx = -2; dx <= 2; dx++) {
						for (int dy = -2; dy <= 2; dy++) {
							for (int dz = -2; dz <= 2; dz++) {
								if (dx * dx + dy * dy + dz * dz > 5) {
									continue;
								}
								BlockPos pos = origin.offset(dx, dy, dz);
								BlockState state = level.getBlockState(pos);
								if (!state.isAir() && state.getDestroySpeed(level, pos) >= 0) {
									level.destroyBlock(pos, false, caster);
								}
							}
						}
					}
				}
			}
		});
	}

	// ---------------------------------------------------------------- Domain Expansion

	/** Infinite Void: everyone inside the sphere (except the caster) is overloaded and immobilised. */
	public static void domain(ServerLevel level, LivingEntity caster, float power) {
		final Vec3 center = caster.position().add(0, 1, 0);
		final double radius = 16;
		final int duration = 200; // 10 seconds
		sound(level, center, SoundEvents.END_PORTAL_SPAWN, 3.0f, 0.5f);
		sound(level, center, SoundEvents.ENDER_DRAGON_GROWL, 2.0f, 0.5f);

		Scheduler.repeat(duration, tick -> {
			for (int i = 0; i < 70; i++) {
				Vec3 p = center.add(randomUnit(level).scale(radius));
				level.sendParticles(ParticleTypes.SQUID_INK, p.x, p.y, p.z, 1, 0, 0, 0, 0);
			}
			for (int i = 0; i < 12; i++) {
				Vec3 p = center.add(randomUnit(level).scale(radius * level.getRandom().nextDouble()));
				level.sendParticles(ParticleTypes.END_ROD, p.x, p.y, p.z, 1, 0, 0, 0, 0.01);
			}

			for (LivingEntity e : entitiesNear(level, caster, center, radius)) {
				if (tick % 10 == 0) {
					e.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 30, 6, false, false));
					e.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 30, 0, false, false));
					e.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 60, 0, false, false));
					e.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 30, 3, false, false));
					e.addEffect(new MobEffectInstance(MobEffects.DIG_SLOWDOWN, 30, 3, false, false));
				}
				boolean isPlayer = e instanceof net.minecraft.world.entity.player.Player;
				if (!isPlayer || DOMAIN_FREEZES_PLAYERS) {
					// Keep gravity, kill horizontal motion.
					e.setDeltaMovement(0, Math.min(0, e.getDeltaMovement().y), 0);
					e.hurtMarked = true;
				}
			}
		});
	}
}
