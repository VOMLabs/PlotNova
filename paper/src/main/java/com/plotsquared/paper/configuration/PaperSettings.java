/*
 * PlotSquared, a land and world management plugin for Minecraft.
 * Copyright (C) IntellectualSites <https://intellectualsites.com>
 * Copyright (C) IntellectualSites team and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.plotsquared.paper.configuration;

import com.plotsquared.core.configuration.Config;

import java.io.File;
import java.util.List;

/**
 * Paper-specific configuration values.
 * <p>
 * Follows the same {@link Config} base class pattern as {@link Settings} for
 * serialization to a dedicated {@code paper-settings.yml} file.
 * Contains settings that only apply to the Paper platform (dialog API,
 * FancyWorlds integration, MiniPlaceholder, etc.).
 */
public class PaperSettings extends Config {

    // ---- General Paper settings ----

    @Comment("Paper-specific configuration for PlotNova")
    @Create
    public static Paper_Components PAPER = null;

    @Comment("Dialog API settings (Paper Adventure dialog)")
    @Create
    public static Dialog DIALOG = null;

    @Comment("FancyWorlds integration settings")
    @Create
    public static FancyWorlds FANCYWORLDS = null;

    @Comment("MiniPlaceholder integration settings (Adventure-native placeholder API)")
    @Create
    public static MiniPlaceholderConfig MINI_PLACEHOLDERS = null;

    public static void save(File file) {
        save(file, PaperSettings.class);
    }

    public static void load(File file) {
        load(file, PaperSettings.class);
    }

    // ---- Paper Components section ----

    @Comment("Paper-specific component toggles")
    public static final class Paper_Components {

        @Comment("Enable Paper's async tab completion feature")
        public static boolean ASYNC_TAB_COMPLETION = true;

        @Comment("Enable Paper's tick shuffle for entity ticking order randomisation")
        public static boolean TICK_SHUFFLE = false;

        @Comment("Enable Paper's optimised entity tracking")
        public static boolean ENTITY_TRACKING_OPTIMISATION = true;

    }

    // ---- Dialog section ----

    @Comment("Controls for the Paper Adventure Dialog API integration")
    public static final class Dialog {

        @Comment("Use Paper's Dialog API for confirmation prompts instead of chat-based prompts")
        public static boolean ENABLED = true;

        @Comment("Fallback to chat-based confirmation when the Dialog API is unavailable")
        public static boolean FALLBACK_TO_CHAT = true;

        @Comment("The timeout in seconds before a dialog-based confirmation expires (0 = no timeout)")
        public static int TIMEOUT_SECONDS = 30;

    }

    // ---- FancyWorlds section ----

    @Comment("FancyWorlds / FancyRanks integration settings")
    public static final class FancyWorlds {

        @Comment("Enable FancyWorlds integration for per-world plot visuals")
        public static boolean ENABLED = false;

        @Comment("The FancyWorlds API key (leave empty if unused)")
        public static String API_KEY = "";

        @Comment("Sync plot border visuals with FancyWorlds when available")
        public static boolean SYNC_BORDERS = true;

    }

    // ---- MiniPlaceholder section ----

    @Comment("MiniPlaceholder plugin integration settings")
    public static final class MiniPlaceholderConfig {

        @Comment("Enable MiniPlaceholder expansion registration")
        public static boolean ENABLED = true;

        @Comment("Cache MiniPlaceholder results for this many ticks (0 = no caching)")
        public static int CACHE_TICKS = 20;

        @Comment({"List of MiniPlaceholder tags to expose to PlotSquared",
                "Use '*' to expose all tags, or list specific tags like 'player_name'"})
        public static List<String> EXPOSED_TAGS = List.of("*");

    }

}
