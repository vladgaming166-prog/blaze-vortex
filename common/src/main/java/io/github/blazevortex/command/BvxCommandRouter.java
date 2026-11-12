package io.github.blazevortex.command;

import io.github.blazevortex.api.BvxException;
import io.github.blazevortex.api.display.DisplayCreateRequest;
import io.github.blazevortex.api.display.DisplayType;
import io.github.blazevortex.api.emoji.Emoji;
import io.github.blazevortex.api.gif.GifAnimation;
import io.github.blazevortex.api.image.BvxImage;
import io.github.blazevortex.api.pack.ResourcePackBuildResult;
import io.github.blazevortex.api.video.VideoAsset;
import io.github.blazevortex.api.video.VideoScreen;
import io.github.blazevortex.api.video.VideoScreenCreateRequest;
import io.github.blazevortex.core.BlazeVortexEngine;
import io.github.blazevortex.platform.PlayerLookTarget;
import io.github.blazevortex.platform.PlayerLocation;
import io.github.blazevortex.security.PathSafety;
import io.github.blazevortex.util.Names;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Stream;

public final class BvxCommandRouter {

    private static final List<String> ROOT = List.of(
            "help", "version", "reload", "debug", "status", "upload", "content",
            "emoji", "spawn", "image", "gif", "video", "put", "sound", "screen",
            "pack", "hologram", "editor"
    );

    private final BlazeVortexEngine engine;

    public BvxCommandRouter(BlazeVortexEngine engine) {
        this.engine = engine;
    }

    public CommandResult execute(CommandSenderView sender, String[] args) {
        if (args.length == 0 || "help".equalsIgnoreCase(args[0])) {
            return CommandResult.ok(helpText());
        }
        String root = args[0].toLowerCase(Locale.ROOT);
        try {
            return switch (root) {
                case "version" -> CommandResult.ok("Blaze Vortex " + engine.version()
                        + " on " + engine.platform().serverBrand() + " " + engine.platform().minecraftVersion());
                case "reload" -> require(sender, Permissions.RELOAD, () -> {
                    engine.reload();
                    return CommandResult.ok("Configuration and assets reloaded.");
                });
                case "debug" -> require(sender, Permissions.DEBUG, () -> CommandResult.ok(debugText()));
                case "status" -> require(sender, Permissions.DEBUG, () -> CommandResult.ok(statusText()));
                case "emoji" -> emoji(sender, tail(args));
                case "image" -> image(sender, tail(args));
                case "gif" -> gif(sender, tail(args));
                case "video" -> video(sender, tail(args));
                case "sound" -> sound(sender, tail(args));
                case "put" -> put(sender, tail(args));
                case "spawn" -> spawn(sender, tail(args));
                case "screen" -> screen(sender, tail(args));
                case "pack" -> pack(sender, tail(args));
                case "hologram" -> hologram(sender, tail(args));
                case "content" -> content(sender, tail(args));
                case "upload" -> require(sender, Permissions.UPLOAD, () ->
                        CommandResult.ok("Drop files into plugins/BlazeVortex/content/ or use the web dashboard. Uploads are validated by magic bytes, never by extension alone."));
                case "editor" -> require(sender, Permissions.GUI, () ->
                        CommandResult.ok("GUI editor is opened by the platform adapter when a player runs this command."));
                default -> CommandResult.error("Unknown subcommand '" + root + "'. Try /bvx help");
            };
        } catch (BvxException ex) {
            return CommandResult.error(ex.getMessage());
        } catch (Exception ex) {
            engine.platform().logger().warning("Command failed: " + ex.getMessage());
            return CommandResult.error("Command failed: " + ex.getMessage());
        }
    }

