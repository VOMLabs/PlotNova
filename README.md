# PlotNova

A Paper-first plot management plugin for Minecraft, forked from PlotSquared.

PlotNova replaces Bukkit-specific APIs with Paper-native equivalents, providing better performance, modern Adventure Components, and access to Paper's built-in Dialog API.

## Features

- Plot creation, management, and protection
- Configurable world generators
- Plot merging, trading, and collaboration
- Flag system for weather, time, game modes, PvP, and more
- Placeholder support via PlaceholderAPI and MiniPlaceholder
- World management via Multiverse-Core, Multiverse-NWT, or FancyWorlds

## Requirements

- **Server software:** Paper 1.21.1 or later (Paper-only, no Spigot/Bukkit support)
- **Java:** 21 or later
- **Optional dependencies:** WorldEdit, FAWE, PlaceholderAPI, MiniPlaceholder, LuckPerms, Vault, EssentialsX, Multiverse-Core, FancyWorlds

## Building

```bash
./gradlew build
```

The output JAR will be in `paper/build/libs/`.

## Project Structure

```bash
PlotNova/
├── shared/        # Core module (platform-independent logic)
├── paper/         # Paper module (Paper-specific implementation)
├── gradle/        # Gradle version catalog
├── PHASES.md      # Migration progress tracker
└── build.gradle.kts
```

## Development

PlotNova targets Paper exclusively. All Bukkit/Spigot compatibility code has been removed in favor of Paper-native APIs:

- **Adventure Components** for all messaging (no legacy ChatColor)
- **Paper Dialog API** for UIs (replacing inventory menus)
- **Paper teleport** with `teleportAsync()` and `TeleportCause`
- **Paper scheduler** via `TaskManager` abstraction

See [PHASES.md](PHASES.md) for the full migration plan and progress.

## Contributing

Contributions are welcome. Please:

1. Fork the repository
2. Create a feature branch
3. Submit a pull request

All contributions must compile against Paper 1.21.1+ and follow the existing code style.

## License

PlotNova is licensed under the [GNU General Public License v3.0](LICENSE).
