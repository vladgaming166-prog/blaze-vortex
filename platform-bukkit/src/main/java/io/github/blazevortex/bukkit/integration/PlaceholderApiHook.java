package io.github.blazevortex.bukkit.integration;

import io.github.blazevortex.api.component.RenderContext;
import io.github.blazevortex.core.BlazeVortexEngine;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class PlaceholderApiHook extends PlaceholderExpansion {

    private final Plugin plugin;
    private final BlazeVortexEngine engine;

    public PlaceholderApiHook(Plugin plugin, BlazeVortexEngine engine) {
        this.plugin = plugin;
        this.engine = engine;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "bvx";
    }

    @Override
    public @NotNull String getAuthor() {
        return "BlazeVortex";
    }

    @Override
    public @NotNull String getVersion() {
        return engine.version();
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public @Nullable String onRequest(OfflinePlayer player, @NotNull String params) {
        if (params.startsWith("emoji_")) {
            String name = params.substring("emoji_".length());
            return engine.emojis().find(name)
                    .map(emoji -> String.valueOf(emoji.glyph()))
                    .orElse("<" + name + ">");
        }
        if (params.equalsIgnoreCase("emojis")) {
            return String.valueOf(engine.emojis().all().size());
        }
        return engine.renderer().renderLegacy("<" + params + ">", RenderContext.CUSTOM);
    }
}
