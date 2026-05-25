# Info Paper Plugin — Agent Handoff Plan

## Goal

Build a small server-side Paper plugin named **Info** that gives players a lightweight WAILA/Jade-style overlay without requiring client mods.

The plugin should ray-trace what a player is looking at and display useful block/entity information in the actionbar. It should be minimal, reliable, configurable, and safe to run on a Paper 1.21.1 server.

Target environment:

- Minecraft server: Paper `1.21.1`
- Java: `21`
- Server type: Paper, not Fabric/Forge/NeoForge
- Deployment target: `/srv/minecraft/paper/plugins`
- Server owner/player name: `DesoTheHusky`

## Non-goals

Do not build a JEI recipe browser.

Do not require client-side mods.

Do not add database storage.

Do not add external plugin dependencies for v1.

Do not make this a large framework. Keep the plugin focused and easy to extend.

## Expected v1 Behavior

When a player looks at a block or entity, show a short actionbar message like:

```text
Oak Log · minecraft:oak_log · 123, 68, -44
```

For entities:

```text
Zombie · minecraft:zombie · Health: 18/20
```

If the player is not looking at anything relevant, clear or omit the actionbar update.

## Suggested Feature Set for v1

Implement:

- Actionbar display for targeted block/entity
- Ray trace from player eye direction
- Configurable max distance
- Configurable update interval
- Per-player toggle
- Permissions
- Reloadable config
- Clean display name formatting
- Basic block fields
- Basic entity fields

Commands:

```text
/info
/info toggle
/info reload
/info status
```

Permissions:

```text
visualid.use       # allows receiving info HUD
visualid.toggle    # allows toggling own HUD
visualid.reload    # allows reloading config
```

Recommended permission defaults:

- `visualid.use`: true
- `visualid.toggle`: true
- `visualid.reload`: op

## Suggested Config

Create `src/main/resources/config.yml`:

```yaml
enabled-by-default: true
update-interval-ticks: 5
max-distance: 8.0
prefer-entities: true

format:
  block: "<name> · <key> · <x>, <y>, <z>"
  entity: "<name> · <key> · Health: <health>/<max_health>"
  no-target: ""

show:
  material-name: true
  namespaced-key: true
  coordinates: true
  biome: false
  light-level: false
  entity-health: true
```

Keep config parsing defensive. If a config value is missing or invalid, fall back to sane defaults.

## Implementation Outline

### Step 1 — Create Project Skeleton

Create a new Gradle Java project for a Paper plugin.

Suggested layout:

```text
Info/
  build.gradle
  settings.gradle
  src/main/java/com/johnseth97/info/InfoPlugin.java
  src/main/java/com/johnseth97/info/command/InfoCommand.java
  src/main/java/com/johnseth97/info/config/InfoConfig.java
  src/main/java/com/johnseth97/info/service/InfoHudService.java
  src/main/java/com/johnseth97/info/service/TargetInfoService.java
  src/main/java/com/johnseth97/info/util/NameUtil.java
  src/main/resources/plugin.yml
  src/main/resources/config.yml
```

Use Java 21.

Use the Paper API for Minecraft 1.21.1.

### Step 2 — Configure Gradle

Use the Paperweight userdev setup if preferred, or a standard Paper API compile-only dependency.

Minimal standard dependency approach is acceptable for this plugin.

Example dependency direction:

```gradle
repositories {
    mavenCentral()
    maven { url = "https://repo.papermc.io/repository/maven-public/" }
}

dependencies {
    compileOnly "io.papermc.paper:paper-api:1.21.1-R0.1-SNAPSHOT"
}
```

Ensure the jar task produces a plugin jar that can be copied into `/srv/minecraft/paper/plugins`.

### Step 3 — Define `plugin.yml`

Create `src/main/resources/plugin.yml`:

