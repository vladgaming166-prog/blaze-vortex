package io.github.blazevortex.display;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.github.blazevortex.api.display.BvxDisplay;
import io.github.blazevortex.api.display.DisplayCreateRequest;
import io.github.blazevortex.api.display.DisplayRegistry;
import io.github.blazevortex.api.event.BvxEventBus;
import io.github.blazevortex.api.event.DisplayCreateEvent;
import io.github.blazevortex.content.ContentLayout;
import io.github.blazevortex.platform.PlatformAdapter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

public final class DisplayRegistryImpl implements DisplayRegistry {

    private final PlatformAdapter platform;
    private final BvxEventBus events;
    private final ContentLayout layout;
    private final Logger logger;
    private final ConcurrentHashMap<String, BvxDisplay> displays = new ConcurrentHashMap<>();
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public DisplayRegistryImpl(PlatformAdapter platform, BvxEventBus events, ContentLayout layout, Logger logger) {
        this.platform = platform;
        this.events = events;
        this.layout = layout;
        this.logger = logger;
    }

    @Override
    public Collection<BvxDisplay> all() {
        return List.copyOf(displays.values());
    }

    @Override
    public Optional<BvxDisplay> find(String id) {
        return Optional.ofNullable(displays.get(id));
    }

    @Override
    public BvxDisplay create(DisplayCreateRequest request) {
        BvxDisplay display = platform.spawnDisplay(request);
        DisplayCreateEvent event = events.fire(new DisplayCreateEvent(display));
        if (event.isCancelled()) {
            platform.despawnDisplay(display.id());
            throw new io.github.blazevortex.api.BvxException("Display creation cancelled");
        }
        displays.put(display.id(), display);
        save();
        return display;
    }

    @Override
    public boolean remove(String id) {
        BvxDisplay removed = displays.remove(id);
        if (removed != null) {
            platform.despawnDisplay(id);
            save();
            return true;
        }
        return false;
    }

    public void shutdown() {
        for (String id : List.copyOf(displays.keySet())) {
            platform.despawnDisplay(id);
        }
        save();
    }

    private void save() {
        try {
            List<Map<String, Object>> payload = new ArrayList<>();
            for (BvxDisplay display : displays.values()) {
                payload.add(io.github.blazevortex.util.Maps.of(
                        "id", display.id(),
                        "type", display.type().name(),
                        "asset", display.assetName(),
                        "worldId", display.worldId().toString(),
                        "world", display.worldName(),
                        "x", display.x(),
                        "y", display.y(),
                        "z", display.z(),
                        "yaw", display.yaw(),
                        "pitch", display.pitch(),
                        "width", display.width(),
                        "height", display.height(),
                        "viewDistance", display.viewDistance()
                ));
            }
            Files.writeString(layout.data().resolve("displays.json"), gson.toJson(payload), StandardCharsets.UTF_8);
        } catch (IOException ex) {
            logger.warning("Failed to persist displays: " + ex.getMessage());
        }
    }
}
