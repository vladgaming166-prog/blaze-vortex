# Commands

`/bvx` and `/blazevortex` are identical. Tab completion is implemented.

| Command | Permission | Behaviour |
|---|---|---|
| `/bvx help` | none | Command list |
| `/bvx version` | none | Plugin and platform version |
| `/bvx reload` | `blazevortex.reload` | Reload configs and rediscover assets |
| `/bvx debug` / `status` | `blazevortex.debug` | Module, asset and FFmpeg status |
| `/bvx emoji list\|info\|reload` | `blazevortex.emoji` | Emoji registry |
| `/bvx spawn image \| gif \| video <name> [w h]` | matching media node | Place a display or video screen at the targeted block |
| `/bvx image list\|info\|resize` | `blazevortex.image` | Image registry / async resize |
| `/bvx gif list\|info\|preview` | `blazevortex.gif` | GIF metadata (does not load every frame into RAM) |
| `/bvx video play\|pause\|stop\|list\|remove` | `blazevortex.video` | Playback on an existing screen |
| `/bvx put sound <name>` | `blazevortex.sound` | Persistent looping source at the target |
| `/bvx sound play\|stop\|list\|info` | `blazevortex.sound` | One-shot / info |
| `/bvx screen list\|info\|remove` | `blazevortex.screen` | Video screens |
| `/bvx pack build\|export\|send\|status` | `blazevortex.pack` | Async pack generation |
| `/bvx hologram list\|reload` | `blazevortex.hologram` | Holograms |
| `/bvx editor` | `blazevortex.gui` | Inventory browser of registered emojis |
| `/bvx content list` | `blazevortex.upload` | Relative paths under `content/` |
| `/bvx upload` | `blazevortex.upload` | Explains file-drop and dashboard upload |

Unauthorized users receive a permission error. Commands never report success for an operation that did not run.
