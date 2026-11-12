package io.github.blazevortex.display;

import io.github.blazevortex.api.display.BvxDisplay;
import io.github.blazevortex.api.display.DisplayCreateRequest;
import io.github.blazevortex.api.display.DisplayType;

import java.util.UUID;
import java.util.function.Consumer;

public final class MemoryDisplay implements BvxDisplay {

    private final DisplayCreateRequest request;
    private final Consumer<String> remover;
    private volatile boolean visible = true;

    public MemoryDisplay(DisplayCreateRequest request, Consumer<String> remover) {
        this.request = request;
        this.remover = remover;
    }

    @Override
    public String id() {
        return request.id();
    }

    @Override
    public DisplayType type() {
        return request.type();
    }

    @Override
    public String assetName() {
        return request.assetName();
    }

    @Override
    public UUID worldId() {
        return request.worldId();
    }

    @Override
    public String worldName() {
        return request.worldName();
    }

    @Override
    public double x() {
        return request.x();
    }

    @Override
    public double y() {
        return request.y();
    }

    @Override
    public double z() {
        return request.z();
    }

    @Override
    public float yaw() {
        return request.yaw();
    }

    @Override
    public float pitch() {
        return request.pitch();
    }

    @Override
    public float width() {
        return request.width();
    }

    @Override
    public float height() {
        return request.height();
    }

    @Override
    public boolean visible() {
        return visible;
    }

    @Override
    public int viewDistance() {
        return request.viewDistance();
    }

    @Override
    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    @Override
    public void remove() {
        remover.accept(id());
    }
}
