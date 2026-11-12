package io.github.blazevortex.emoji;

import io.github.blazevortex.api.component.RenderContext;
import io.github.blazevortex.api.emoji.Emoji;
import io.github.blazevortex.api.emoji.EmojiRenderer;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;

import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class EmojiRendererImpl implements EmojiRenderer {

    public static final Key FONT = Key.key("blazevortex", "emoji");
    private static final Pattern TAG = Pattern.compile("<([a-zA-Z0-9_]+)>");
    /**
     * MiniMessage tags that are never treated as emoji names even if they match {@code <word>}.
     * Registered emojis still win for a self-closing tag when the name is in the registry.
     */
    private static final Set<String> MINIMESSAGE_TAGS = Set.of(
            "black", "dark_blue", "dark_green", "dark_aqua", "dark_red", "dark_purple", "gold", "gray",
            "dark_gray", "blue", "green", "aqua", "red", "light_purple", "yellow", "white",
            "bold", "italic", "underlined", "strikethrough", "obfuscated", "reset",
            "gradient", "rainbow", "hover", "click", "font", "key", "keybind", "lang", "tr",
            "translatable", "insertion", "newline", "br", "selector", "score", "nbt", "pride",
            "shadow", "color", "colour", "transition"
    );

    private final EmojiRegistryImpl registry;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();
    private final boolean animate;

    public EmojiRendererImpl(EmojiRegistryImpl registry, boolean animate) {
        this.registry = registry;
        this.animate = animate;
    }

    @Override
    public Component render(String text) {
        return render(text, RenderContext.CUSTOM);
    }

    @Override
    public Component render(String text, RenderContext context) {
        if (text == null || text.isEmpty()) {
            return Component.empty();
        }
        String replaced = replaceTags(text, context, animate);
        try {
            return miniMessage.deserialize(replaced);
        } catch (Exception ex) {
            return Component.text(replaced);
        }
    }

    @Override
    public Component render(Component component, RenderContext context) {
        String plain = PlainTextComponentSerializer.plainText().serialize(component);
        if (!TAG.matcher(plain).find()) {
            return component;
        }
        return rewrite(component, context);
    }

    @Override
    public String renderLegacy(String text, RenderContext context) {
        return LegacyComponentSerializer.legacySection().serialize(render(text, context));
    }

    public String replaceTags(String text, RenderContext context, boolean animatedNow) {
        Matcher matcher = TAG.matcher(text);
        StringBuilder out = new StringBuilder();
        long now = System.currentTimeMillis();
        while (matcher.find()) {
            String name = matcher.group(1).toLowerCase();
            Emoji emoji = registry.find(name).orElse(null);
            if (emoji instanceof RegisteredEmoji registered && registered.enabled()) {
                char glyph = animatedNow ? registered.frameGlyph(now) : registered.glyph();
                String insert = "<font:" + fontName(context) + ">" + glyph + "</font>";
                matcher.appendReplacement(out, Matcher.quoteReplacement(insert));
            } else if (emoji == null && MINIMESSAGE_TAGS.contains(name)) {
                matcher.appendReplacement(out, Matcher.quoteReplacement(matcher.group()));
            } else {
                matcher.appendReplacement(out, Matcher.quoteReplacement(matcher.group()));
            }
        }
        matcher.appendTail(out);
        return out.toString();
    }

    private Component rewrite(Component component, RenderContext context) {
        if (component instanceof TextComponent text) {
            String content = text.content();
            if (TAG.matcher(content).find()) {
                Component parsed = render(content, context);
                Component result = parsed.style(text.style().merge(parsed.style()));
                for (Component child : text.children()) {
                    result = result.append(rewrite(child, context));
                }
                return result;
            }
        }
        Component rebuilt = component.children(java.util.List.of());
        for (Component child : component.children()) {
            rebuilt = rebuilt.append(rewrite(child, context));
        }
        return rebuilt;
    }

    private String fontName(RenderContext context) {
        return switch (context) {
            case SCOREBOARD -> "blazevortex:emoji_scoreboard";
            case TAB_LIST -> "blazevortex:emoji_tab";
            case GUI_TITLE, GUI_ITEM -> "blazevortex:emoji_gui";
            default -> "blazevortex:emoji";
        };
    }

    public static Component glyphComponent(char glyph) {
        return Component.text(String.valueOf(glyph)).font(FONT).color(NamedTextColor.WHITE);
    }
}
