package io.github.blazevortex.pack;

import io.github.blazevortex.api.emoji.EmojiRegistration;
import io.github.blazevortex.api.event.BvxEventBus;
import io.github.blazevortex.api.pack.ResourcePackBuildResult;
import io.github.blazevortex.config.BvxConfig;
import io.github.blazevortex.config.ConfigLoader;
import io.github.blazevortex.content.ContentLayout;
import io.github.blazevortex.emoji.EmojiRegistryImpl;
import io.github.blazevortex.font.GlyphAllocator;
import io.github.blazevortex.media.TestMedia;
import io.github.blazevortex.media.gif.GifDecoder;
import io.github.blazevortex.media.image.ImageProcessor;
import io.github.blazevortex.security.MediaLimits;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.awt.Color;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.logging.Logger;
import java.util.zip.ZipFile;

import static org.assertj.core.api.Assertions.assertThat;

class ResourcePackAndConfigTest {

    @TempDir
    Path temp;

    @Test
    void packFormatCatalogCoversSupportedVersions() {
        assertThat(PackFormatCatalog.forMinecraftVersion("1.19.2")).isEqualTo(9);
        assertThat(PackFormatCatalog.forMinecraftVersion("1.21.11")).isEqualTo(75);
        assertThat(PackFormatCatalog.forMinecraftVersion("26.3")).isEqualTo(97);
    }

    @Test
    void generatesZipWithFontAndSha1() throws Exception {
        ContentLayout layout = new ContentLayout(temp);
        layout.createDirectories();
        Path png = TestMedia.png(layout.emojis(), "fire.png", 128, 128, Color.ORANGE);
        Logger logger = Logger.getLogger("test");
        ConfigLoader loader = new ConfigLoader(logger);
        BvxConfig config = loader.load(temp.resolve("config.yml"), "config/config.yml");
        ImageProcessor images = new ImageProcessor(MediaLimits.defaults(), 8);
        GifDecoder gifs = new GifDecoder(MediaLimits.defaults());
        GlyphAllocator glyphs = new GlyphAllocator(layout.glyphRegistry());
        EmojiRegistryImpl emojis = new EmojiRegistryImpl(layout, config, images, gifs, glyphs, new BvxEventBus(), logger, loader);
        emojis.register(EmojiRegistration.builder("fire", png).size(80).build());
        ResourcePackBuilder builder = new ResourcePackBuilder(layout, config, emojis, images, gifs, logger);
        ResourcePackBuildResult result = builder.build(List.of(), "1.21.1");
        assertThat(result.success()).isTrue();
        assertThat(result.glyphCount()).isGreaterThan(0);
        assertThat(result.sha1()).hasSize(40);
        assertThat(Files.size(result.packFile())).isGreaterThan(0);
        try (ZipFile zip = new ZipFile(result.packFile().toFile())) {
            assertThat(zip.getEntry("pack.mcmeta")).isNotNull();
            assertThat(zip.getEntry("assets/blazevortex/font/emoji.json")).isNotNull();
            assertThat(zip.getEntry("assets/blazevortex/textures/font/emoji/fire.png")).isNotNull();
        }
        String json = Files.readString(layout.generatedPack().resolve("assets/blazevortex/font/emoji.json"));
        assertThat(json).contains("\"height\":");
        assertThat(json).contains("fire.png");
    }

    @Test
    void configLoaderUsesDefaultsWithoutDestroyingUserFile() throws Exception {
        Path file = temp.resolve("config.yml");
        Files.writeString(file, "debug: true\n");
        BvxConfig config = new ConfigLoader(Logger.getLogger("test")).load(file, "config/config.yml");
        assertThat(config.debug()).isTrue();
        assertThat(config.moduleEnabled("emoji")).isTrue();
        String text = Files.readString(file);
        assertThat(text).contains("debug: true");
        assertThat(text).doesNotContain("modules:");
    }
}
