package io.github.blazevortex.platform;

import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * Scheduler used by tests and by engine startup before a Minecraft scheduler exists.
 * Minecraft plugins must replace this with a platform scheduler that honours the main thread.
 */
public final class StandaloneScheduler implements BvxScheduler {

    private static final long TICK_MS = 50L;

    private final ScheduledExecutorService executor;
    private final Set<StandaloneHandle> handles = ConcurrentHashMap.newKeySet();
    private final boolean ownExecutor;

    public StandaloneScheduler() {
        this(Executors.newScheduledThreadPool(4, runnable -> {
            Thread thread = new Thread(runnable, "bvx-standalone-scheduler");
            thread.setDaemon(true);
            return thread;
        }), true);
    }

    public StandaloneScheduler(ScheduledExecutorService executor, boolean ownExecutor) {
        this.executor = executor;
        this.ownExecutor = ownExecutor;
    }

    @Override
    public void runSync(Runnable task) {
        task.run();
    }

    @Override
    public void runAsync(Runnable task) {
        executor.execute(task);
    }

    @Override
    public ScheduledHandle runSyncLater(Runnable task, long delayTicks) {
        return schedule(task, delayTicks, -1);
    }

    @Override
    public ScheduledHandle runAsyncLater(Runnable task, long delayTicks) {
        return schedule(task, delayTicks, -1);
    }

    @Override
    public ScheduledHandle runSyncRepeating(Runnable task, long delayTicks, long periodTicks) {
        return schedule(task, delayTicks, periodTicks);
    }

    @Override
    public ScheduledHandle runAsyncRepeating(Runnable task, long delayTicks, long periodTicks) {
        return schedule(task, delayTicks, periodTicks);
    }

    @Override
    public <T> CompletableFuture<T> supplyAsync(Supplier<T> supplier) {
        return CompletableFuture.supplyAsync(supplier, executor);
    }

    @Override
    public void cancelAll() {
        for (StandaloneHandle handle : Set.copyOf(handles)) {
            handle.cancel();
        }
        if (ownExecutor) {
            executor.shutdownNow();
        }
    }

    @Override
    public boolean isMainThread() {
        return true;
    }

    private ScheduledHandle schedule(Runnable task, long delayTicks, long periodTicks) {
        long delayMs = Math.max(0, delayTicks) * TICK_MS;
        ScheduledFuture<?> future;
        if (periodTicks > 0) {
            future = executor.scheduleAtFixedRate(task, delayMs, periodTicks * TICK_MS, TimeUnit.MILLISECONDS);
        } else {
            future = executor.schedule(task, delayMs, TimeUnit.MILLISECONDS);
        }
        StandaloneHandle handle = new StandaloneHandle(future);
        handles.add(handle);
        return handle;
    }

    private final class StandaloneHandle implements ScheduledHandle {
        private final ScheduledFuture<?> future;

        private StandaloneHandle(ScheduledFuture<?> future) {
            this.future = future;
        }

        @Override
        public void cancel() {
            future.cancel(false);
            handles.remove(this);
        }

        @Override
        public boolean isCancelled() {
            return future.isCancelled();
        }
    }
}
