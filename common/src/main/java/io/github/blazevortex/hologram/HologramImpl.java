package io.github.blazevortex.hologram;

import io.github.blazevortex.api.hologram.Hologram;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

public final class HologramImpl implements Hologram {

    private final String id;
    private final UUID worldId;
    private final String worldName;
    private final double x;
    private final double y;
    private final double z;
    private volatile List<String> lines;
    private final Consumer<HologramImpl> updater;
    private final Consumer<String> remover;

    public HologramImpl(
            String id,
            UUID worldId,
            String worldName,
            double x,
            double y,
            double z,
            List<String> lines,
            Consumer<HologramImpl> updater,
            Consumer<String> remover
    ) {
        this.id = id;
        this.worldId = worldId;
        this.worldName = worldName;
        this.x = x;
        this.y = y;
        this.z = z;
        this.lines = List.copyOf(lines);
        this.updater = updater;
        this.remover = remover;
    }

    @Override
    public String id() {
        return id;
    }

    @Override
    public UUID worldId() {
        return worldId;
    }

    @Override
    public String worldName() {
        return worldName;
    }

    @Override
    public double x() {
        return x;
    }

    @Override
    public double y() {
        return y;
    }

    @Override
    public double z() {
        return z;
    }

    @Override
    public List<String> lines() {
        return lines;
    }

    @Override
    public void setLines(List<String> lines) {
        this.lines = List.copyOf(new ArrayList<>(lines));
        updater.accept(this);
    }

    @Override
    public void remove() {
        remover.accept(id);
    }
}
