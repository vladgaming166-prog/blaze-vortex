# Blaze Vortex (BVX)

**The Ultimate Minecraft Content, Graphics and Multimedia Engine.**

Blaze Vortex is a modular Minecraft plugin for custom emojis, images, animated GIFs, video screens, positional audio, holograms, custom items, cosmetics, and resource-pack generation. It is designed as a successor-style content engine in the same problem space as ItemsAdder and Oraxen, with a public developer API.

Primary commands: `/bvx` and `/blazevortex`  
Main class: `io.github.blazevortex.BlazeVortexPlugin`  
Data folder: `plugins/BlazeVortex/`

## Artifacts

| JAR | Target | Java | Status |
|---|---|---|---|
| `BlazeVortex-legacy-1.0.0.jar` | Bukkit / Spigot / Paper **1.19.x–1.21.11** | 17 | Unit-tested engine; **not** launched on a live 1.19–1.21 client in this repository |
| `BlazeVortex-paper-1.0.0.jar` | Paper **26.1 / 26.2 / 26.3** | 25 | Compiles against Paper API `26.3.build.248-stable`; **not** launched on a live Paper 26 server in this repository |

Never drop both JARs into the same `plugins/` folder.

## What actually works today

Implemented and covered by automated tests (no Minecraft client required):

- Emoji `size: 1–100` with original width/height and aspect-ratio-aware glyph metrics
- Persistent Private Use Area glyph allocation
- PNG/JPEG/WEBP/GIF validation by magic bytes, not extension
- GIF frame extraction, delay preservation, and transparency handling
- Resource-pack ZIP generation (`pack.mcmeta`, bitmap fonts, SHA-1)
- FFmpeg audio conversion (WAV/MP3 → OGG) and video frame extraction when FFmpeg is installed
- Configuration load/merge that does not overwrite administrator YAML
- Command permission checks
- Optional localhost web dashboard for uploads and emoji size edits

Implemented against the Bukkit/Paper API (compiles; needs a running server to observe in-game):

- Chat/hologram/GUI `<name>` emoji replacement
- Image, GIF and video world displays (maps / display entities)
- Persistent video screens and sound sources
- Custom items, cosmetics registry, particle effects
- PlaceholderAPI expansion `%bvx_emoji_<name>%` when PlaceholderAPI is present

Minecraft Java **cannot** play arbitrary MP4, MOV, GIF or MP3 files natively. BVX converts and renders:

- GIF emoji → per-frame font glyphs (dynamic UIs cycle frames; chat is the current glyph at send time)
- GIF/image world displays → maps or item displays
- MP4/MOV → FFmpeg frame extraction + map screens + optional OGG audio
- MP3/WAV → OGG Vorbis in the generated resource pack

## Download

Prebuilt JARs are in [`dist/`](dist/):

- [BlazeVortex-legacy-1.0.0.jar](https://github.com/vladgaming166-prog/blaze-vortex/raw/cursor/blaze-vortex-plugin-9f1a/dist/BlazeVortex-legacy-1.0.0.jar) — Bukkit / Spigot / Paper 1.19–1.21.11 (Java 17)
- [BlazeVortex-paper-1.0.0.jar](https://github.com/vladgaming166-prog/blaze-vortex/raw/cursor/blaze-vortex-plugin-9f1a/dist/BlazeVortex-paper-1.0.0.jar) — Paper 26.1–26.3 (Java 25)

CI also uploads the same files as the `blaze-vortex-jars` artifact on each [Actions run](https://github.com/vladgaming166-prog/blaze-vortex/actions).

## Quick start

1. Install Java 17+ (legacy JAR) or Java 25 (Paper 26 JAR).
2. Copy the matching JAR to `plugins/`.
3. Optional: install [FFmpeg](https://ffmpeg.org/) on the server PATH for video/audio conversion.
4. Start the server, then put files in `plugins/BlazeVortex/content/`.
5. Configure emojis in `emojis.yml`, run `/bvx pack build`, host the ZIP, set `resource-pack.url`, `/bvx pack send`.

See [INSTALLATION.md](INSTALLATION.md) and the rest of the documentation set.

## Build

```bash
./gradlew test
./gradlew :plugin-legacy:shadowJar :plugin-paper:shadowJar
```

Outputs:

- `plugin-legacy/build/libs/BlazeVortex-legacy-1.0.0.jar`
- `plugin-paper/build/libs/BlazeVortex-paper-1.0.0.jar`
