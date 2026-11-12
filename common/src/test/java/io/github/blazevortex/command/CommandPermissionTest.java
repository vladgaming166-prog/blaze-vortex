package io.github.blazevortex.command;

import io.github.blazevortex.core.BlazeVortexEngine;
import io.github.blazevortex.core.HeadlessPlatformAdapter;
import io.github.blazevortex.platform.StandaloneScheduler;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Logger;

import static org.assertj.core.api.Assertions.assertThat;

class CommandPermissionTest {

    @TempDir
    Path temp;

    @Test
    void rejectsUnauthorizedReloadAndAllowsAdmin() throws Exception {
        BlazeVortexEngine engine = new BlazeVortexEngine(
                new HeadlessPlatformAdapter(temp, Logger.getLogger("test"), new StandaloneScheduler()));
        engine.start();
        try {
            BvxCommandRouter router = new BvxCommandRouter(engine);
            CommandResult denied = router.execute(new FakeSender(Set.of()), new String[]{"reload"});
            assertThat(denied.success()).isFalse();
            assertThat(denied.message()).contains("permission");

            CommandResult allowed = router.execute(new FakeSender(Set.of(Permissions.ADMIN)), new String[]{"reload"});
            assertThat(allowed.success()).isTrue();

            CommandResult help = router.execute(new FakeSender(Set.of()), new String[]{"help"});
            assertThat(help.success()).isTrue();
            assertThat(help.message()).contains("/bvx");
        } finally {
            engine.shutdown();
        }
    }

    private record FakeSender(Set<String> permissions) implements CommandSenderView {
        @Override
        public String name() {
            return "tester";
        }

        @Override
        public UUID uuid() {
            return UUID.fromString("00000000-0000-0000-0000-000000000001");
        }

        @Override
        public boolean isPlayer() {
            return true;
        }

        @Override
        public boolean hasPermission(String permission) {
            return permissions.contains(permission);
        }

        @Override
        public void send(String message) {
        }
    }
}
