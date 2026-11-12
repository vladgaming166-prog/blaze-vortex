package io.github.blazevortex.platform;

import io.github.blazevortex.api.display.DisplayCreateRequest;
import io.github.blazevortex.api.display.DisplayType;
import io.github.blazevortex.core.HeadlessPlatformAdapter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.UUID;
import java.util.logging.Logger;

import static org.assertj.core.api.Assertions.assertThat;

class PlatformAdapterTest {

    @TempDir
    Path temp;

    @Test
    void headlessAdapterSpawnsAndRemovesDisplays() {
        HeadlessPlatformAdapter adapter = new HeadlessPlatformAdapter(temp, Logger.getLogger("test"), new StandaloneScheduler());
        var display = adapter.spawnDisplay(DisplayCreateRequest.builder(
                DisplayType.IMAGE, "logo", UUID.randomUUID(), "world", 0, 64, 0
        ).build());
        assertThat(display.id()).isNotBlank();
        adapter.despawnDisplay(display.id());
        assertThat(adapter.capabilities().displayEntities()).isFalse();
        assertThat(adapter.minecraftVersion()).isEqualTo("1.21.1");
    }
}
