package io.github.blazevortex.api.event;

import io.github.blazevortex.api.display.BvxDisplay;

public final class DisplayCreateEvent extends BvxEvent {

    private final BvxDisplay display;

    public DisplayCreateEvent(BvxDisplay display) {
        this.display = display;
    }

    public BvxDisplay display() {
        return display;
    }

    @Override
    public boolean isCancellable() {
        return true;
    }
}
