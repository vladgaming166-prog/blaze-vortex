package io.github.blazevortex.font;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class GlyphAllocatorTest {

    @TempDir
    Path temp;

    @Test
    void allocatesUniquePersistentGlyphs() {
        Path file = temp.resolve("glyphs.yml");
        GlyphAllocator first = new GlyphAllocator(file);
        char fire = first.allocate("emoji:fire");
        char heart = first.allocate("emoji:heart");
        assertThat(fire).isNotEqualTo(heart);
        assertThat((int) fire).isBetween(GlyphAllocator.PUA_START, GlyphAllocator.PUA_END);

        GlyphAllocator second = new GlyphAllocator(file);
        assertThat(second.allocate("emoji:fire")).isEqualTo(fire);
        assertThat(second.allocate("emoji:heart")).isEqualTo(heart);
        char star = second.allocate("emoji:star");
        assertThat(star).isNotEqualTo(fire).isNotEqualTo(heart);
    }
}
