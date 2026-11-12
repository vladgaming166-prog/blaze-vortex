# Video engine

Upload `mp4` or `mov` into `content/videos/`.

Minecraft does not decode those containers. BVX:

1. Validates magic bytes (`ftyp`)
2. Probes duration/resolution with **ffprobe**
3. Rejects files over configured duration/resolution caps
4. Extracts PNG frames with FFmpeg at `video.default-fps` and scaled resolution
5. Optionally extracts OGG Vorbis audio
6. Plays frames on a map grid (one 128×128 map per block tile)

Commands:

```text
/bvx spawn video trailer 5 3
/bvx video play trailer
/bvx video pause trailer
/bvx video stop trailer
/bvx screen list
/bvx video remove <screen-id>
```

Playback is synchronized by a shared clock on the screen object (elapsed time × fps, with skip-to-latest-frame under load — the renderer always draws the current index, never a backlog). Multiple screens are limited by `video.max-concurrent` during FFmpeg work.

Without FFmpeg, videos can be registered as files but `process` / playback frames fail with an explicit error.
