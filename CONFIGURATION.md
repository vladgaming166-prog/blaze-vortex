# Configuration

All files live in `plugins/BlazeVortex/`. Defaults are copied from the JAR on first run. Reloads **do not rewrite** your YAML. Missing keys are applied from bundled defaults in memory and noted in the log.

## config.yml

Important groups:

- `modules.*` — enable or disable each engine (`emoji`, `video`, `web`, …)
- `emoji.reference-height` — font units occupied by a `native-pixel-reference`-tall image at `size: 100` (default 16)
- `emoji.native-pixel-reference` — source pixels that map to `reference-height` at 100% (default 128)
- `security.*` — upload caps, GIF frame cap, video duration, FFmpeg timeout
- `resource-pack.url` — HTTPS URL of the generated ZIP
- `ffmpeg.path`
- `web.enabled` / `web.bind` / `web.port` — dashboard; bind is `127.0.0.1` by default
- `web.password-hash` / `web.password-salt` — required before binding a non-loopback address

## emojis.yml

```yaml
emojis:
  fire:
    file: "emojis/fire.png"
    enabled: true
    size: 100
    y-offset: 0
    animated: false
  loading:
    file: "gifs/loading.gif"
    enabled: true
    size: 75
    y-offset: 0
    animated: true
    fps: 15
    loop: true
```

`size` is an integer **1–100**. Invalid values are rejected, not clamped silently.

Files discovered under `content/` that are not listed here are auto-registered at `size: 100`.

## Other files

- `holograms.yml` — world holograms with `<emoji>` lines
- `items.yml` — custom items
- `cosmetics.yml` / `effects.yml`
- `videos.yml` / `sounds.yml` / `images.yml` — optional metadata overlays
- `integrations.yml` — reserved flags; missing plugins only disable that integration
- `data/glyph-registry.yml` — persistent Unicode PUA map; do not edit while the server is running
