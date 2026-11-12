package io.github.blazevortex.bukkit;

import io.github.blazevortex.platform.BvxScheduler;
import io.github.blazevortex.platform.ScheduledHandle;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

public final class BukkitSchedulerAdapter implements BvxScheduler {

    private final Plugin plugin;
    private final Set<BukkitTask> tasks = ConcurrentHashMap.newKeySet();

    public BukkitSchedulerAdapter(Plugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void runSync(Runnable task) {
        if (Bukkit.isPrimaryThread()) {
            task.run();
        } else {
            Bukkit.getScheduler().runTask(plugin, task);
        }
    }

    @Override
    public void runAsync(Runnable task) {
        track(Bukkit.getScheduler().runTaskAsynchronously(plugin, task));
    }

    @Override
    public ScheduledHandle runSyncLater(Runnable task, long delayTicks) {
        return track(Bukkit.getScheduler().runTaskLater(plugin, task, Math.max(0, delayTicks)));
    }

    @Override
    public ScheduledHandle runAsyncLater(Runnable task, long delayTicks) {
        return track(Bukkit.getScheduler().runTaskLaterAsynchronously(plugin, task, Math.max(0, delayTicks)));
    }

    @Override
    public ScheduledHandle runSyncRepeating(Runnable task, long delayTicks, long periodTicks) {
        return track(Bukkit.getScheduler().runTaskTimer(plugin, task, Math.max(0, delayTicks), Math.max(1, periodTicks)));
    }

    @Override
    public ScheduledHandle runAsyncRepeating(Runnable task, long delayTicks, long periodTicks) {
        return track(Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, task, Math.max(0, delayTicks), Math.max(1, periodTicks)));
    }

    @Override
    public <T> CompletableFuture<T> supplyAsync(Supplier<T> supplier) {
        CompletableFuture<T> future = new CompletableFuture<>();
        runAsync(() -> {
            try {
                future.complete(supplier.get());
            } catch (Exception ex) {
                future.completeExceptionally(ex);
            }
        });
        return future;
    }

    @Override
    public void cancelAll() {
        for (BukkitTask task : Set.copyOf(tasks)) {
            task.cancel();
        }
        tasks.clear();
    }

    @Override
    public boolean isMainThread() {
        return Bukkit.isPrimaryThread();
    }

    private ScheduledHandle track(BukkitTask task) {
        tasks.add(task);
        return new ScheduledHandle() {
            @Override
            public void cancel() {
                task.cancel();
                tasks.remove(task);
            }

            @Override
            public boolean isCancelled() {
                return task.isCancelled();
            }
        };
    }
}
