package com.example.gojospells;

import java.util.function.Supplier;

import com.example.gojospells.spells.BlueSpell;
import com.example.gojospells.spells.HollowPurpleSpell;
import com.example.gojospells.spells.InfiniteVoidSpell;
import com.example.gojospells.spells.RedSpell;

import io.redspace.ironsspellbooks.api.registry.SpellRegistry;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;

@Mod(GojoSpells.MOD_ID)
public class GojoSpells {
	public static final String MOD_ID = "gojo_spells";

	public static final DeferredRegister<AbstractSpell> SPELLS =
			DeferredRegister.create(SpellRegistry.SPELL_REGISTRY_KEY, MOD_ID);

	private static Supplier<AbstractSpell> register(AbstractSpell spell) {
		return SPELLS.register(spell.getSpellName(), () -> spell);
	}

	public static final Supplier<AbstractSpell> BLUE = register(new BlueSpell());
	public static final Supplier<AbstractSpell> RED = register(new RedSpell());
	public static final Supplier<AbstractSpell> HOLLOW_PURPLE = register(new HollowPurpleSpell());
	public static final Supplier<AbstractSpell> INFINITE_VOID = register(new InfiniteVoidSpell());

	public GojoSpells() {
		IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
		SPELLS.register(modBus);
	}
}
