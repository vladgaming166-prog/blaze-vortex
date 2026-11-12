package io.github.blazevortex.security;

import io.github.blazevortex.api.BvxException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class UploadValidator {

    private final MediaLimits limits;

    public UploadValidator(MediaLimits limits) {
        this.limits = limits;
    }

    public MediaKind validate(Path file, String originalName) throws IOException {
        PathSafety.assertRegularFile(file);
        long size = Files.size(file);
        if (size <= 0) {
            throw new BvxException("Empty upload rejected: " + originalName);
        }
        if (size > limits.maxUploadBytes()) {
            throw new BvxException("File exceeds maximum upload size of " + limits.maxUploadBytes() + " bytes");
        }
        String safeName = PathSafety.sanitizeFileName(originalName);
        MediaKind declared = MediaKind.fromExtension(safeName)
                .orElseThrow(() -> new BvxException("Unsupported extension: " + safeName));
        return MagicBytes.requireMatching(file, declared);
    }
}