    public List<String> complete(CommandSenderView sender, String[] args) {
        if (args.length <= 1) {
            return prefix(ROOT, args.length == 0 ? "" : args[0]);
        }
        String root = args[0].toLowerCase(Locale.ROOT);
        String last = args[args.length - 1];
        return switch (root) {
            case "emoji" -> args.length == 2 ? prefix(List.of("list", "info", "reload"), last)
                    : args.length == 3 && "info".equalsIgnoreCase(args[1]) ? names(engine.emojis().all().stream().map(Emoji::name), last)
                    : List.of();
            case "image" -> args.length == 2 ? prefix(List.of("list", "info", "resize"), last)
                    : names(engine.images().all().stream().map(BvxImage::name), last);
            case "gif" -> args.length == 2 ? prefix(List.of("list", "info", "preview"), last)
                    : names(engine.gifs().all().stream().map(GifAnimation::name), last);
            case "spawn" -> args.length == 2 ? prefix(List.of("image", "gif", "video"), last)
                    : completeSpawn(args, last);
            case "video" -> args.length == 2 ? prefix(List.of("list", "play", "pause", "stop", "remove"), last)
                    : names(engine.videos().assets().stream().map(VideoAsset::name), last);
            case "sound" -> args.length == 2 ? prefix(List.of("list", "play", "stop", "info"), last)
                    : names(engine.sounds().assets().stream().map(a -> a.name()), last);
            case "put" -> args.length == 2 ? prefix(List.of("sound"), last)
                    : names(engine.sounds().assets().stream().map(a -> a.name()), last);
            case "screen" -> args.length == 2 ? prefix(List.of("list", "info", "remove"), last)
                    : names(engine.videos().screens().stream().map(VideoScreen::id), last);
            case "pack" -> prefix(List.of("build", "export", "send", "status"), last);
            case "hologram" -> prefix(List.of("list", "reload"), last);
            case "content" -> prefix(List.of("list"), last);
            default -> List.of();
        };
    }

    private List<String> completeSpawn(String[] args, String last) {
        if (args.length != 3) {
            return List.of();
        }
        return switch (args[1].toLowerCase(Locale.ROOT)) {
            case "image" -> names(engine.images().all().stream().map(BvxImage::name), last);
            case "gif" -> names(engine.gifs().all().stream().map(GifAnimation::name), last);
            case "video" -> names(engine.videos().assets().stream().map(VideoAsset::name), last);
            default -> List.of();
        };
    }

    private CommandResult emoji(CommandSenderView sender, String[] args) {
        return require(sender, Permissions.EMOJI, () -> {
            if (args.length == 0 || "list".equalsIgnoreCase(args[0])) {
                String list = engine.emojis().all().stream()
                        .map(e -> e.name() + " size=" + e.size() + " " + e.sourceWidth() + "x" + e.sourceHeight()
                                + " glyph=" + e.glyphHeight() + "x" + Math.round(e.glyphWidth()))
                        .reduce((a, b) -> a + "\n" + b)
                        .orElse("(no emojis)");
                return CommandResult.ok(list);
            }
            if ("reload".equalsIgnoreCase(args[0])) {
                engine.emojis().reload();
                return CommandResult.ok("Emojis reloaded (" + engine.emojis().all().size() + ")");
            }
            if ("info".equalsIgnoreCase(args[0]) && args.length >= 2) {
                Emoji emoji = engine.emojis().find(args[1]).orElseThrow(() -> new BvxException("Unknown emoji: " + args[1]));
                return CommandResult.ok("Emoji <" + emoji.name() + ">\n"
                        + "file: " + emoji.file().getFileName() + "\n"
                        + "source: " + emoji.sourceWidth() + "x" + emoji.sourceHeight()
                        + " aspect=" + String.format(Locale.ROOT, "%.3f", emoji.aspectRatio()) + "\n"
                        + "size: " + emoji.size() + "%\n"
                        + "glyph: height=" + emoji.glyphHeight() + " width=" + String.format(Locale.ROOT, "%.1f", emoji.glyphWidth())
                        + " ascent=" + emoji.ascent() + "\n"
                        + "animated: " + emoji.animated());
            }
            return CommandResult.error("Usage: /bvx emoji <list|info|reload>");
        });
    }

