package io.github.blazevortex.api.event;

import io.github.blazevortex.api.pack.ResourcePackBuildResult;

public final class ResourcePackBuildEvent extends BvxEvent {

    private final ResourcePackBuildResult result;

    public ResourcePackBuildEvent(ResourcePackBuildResult result) {
        this.result = result;
    }

    public ResourcePackBuildResult result() {
        return result;
    }
}
