# Resource pack engine

## What BVX generates

`/bvx pack build` writes:

- `plugins/BlazeVortex/resourcepack/generated/` — exploded pack
- `plugins/BlazeVortex/resourcepack/exports/BlazeVortex-pack.zip`
- `sha1.txt`

Previous ZIPs are renamed to `*.zip.bak` rather than overwritten in place.

Contents:

- `pack.mcmeta` with `pack_format` for the running Minecraft version plus `supported_formats` / `min_format` / `max_format`
- `assets/blazevortex/font/emoji.json` bitmap providers (one glyph per emoji, extra glyphs per GIF frame)
- `assets/blazevortex/textures/font/emoji/*.png`
- `assets/blazevortex/sounds.json` and OGG files for converted audio

Namespace: `blazevortex`.

## Font animation limitation

Minecraft **font bitmaps do not animate** through `.png.mcmeta`. BVX therefore:

1. Exports each GIF frame as its own PNG + PUA glyph
2. Cycles glyphs in holograms / other updatable UIs
3. Also writes `.png.mcmeta` animation metadata for **item/display textures**, which the client can animate

Chat messages are snapshots: the glyph inserted is the frame current at send time.

## Delivery

Set `resource-pack.url` to an HTTPS URL of the ZIP. `/bvx pack send` pushes URL + SHA-1. Empty URL logs a warning and does not pretend the client received the pack.
