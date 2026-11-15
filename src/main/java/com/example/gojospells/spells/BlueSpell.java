package com.example.gojospells.spells;

import com.example.gojospells.GojoEffects;
import com.example.gojospells.GojoSpells;

import io.redspace.ironsspellbooks.api.config.DefaultConfig;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.registry.SchoolRegistry;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.api.spells.CastSource;
import io.redspace.ironsspellbooks.api.spells.CastType;
import io.redspace.ironsspellbooks.api.spells.SpellRarity;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

public class BlueSpell extends AbstractSpell {
	private final ResourceLocation spellId = new ResourceLocation(GojoSpells.MOD_ID, "blue");

	private final DefaultConfig defaultConfig = new DefaultConfig()
			.setMinRarity(SpellRarity.RARE)
			.setSchoolResource(SchoolRegistry.ENDER_RESOURCE)
			.setMaxLevel(10)
			.setCooldownSeconds(8)
			.build();

	public BlueSpell() {
		this.manaCostPerLevel = 5;
		this.baseSpellPower = 6;
		this.spellPowerPerLevel = 1;
		this.castTime = 0;
		this.baseManaCost = 25;
	}

	@Override
	public ResourceLocation getSpellResource() {
		return spellId;
	}

	@Override
	public DefaultConfig getDefaultConfig() {
		return defaultConfig;
	}

	@Override
	public CastType getCastType() {
		return CastType.INSTANT;
	}

	@Override
	public void onCast(Level level, int spellLevel, LivingEntity entity, CastSource castSource, MagicData playerMagicData) {
		if (level instanceof ServerLevel serverLevel) {
			GojoEffects.blue(serverLevel, entity, getSpellPower(spellLevel, entity));
		}
		super.onCast(level, spellLevel, entity, castSource, playerMagicData);
	}
}
