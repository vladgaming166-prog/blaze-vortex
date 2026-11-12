package io.github.blazevortex.bukkit.display;

import io.github.blazevortex.api.display.BvxDisplay;
import io.github.blazevortex.api.display.DisplayCreateRequest;
import io.github.blazevortex.api.video.VideoScreen;
import io.github.blazevortex.display.MemoryDisplay;
import io.github.blazevortex.media.image.MapTileBaker;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Rotation;
import org.bukkit.World;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ItemFrame;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.MapMeta;
import org.bukkit.map.MapCanvas;
import org.bukkit.map.MapRenderer;
import org.bukkit.map.MapView;
import org.bukkit.plugin.Plugin;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

/**
 * Map-in-item-frame screens. Item frames are used because they are the client
 * primitive that actually paints {@link MapView} pixels. ItemDisplay of an empty
 * filled map only shows the blank map item model.
 */
public final class MapScreenSupport {

    private final Plugin plugin;
    private final Map<String, ScreenBinding> screens = new ConcurrentHashMap<>();

    public MapScreenSupport(Plugin plugin) {
        this.plugin = plugin;
    }

    public BvxDisplay spawnImage(DisplayCreateRequest request, BufferedImage image) {
        return spawnAnimated(request, List.of(image), new int[]{Integer.MAX_VALUE});
    }

    public BvxDisplay spawnAnimated(DisplayCreateRequest request, List<BufferedImage> frames, int[] delaysMs) {
        unbind(request.id());
        int width = Math.max(1, Math.round(request.width()));
        int height = Math.max(1, Math.round(request.height()));
        List<BufferedImage[][]> baked = new ArrayList<>(frames.size());
        for (BufferedImage frame : frames) {
            baked.add(MapTileBaker.bake(frame, width, height));
        }
        ScreenBinding binding = spawnGrid(
                request.id(), request.worldId(), request.worldName(),
                request.x(), request.y(), request.z(), request.yaw(),
                width, height, new AnimationBuffer(baked, delaysMs));
        screens.put(request.id(), binding);
        plugin.getLogger().info("Spawned " + request.type() + " '" + request.assetName()
                + "' as a " + width + "x" + height + " map screen (" + request.id() + ")");
        return new MemoryDisplay(request, this::unbind);
    }

    public void bindVideo(VideoScreen screen, Supplier<BufferedImage> frames) {
        unbind(screen.id());
        ScreenBinding binding = spawnGrid(screen.id(), screen.worldId(), screen.worldName(),
                screen.x(), screen.y(), screen.z(), screen.yaw(),
                screen.widthBlocks(), screen.heightBlocks(), new LiveBuffer(screen.widthBlocks(), screen.heightBlocks(), frames));
        screens.put(screen.id(), binding);
    }

    public void unbind(String id) {
        despawn(screens.remove(id));
    }

    public void shutdown() {
        for (String id : List.copyOf(screens.keySet())) {
            unbind(id);
        }
    }

    private ScreenBinding spawnGrid(
            String id,
            UUID worldId,
            String worldName,
            double x,
            double y,
            double z,
            float yaw,
            int width,
            int height,
            TileSource tiles
    ) {
        World world = Bukkit.getWorld(worldId);
        if (world == null) {
            world = Bukkit.getWorld(worldName);
        }
        if (world == null) {
            throw new IllegalStateException("World not loaded: " + worldName);
        }
        BlockFace face = yawToFace(yaw);
        List<UUID> framesEntities = new ArrayList<>();
        List<MapView> maps = new ArrayList<>();
        int baseX = (int) Math.floor(x);
        int baseY = (int) Math.floor(y);
        int baseZ = (int) Math.floor(z);
        for (int row = 0; row < height; row++) {
            for (int col = 0; col < width; col++) {
                MapView view = Bukkit.createMap(world);
                new ArrayList<>(view.getRenderers()).forEach(view::removeRenderer);
                view.setTrackingPosition(false);
                try {
                    view.setUnlimitedTracking(false);
                } catch (NoSuchMethodError ignored) {
                    // 1.19 always has this; keep the catch for unexpected forks.
                }
                view.addRenderer(new TileRenderer(tiles, col, row, tiles.animated()));
                maps.add(view);
                ItemStack map = new ItemStack(Material.FILLED_MAP);
                MapMeta meta = (MapMeta) map.getItemMeta();
                if (meta != null) {
                    meta.setMapView(view);
                    map.setItemMeta(meta);
                }
                Location location = new Location(
                        world,
                        baseX + offsetX(face, col),
                        baseY + (height - 1 - row),
                        baseZ + offsetZ(face, col)
                );
                ItemFrame frame = world.spawn(location, ItemFrame.class, entity -> {
                    entity.setFacingDirection(face, true);
                    entity.setVisible(false);
                    entity.setFixed(true);
                    entity.setInvulnerable(true);
                    entity.setRotation(Rotation.NONE);
                    entity.setItemDropChance(0f);
                    entity.setItem(map, false);
                });
                frame.setItem(map, false);
                framesEntities.add(frame.getUniqueId());
            }
        }
        return new ScreenBinding(id, framesEntities, maps, tiles);
    }

