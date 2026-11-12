# Emoji engine

Usage everywhere BVX renders text:

```text
<fire> Hello <heart>
```

There is no colon syntax.

## Size 1–100

`size` is a **percentage of the image's normalized native rendering size**, not a shared square.

1. Read the source width and height.
2. Keep aspect ratio.
3. At `size: 100`, `native-pixel-reference` source pixels (default 128) map to `reference-height` font units (default 16).
4. Apply `size / 100`.
5. Clamp to `max-glyph-height` / `max-glyph-width` without distortion.
6. `height` and `ascent` in the bitmap font provider come from that result; `y-offset` shifts ascent.

Examples with defaults (reference 16, native 128):

| Source | size | glyph height | glyph width | aspect |
|---|---|---|---|---|
| 128×128 | 100 | 16 | 16 | 1:1 |
| 128×128 | 50 | 8 | 8 | 1:1 |
| 256×64 | 100 | 8 | 32 | 4:1 |
| 64×64 | 100 | 8 | 8 | 1:1 |

A 128×128 image and a 256×64 image **never** collapse to the same rectangle.

Optional per-context overrides (`chat`, `scoreboard`, …) can further cap metrics without changing the stored `size`.

## Parser

Unknown `<tags>` are left intact so MiniMessage (`<red>`, `<bold>`, wrapping `<rainbow>`) and user text keep working. If an emoji named `rainbow` is registered, the self-closing `<rainbow>` tag is that emoji.

## Caching

Image probes are LRU-cached. GIF bitmaps are decoded when packing or when a world GIF display needs frames, not when listing files.
