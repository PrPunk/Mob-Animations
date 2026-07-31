package name.jschatz2.util;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;

import java.util.ArrayList;
import java.util.List;

public final class TickScheduler {

    private record ScheduledTask(long executeAtTick, Runnable action) {}

    private static final List<ScheduledTask> TASKS = new ArrayList<>();
    private static long currentTick = 0;

    private TickScheduler() {}

    /** Call this once from your mod initializer. */
    public static void register() {
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            currentTick++;
            if (TASKS.isEmpty()) return;

            List<ScheduledTask> due = new ArrayList<>();
            for (ScheduledTask task : TASKS) {
                if (task.executeAtTick() <= currentTick) {
                    due.add(task);
                }
            }
            TASKS.removeAll(due);
            for (ScheduledTask task : due) {
                task.action().run();
            }
        });
    }

    /** Runs {@code action} after {@code delayTicks} server ticks. */
    public static void schedule(int delayTicks, Runnable action) {
        TASKS.add(new ScheduledTask(currentTick + delayTicks, action));
    }
}