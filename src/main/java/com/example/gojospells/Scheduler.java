package com.example.gojospells;

import java.util.ArrayList;
import java.util.List;
import java.util.function.IntConsumer;

import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/** Tiny server-tick scheduler used for charge-ups, beams and the domain duration. */
@Mod.EventBusSubscriber(modid = GojoSpells.MOD_ID)
public final class Scheduler {
	private static final class Task {
		int delay;
		int remaining;
		int elapsed = 0;
		final IntConsumer action;

		Task(int delay, int duration, IntConsumer action) {
			this.delay = delay;
			this.remaining = duration;
			this.action = action;
		}
	}

	private static final List<Task> TASKS = new ArrayList<>();

	private Scheduler() {
	}

	@SubscribeEvent
	public static void onServerTick(TickEvent.ServerTickEvent event) {
		if (event.phase != TickEvent.Phase.END || TASKS.isEmpty()) {
			return;
		}
		for (Task task : new ArrayList<>(TASKS)) {
			if (task.delay > 0) {
				task.delay--;
				continue;
			}
			task.action.accept(task.elapsed++);
			if (--task.remaining <= 0) {
				TASKS.remove(task);
			}
		}
	}

	/** Runs {@code perTick} once per server tick for {@code duration} ticks, passing the tick index. */
	public static void repeat(int duration, IntConsumer perTick) {
		TASKS.add(new Task(0, duration, perTick));
	}

	/** Runs {@code action} once after {@code delayTicks}. */
	public static void later(int delayTicks, Runnable action) {
		TASKS.add(new Task(delayTicks, 1, i -> action.run()));
	}
}
