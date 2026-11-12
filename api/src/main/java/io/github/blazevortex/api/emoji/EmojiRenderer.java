package io.github.blazevortex.api.emoji;

import io.github.blazevortex.api.component.RenderContext;
import net.kyori.adventure.text.Component;

/**
 * Parses {@code <name>} tags and produces Adventure components using allocated glyphs.
 * Unknown emoji names are left unchanged and never break the rest of the message.
 */
public interface EmojiRenderer {

    Component render(String text);

    Component render(String text, RenderContext context);

    Component render(Component component, RenderContext context);

    String renderLegacy(String text, RenderContext context);
}