    private CommandResult image(CommandSenderView sender, String[] args) {
        return require(sender, Permissions.IMAGE, () -> {
            if (args.length == 0 || "list".equalsIgnoreCase(args[0])) {
                return CommandResult.ok(engine.images().all().stream()
                        .map(img -> img.name() + " " + img.width() + "x" + img.height())
                        .reduce((a, b) -> a + "\n" + b).orElse("(no images)"));
            }
            if ("info".equalsIgnoreCase(args[0]) && args.length >= 2) {
                BvxImage image = engine.images().find(args[1]).orElseThrow(() -> new BvxException("Unknown image: " + args[1]));
                return CommandResult.ok(image.name() + " " + image.width() + "x" + image.height()
                        + " format=" + image.format() + " alpha=" + image.hasAlpha());
            }
            if ("resize".equalsIgnoreCase(args[0]) && args.length >= 4) {
                int w = Integer.parseInt(args[2]);
                int h = Integer.parseInt(args[3]);
                engine.images().resize(args[1], w, h);
                return CommandResult.ok("Resize of " + args[1] + " queued to " + w + "x" + h + " (aspect preserved).");
            }
            return CommandResult.error("Usage: /bvx image <list|info|resize>");
        });
    }

    private CommandResult gif(CommandSenderView sender, String[] args) {
        return require(sender, Permissions.GIF, () -> {
            if (args.length == 0 || "list".equalsIgnoreCase(args[0])) {
                return CommandResult.ok(engine.gifs().all().stream()
                        .map(g -> g.name() + " " + g.width() + "x" + g.height() + " frames=" + g.frameCount())
                        .reduce((a, b) -> a + "\n" + b).orElse("(no gifs)"));
            }
            if (("info".equalsIgnoreCase(args[0]) || "preview".equalsIgnoreCase(args[0])) && args.length >= 2) {
                GifAnimation gif = engine.gifs().find(args[1]).orElseThrow(() -> new BvxException("Unknown GIF: " + args[1]));
                return CommandResult.ok(gif.name() + " " + gif.width() + "x" + gif.height()
                        + " frames=" + gif.frameCount() + " fps~=" + String.format(Locale.ROOT, "%.1f", gif.defaultFps())
                        + " delays=" + gif.frameDelaysMs());
            }
            return CommandResult.error("Usage: /bvx gif <list|info|preview>");
        });
    }

    private CommandResult video(CommandSenderView sender, String[] args) {
        return require(sender, Permissions.VIDEO, () -> {
            if (args.length == 0 || "list".equalsIgnoreCase(args[0])) {
                return CommandResult.ok(engine.videos().assets().stream()
                        .map(v -> v.name() + " " + v.width() + "x" + v.height() + " processed=" + v.processed())
                        .reduce((a, b) -> a + "\n" + b).orElse("(no videos)"));
            }
            if (args.length >= 2) {
                String action = args[0].toLowerCase(Locale.ROOT);
                String name = args[1];
                return switch (action) {
                    case "play" -> {
                        VideoScreen screen = findScreenByVideo(name).orElseThrow(() -> new BvxException("No screen for video " + name + ". Spawn one first."));
                        screen.play();
                        yield CommandResult.ok("Playing " + name + " on screen " + screen.id());
                    }
                    case "pause" -> {
                        VideoScreen screen = findScreenByVideo(name).orElseThrow(() -> new BvxException("No screen for " + name));
                        screen.pause();
                        yield CommandResult.ok("Paused " + screen.id());
                    }
                    case "stop" -> {
                        VideoScreen screen = findScreenByVideo(name).orElseThrow(() -> new BvxException("No screen for " + name));
                        screen.stop();
                        yield CommandResult.ok("Stopped " + screen.id());
                    }
                    case "remove" -> {
                        boolean removed = engine.videos().removeScreen(name);
                        yield removed ? CommandResult.ok("Removed screen " + name)
                                : CommandResult.error("No screen " + name);
                    }
                    default -> CommandResult.error("Usage: /bvx video <list|play|pause|stop|remove>");
                };
            }
            return CommandResult.error("Usage: /bvx video <list|play|pause|stop|remove>");
        });
    }

