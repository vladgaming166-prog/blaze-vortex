package io.github.blazevortex.media;

import io.github.blazevortex.media.ffmpeg.FfmpegService;
import io.github.blazevortex.security.MediaLimits;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIf;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import static org.assertj.core.api.Assertions.assertThat;

class FfmpegPipelineTest {

    @TempDir
    Path temp;

    static boolean ffmpegAvailable() {
        return new FfmpegService("ffmpeg", MediaLimits.defaults(), Logger.getLogger("test")).available();
    }

    @Test
    @EnabledIf("ffmpegAvailable")
    void convertsWavToOggAndExtractsVideoFrames() throws Exception {
        FfmpegService ffmpeg = new FfmpegService("ffmpeg", MediaLimits.defaults(), Logger.getLogger("test"));
        Path wav = temp.resolve("tone.wav");
        run(List.of("ffmpeg", "-y", "-f", "lavfi", "-i", "sine=frequency=440:duration=1", wav.toString()));
        Path ogg = temp.resolve("tone.ogg");
        ffmpeg.convertToOgg(wav, ogg);
        assertThat(Files.size(ogg)).isGreaterThan(0);

        Path mp4 = temp.resolve("clip.mp4");
        run(List.of("ffmpeg", "-y", "-f", "lavfi", "-i", "testsrc=duration=1:size=64x64:rate=10",
                "-pix_fmt", "yuv420p", mp4.toString()));
        FfmpegService.ProbeResult probe = ffmpeg.probe(mp4);
        assertThat(probe.width()).isEqualTo(64);
        assertThat(probe.height()).isEqualTo(64);
        assertThat(probe.duration().toMillis()).isGreaterThan(200);
        Path frames = temp.resolve("frames");
        ffmpeg.extractFrames(mp4, frames, 5, 64, 64);
        try (var stream = Files.list(frames)) {
            assertThat(stream.filter(path -> path.getFileName().toString().endsWith(".png")).count()).isGreaterThan(0);
        }
    }

    private static void run(List<String> command) throws Exception {
        Process process = new ProcessBuilder(command).redirectErrorStream(true).start();
        boolean finished = process.waitFor(30, TimeUnit.SECONDS);
        assertThat(finished).isTrue();
        assertThat(process.exitValue()).isZero();
    }
}
