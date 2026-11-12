package io.github.blazevortex.api.pack;

import java.nio.file.Path;
import java.util.List;

public record ResourcePackBuildResult(
        boolean success,
        Path packFile,
        String sha1,
        long bytes,
        int glyphCount,
        int soundCount,
        List<String> warnings,
        String error
) {
    public static ResourcePackBuildResult failed(String error) {
        return new ResourcePackBuildResult(false, null, null, 0, 0, 0, List.of(), error);
    }
}
