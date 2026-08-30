# PlotNova — Migration Plan

PlotNova is a Paper-first fork of PlotSquared. This document tracks the migration from Bukkit APIs to Paper-native equivalents.

---

## Status

| #  | Phase                       | Status     |
| -- | --------------------------- | ---------- |
| 1  | Rename & Restructure        | ✅ Done     |
| 2  | Replace Bukkit APIs         | ✅ Done     |
| 3  | Adventure Everywhere        | ✅ Done     |
| 4  | Paper Dialog API            | ✅ Done     |
| 5  | Paper Player API            | ⏳ Pending  |
| 6  | World & Chunk               | ⏳ Pending  |
| 7  | Performance                 | ⏳ Pending  |
| 8  | Configuration               | ⏳ Pending  |
| 9  | API Cleanup                 | ⏳ Pending  |
| 10 | Dependency Cleanup          | ⏳ Pending  |
| 11 | Integrations                | ✅ Done     |
| 12 | Language & Config Migration | ⏳ Pending  |
| 13 | Dialog Confirmations        | 🔮 Future   |

---

## Phase 1 — Rename & Restructure ✅

* `Bukkit/` → `paper/`, `Core/` → `shared/`
* Gradle modules: `plotsquared-bukkit` → `plotnova-paper`, `plotsquared-core` → `plotnova-core`
* Root project: `PlotSquared` → `PlotNova`
* Group ID: `com.intellectualsites.plotsquared` → `com.plotnova`
* Package: `com.plotsquared.bukkit.*` → `com.plotsquared.paper.*`
* `plugin.yml` → `paper-plugin.yml` (Paper format)
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

| Command           | Before         | After                |
| ----------------- | -------------- | -------------------- |
| `/plot components` | Chest GUI      | Multi-action dialog  |
| `/plot music`      | Chest GUI      | Multi-action dialog  |
| `/plot rate`       | Chest GUI      | Multi-action dialog  |
| `/plot setup`      | Chat prompts   | Multi-action dialog  |

All dialogs fall back to PlotInventory/chat on non-Paper servers.

---

## Phase 5 — Paper Player API ⏳

Replace Bukkit player interactions with Paper-specific features.

* Paper's `PlayerProfile` API for UUID resolution and skin data
* Paper inventory improvements (where not suitable for dialogs)
* Paper teleport causes/options exclusively
* Adventure sound/title/boss bar/book APIs
* Paper resource pack API and particle builders

---

## Phase 6 — World & Chunk ⏳

* Async chunk loading via Paper APIs
* Paper heightmap, biome, and world border APIs
* Paper snapshot APIs for efficient block state manipulation
* Async world creation with proper failure handling

---

## Phase 7 — Performance ⏳

* Move database queries, plot searches, permission lookups off main thread
* Folia-compatible region scheduler abstractions
* Replace synchronized legacy collections, reduce allocations
* Cache immutable Components

---

## Phase 8 — Configuration ⏳

* Typed configuration objects with startup validation
* Paper configuration APIs and migration system
* Configuration validation rules with clear error reporting
* Dialog integration configuration

---

## Phase 9 — API Cleanup ⏳

* Remove `BukkitPlayer`, `BukkitWorld`, `BukkitLocation` wrappers
* Remove dialog abstractions that emulate inventories/chat prompts
* Remove command-answer state machines
* Keep public API stable, document breaking changes

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

* Convert `messages_en.json` → `messages_en.yml`
* Add `config.yml` for general PlotNova settings
* Typed configuration objects with defaults
* Startup validation with clear error messages

---

## Phase 13 — Dialog Confirmations 🔮 Future

Replace the text-based `/plot confirm` flow with a native Paper Dialog confirmation prompt.

Currently, destructive commands (delete, merge, unlink, trust, etc.) require the player to type `/plot confirm` after a chat message. This phase would:

* Show a confirmation dialog (Yes/No) instead of a chat prompt when the player runs a destructive command
* Use `DialogType.confirmation()` with Yes/No buttons
* Maintain the same timeout and expiry logic
* Fall back to the existing `/plot confirm` text flow on non-Paper servers

This is a quality-of-life improvement, not a functional change. The existing `ConfirmationHandler` hook already supports this — it just needs the confirmation UI to be wired through `CmdConfirm` properly.

---

## Dependencies

| Dependency     | Type        | Version | Repository                           |
| -------------- | ----------- | ------- | ------------------------------------ |
| FancyWorlds    | compileOnly | TODO    | `https://repo.fancyinnovations.com`  |
| MiniPlaceholder | compileOnly | 2.3.0  | Maven Central                        |
| VaultUnlocked  | compileOnly | 2.16    | `https://repo.codemc.io`            |
| Vault (legacy) | compileOnly | 1.7.1   | JitPack                              |
| Adventure      | compileOnly | 5.2.0   | Maven Central (via Paper)            |
