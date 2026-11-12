package io.github.blazevortex.platform;

public interface ScheduledHandle {

    void cancel();

    boolean isCancelled();
}
