package io.github.blazevortex.emoji;

import io.github.blazevortex.api.component.RenderContext;
import io.github.blazevortex.api.emoji.EmojiRegistration;
import io.github.blazevortex.core.BlazeVortexEngine;
import io.github.blazevortex.media.TestMedia;
import io.github.blazevortex.core.HeadlessPlatformAdapter;
import io.github.blazevortex.platform.StandaloneScheduler;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.awt.Color;
import java.nio.file.Path;
import java.util.logging.Logger;

import static org.assertj.core.api.Assertions.assertThat;

class EmojiParserAndEngineTest {

    @TempDir
    Path temp;

    @Test
    void replacesKnownTagsAndLeavesUnknownTagsIntact() throws Exception {
        BlazeVortexEngine engine = new BlazeVortexEngine(
                new HeadlessPlatformAdapter(temp, Logger.getLogger("test"), new StandaloneScheduler()));
        engine.start();
        try {
            Path png = TestMedia.png(engine.layout().emojis(), "fire.png", 128, 128, Color.RED);
            engine.emojis().register(EmojiRegistration.builder("fire", png).size(100).build());
            String rendered = engine.renderer().renderLegacy("hello <fire> and <unknown_emoji>", RenderContext.CHAT);
            assertThat(rendered).doesNotContain("<fire>");
            assertThat(rendered).contains("<unknown_emoji>");
            String plain = PlainTextComponentSerializer.plainText()
                    .serialize(engine.renderer().render("hi <fire>", RenderContext.CHAT));
            assertThat(plain).isNotBlank();
        } finally {
            engine.shutdown();
        }
    }
}
