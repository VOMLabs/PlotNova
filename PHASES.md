# PlotNova — Migration Plan

PlotNova is a Paper-first fork of PlotSquared. This document tracks the migration from Bukkit APIs to Paper-native equivalents.

---

## Infrastructure Changes

* **Paper Version**: Upgraded from 1.21.1 to 1.21.7 (minimum for Dialog API support)
* **bStats → FastStats**: Replaced bStats with FastStats for metrics collection. Includes error tracking and general statistics (MC version, OS, Java).
* **paper-plugin.yml**: Rewritten with YAML best practices — consistent quoting, logical section grouping, comments for readability.

---

## Status

| #  | Phase                       | Status        |
| -- | --------------------------- | ------------- |
| 1  | Rename & Restructure        | ✅ Done        |
| 2  | Replace Bukkit APIs         | ✅ Done        |
| 3  | Adventure Everywhere        | ✅ Done        |
| 4 | Paper Dialog API | ✅ Done |
| 5  | Paper Player API            | ⏳ Pending     |
| 6  | World & Chunk               | ⏳ Pending     |
| 7  | Performance                 | ⏳ Pending     |
| 8  | Configuration               | ⏳ Pending     |
| 9  | API Cleanup                 | ⏳ Pending     |
| 10 | Dependency Cleanup          | ⏳ Pending     |
| 11 | Integrations                | ⏳ Pending     |
| 12 | Language & Config Migration | ⏳ Pending     |

---

## Phase 1 — Rename & Restructure ✅

Renamed the Bukkit module to Paper and updated all project references.

**Changes:**

* `Bukkit/` → `paper/`, `Core/` → `shared/`
* Gradle modules: `plotsquared-bukkit` → `plotnova-paper`, `plotsquared-core` → `plotnova-core`
* Root project: `PlotSquared` → `PlotNova`
* Group ID: `com.intellectualsites.plotsquared` → `com.plotnova`
* Package: `com.plotsquared.bukkit.*` → `com.plotsquared.paper.*`
* `plugin.yml` → `paper-plugin.yml` (Paper format)
* Key classes renamed: `BukkitPlatform` → `PaperPlatform`, `BukkitCommand` → `PaperCommand`, etc.
* `BukkitWorld` → `PlatformWorld` (avoids WorldEdit conflict)

---

## Phase 2 — Replace Bukkit APIs ✅

### 2.1 — Plugin Lifecycle

* Removed all `PaperSupport.isPaper()` conditionals (always-true now)
* Simplified `PaperSupport.java` to Paper-only utilities
* Added startup validation for optional integrations

### 2.2 — Scheduler

* Replaced 7 direct `Bukkit.getScheduler()` calls with `TaskManager` abstraction
* Updated `PaperPlatform.java`, `UpdateUtility.java`, `PaperUtil.java`

### 2.3 — Events

* Deleted `SpigotListener.java` (unused, not registered)
* Moved beacon effect handler to `EntityEventListener.java`

### 2.4 — Commands

* Skipped — `CommandExecutor`/`TabCompleter` is the correct pattern for Paper commands

### 2.5 — Player Interactions

* Replaced 14 synchronous `player.teleport()` calls with `PaperSupport.teleportAsync()`
* Files: `PlayerEventListener.java`, `PaperSetupUtils.java`, `TeleportEntityWrapper.java`
* Used `PlayerTeleportEvent.TeleportCause.PLUGIN` for all async teleports

---

## Phase 3 — Adventure Everywhere ✅

Converted all messaging to Adventure Components.

**Changes:**

* Replaced `ChatColor.translateAlternateColorCodes` with `LegacyComponentSerializer` (ampersand/section) in `PaperInventoryUtil.java` and `StateWrapper.java`
* Removed `BukkitAudiences` bridge — Paper's `Player` already implements `Audience`
* `PaperPlayer.getAudience()` now returns `this.player` directly
* `PaperPlatform.consoleAudience()` now uses `server.getConsoleSender()` (Paper-native)
* Replaced Bukkit `Sound`/`SoundCategory` with Adventure `Sound`/`Sound.Emitter` in `PaperPlayer.playMusic()`
* Removed `adventure-platform-bukkit` dependency (Paper bundles Adventure natively)
* Removed Adventure relocation from shadow config
* Cleaned up unused imports (`MinecraftVersion`, `ChatColor`, `BukkitAudiences`)

