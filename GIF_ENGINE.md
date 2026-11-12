# GIF engine

Put files in `content/gifs/`.

BVX decodes GIF89a/GIF87a with:

- Original frame order
- Per-frame delay (GIF delay units × 10 ms, minimum 10 ms)
- Disposal methods (none, background, previous)
- Transparency
- NETSCAPE loop count when present

Commands: `/bvx gif list`, `info`, `preview`, `/bvx spawn gif <name>`.

Listing uses a metadata probe and does **not** keep every decoded frame in memory. The bounded GIF cache holds full sequences only when a display or pack build needs them.

Animated emoji GIFs become one font glyph per frame. World GIF displays use map tiles (all versions) or item displays (1.19.4+).
