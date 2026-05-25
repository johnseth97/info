# Info

Lightweight server-side WAILA/Jade-style plugin for Paper. Shows players what block or entity they are looking at via the actionbar. No client mod required.

## Requirements

- Paper 1.21.4 (API 1.21.4-R0.1-SNAPSHOT)
- Java 21

## Build

```bash
./gradlew clean build
```

Output: `build/libs/Info-1.0.0.jar`

## Release

Publishing runs via GitHub Actions on tag push (`vX.Y.Z`) or the manual **Publish** workflow. The workflow creates a GitHub Release with the built jar attached.

To enable Modrinth publishing, set repository variable `MODRINTH_PROJECT_ID` and secret `MODRINTH_TOKEN`. When present, the same jar is uploaded for Paper 1.21.4.

## Install

```bash
sudo cp build/libs/Info-1.0.0.jar /srv/minecraft/paper/plugins/
sudo chown minecraft:minecraft /srv/minecraft/paper/plugins/Info-1.0.0.jar
```

Restart the server.

## Commands

| Command | Description | Permission |
|---|---|---|
| `/info` | Show help | `info.use` |
| `/info toggle` | Enable/disable your HUD | `info.toggle` |
| `/info status` | Show current HUD state | `info.use` |
| `/info reload` | Reload config (admin) | `info.reload` |

## Permissions

| Permission | Default | Description |
|---|---|---|
| `info.use` | true | Receive the HUD |
| `info.toggle` | true | Toggle own HUD on/off |
| `info.reload` | op | Reload config |

## Config

`plugins/Info/config.yml` (created on first start):

```yaml
enabled-by-default: true
update-interval-ticks: 5
max-distance: 8.0
prefer-entities: true
display-mode: actionbar

show:
  material-name: true
  namespaced-key: true
  coordinates: true
  biome: false
  light-level: false
  entity-health: true
```

## Example Output

Looking at a block:
```
Oak Log · minecraft:oak_log · 123, 68, -44
```

Looking at an entity:
```
Zombie · minecraft:zombie · Health: 18/20
```
