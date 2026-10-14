# PlotNova — Migration Plan

PlotNova is a Paper-first fork of PlotSquared. This document tracks the migration from Bukkit APIs to Paper-native equivalents.

---

## Status

| #  | Phase                       | Status      | Commit      |
| -- | --------------------------- | ----------- | ----------- |
| 1  | Rename & Restructure        | ✅ Done      | —           |
| 2  | Replace Bukkit APIs         | ✅ Done      | —           |
| 3  | Adventure Everywhere        | ✅ Done      | —           |
| 4  | Paper Dialog API            | ✅ Done      | —           |
| 5  | Paper Player API            | ✅ Done      | —           |
| 6  | World & Chunk               | ✅ Done      | `43d09327e` |
| 7  | Performance                 | ✅ Done      | `97d8f2a26` |
| 8  | Configuration               | ✅ Done      | `e2f7dc310` |
| 9  | API Cleanup                 | ✅ Done      | `e2f7dc310` |
| 10 | Dependency Cleanup          | ⏳ Pending   | —           |
| 11 | Integrations                | ✅ Done      | —           |
| 12 | Language & Config Migration | ⏳ Pending   | —           |
| 13 | Dialog Confirmations        | 🔮 Future    | —           |
| 14 | ServiceIO Integration        | 🔮 Future    | —           |

> **Plugin format**: PlotNova uses Bukkit `plugin.yml` format (not `paper-plugin.yml`) because Paper's classloader isolation prevents Paper plugins from accessing Bukkit plugin classes (WorldEdit, JNBT) at runtime. All Paper APIs (Dialog, Adventure, etc.) still work — they're available to all plugins through `paper-api`.

---

## Phase 1 — Rename & Restructure ✅

* `Bukkit/` → `paper/`, `Core/` → `shared/`
* Gradle modules: `plotsquared-bukkit` → `plotnova-paper`, `plotsquared-core` → `plotnova-core`
* Root project: `PlotSquared` → `PlotNova`
* Group ID: `com.intellectualsites.plotsquared` → `com.plotnova`
* Package: `com.plotsquared.bukkit.*` → `com.plotsquared.paper.*`
* Plugin descriptor: `plugin.yml` (Bukkit format, required for WorldEdit classloader access)
* Key classes renamed: `BukkitPlatform` → `PaperPlatform`, `BukkitWorld` → `PlatformWorld`

---

## Phase 2 — Replace Bukkit APIs ✅

* Removed all `PaperSupport.isPaper()` conditionals
* Replaced 7 direct `Bukkit.getScheduler()` calls with `TaskManager`
* Deleted unused `SpigotListener.java`
* Replaced 14 synchronous `player.teleport()` with async teleport

---

## Phase 3 — Adventure Everywhere ✅

* Replaced `ChatColor.translateAlternateColorCodes` with `LegacyComponentSerializer`
* Removed `BukkitAudiences` bridge — Paper's `Player` implements `Audience` natively
* Replaced Bukkit `Sound`/`SoundCategory` with Adventure `Sound`/`Sound.Emitter`
* Removed `adventure-platform-bukkit` dependency

---

## Phase 4 — Paper Dialog API ✅

Replaced chest GUIs and chat-based confirmations with Paper's native Dialog API.

### Core

* Created `DialogFactory` / `DialogFactoryHolder` for platform-specific dialog hooks
* Created `ConfirmationHandler` for `CmdConfirm` dialog integration
* Upgraded Adventure from 4.26.1 to 5.2.0 (required for `ClickCallback.Options`)
* Fixed Adventure 5.x breaking changes (`ClickEvent.Action` is no longer an enum)

### Converted GUIs

| Command            | Before         | After                |
| ------------------ | -------------- | -------------------- |
| `/plot components` | Chest GUI      | Multi-action dialog  |
| `/plot music`      | Chest GUI      | Multi-action dialog  |
| `/plot rate`       | Chest GUI      | Multi-action dialog  |
| `/plot setup`      | Chat prompts   | Multi-action dialog  |

All dialogs fall back to PlotInventory/chat on non-Paper servers.

---

## Phase 5 — Paper Player API ✅

Replaced deprecated Bukkit player interactions with Paper-native equivalents.

