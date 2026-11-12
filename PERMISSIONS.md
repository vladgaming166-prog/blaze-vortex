# Permissions

| Node | Default | Description |
|---|---|---|
| `blazevortex.admin` | op | All administrative children |
| `blazevortex.reload` | op | `/bvx reload` |
| `blazevortex.upload` | op | Content listing and uploads |
| `blazevortex.emoji` | op | Emoji admin |
| `blazevortex.image` | op | Images |
| `blazevortex.gif` | op | GIFs |
| `blazevortex.video` | op | Videos |
| `blazevortex.sound` | op | Sounds |
| `blazevortex.screen` | op | Screens |
| `blazevortex.pack` | op | Resource packs |
| `blazevortex.hologram` | op | Holograms |
| `blazevortex.gui` | op | Editor GUI |
| `blazevortex.debug` | op | Status / debug |
| `blazevortex.use` | true | Replace `<name>` tags in chat |

`blazevortex.admin` is treated as a parent in command checks even if a child node is unset.
