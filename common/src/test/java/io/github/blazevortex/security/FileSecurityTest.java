package io.github.blazevortex.security;

import io.github.blazevortex.api.BvxException;
import io.github.blazevortex.media.TestMedia;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.awt.Color;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FileSecurityTest {

    @TempDir
    Path temp;

    @Test
    void rejectsPathTraversal() {
        Path root = temp.resolve("content");
        assertThatThrownBy(() -> PathSafety.resolveInside(root, "../secret.yml"))
                .isInstanceOf(BvxException.class);
        assertThatThrownBy(() -> PathSafety.resolveInside(root, "/etc/passwd"))
                .isInstanceOf(BvxException.class);
    }

    @Test
    void sanitizesFileNames() {
        assertThat(PathSafety.sanitizeFileName("../../Fire ICON.PNG")).isEqualTo("fire_icon.png");
    }

    @Test
    void magicBytesDetectPngAndRejectMismatchedExtension() throws Exception {
        Path png = TestMedia.png(temp, "icon.png", 8, 8, Color.RED);
        assertThat(MagicBytes.detect(png)).contains(MediaKind.PNG);
        Path lying = temp.resolve("icon.gif");
        Files.copy(png, lying);
        assertThatThrownBy(() -> MagicBytes.requireMatching(lying, MediaKind.GIF))
                .isInstanceOf(SecurityException.class);
    }

    @Test
    void uploadValidatorRejectsEmptyFiles() throws Exception {
        Path empty = temp.resolve("empty.png");
        Files.write(empty, new byte[0]);
        UploadValidator validator = new UploadValidator(MediaLimits.defaults());
        assertThatThrownBy(() -> validator.validate(empty, "empty.png"))
                .isInstanceOf(BvxException.class);
    }
}