**Files modified:** `PaperUtil.java`, `PaperPlayer.java`, `PaperPlatform.java`, `PaperInventoryUtil.java`, `StateWrapper.java`, `paper/build.gradle.kts`, `libs.versions.toml`

---

## Phase 4 — Paper Dialog API ⏳ In Progress

Replace inventory menus and chat confirmations with Paper's built-in Dialog API.

### 4.1 — Core Dialog Utilities ✅

* Created `PaperDialogUtil.java` — utility class for creating and showing dialogs
* Created `PaperConfirmationHandler.java` — Paper-specific handler using Dialog API
* Created `ConfirmationHandler.java` — functional interface in shared module
* Updated `CmdConfirm.java` to support platform-specific handlers
* Upgraded Adventure from 4.26.1 to 5.2.0 (required for ClickCallback.Options)
* Fixed Adventure 5.x breaking changes: `ClickEvent.Action` changed from enum to abstract class

### 4.2 — Confirmation System ✅

* `CmdConfirm.addPending()` now delegates to `ConfirmationHandler` when available
* Paper module registers `PaperConfirmationHandler` during initialization
* Falls back to text-based confirmation on non-Paper servers
* `/plot confirm` command unchanged — still works with existing flow

### 4.3 — Inventory GUI Conversion ⏳ Pending

* Convert Component Preset (`/plot components`) to multi-action dialog
* Convert Music/Jukebox (`/plot music`) to multi-action dialog
* Convert Rating (`/plot rate`) to notice dialog

**Files created:** `PaperDialogUtil.java`, `PaperConfirmationHandler.java`, `ConfirmationHandler.java`
**Files modified:** `CmdConfirm.java`, `PaperPlatform.java`, `libs.versions.toml`, `ClickEvent.Action` fixes in shared module

---

## Phase 5 — Paper Player API ⏳

Replace Bukkit player interactions with Paper-specific features.

* Use Paper's `PlayerProfile` API for UUID resolution and skin data
* Use Paper inventory improvements (where not suitable for dialogs)
* Use Paper teleport causes/options exclusively
* Use Adventure sound/title/boss bar/book APIs
* Use Paper resource pack API and particle builders

**Files:** `PaperPlayer.java`, `PaperEntityUtil.java`, all listener classes

---

## Phase 6 — World & Chunk ⏳

Modernize world interaction with Paper APIs.

* Async chunk loading via Paper APIs
* Paper heightmap, biome, and world border APIs
* Paper snapshot APIs for efficient block state manipulation
* Async world creation with proper failure handling

**Files:** `PaperChunkManager.java`, `PaperRegionManager.java`, `PaperWorldManager.java`, `PaperQueueCoordinator.java`

---

## Phase 7 — Performance ⏳

Leverage Paper-specific optimizations.

* Move database queries, plot searches, permission lookups off the main thread
* Folia-compatible region scheduler abstractions
* Replace synchronized legacy collections, reduce allocations
* Cache immutable Components

**Files:** `PaperTaskManager.java`, `PaperChunkManager.java`, `PaperRegionManager.java`, all listener classes

---

## Phase 8 — Configuration ⏳

Modernize configuration loading and validation.

* Typed configuration objects with startup validation
* Paper configuration APIs and migration system
* Configuration validation rules with clear error reporting
* Dialog integration configuration

**Files:** Configuration loading classes, Settings classes, `PaperPlatform.java`

---

## Phase 9 — API Cleanup ⏳

Remove Bukkit-specific wrappers and clean up the API surface.

* Remove `BukkitPlayer`, `BukkitWorld`, `BukkitLocation` wrappers
* Remove dialog abstractions that emulate inventories/chat prompts
* Remove command-answer state machines
* Keep public API stable, document breaking changes

**Files:** All wrapper classes, public API interfaces, documentation

---

## Phase 10 — Dependency Cleanup ⏳

Remove obsolete dependencies and ensure minimal footprint.

* Remove CraftBukkit-only utilities and legacy chat libraries
* Remove deprecated inventory GUI libraries
* Audit all remaining dependencies
* Update `libs.versions.toml` with new dependencies