    private CommandResult sound(CommandSenderView sender, String[] args) {
        return require(sender, Permissions.SOUND, () -> {
            if (args.length == 0 || "list".equalsIgnoreCase(args[0])) {
                return CommandResult.ok(engine.sounds().assets().stream()
                        .map(s -> s.name() + " " + s.format() + " converted=" + s.converted())
                        .reduce((a, b) -> a + "\n" + b).orElse("(no sounds)"));
            }
            if ("play".equalsIgnoreCase(args[0]) && args.length >= 2) {
                if (!sender.isPlayer()) {
                    return CommandResult.error("A player is required to play positional sound.");
                }
                PlayerLocation loc = engine.platform().locationOf(sender.uuid())
                        .orElseThrow(() -> new BvxException("Player location unavailable"));
                engine.sounds().play(io.github.blazevortex.api.audio.SoundPlayRequest.of(args[1])
                        .player(sender.uuid())
                        .location(loc.worldId(), loc.x(), loc.y(), loc.z())
                        .build());
                return CommandResult.ok("Playing " + args[1]);
            }
            if ("stop".equalsIgnoreCase(args[0]) && args.length >= 2) {
                engine.sounds().stop(args[1]);
                return CommandResult.ok("Stopped " + args[1]);
            }
            if ("info".equalsIgnoreCase(args[0]) && args.length >= 2) {
                var sound = engine.sounds().findAsset(args[1]).orElseThrow(() -> new BvxException("Unknown sound: " + args[1]));
                return CommandResult.ok(sound.name() + " format=" + sound.format() + " duration=" + sound.duration()
                        + " ogg=" + sound.converted() + " key=" + sound.namespacedKey());
            }
            return CommandResult.error("Usage: /bvx sound <list|play|stop|info>");
        });
    }

    private CommandResult put(CommandSenderView sender, String[] args) {
        return require(sender, Permissions.SOUND, () -> {
            if (args.length < 2 || !"sound".equalsIgnoreCase(args[0])) {
                return CommandResult.error("Usage: /bvx put sound <filename>");
            }
            if (!sender.isPlayer()) {
                return CommandResult.error("A player is required to place a sound source.");
            }
            PlayerLookTarget target = engine.platform().lookTarget(sender.uuid())
                    .orElseThrow(() -> new BvxException("Look at a block or stand still to place a sound source"));
            var source = engine.sounds().putSource(io.github.blazevortex.api.audio.SoundSourceCreateRequest.builder(
                    args[1], target.worldId(), target.worldName(), target.x(), target.y(), target.z()
            ).build());
            return CommandResult.ok("Placed looping sound source " + source.id() + " at the target with radius " + source.radius());
        });
    }

    private CommandResult spawn(CommandSenderView sender, String[] args) {
        if (args.length < 2) {
            return CommandResult.error("Usage: /bvx spawn <image|gif|video> <name> [width height]");
        }
        if (!sender.isPlayer()) {
            return CommandResult.error("A player is required to spawn world displays.");
        }
        PlayerLookTarget target = engine.platform().lookTarget(sender.uuid())
                .orElseThrow(() -> new BvxException("Look at a block to spawn a display"));
        String type = args[0].toLowerCase(Locale.ROOT);
        String name = Names.fromUserInput(args[1]);
        int width = args.length >= 3 ? Integer.parseInt(args[2]) : 2;
        int height = args.length >= 4 ? Integer.parseInt(args[3]) : 2;
        return switch (type) {
            case "image" -> require(sender, Permissions.IMAGE, () -> {
                if (engine.images().find(name).isEmpty() && engine.emojis().find(name).isEmpty()) {
                    throw new BvxException("Unknown image '" + name + "'. Put a PNG/JPG/WEBP in plugins/BlazeVortex/content/images/ then /bvx reload. Use the file name without extension.");
                }
                var display = engine.displays().create(DisplayCreateRequest.builder(
                        DisplayType.IMAGE, name, target.worldId(), target.worldName(), target.x(), target.y() + 1, target.z()
                ).yaw(target.yaw()).width(width).height(height).build());
                return CommandResult.ok("Spawned image " + display.id() + " as a " + width + "x" + height
                        + " map screen. You should see the picture on invisible item frames, not a blank floating map.");
            });
            case "gif" -> require(sender, Permissions.GIF, () -> {
                engine.gifs().find(name).orElseThrow(() -> new BvxException(
                        "Unknown GIF '" + name + "'. Put a .gif in plugins/BlazeVortex/content/gifs/ then /bvx reload."));
                var display = engine.displays().create(DisplayCreateRequest.builder(
                        DisplayType.GIF, name, target.worldId(), target.worldName(), target.x(), target.y() + 1, target.z()
                ).yaw(target.yaw()).width(width).height(height).build());
                return CommandResult.ok("Spawned GIF " + display.id() + " as a " + width + "x" + height + " map screen.");
            });
            case "video" -> require(sender, Permissions.VIDEO, () -> {
                engine.videos().findAsset(name).orElseThrow(() -> new BvxException("Unknown video: " + name));
                VideoScreen screen = engine.videos().createScreen(VideoScreenCreateRequest.builder(
                        name, target.worldId(), target.worldName(), target.x(), target.y() + 1, target.z()
                ).yaw(target.yaw()).widthBlocks(width).heightBlocks(height).fps(engine.config().defaultVideoFps()).build());
                engine.videos().process(name);
                return CommandResult.ok("Created video screen " + screen.id() + ". Frames are extracted asynchronously with FFmpeg; the screen stays stopped until /bvx video play " + name);
            });
            default -> CommandResult.error("Usage: /bvx spawn <image|gif|video> <name>");
        };
    }

