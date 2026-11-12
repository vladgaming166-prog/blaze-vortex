package io.github.blazevortex.media.ffmpeg;

import io.github.blazevortex.api.BvxException;
import io.github.blazevortex.security.MediaLimits;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Runs FFmpeg/FFprobe as an optional external dependency using argument lists only.
 * Never concatenates a shell string.
 */
public final class FfmpegService {

    private static final Pattern DURATION = Pattern.compile("duration=([0-9.]+)");
    private static final Pattern WIDTH = Pattern.compile("width=([0-9]+)");
    private static final Pattern HEIGHT = Pattern.compile("height=([0-9]+)");
    private static final Pattern FPS = Pattern.compile("r_frame_rate=([0-9]+)/([0-9]+)");

    private final String ffmpeg;
    private final String ffprobe;
    private final MediaLimits limits;
    private final Logger logger;
    private final boolean available;

    public FfmpegService(String ffmpegPath, MediaLimits limits, Logger logger) {
        this.limits = limits;
        this.logger = logger;
        this.ffmpeg = resolve(ffmpegPath, "ffmpeg");
        this.ffprobe = resolve(sibling(ffmpegPath, "ffprobe"), "ffprobe");
        this.available = canRun(ffmpeg, "-version") && canRun(ffprobe, "-version");
        if (!available) {
            logger.warning("FFmpeg is not available. Video conversion, audio conversion and frame extraction are disabled until it is installed.");
        }
    }

    public boolean available() {
        return available;
    }

    public void require() {
        if (!available) {
            throw new BvxException("FFmpeg is required for this operation but was not found. Install FFmpeg and set ffmpeg.path in config.yml.");
        }
    }

    public ProbeResult probe(Path file) {
        require();
        List<String> command = List.of(
                ffprobe, "-v", "error",
                "-show_entries", "format=duration:stream=width,height,r_frame_rate,codec_type",
                "-of", "default=nw=1",
                file.toAbsolutePath().toString()
        );
        String output = run(command, limits.ffmpegTimeoutSeconds());
        double duration = findDouble(DURATION, output, 0);
        int width = findInt(WIDTH, output, 0);
        int height = findInt(HEIGHT, output, 0);
        double fps = 0;
        Matcher matcher = FPS.matcher(output);
        if (matcher.find()) {
            double num = Double.parseDouble(matcher.group(1));
            double den = Double.parseDouble(matcher.group(2));
            if (den != 0) {
                fps = num / den;
            }
        }
        return new ProbeResult(Duration.ofMillis((long) (duration * 1000)), width, height, fps);
    }

    public void extractFrames(Path video, Path outputDir, int fps, int width, int height) {
        require();
        try {
            Files.createDirectories(outputDir);
        } catch (IOException ex) {
            throw new BvxException("Cannot create frame directory", ex);
        }
        String scale = width + ":" + height + ":force_original_aspect_ratio=decrease";
        List<String> command = List.of(
                ffmpeg, "-y", "-hide_banner", "-loglevel", "error",
                "-i", video.toAbsolutePath().toString(),
                "-vf", "fps=" + Math.max(1, fps) + ",scale=" + scale,
                "-q:v", "3",
                outputDir.resolve("frame_%05d.png").toAbsolutePath().toString()
        );
        run(command, limits.ffmpegTimeoutSeconds());
    }

    public void extractAudio(Path video, Path oggTarget) {
        require();
        try {
            Files.createDirectories(oggTarget.getParent());
        } catch (IOException ex) {
            throw new BvxException("Cannot create audio output directory", ex);
        }
        List<String> command = List.of(
                ffmpeg, "-y", "-hide_banner", "-loglevel", "error",
                "-i", video.toAbsolutePath().toString(),
                "-vn", "-c:a", "libvorbis", "-q:a", "4",
                oggTarget.toAbsolutePath().toString()
        );
        run(command, limits.ffmpegTimeoutSeconds());
    }

    public void convertToOgg(Path source, Path oggTarget) {
        require();
        try {
            Files.createDirectories(oggTarget.getParent());
        } catch (IOException ex) {
            throw new BvxException("Cannot create OGG output directory", ex);
        }
        List<String> command = List.of(
                ffmpeg, "-y", "-hide_banner", "-loglevel", "error",
                "-i", source.toAbsolutePath().toString(),
                "-c:a", "libvorbis", "-q:a", "5",
                oggTarget.toAbsolutePath().toString()
        );
        run(command, limits.ffmpegTimeoutSeconds());
    }

    private String run(List<String> command, int timeoutSeconds) {
        ProcessBuilder builder = new ProcessBuilder(command);
        builder.redirectErrorStream(true);
        try {
            Process process = builder.start();
            StringBuilder output = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    output.append(line).append('\n');
                }
            }
            boolean finished = process.waitFor(timeoutSeconds, TimeUnit.SECONDS);
            if (!finished) {
                process.destroyForcibly();
                throw new BvxException("FFmpeg timed out after " + timeoutSeconds + "s");
            }
            if (process.exitValue() != 0) {
                throw new BvxException("FFmpeg failed (" + process.exitValue() + "): " + output);
            }
            return output.toString();
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new BvxException("FFmpeg interrupted", ex);
        } catch (IOException ex) {
            throw new BvxException("Failed to execute FFmpeg: " + ex.getMessage(), ex);
        }
    }

    private boolean canRun(String executable, String argument) {
        try {
            Process process = new ProcessBuilder(executable, argument).redirectErrorStream(true).start();
            boolean finished = process.waitFor(5, TimeUnit.SECONDS);
            if (!finished) {
                process.destroyForcibly();
                return false;
            }
            return process.exitValue() == 0;
        } catch (Exception ex) {
            return false;
        }
    }

    private String resolve(String configured, String fallback) {
        if (configured != null && !configured.isBlank()) {
            Path path = Path.of(configured);
            if (Files.isExecutable(path) || Files.isRegularFile(path)) {
                return path.toAbsolutePath().toString();
            }
            return configured;
        }
        return fallback;
    }

    private String sibling(String ffmpegPath, String name) {
        try {
            Path path = Path.of(ffmpegPath);
            Path parent = path.getParent();
            if (parent != null) {
                Path probe = parent.resolve(name);
                if (Files.isRegularFile(probe)) {
                    return probe.toString();
                }
                Path win = parent.resolve(name + ".exe");
                if (Files.isRegularFile(win)) {
                    return win.toString();
                }
            }
        } catch (Exception ignored) {
        }
        return name;
    }

    private static double findDouble(Pattern pattern, String text, double fallback) {
        Matcher matcher = pattern.matcher(text.toLowerCase(Locale.ROOT));
        if (matcher.find()) {
            return Double.parseDouble(matcher.group(1));
        }
        return fallback;
    }

    private static int findInt(Pattern pattern, String text, int fallback) {
        Matcher matcher = pattern.matcher(text.toLowerCase(Locale.ROOT));
        if (matcher.find()) {
            return Integer.parseInt(matcher.group(1));
        }
        return fallback;
    }

    public record ProbeResult(Duration duration, int width, int height, double fps) {
    }
}
