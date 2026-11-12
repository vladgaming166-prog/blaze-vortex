package io.github.blazevortex.api.emoji;

import java.nio.file.Path;
import java.util.Collection;
import java.util.Optional;

public interface EmojiRegistry {

    Collection<Emoji> all();

    Optional<Emoji> find(String name);

    /**
     * Register or replace an emoji from a validated image file. Glyph assignment is stable
     * across restarts when the persistent glyph registry can be written.
     */
    Emoji register(EmojiRegistration registration);

    boolean unregister(String name);

    void reload();

    Path contentDirectory();
}
