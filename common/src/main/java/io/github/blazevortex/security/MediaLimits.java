package io.github.blazevortex.security;

public final class MediaLimits {

    private final long maxUploadBytes;
    private final int maxImageWidth;
    private final int maxImageHeight;
    private final long maxDecodedPixels;
    private final int maxGifFrames;
    private final int maxVideoWidth;
    private final int maxVideoHeight;
    private final int maxVideoDurationSeconds;
    private final int maxAudioDurationSeconds;
    private final int maxFps;
    private final int ffmpegTimeoutSeconds;

    public MediaLimits(
            long maxUploadBytes,
            int maxImageWidth,
            int maxImageHeight,
            long maxDecodedPixels,
            int maxGifFrames,
            int maxVideoWidth,
            int maxVideoHeight,
            int maxVideoDurationSeconds,
            int maxAudioDurationSeconds,
            int maxFps,
            int ffmpegTimeoutSeconds
    ) {
        this.maxUploadBytes = maxUploadBytes;
        this.maxImageWidth = maxImageWidth;
        this.maxImageHeight = maxImageHeight;
        this.maxDecodedPixels = maxDecodedPixels;
        this.maxGifFrames = maxGifFrames;
        this.maxVideoWidth = maxVideoWidth;
        this.maxVideoHeight = maxVideoHeight;
        this.maxVideoDurationSeconds = maxVideoDurationSeconds;
        this.maxAudioDurationSeconds = maxAudioDurationSeconds;
        this.maxFps = maxFps;
        this.ffmpegTimeoutSeconds = ffmpegTimeoutSeconds;
    }

    public static MediaLimits defaults() {
        return new MediaLimits(
                50L * 1024 * 1024,
                4096,
                4096,
                16L * 1024 * 1024,
                256,
                1920,
                1080,
                180,
                300,
                30,
                60
        );
    }

    public long maxUploadBytes() {
        return maxUploadBytes;
    }

    public int maxImageWidth() {
        return maxImageWidth;
    }

    public int maxImageHeight() {
        return maxImageHeight;
    }

    public long maxDecodedPixels() {
        return maxDecodedPixels;
    }

    public int maxGifFrames() {
        return maxGifFrames;
    }

    public int maxVideoWidth() {
        return maxVideoWidth;
    }

    public int maxVideoHeight() {
        return maxVideoHeight;
    }

    public int maxVideoDurationSeconds() {
        return maxVideoDurationSeconds;
    }

    public int maxAudioDurationSeconds() {
        return maxAudioDurationSeconds;
    }

    public int maxFps() {
        return maxFps;
    }

    public int ffmpegTimeoutSeconds() {
        return ffmpegTimeoutSeconds;
    }
}
