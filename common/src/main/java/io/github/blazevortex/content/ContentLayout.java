package io.github.blazevortex.content;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public final class ContentLayout {

    public static final List<String> CONTENT_FOLDERS = List.of(
            "emojis", "images", "gifs", "videos", "sounds", "fonts", "models", "cosmetics", "animations"
    );

    public static final List<String> ROOT_FOLDERS = List.of(
            "content", "modules", "resourcepack/generated", "resourcepack/assets", "resourcepack/exports",
            "cache", "data", "logs", "temp"
    );

    private final Path dataFolder;
    private final Path content;

    public ContentLayout(Path dataFolder) {
        this.dataFolder = dataFolder;
        this.content = dataFolder.resolve("content");
    }

    public void createDirectories() throws IOException {
        for (String folder : ROOT_FOLDERS) {
            Files.createDirectories(dataFolder.resolve(folder));
        }
        for (String folder : CONTENT_FOLDERS) {
            Files.createDirectories(content.resolve(folder));
        }
    }

    public Path dataFolder() {
        return dataFolder;
    }

    public Path content() {
        return content;
    }

    public Path emojis() {
        return content.resolve("emojis");
    }

    public Path images() {
        return content.resolve("images");
    }

    public Path gifs() {
        return content.resolve("gifs");
    }

    public Path videos() {
        return content.resolve("videos");
    }

    public Path sounds() {
        return content.resolve("sounds");
    }

    public Path fonts() {
        return content.resolve("fonts");
    }

    public Path models() {
        return content.resolve("models");
    }

    public Path cosmetics() {
        return content.resolve("cosmetics");
    }

    public Path animations() {
        return content.resolve("animations");
    }

    public Path cache() {
        return dataFolder.resolve("cache");
    }

    public Path data() {
        return dataFolder.resolve("data");
    }

    public Path temp() {
        return dataFolder.resolve("temp");
    }

    public Path resourcePack() {
        return dataFolder.resolve("resourcepack");
    }

    public Path generatedPack() {
        return resourcePack().resolve("generated");
    }

    public Path packExports() {
        return resourcePack().resolve("exports");
    }

    public Path glyphRegistry() {
        return data().resolve("glyph-registry.yml");
    }
}
