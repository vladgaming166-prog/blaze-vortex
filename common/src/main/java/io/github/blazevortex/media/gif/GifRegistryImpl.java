package io.github.blazevortex.media.gif;

import io.github.blazevortex.api.BvxException;
import io.github.blazevortex.api.gif.GifAnimation;
import io.github.blazevortex.api.gif.GifRegistry;
import io.github.blazevortex.cache.BoundedCache;
import io.github.blazevortex.content.AssetDiscovery;
import io.github.blazevortex.content.ContentLayout;
import io.github.blazevortex.security.MagicBytes;
import io.github.blazevortex.security.MediaKind;
import io.github.blazevortex.util.Names;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class GifRegistryImpl implements GifRegistry {

    private final ContentLayout layout;
    private final GifDecoder decoder;
    private final Logger logger;
    private final ConcurrentHashMap<String, GifAssetImpl> gifs = new ConcurrentHashMap<>();
    private final BoundedCache<String, GifSequence> frames;

    public GifRegistryImpl(ContentLayout layout, GifDecoder decoder, Logger logger, int cacheSize) {
        this.layout = layout;
        this.decoder = decoder;
        this.logger = logger;
        this.frames = new BoundedCache<>(cacheSize);
    }

    public void discover() {
        try {
            for (Path file : AssetDiscovery.discover(layout.gifs(), AssetDiscovery.kinds(MediaKind.GIF))) {
                try {
                    register(file);
                } catch (Exception ex) {
                    logger.log(Level.WARNING, "GIF '" + file.getFileName() + "' rejected: " + ex.getMessage(), ex);
                }
            }
        } catch (IOException ex) {
            logger.log(Level.WARNING, "GIF discovery failed: " + ex.getMessage(), ex);
        }
    }

    @Override
    public Collection<GifAnimation> all() {
        return List.copyOf(gifs.values());
    }

    @Override
    public Optional<GifAnimation> find(String name) {
        return Optional.ofNullable(gifs.get(name.toLowerCase()));
    }

    @Override
    public GifAnimation register(Path file) {
        try {
            MagicBytes.requireMatching(file, MediaKind.GIF);
            GifInfo info = decoder.probe(file);
            GifAssetImpl asset = new GifAssetImpl(
                    Names.assetName(file),
                    file,
                    info.width(),
                    info.height(),
                    info.frameCount(),
                    info.delaysMs(),
                    info.loopCount(),
                    info.hasTransparency(),
                    info.averageFps()
            );
            gifs.put(asset.name(), asset);
            return asset;
        } catch (IOException ex) {
            throw new BvxException("Failed to register GIF: " + ex.getMessage(), ex);
        }
    }

    public GifSequence frames(String name) {
        GifAssetImpl asset = gifs.get(name.toLowerCase());
        if (asset == null) {
            throw new BvxException("Unknown GIF: " + name);
        }
        return frames.getOrCompute(asset.name(), key -> {
            try {
                return decoder.decode(asset.file());
            } catch (IOException ex) {
                throw new BvxException("GIF decode failed: " + ex.getMessage(), ex);
            }
        });
    }

    @Override
    public Path contentDirectory() {
        return layout.gifs();
    }
}