    private CommandResult screen(CommandSenderView sender, String[] args) {
        return require(sender, Permissions.SCREEN, () -> {
            if (args.length == 0 || "list".equalsIgnoreCase(args[0])) {
                return CommandResult.ok(engine.videos().screens().stream()
                        .map(s -> s.id() + " video=" + s.videoName() + " " + s.state())
                        .reduce((a, b) -> a + "\n" + b).orElse("(no screens)"));
            }
            if ("info".equalsIgnoreCase(args[0]) && args.length >= 2) {
                VideoScreen screen = engine.videos().findScreen(args[1]).orElseThrow(() -> new BvxException("Unknown screen: " + args[1]));
                return CommandResult.ok(screen.id() + " video=" + screen.videoName() + " " + screen.widthBlocks() + "x"
                        + screen.heightBlocks() + " state=" + screen.state() + " fps=" + screen.fps());
            }
            if ("remove".equalsIgnoreCase(args[0]) && args.length >= 2) {
                boolean removed = engine.videos().removeScreen(args[1]);
                return removed ? CommandResult.ok("Removed " + args[1]) : CommandResult.error("Unknown screen " + args[1]);
            }
            return CommandResult.error("Usage: /bvx screen <list|info|remove>");
        });
    }

    private CommandResult pack(CommandSenderView sender, String[] args) {
        return require(sender, Permissions.PACK, () -> {
            if (args.length == 0 || "status".equalsIgnoreCase(args[0])) {
                return CommandResult.ok("Resource pack status: " + engine.resourcePacks().status()
                        + (engine.resourcePacks().lastSha1().map(sha -> " sha1=" + sha).orElse("")));
            }
            if ("build".equalsIgnoreCase(args[0]) || "export".equalsIgnoreCase(args[0])) {
                engine.resourcePacks().build().thenAccept(result ->
                        sender.send(describe(result)));
                return CommandResult.ok("Resource pack build started asynchronously.");
            }
            if ("send".equalsIgnoreCase(args[0])) {
                ResourcePackBuildResult last = engine.packBuilder().lastResult();
                if (last == null || !last.success()) {
                    return CommandResult.error("Build a pack first with /bvx pack build");
                }
                engine.platform().sendResourcePack(last, engine.config().packRequired());
                return CommandResult.ok("Resource pack send requested for online players.");
            }
            return CommandResult.error("Usage: /bvx pack <build|export|send|status>");
        });
    }

    private CommandResult hologram(CommandSenderView sender, String[] args) {
        return require(sender, Permissions.HOLOGRAM, () -> {
            if (args.length == 0 || "list".equalsIgnoreCase(args[0])) {
                return CommandResult.ok(engine.holograms().all().stream()
                        .map(h -> h.id() + " lines=" + h.lines().size())
                        .reduce((a, b) -> a + "\n" + b).orElse("(no holograms)"));
            }
            if ("reload".equalsIgnoreCase(args[0])) {
                engine.holograms().reload();
                return CommandResult.ok("Holograms reloaded.");
            }
            return CommandResult.error("Usage: /bvx hologram <list|reload>");
        });
    }

