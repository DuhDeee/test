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

public class HollowPurpleSpell extends AbstractSpell {
	private final ResourceLocation spellId = new ResourceLocation(GojoSpells.MOD_ID, "hollow_purple");

	private final DefaultConfig defaultConfig = new DefaultConfig()
			.setMinRarity(SpellRarity.LEGENDARY)
			.setSchoolResource(SchoolRegistry.ENDER_RESOURCE)
			.setMaxLevel(5)
			.setCooldownSeconds(30)
			.build();

	public HollowPurpleSpell() {
		this.manaCostPerLevel = 20;
		this.baseSpellPower = 10;
		this.spellPowerPerLevel = 2;
		this.castTime = 40; // 2 second charge
		this.baseManaCost = 100;
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
		return CastType.LONG;
	}

	@Override
	public void onCast(Level level, int spellLevel, LivingEntity entity, CastSource castSource, MagicData playerMagicData) {
		if (level instanceof ServerLevel serverLevel) {
			GojoEffects.purple(serverLevel, entity, getSpellPower(spellLevel, entity));
		}
		super.onCast(level, spellLevel, entity, castSource, playerMagicData);
	}
}