**Files:** `gradle/libs.versions.toml`, `paper/build.gradle.kts`, `shared/build.gradle.kts`

---

## Phase 11 — Integrations ⏳

### 11.1 — FancyWorlds ✅

Added FancyWorlds as a supported world manager via reflection-based integration.

* `FancyWorldsManager.java` checks for API availability at runtime
* Falls back to `PaperWorldManager` if FancyWorlds is not available
* Priority: FancyWorlds → Multiverse-Core → default

**Note:** FancyWorlds API not yet published to Maven. Compile dependency commented out in `libs.versions.toml`.

### 11.2 — MiniPlaceholder ✅

Added MiniPlaceholder as a supported placeholder expansion alongside PlaceholderAPI.

* MiniPlaceholder API v2.3.0 (v3 requires JVM 25+)
* Adventure-native `Tag` return types
* Registered in `PaperPlatform.java` on startup

| Placeholder                      | Description                  |
| -------------------------------- | ---------------------------- |
| `<plotnova_server_plot_count>`   | Total plots on server        |
| `<plotnova_has_plot>`            | Whether player owns any plot |
| `<plotnova_plot_count>`          | Number of plots player owns  |
| `<plotnova_plot_count_world>`    | Plot count in current world  |
| `<plotnova_plot_id>`             | Current plot ID              |
| `<plotnova_plot_owner>`          | Current plot owner name      |
| `<plotnova_plot_count_not_done>` | Count of undid plots         |

Both PlaceholderAPI and MiniPlaceholder can coexist.

### 11.3 — VaultUnlocked & Legacy Vault ⏳

Add dual economy and permission integration support.

* Detect VaultUnlocked first
* Fall back to legacy Vault when VaultUnlocked is unavailable
* Prefer VaultUnlocked for UUID-native, multi-currency, and Folia-compatible functionality
* Keep legacy Vault support for existing server installations
* Both economy and permission handlers support dual detection

### 11.4 — World Manager Compatibility ⏳

Formalize world manager integration and detection.

* FancyWorlds
* Multiverse-Core
* PlotNova's built-in Paper world manager
* Runtime detection with deterministic priority
* Isolate integration-specific logic from core PlotNova functionality

### 11.5 — Placeholder Integration ⏳

Formalize placeholder integrations.

* MiniPlaceholder
* PlaceholderAPI
* Allow both integrations to coexist
* Keep PlotNova's placeholder definitions consistent across both systems
* Ensure integrations are optional and do not prevent PlotNova from starting when unavailable

**Integration Architecture:**

* Detect optional integrations at startup
* Initialize only available integrations
* Keep integration-specific code isolated
* Prefer native/modern integrations where possible
* Provide safe fallbacks where legacy integrations remain supported

---

## Phase 12 — Language & Config Migration ⏳

Migrate language files from JSON to YAML and introduce a proper configuration system.

### 12a — Language File Migration

* Convert `shared/src/main/resources/lang/messages_en.json` to `messages_en.yml`
* Update all code that loads/references language files
* YAML provides better readability, comments, and less visual noise

### 12b — Configuration System

* Add a `config.yml` for general PlotNova settings (separate from PlotSquared's `settings.yml`)
* Typed configuration objects with defaults
* Startup validation with clear error messages
* Hot-reload support where possible

**Files:** `shared/src/main/resources/lang/`, configuration loading classes, `PaperPlatform.java`

---

## External Dependencies

| Dependency                 | Type        | Version | Repository                                           |
| -------------------------- | ----------- | ------- | ---------------------------------------------------- |
| FancyWorlds                | compileOnly | TODO    | `https://repo.fancyinnovations.com/releases`         |
| MiniPlaceholder            | compileOnly | 2.3.0   | Maven Central                                        |
| VaultUnlocked              | compileOnly | 2.16    | `https://repo.codemc.io/repository/creatorfromhell/` |
| Vault (legacy)             | compileOnly | 1.7.1   | JitPack                                              |
| adventure-text-minimessage | compileOnly | 4.26.1  | Maven Central                                        |

**Vault Support:** PlotNova detects VaultUnlocked first (UUID-native, multi-currency, Folia-compatible), then falls back to legacy Vault. Both economy and permission handlers support dual detection.