    private CommandResult content(CommandSenderView sender, String[] args) {
        return require(sender, Permissions.UPLOAD, () -> {
            try (Stream<Path> stream = Files.walk(engine.contentFolder())) {
                String list = stream.filter(Files::isRegularFile)
                        .map(path -> engine.contentFolder().relativize(path).toString())
                        .reduce((a, b) -> a + "\n" + b)
                        .orElse("(empty)");
                return CommandResult.ok(list);
            } catch (Exception ex) {
                throw new BvxException(ex.getMessage(), ex);
            }
        });
    }

    private Optional<VideoScreen> findScreenByVideo(String name) {
        Optional<VideoScreen> byId = engine.videos().findScreen(name);
        if (byId.isPresent()) {
            return byId;
        }
        return engine.videos().screens().stream()
                .filter(screen -> screen.videoName().equalsIgnoreCase(name))
                .findFirst();
    }

    private String describe(ResourcePackBuildResult result) {
        if (!result.success()) {
            return "Resource pack build failed: " + result.error();
        }
        String warn = result.warnings().isEmpty() ? "" : "\nWarnings:\n" + String.join("\n", result.warnings());
        return "Resource pack ready: " + result.packFile().getFileName()
                + " (" + result.bytes() + " bytes, sha1=" + result.sha1()
                + ", glyphs=" + result.glyphCount() + ", sounds=" + result.soundCount() + ")" + warn;
    }

    private CommandResult require(CommandSenderView sender, String permission, java.util.concurrent.Callable<CommandResult> action) {
        if (!Permissions.allows(sender, permission)) {
            return CommandResult.error("You need permission " + permission);
        }
        try {
            return action.call();
        } catch (BvxException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new BvxException(ex.getMessage(), ex);
        }
    }

    private static String[] tail(String[] args) {
        if (args.length <= 1) {
            return new String[0];
        }
        String[] next = new String[args.length - 1];
        System.arraycopy(args, 1, next, 0, next.length);
        return next;
    }

    private static List<String> prefix(List<String> options, String start) {
        String lower = start.toLowerCase(Locale.ROOT);
        List<String> out = new ArrayList<>();
        for (String option : options) {
            if (option.startsWith(lower)) {
                out.add(option);
            }
        }
        return out;
    }

    private static List<String> names(Stream<String> stream, String start) {
        return prefix(stream.toList(), start);
    }

    private String helpText() {
        return """
                Blaze Vortex commands:
                /bvx help | version | reload | debug | status
                /bvx emoji list|info|reload
                /bvx spawn image|gif|video <name> [w h]
                /bvx image list|info|resize
                /bvx gif list|info|preview
                /bvx video play|pause|stop|list|remove
                /bvx put sound <name>
                /bvx sound play|stop|list|info
                /bvx screen list|info|remove
                /bvx pack build|export|send|status
                /bvx hologram list|reload
                /bvx editor""";
    }

    private String statusText() {
        return "Modules: " + engine.modules().modules().size()
                + "\nEmojis: " + engine.emojis().all().size()
                + "\nImages: " + engine.images().all().size()
                + "\nGIFs: " + engine.gifs().all().size()
                + "\nVideos: " + engine.videos().assets().size()
                + "\nSounds: " + engine.sounds().assets().size()
                + "\nScreens: " + engine.videos().screens().size()
                + "\nDisplays: " + engine.displays().all().size()
                + "\nFFmpeg: " + engine.ffmpeg().available()
                + "\nPack: " + engine.resourcePacks().status();
    }

    private String debugText() {
        return statusText()
                + "\nGlyphs allocated: " + engine.glyphs().size()
                + "\nData folder: " + engine.dataFolder()
                + "\nCapabilities: displays=" + engine.platform().capabilities().displayEntities()
                + " adventure=" + engine.platform().capabilities().nativeAdventure();
    }

    public static Path resolveUpload(Path contentRoot, String relative) {
        return PathSafety.resolveInside(contentRoot, relative);
    }
}