```yaml
name: Info
version: 1.0.0
main: com.johnseth97.info.InfoPlugin
api-version: '1.21'
author: johnseth97
description: Lightweight server-side block/entity info HUD for Paper.
commands:
  info:
    description: Toggle or manage the Info HUD.
    usage: /info [toggle|reload|status]
    permission: info.toggle
permissions:
  info.use:
    description: Allows receiving the Info HUD.
    default: true
  info.toggle:
    description: Allows toggling the Info HUD.
    default: true
  info.reload:
    description: Allows reloading the Info config.
    default: op
```

### Step 4 — Main Plugin Class

`InfoPlugin` should:

- Save default config on enable
- Load config into an `InfoConfig` object
- Start the HUD repeating task
- Register `/info`
- Stop/cancel the HUD task on disable

### Step 5 — Config Wrapper

Create `InfoConfig` to centralize config reads.

Fields:

```java
boolean enabledByDefault;
long updateIntervalTicks;
double maxDistance;
boolean preferEntities;
String blockFormat;
String entityFormat;
String noTargetFormat;
boolean showMaterialName;
boolean showNamespacedKey;
boolean showCoordinates;
boolean showBiome;
boolean showLightLevel;
boolean showEntityHealth;
```

Clamp values:

- `updateIntervalTicks`: minimum `1`, recommended default `5`
- `maxDistance`: minimum `1.0`, maximum maybe `32.0`, default `8.0`

### Step 6 — Per-player Toggle State

Use an in-memory `Set<UUID>` for disabled players if `enabled-by-default` is true.

Alternative:

- If enabled by default: set stores disabled users
- If disabled by default: set stores enabled users

For v1, in-memory only is fine. No persistence required unless easy.

### Step 7 — Ray Trace Logic

Implement `TargetInfoService`.

For blocks:

```java
RayTraceResult result = player.rayTraceBlocks(maxDistance);
Block block = result != null ? result.getHitBlock() : null;
```

For entities, use the world ray trace API or player/world ray tracing facilities available in Paper/Bukkit. Prefer an approach that:

- Starts at player eye location
- Uses player direction
- Respects max distance
- Ignores the player themselves
- Avoids selecting entities behind walls if practical

If entity ray tracing is more complex, implement blocks first, then entity support second.

### Step 8 — HUD Scheduler

`InfoHudService` should run every `update-interval-ticks`.

Pseudo-flow:

```java
for (Player player : Bukkit.getOnlinePlayers()) {
    if (!player.hasPermission("visualid.use")) continue;
    if (!isHudEnabled(player)) continue;

    TargetInfo info = targetInfoService.getTargetInfo(player, config);

    if (info == null) {
        sendActionBar(player, config.noTargetFormat());
        continue;
    }

    sendActionBar(player, info.renderedText());
}
```

Use Adventure components:

```java
player.sendActionBar(Component.text(message));
```

Avoid excessive allocation where reasonable, but do not prematurely optimize.

### Step 9 — Formatting

Create a small `NameUtil`:

- Convert `OAK_LOG` to `Oak Log`
- Convert `ZOMBIE_VILLAGER` to `Zombie Villager`
- Use namespaced keys where available, e.g. `minecraft:oak_log`

Create placeholder replacement for configured formats:

Block placeholders:

```text
<name>
<key>
<x>
<y>
<z>
<world>
<biome>
<light>
```

Entity placeholders:

```text
<name>
<key>
<x>
<y>
<z>
<world>
<health>
<max_health>
```

If a placeholder is unavailable, replace it with an empty string or a safe fallback.

### Step 10 — Commands

Implement `/info` as status/help.

Implement `/info toggle`:

- Requires `info.toggle`
- Toggles the current player's HUD state
- Sends confirmation message

Implement `/info status`:

- Shows enabled/disabled state
- Shows distance and update interval

Implement `/info reload`:

- Requires `info.reload`
- Calls `reloadConfig()`
- Rebuilds `InfoConfig`
- Restarts scheduler if interval changed, or design service to read current config safely