    private void despawn(ScreenBinding binding) {
        if (binding == null) {
            return;
        }
        for (UUID uuid : binding.frames()) {
            Entity entity = Bukkit.getEntity(uuid);
            if (entity != null) {
                entity.remove();
            }
        }
    }

    private static BlockFace yawToFace(float yaw) {
        float rot = (yaw % 360 + 360) % 360;
        if (rot >= 45 && rot < 135) {
            return BlockFace.WEST;
        }
        if (rot >= 135 && rot < 225) {
            return BlockFace.NORTH;
        }
        if (rot >= 225 && rot < 315) {
            return BlockFace.EAST;
        }
        return BlockFace.SOUTH;
    }

    private static int offsetX(BlockFace face, int col) {
        return switch (face) {
            case SOUTH -> col;
            case NORTH -> -col;
            default -> 0;
        };
    }

    private static int offsetZ(BlockFace face, int col) {
        return switch (face) {
            case EAST -> col;
            case WEST -> -col;
            default -> 0;
        };
    }

    private record ScreenBinding(String id, List<UUID> frames, List<MapView> maps, TileSource tiles) {
    }

    interface TileSource {
        BufferedImage tile(int col, int row);

        boolean animated();
    }

    static final class AnimationBuffer implements TileSource {
        private final List<BufferedImage[][]> frames;
        private final int[] delaysMs;
        private final int totalMs;

        AnimationBuffer(List<BufferedImage[][]> frames, int[] delaysMs) {
            this.frames = List.copyOf(frames);
            this.delaysMs = delaysMs == null || delaysMs.length == 0 ? new int[]{100} : delaysMs;
            int sum = 0;
            for (int delay : this.delaysMs) {
                sum += Math.max(1, delay);
            }
            this.totalMs = Math.max(1, sum);
        }

        @Override
        public BufferedImage tile(int col, int row) {
            BufferedImage[][] grid = frames.get(frameIndex());
            return grid[Math.min(row, grid.length - 1)][Math.min(col, grid[0].length - 1)];
        }

        @Override
        public boolean animated() {
            return frames.size() > 1;
        }

        int frameIndex() {
            if (frames.size() == 1) {
                return 0;
            }
            long cursor = System.currentTimeMillis() % totalMs;
            long acc = 0;
            for (int i = 0; i < frames.size(); i++) {
                acc += Math.max(1, delaysMs[Math.min(i, delaysMs.length - 1)]);
                if (cursor < acc) {
                    return i;
                }
            }
            return frames.size() - 1;
        }
    }

    private static final BufferedImage EMPTY_TILE = new BufferedImage(MapTileBaker.TILE_SIZE, MapTileBaker.TILE_SIZE, BufferedImage.TYPE_INT_RGB);

    static final class LiveBuffer implements TileSource {
        private final int tilesX;
        private final int tilesY;
        private final Supplier<BufferedImage> source;
        private volatile BufferedImage lastSource;
        private volatile BufferedImage[][] baked;

        LiveBuffer(int tilesX, int tilesY, Supplier<BufferedImage> source) {
            this.tilesX = tilesX;
            this.tilesY = tilesY;
            this.source = source;
        }

        @Override
        public BufferedImage tile(int col, int row) {
            BufferedImage image = source.get();
            if (image != lastSource) {
                lastSource = image;
                baked = image == null ? null : MapTileBaker.bake(image, tilesX, tilesY);
            }
            if (baked == null) {
                return EMPTY_TILE;
            }
            return baked[Math.min(row, baked.length - 1)][Math.min(col, baked[0].length - 1)];
        }

        @Override
        public boolean animated() {
            return true;
        }
    }

    static final class TileRenderer extends MapRenderer {
        private final TileSource buffer;
        private final int col;
        private final int row;
        private final boolean animated;
        private boolean drawn;

        TileRenderer(TileSource buffer, int col, int row, boolean animated) {
            super(false);
            this.buffer = buffer;
            this.col = col;
            this.row = row;
            this.animated = animated;
        }

        @Override
        public void render(MapView map, MapCanvas canvas, org.bukkit.entity.Player player) {
            if (!animated && drawn) {
                return;
            }
            canvas.drawImage(0, 0, buffer.tile(col, row));
            drawn = true;
        }
    }
}
