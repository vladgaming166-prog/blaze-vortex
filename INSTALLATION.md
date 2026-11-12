# Installation

## Choose an artifact

- **1.19.x, 1.20.x, 1.21.x through 1.21.11** (Bukkit, Spigot, or Paper): `BlazeVortex-legacy-1.0.0.jar`
- **Paper 26.1, 26.2, 26.3**: `BlazeVortex-paper-1.0.0.jar` and **Java 25**

Paper 26.3 is documented by Paper as requiring Java 25. The Paper artifact is compiled with `--release 25`.

## Server steps

1. Stop the server.
2. Copy **one** BVX JAR into `plugins/`.
3. Start the server once so `plugins/BlazeVortex/` is created.
4. Place media under `plugins/BlazeVortex/content/` (see the tree in the main README).
5. Edit `emojis.yml`, `holograms.yml`, `items.yml` as needed.
6. Install FFmpeg if you will use MP4/MOV/MP3/WAV conversion.
7. Run `/bvx pack build`. Serve `plugins/BlazeVortex/resourcepack/exports/BlazeVortex-pack.zip` over HTTPS.
8. Set `resource-pack.url` in `config.yml` to that HTTPS URL and `/bvx pack send` or enable `auto-send`.

## FFmpeg

Video screens and MP3/WAV conversion **do not work** without FFmpeg. The plugin starts without it and reports the limitation in `/bvx status`.

```text
ffmpeg.path: ffmpeg
```

Use an absolute path if `ffmpeg` is not on PATH. `ffprobe` is expected next to it.

## Resource pack hosting

Minecraft clients download a ZIP from a URL. BVX generates the ZIP and SHA-1; it does not ship a public file server. Put the export on nginx, Caddy, or a CDN, then set the URL.

## Soft dependencies

None are required. If present, BVX will hook:

- PlaceholderAPI
- Vault (detected only)
- WorldGuard (detected only)

ProtocolLib is **not** required and is not used for core features.