### Step 11 — Build and Deploy

Build:

```bash
./gradlew clean build
```

Copy jar to server plugins directory:

```bash
sudo cp build/libs/Info-*.jar /srv/minecraft/paper/plugins/
sudo chown minecraft:minecraft /srv/minecraft/paper/plugins/Info-*.jar
```

Restart Paper:

```bash
cd /srv/minecraft/paper
sudo minecraft-paper restart
```

Check plugin load:

```bash
sudo minecraft-paper rcon "plugins"
sudo minecraft-paper logs | grep -i info
```

### Step 12 — Test Plan

Test in-game as `DesoTheHusky`:

1. Join server.
2. Look at common blocks: stone, grass block, oak log, chest.
3. Confirm actionbar updates.
4. Look away into sky; confirm actionbar clears or stops updating.
5. Run `/info toggle`; confirm it disables.
6. Run `/info toggle`; confirm it enables.
7. Run `/info status`.
8. As op/admin, run `/info reload`.
9. Change config interval/distance and reload.
10. Test entity display with animals/mobs.
11. Confirm no console spam.
12. Confirm TPS remains stable.

Performance sanity:

```bash
sudo minecraft-paper rcon "tps"
sudo minecraft-paper rcon "tickinfo"
```

## Acceptance Criteria

The plugin is complete for v1 when:

- Server starts cleanly with the plugin installed
- `/plugins` shows Info enabled
- `/visualid toggle` works
- `/visualid reload` works for admins
- Players with `visualid.use` see block info in actionbar
- Players without `visualid.use` do not see HUD info
- The HUD updates at the configured interval
- The max distance setting works
- The plugin does not require client mods
- The plugin does not expose web services or require networking
- No repeated errors appear in console

## Future Enhancements

Possible v2 features:

- Bossbar mode
- CoreProtect integration to show placer/breaker info
- LuckPerms context-sensitive advanced info
- Container display: chest/barrel/shulker names
- Spawner entity type
- Sign text preview
- Redstone power level
- Crop growth state
- Bee nest occupancy
- Villager profession and level
- Horse stats
- Region/claim plugin integration
- Per-player persisted preferences
- MiniMessage formatting support

## Initial Agent Prompt

Use the following prompt to start the implementation agent:

```text
You are implementing a new Paper server plugin named Info.

Goal: Build a lightweight server-side WAILA/Jade-style plugin for Paper 1.21.1 that displays information about the block or entity a player is looking at. This must not require client mods.

Environment:
- Java 21
- Paper API 1.21.1
- Plugin name: Info
- Main class: com.johnseth97.info.InfoPlugin
- Target server deploy path: /srv/minecraft/paper/plugins
- Server owner/player name: DesoTheHusky

Implement v1 only:
- Actionbar display
- Ray trace targeted block/entity
- Configurable max distance
- Configurable update interval
- Configurable display format
- Per-player toggle with /info toggle
- /info status
- /info reload
- Permissions: info.use, info.toggle, info.reload
- No database
- No external runtime dependencies
- No client mods

Use this project layout:

Info/
  build.gradle
  settings.gradle
  src/main/java/com/johnseth97/info/InfoPlugin.java
  src/main/java/com/johnseth97/info/command/InfoCommand.java
  src/main/java/com/johnseth97/info/config/InfoConfig.java
  src/main/java/com/johnseth97/info/service/InfoHudService.java
  src/main/java/com/johnseth97/info/service/TargetInfoService.java
  src/main/java/com/johnseth97/info/util/NameUtil.java
  src/main/resources/plugin.yml
  src/main/resources/config.yml

Start by creating the Gradle project and all source files. Keep the code simple, readable, and production-safe. Prefer Paper/Bukkit APIs and Adventure Components. After implementation, provide build and deployment commands for copying the jar into /srv/minecraft/paper/plugins and restarting the server.
```
