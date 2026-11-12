package io.github.blazevortex.bukkit.display;

import io.github.blazevortex.api.display.BvxDisplay;
import io.github.blazevortex.api.display.DisplayCreateRequest;
import io.github.blazevortex.api.video.VideoScreen;
import io.github.blazevortex.display.MemoryDisplay;
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
 * Map-in-item-frame screens. Works on all supported Bukkit versions that have maps.
 * Frames are updated from a pre-decoded {@link BufferedImage} supplied by the engine;
 * decoding never happens on the render callback itself.
 */
public final class MapScreenSupport {

    private final Plugin plugin;
    private final Map<String, ScreenBinding> screens = new ConcurrentHashMap<>();

    public MapScreenSupport(Plugin plugin) {
        this.plugin = plugin;
    }

    public BvxDisplay spawnImage(DisplayCreateRequest request, BufferedImage image) {
        ScreenBinding binding = spawnGrid(request.id(), request.worldId(), request.worldName(),
                request.x(), request.y(), request.z(), request.yaw(),
                Math.max(1, Math.round(request.width())), Math.max(1, Math.round(request.height())),
                () -> image);
        return new MemoryDisplay(request, id -> despawn(binding));
    }

    public void bindVideo(VideoScreen screen, Supplier<BufferedImage> frames) {
        despawn(screens.get(screen.id()));
        ScreenBinding binding = spawnGrid(screen.id(), screen.worldId(), screen.worldName(),
                screen.x(), screen.y(), screen.z(), 0f,
                screen.widthBlocks(), screen.heightBlocks(), frames);
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
            Supplier<BufferedImage> frames
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
        FrameBuffer buffer = new FrameBuffer(width, height, frames);
        for (int row = 0; row < height; row++) {
            for (int col = 0; col < width; col++) {
                MapView view = Bukkit.createMap(world);
                view.getRenderers().forEach(view::removeRenderer);
                view.addRenderer(new TileRenderer(buffer, col, row));
                maps.add(view);
                ItemStack map = new ItemStack(Material.FILLED_MAP);
                MapMeta meta = (MapMeta) map.getItemMeta();
                meta.setMapView(view);
                map.setItemMeta(meta);
                Location location = new Location(world, Math.floor(x) + offsetX(face, col), Math.floor(y) + (height - 1 - row), Math.floor(z) + offsetZ(face, col));
                ItemFrame frame = world.spawn(location, ItemFrame.class, entity -> {
                    entity.setFacingDirection(face, true);
                    entity.setItem(map);
                    entity.setVisible(false);
                    entity.setFixed(true);
                    entity.setRotation(Rotation.NONE);
                });
                framesEntities.add(frame.getUniqueId());
            }
        }
        return new ScreenBinding(id, framesEntities, maps, buffer);
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

    private record ScreenBinding(String id, List<UUID> frames, List<MapView> maps, FrameBuffer buffer) {
    }

    static final class FrameBuffer {
        private final int tilesX;
        private final int tilesY;
        private final Supplier<BufferedImage> source;
        private volatile BufferedImage current;

        FrameBuffer(int tilesX, int tilesY, Supplier<BufferedImage> source) {
            this.tilesX = tilesX;
            this.tilesY = tilesY;
            this.source = source;
        }

        BufferedImage tile(int col, int row) {
            BufferedImage image = source.get();
            if (image == null) {
                image = current;
            } else {
                current = image;
            }
            if (image == null) {
                return new BufferedImage(128, 128, BufferedImage.TYPE_INT_RGB);
            }
            int tileW = Math.max(1, image.getWidth() / tilesX);
            int tileH = Math.max(1, image.getHeight() / tilesY);
            int x = Math.min(image.getWidth() - 1, col * tileW);
            int y = Math.min(image.getHeight() - 1, row * tileH);
            int w = Math.min(tileW, image.getWidth() - x);
            int h = Math.min(tileH, image.getHeight() - y);
            BufferedImage tile = image.getSubimage(x, y, Math.max(1, w), Math.max(1, h));
            BufferedImage scaled = new BufferedImage(128, 128, BufferedImage.TYPE_INT_RGB);
            scaled.getGraphics().drawImage(tile, 0, 0, 128, 128, null);
            return scaled;
        }
    }

    static final class TileRenderer extends MapRenderer {
        private final FrameBuffer buffer;
        private final int col;
        private final int row;

        TileRenderer(FrameBuffer buffer, int col, int row) {
            super(true);
            this.buffer = buffer;
            this.col = col;
            this.row = row;
        }

        @Override
        public void render(MapView map, MapCanvas canvas, org.bukkit.entity.Player player) {
            canvas.drawImage(0, 0, buffer.tile(col, row));
        }
    }
}
