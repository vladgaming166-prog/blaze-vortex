# Compatibility matrix

**Legend**

- **Engine** — Java unit/integration tests in this repo
- **API compile** — module compiles against that Paper/Spigot API
- **In-game** — observed on a running server **and** vanilla client of that version

This repository's CI/agent environment compiled the JARs and ran the automated engine tests. It did **not** start Paper/Spigot servers or Minecraft clients. Rows marked in-game `no` are therefore **unverified in-game**.

| Platform | Versions | Artifact | Java | Engine tests | API compile | In-game |
|---|---|---|---|---|---|---|
| Bukkit / Spigot / Paper | 1.19.0–1.19.3 | legacy | 17 | yes | via 1.21.1 paper-api + armor-stand fallback | no |
| Bukkit / Spigot / Paper | 1.19.4 | legacy | 17 | yes | display-entity classes present | no |
| Bukkit / Spigot / Paper | 1.20.x | legacy | 17 | yes | yes | no |
| Paper / Spigot | 1.21.0–1.21.10 | legacy | 17 | yes | compiled against `1.21.1-R0.1-SNAPSHOT` | no |
| Paper | 1.21.11 | legacy | 17 | pack format 75 known | not compiled against 1.21.11 API | no |
| Paper | 26.1.x | paper | 25 | pack format 84 known | compiled against 26.3 API, `api-version: 26.1` | no |
| Paper | 26.2 | paper | 25 | pack format 88 known | same | no |
| Paper | 26.3 | paper | 25 | pack format 97 known | `26.3.build.248-stable` | no |

## Feature notes by generation

| Feature | 1.19.0–1.19.3 | 1.19.4–1.21.11 | Paper 26.x |
|---|---|---|---|
| `<name>` emoji parser + pack fonts | yes | yes | yes |
| Armor-stand holograms | yes | fallback | unused if TextDisplay exists |
| TextDisplay / ItemDisplay | no (class isolated) | yes | yes |
| Map image / video screens | yes | yes | yes |
| Paper AsyncChatEvent | Paper only | Paper only | yes |
| Spigot chat | AsyncPlayerChatEvent | same | N/A |
| Item model components | no | 1.21.4+ detected at runtime | detected |
| FFmpeg pipelines | OS dependency | same | same |

Display-entity code lives in `DisplayEntitySupport` and is loaded only after `org.bukkit.entity.TextDisplay` exists, so 1.19.0–1.19.3 servers do not load that class.
