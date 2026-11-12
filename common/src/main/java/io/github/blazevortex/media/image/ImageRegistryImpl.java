package io.github.blazevortex.media.image;

import io.github.blazevortex.api.BvxException;
import io.github.blazevortex.api.image.BvxImage;
import io.github.blazevortex.api.image.ImageRegistry;
import io.github.blazevortex.content.AssetDiscovery;
import io.github.blazevortex.content.ContentLayout;
import io.github.blazevortex.security.MagicBytes;
import io.github.blazevortex.security.MediaKind;
import io.github.blazevortex.util.Names;
import io.github.blazevortex.platform.BvxScheduler;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class ImageRegistryImpl implements ImageRegistry {

    private final ContentLayout layout;
    private final ImageProcessor processor;
    private final BvxScheduler scheduler;
    private final Logger logger;
    private final ConcurrentHashMap<String, ImageAssetImpl> images = new ConcurrentHashMap<>();

    public ImageRegistryImpl(ContentLayout layout, ImageProcessor processor, BvxScheduler scheduler, Logger logger) {
        this.layout = layout;
        this.processor = processor;
        this.scheduler = scheduler;
        this.logger = logger;
    }

    public void discover() {
        try {
            for (Path file : AssetDiscovery.discover(layout.images(), AssetDiscovery.kinds(
                    MediaKind.PNG, MediaKind.JPEG, MediaKind.WEBP))) {
                try {
                    register(file);
                } catch (Exception ex) {
                    logger.log(Level.WARNING, "Image '" + file.getFileName() + "' rejected: " + ex.getMessage(), ex);
                }
            }
        } catch (IOException ex) {
            logger.log(Level.WARNING, "Image discovery failed: " + ex.getMessage(), ex);
        }
    }

    @Override
    public Collection<BvxImage> all() {
        return List.copyOf(images.values());
    }

    @Override
    public Optional<BvxImage> find(String name) {
        return Optional.ofNullable(images.get(name.toLowerCase()));
    }

    @Override
    public BvxImage register(Path file) {
        try {
            MediaKind kind = MagicBytes.detect(file).orElseThrow(() ->
                    new BvxException("Not a supported image: " + file.getFileName()));
            if (kind == MediaKind.GIF) {
                throw new BvxException("GIF files belong in content/gifs, not content/images");
            }
            ImageMetadata meta = processor.probe(file);
            ImageAssetImpl asset = new ImageAssetImpl(
                    Names.assetName(file), file, kind.name().toLowerCase(),
                    meta.width(), meta.height(), meta.hasAlpha(), meta.fileBytes());
            images.put(asset.name(), asset);
            return asset;
        } catch (IOException ex) {
            throw new BvxException("Failed to register image: " + ex.getMessage(), ex);
        }
    }

    @Override
    public CompletableFuture<BvxImage> resize(String name, int width, int height) {
        ImageAssetImpl existing = images.get(name.toLowerCase());
        if (existing == null) {
            return CompletableFuture.failedFuture(new BvxException("Unknown image: " + name));
        }
        return scheduler.supplyAsync(() -> {
            try {
                BufferedImage source = processor.read(existing.file());
                BufferedImage resized = processor.resize(source, width, height, true);
                Path target = existing.file().getParent().resolve(existing.name() + "_" + width + "x" + height + ".png");
                processor.writePng(resized, target);
                return register(target);
            } catch (IOException ex) {
                throw new BvxException("Resize failed: " + ex.getMessage(), ex);
            }
        });
    }

    @Override
    public Path contentDirectory() {
        return layout.images();
    }
}