* `player.kickPlayer(String)` → `player.kick(Component)` (Adventure)
* `player.stopSound(SoundCategory.MUSIC)` → annotated as deprecated (no Paper replacement yet)
* `player.getLocale()` → `player.locale()` (Paper's non-deprecated method)
* `player.getTargetBlock(null, 7)` → `player.getTargetBlockExact(7)` (non-deprecated)
* `getItemInHand()` → `getItemInMainHand()` (non-deprecated)
* `event.getEventName().equals("InventoryCreativeEvent")` → `instanceof InventoryCreativeEvent`
* Removed `player.updateInventory()` (no-op in Paper)

---

## Phase 6 — World & Chunk ✅

* `Chunk.unload(boolean)` → `World.unloadChunk(Chunk)` (non-deprecated) in ChunkListener.java
* Removed manual chunk-by-chunk unloading in `PaperSetupUtils.unload()` — Paper handles this internally
* `World.refreshChunk()` annotated as deprecated (no Paper replacement in 1.21.7 API)
* Async chunk loading already uses Paper's native `world.getChunkAtAsync()` via `PaperSupport`
* Chunk ticket system (`addPluginChunkTicket`/`removePluginChunkTicket`) is already Paper-native

---

## Phase 7 — Performance ✅

Reduced synchronization overhead, cached Component allocations, and improved concurrency.

### Component Caching

* `MiniPlaceholderExpansion`: Cached `Component.empty()`, `Component.text("0")`, `Component.text("false")` as static finals
* `PaperDialogUtil`: Cached `Component.text("Yes")`, `Component.text("No")` button labels
* `PaperDialogFactory`: Cached `Component.text("Back")`, `Component.text("Cancel")` button labels
* `PaperConfirmationHelper`: Cached title, message, and command components

### Synchronized Removal

* `SQLManager`: Removed `synchronized` from 7 methods — replaced check-then-act patterns with `computeIfAbsent` on `ConcurrentHashMap`
* `ScatteredPlotWorld`: Replaced `Object treeLock` + `synchronized` with `ReentrantReadWriteLock` — read operations (hot path) now allow concurrency, writes are exclusive

---

## Phase 8 — Configuration ✅

Startup validation and Paper-specific configuration.

### Implemented

* Created `ConfigValidator.java` — validates ~22 numeric `Settings` fields at startup with min/max range checks and clear error reporting
* Created `PaperSettings.java` — Paper-specific configuration following same `Config` base class pattern as `Settings.java`, serializes to `paper-settings.yml`
* Added config migration support in `PlotSquared.java` (handles renamed/removed fields gracefully)
* Added PaperSettings loading in `PaperPlatform.onEnable()`
* Server startup log: `[PlotSquared/ConfigValidator] Configuration validation passed — no issues found`

### Configuration Flow

1. `Settings.java` loads `config.yml` (shared)
2. `PaperSettings.java` loads `paper-settings.yml` (Paper-specific)
3. `ConfigValidator` runs at startup, checks all numeric fields
4. Migrations run for any renamed/removed config keys

---

## Phase 9 — API Cleanup ✅

Removed deprecated web interface, legacy inventory utilities, and dead code.

### Removed

| What                          | Why                                           |
| ----------------------------- | --------------------------------------------- |
| `Settings.Web`                | `@Deprecated(forRemoval = true)` — legacy web interface |
| `PlotInventory.java`          | Legacy inventory wrapper, replaced by Paper Dialog API |
| `InventoryUtil.java`          | Legacy inventory utility, unused after PlotInventory removal |
| `PaperInventoryUtil.java`     | Paper-specific inventory utility, unused after PlotInventory removal |
| `SchematicHandler.upload()`   | Legacy web upload to plotsquared.com (site sunset) |
| `WorldUtil.upload()`          | Legacy world upload to web interface |
| `PERMISSION_DOWNLOAD_WORLD`   | Permission constant for removed download command |
| `Download` world subcommand   | Removed — only schematic upload remains |
| `FaweSchematicHandler.upload()` | Override for removed `SchematicHandler.upload()` |
| `Web.URL` reference in `PlotSquared.java` | Dead code from removed `Settings.Web` |

### Fixed

* `PaperPlatform.java`: Removed `Web.URL` reference from config load logging
* `PaperModule.java`: Removed `PlotInventory` binding
* `PlayerEventListener.java`: Removed `PlotInventory` import
* `FaweSchematicHandler.java`: Removed `upload()` override and unused imports
* Javadoc: Fixed references to removed `PERMISSION_DOWNLOAD_WORLD`
* `DialogFactory.java`: Updated imports (removed inventory-related references)
* `ComponentPresetManager.java`: Removed legacy inventory fallback code
* `ComponentCommand.java`: Removed legacy inventory fallback code

---

## Phase 10 — Dependency Cleanup ⏳

* Remove CraftBukkit-only utilities and legacy chat libraries
* Remove deprecated inventory GUI libraries
* Audit all remaining dependencies

---

## Phase 11 — Integrations ✅

### 11.1 — FancyWorlds ✅

* Reflection-based integration (API not yet published to Maven)
* Falls back to `PaperWorldManager` if FancyWorlds is not available
* Priority: FancyWorlds → Multiverse-Core → default

### 11.2 — MiniPlaceholder ✅

* MiniPlaceholder API v2.3.0 (v3 requires JVM 25+)
* Adventure-native `Tag` return types
* Both PlaceholderAPI and MiniPlaceholder can coexist

### 11.3 — VaultUnlocked & Vault ✅

* Detect VaultUnlocked first, fall back to legacy Vault
* UUID-native, multi-currency, Folia-compatible via VaultUnlocked
* Both economy and permission handlers support dual detection

---

## Phase 12 — Language & Config Migration ⏳

* Convert `messages_en.json` → `messages_en.yml` (or keep JSON — Paper supports both)
* Add `config.yml` for general PlotNova settings (partially done — `PaperSettings` handles Paper-specific settings)
* Typed configuration objects with defaults (done for `PaperSettings`)
* Startup validation with clear error messages (done — `ConfigValidator`)
* Remaining: Migrate any remaining Bukkit-style config handling to Paper-native equivalents

---

## Phase 13 — Dialog Confirmations 🔮 Future

Replace the text-based `/plot confirm` flow with native Paper Dialog confirmation prompts.

Currently, destructive commands (delete, merge, unlink, trust, etc.) require the player to type `/plot confirm` after a chat message. This phase would:

* Show a confirmation dialog (Yes/No) instead of a chat prompt when the player runs a destructive command
* Use `DialogType.confirmation()` with Yes/No buttons
* Maintain the same timeout and expiry logic
* Remove the need for players to manually type `/plot confirm`
* Keep the underlying confirmation system and command flow intact

This is a quality-of-life improvement, not a functional change. The existing `ConfirmationHandler` hook already supports this — it just needs the confirmation UI to be wired through `CmdConfirm` properly.

---

## Phase 14 — ServiceIO Integration 🔮 Future - ONLY IF INCOMPATIBLE

Integrate ServiceIO for structured service dependency management and lifecycle control.

> ServiceIO is already compatible with PlotNova's Vault-based/VaultUnlocked-based architecture and will provide a standardized abstraction for service discovery, registration, dependency resolution, and lifecycle management.

* Register PlotNova services through ServiceIO
* Use ServiceIO for service discovery and dependency resolution
* Integrate economy and permission services through ServiceIO
* Replace manual Vault/VaultUnlocked detection where appropriate
* Replace FancyWorlds reflection-based integration with ServiceIO service resolution where supported
* Integrate placeholder and other optional services through ServiceIO
* Add or contribute missing ServiceIO support where required for PlotNova integrations
* Ensure graceful degradation when optional services are unavailable

---

## Dependencies

| Dependency      | Type        | Version | Repository                           |
| --------------- | ----------- | ------- | ------------------------------------ |
| FancyWorlds     | compileOnly | TODO    | `https://repo.fancyinnovations.com`  |
| MiniPlaceholder | compileOnly | 2.3.0   | Maven Central                        |
| VaultUnlocked   | compileOnly | 2.16    | `https://repo.codemc.io`            |
| Vault (legacy)  | compileOnly | 1.7.1   | JitPack                              |
| Adventure       | compileOnly | 5.2.0   | Maven Central (via Paper)            |
| WorldEdit       | compileOnly | 7.2.20  | `https://maven.enginehub.org/repo/` |
| FAWE            | compileOnly | 2.15.3  | `https://ci.athion.net`              |

### Build & Dev

| Tool            | Version | Purpose                                    |
| --------------- | ------- | ------------------------------------------ |
| Paper API       | 1.21.7  | Server API (compileOnly)                   |
| run-paper       | latest  | Dev server runner (`./gradlew runDevServer`)|
| Shadow          | latest  | JAR shading                                |
| Spotless        | latest  | Code formatting                            |

### Runtime Notes

* **Paper version**: 1.21.7 (upgraded from 1.21.1 for Dialog API support)
* **Adventure version**: 5.2.0 (upgraded from 4.26.1 — required for `ClickCallback.Options`)
* **Java target**: 21 (Paper 26.x requires JVM 25+, cannot upgrade beyond 1.21.x on JVM 21)
* **MiniPlaceholder**: v2.3.0 (v3 requires JVM 25+)
* **Plugin format**: Bukkit `plugin.yml` (required — Paper `paper-plugin.yml` classloader isolation prevents WorldEdit access)
