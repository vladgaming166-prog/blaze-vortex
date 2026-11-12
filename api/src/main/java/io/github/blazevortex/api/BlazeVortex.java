package io.github.blazevortex.api;

import io.github.blazevortex.api.audio.SoundRegistry;
import io.github.blazevortex.api.cosmetic.CosmeticRegistry;
import io.github.blazevortex.api.display.DisplayRegistry;
import io.github.blazevortex.api.effect.EffectRegistry;
import io.github.blazevortex.api.emoji.EmojiRegistry;
import io.github.blazevortex.api.gif.GifRegistry;
import io.github.blazevortex.api.hologram.HologramRegistry;
import io.github.blazevortex.api.image.ImageRegistry;
import io.github.blazevortex.api.item.CustomItemRegistry;
import io.github.blazevortex.api.module.ModuleManager;
import io.github.blazevortex.api.pack.ResourcePackService;
import io.github.blazevortex.api.video.VideoRegistry;

import java.nio.file.Path;

/**
 * Public entry point for the Blaze Vortex content engine.
 *
 * <p>Other plugins must obtain the running instance through {@link BlazeVortexProvider#get()}
 * after BVX has finished enabling.
 */
public interface BlazeVortex {

    String NAME = "BlazeVortex";
    String SHORT_NAME = "BVX";
    String DESCRIPTION = "The Ultimate Minecraft Content, Graphics and Multimedia Engine.";

    String version();

    Path dataFolder();

    Path contentFolder();

    ModuleManager modules();

    EmojiRegistry emojis();

    io.github.blazevortex.api.emoji.EmojiRenderer renderer();

    ImageRegistry images();

    GifRegistry gifs();

    VideoRegistry videos();

    SoundRegistry sounds();

    DisplayRegistry displays();

    HologramRegistry holograms();

    CustomItemRegistry items();

    CosmeticRegistry cosmetics();

    EffectRegistry effects();

    ResourcePackService resourcePacks();

    /**
     * Reloads configuration, rediscovers assets and rebuilds generated metadata.
     * Resource-pack ZIP generation is asynchronous and is not guaranteed to finish
     * before this method returns.
     */
    void reload();
}
