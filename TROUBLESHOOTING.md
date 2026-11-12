# Troubleshooting

**Spawned image is a blank floating map**  
Older builds spawned an ItemDisplay holding an empty filled map and never painted pixels. Current builds use map item frames. Replace the JAR, look at a block, and run `/bvx spawn image <name>` again.

If it is still blank: `/bvx image list` must show the name; PNG/JPG/WEBP belong in `content/images/`; GIFs belong in `content/gifs/` with `/bvx spawn gif`. Face a solid block.

**Image disappears after a restart**  
Displays are stored in `plugins/BlazeVortex/data/displays.json` and restored about one second after the plugin enables. If that file is missing, spawn the image again on the new JAR.

**Plugin will not load on 1.19**  
Use the **legacy** JAR. The Paper 26 JAR has `api-version: 26.1` and Java 25 bytecode.

**Emojis show as boxes**  
The client did not accept the resource pack, or `pack send` ran with an empty `resource-pack.url`. Check `/bvx pack status` and the player's resource-pack status in the log.

**GIFs do not animate in chat**  
Expected. Font glyphs are static snapshots. Holograms with GIF emojis refresh; world GIF displays animate via maps / display textures.

**`/bvx video play` does nothing visible**  
1. Confirm FFmpeg: `/bvx status`  
2. Spawn a screen first (`/bvx spawn video <name>`)  
3. Wait for async frame extraction  
4. Maps only update for players who can see the item frames

**Upload rejected**  
Magic bytes did not match the extension, the file exceeded `security.max-upload-bytes`, or the path contained `..`.

**Dashboard refused to start**  
`web.bind` is not loopback and `password-hash` is empty. Keep `127.0.0.1` or set a PBKDF2 hash.

**Main thread lag on pack build**  
Pack build is async. Image `spawn` still decodes the chosen file on the server thread — keep source images within the configured caps.

**Glyphs remapped after deleting `data/glyph-registry.yml`**  
That file is the stable PUA map. Restoring a backup is the only way to keep old client caches matching.
