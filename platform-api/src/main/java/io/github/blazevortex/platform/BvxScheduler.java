package io.github.blazevortex.platform;

import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

public interface BvxScheduler {

    void runSync(Runnable task);

    void runAsync(Runnable task);

    ScheduledHandle runSyncLater(Runnable task, long delayTicks);

    ScheduledHandle runAsyncLater(Runnable task, long delayTicks);

    ScheduledHandle runSyncRepeating(Runnable task, long delayTicks, long periodTicks);

    ScheduledHandle runAsyncRepeating(Runnable task, long delayTicks, long periodTicks);

    <T> CompletableFuture<T> supplyAsync(Supplier<T> supplier);

    void cancelAll();

    boolean isMainThread();
}
