package io.github.blazevortex.display;

import io.github.blazevortex.api.display.DisplayCreateRequest;
import io.github.blazevortex.api.display.DisplayType;
import io.github.blazevortex.api.event.BvxEventBus;
import io.github.blazevortex.content.ContentLayout;
import io.github.blazevortex.core.HeadlessPlatformAdapter;
import io.github.blazevortex.platform.StandaloneScheduler;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;
import java.util.logging.Logger;

import static org.assertj.core.api.Assertions.assertThat;

class DisplayPersistenceTest {

    @TempDir
    Path temp;

    @Test
    void restoresDisplaysFromJsonAfterRestart() throws Exception {
        ContentLayout layout = new ContentLayout(temp);
        layout.createDirectories();
        HeadlessPlatformAdapter adapter = new HeadlessPlatformAdapter(temp, Logger.getLogger("test"), new StandaloneScheduler());
        DisplayRegistryImpl first = new DisplayRegistryImpl(adapter, new BvxEventBus(), layout, Logger.getLogger("test"));
        var created = first.create(DisplayCreateRequest.builder(
                DisplayType.IMAGE, "logo", UUID.randomUUID(), "world", 10, 64, -4
        ).id("image-logo").width(4).height(3).yaw(90f).build());

        assertThat(Files.readString(layout.data().resolve("displays.json"))).contains("image-logo", "logo");

        first.shutdown();
        HeadlessPlatformAdapter restarted = new HeadlessPlatformAdapter(temp, Logger.getLogger("test"), new StandaloneScheduler());
        DisplayRegistryImpl second = new DisplayRegistryImpl(restarted, new BvxEventBus(), layout, Logger.getLogger("test"));
        second.restore();

        assertThat(second.find(created.id())).isPresent();
        assertThat(second.find(created.id()).orElseThrow().assetName()).isEqualTo("logo");
        assertThat(second.find(created.id()).orElseThrow().width()).isEqualTo(4f);
        assertThat(second.find(created.id()).orElseThrow().height()).isEqualTo(3f);
    }
}
