# Audio engine

Upload `mp3`, `wav`, or `ogg` into `content/sounds/`.

Vanilla clients play **resource-pack sounds**, typically OGG Vorbis. BVX converts MP3/WAV with FFmpeg (`libvorbis`) into `cache/sounds/<name>.ogg` and writes `sounds.json` as `blazevortex:<name>`.

It does **not** stream raw MP3 to the client.

```text
/bvx put sound <filename>     # persistent source at the targeted block
/bvx sound play <filename>
/bvx sound stop <filename>
/bvx sound list
/bvx sound info <filename>
```

Placed sources store `data/sound-sources.json` (world, xyz, volume, pitch, radius, loop). Looping is approximated by periodic replay of the namespaced sound; true client-side seamless loops depend on the sound length and resource-pack event.
