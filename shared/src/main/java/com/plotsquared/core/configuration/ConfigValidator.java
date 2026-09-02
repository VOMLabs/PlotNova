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
package com.plotsquared.core.configuration;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

/**
 * Validates {@link Settings} fields at startup after loading.
 * <p>
 * Checks type ranges and logs warnings for invalid values, resetting them to defaults.
 * This class is designed to wrap around the existing Settings system without replacing it.
 */
public final class ConfigValidator {

    private static final Logger LOGGER = LogManager.getLogger("PlotSquared/ConfigValidator");

    /**
     * The current settings schema version. Bump this when adding or changing
     * validated fields so that migrations can be triggered.
     */
    public static final int SCHEMA_VERSION = 1;

    private ConfigValidator() {
        // utility
    }

    /**
     * Run all validation checks against the loaded {@link Settings} fields.
     * Invalid values are logged as warnings and reset to their compiled defaults.
     *
     * @return the number of issues found and corrected
     */
    public static int validate() {
        final List<String> issues = new ArrayList<>();

        // --- Teleport ---
        validateRange("Teleport.DELAY", Settings.Teleport.DELAY, 0, 1000, 0, issues);

        // --- Confirmation ---
        validateRange("Confirmation.CONFIRMATION_TIMEOUT_SECONDS",
                Settings.Confirmation.CONFIRMATION_TIMEOUT_SECONDS, 1, 300, 20, issues);

        // --- Chunk Processor ---
        validateRange("Chunk_Processor.MAX_TILES", Settings.Chunk_Processor.MAX_TILES, 1, 100000, 4096, issues);
        validateRange("Chunk_Processor.MAX_ENTITIES", Settings.Chunk_Processor.MAX_ENTITIES, 1, 100000, 512, issues);

        // --- Backup ---
        validateRange("Backup.BACKUP_LIMIT", Settings.Backup.BACKUP_LIMIT, 1, 100, 3, issues);

        // --- Claim ---
        validateRange("Claim.MAX_AUTO_AREA", Settings.Claim.MAX_AUTO_AREA, 1, 1000, 4, issues);

        // --- Limit ---
        validateRange("Limit.MAX_PLOTS", Settings.Limit.MAX_PLOTS, 1, 10000, 127, issues);

        // --- Update Checker ---
        validateRange("UpdateChecker.POLL_RATE", Settings.UpdateChecker.POLL_RATE, 1, 100000, 360, issues);

        // --- Titles ---
        validateRange("Titles.TITLES_FADE_IN", Settings.Titles.TITLES_FADE_IN, 0, 1000, 10, issues);
        validateRange("Titles.TITLES_STAY", Settings.Titles.TITLES_STAY, 0, 10000, 50, issues);
        validateRange("Titles.TITLES_FADE_OUT", Settings.Titles.TITLES_FADE_OUT, 0, 1000, 20, issues);

        // --- Queue ---
        validateRange("QUEUE.MAX_ITERATION_TIME", Settings.QUEUE.MAX_ITERATION_TIME, 1, 1000, 30, issues);
        validateRange("QUEUE.INITIAL_BATCH_SIZE", Settings.QUEUE.INITIAL_BATCH_SIZE, 1, 1000, 5, issues);
        validateRange("QUEUE.NOTIFY_INTERVAL", Settings.QUEUE.NOTIFY_INTERVAL, 100, 60000, 5000, issues);
        validateRange("QUEUE.NOTIFY_WAIT", Settings.QUEUE.NOTIFY_WAIT, 0, 60000, 5000, issues);
        validateRange("QUEUE.LIGHTING_MODE", Settings.QUEUE.LIGHTING_MODE, 0, 3, 1, issues);

        // --- Tab Completions ---
        validateRange("Tab_Completions.CACHE_EXPIRATION", Settings.Tab_Completions.CACHE_EXPIRATION, 0, 3600, 15, issues);

        // --- UUID ---
        validateRange("UUID.UUID_CACHE_SIZE", Settings.UUID.UUID_CACHE_SIZE, 1, 10000000, 100000, issues);
        validateRange("UUID.BACKGROUND_LIMIT", Settings.UUID.BACKGROUND_LIMIT, 1, 10000, 200, issues);
        validateRange("UUID.IMPROMPTU_LIMIT", Settings.UUID.IMPROMPTU_LIMIT, 1, 10000, 300, issues);
        validateRange("UUID.NON_BLOCKING_TIMEOUT", Settings.UUID.NON_BLOCKING_TIMEOUT, 1L, 60000L, 3000L, issues);
        validateRange("UUID.BLOCKING_TIMEOUT", Settings.UUID.BLOCKING_TIMEOUT, 1L, 60000L, 10L, issues);

        if (issues.isEmpty()) {
            LOGGER.info("Configuration validation passed — no issues found");
        } else {
            LOGGER.warn("Configuration validation found {} issue(s) which have been reset to defaults:", issues.size());
            for (String issue : issues) {
                LOGGER.warn("  - {}", issue);
            }
        }
        return issues.size();
    }

    /**
     * Validate an int field is within [min, max]. If not, reset it to the default and log.
     */
    private static void validateRange(
            final String path, final int value, final int min, final int max,
            final int defaultValue, final List<String> issues
    ) {
        if (value < min || value > max) {
            LOGGER.warn("Config '{} = {}' is out of range [{}, {}], resetting to {}", path, value, min, max, defaultValue);
            setField(path, defaultValue);
            issues.add(path + " was " + value + ", reset to " + defaultValue);
        }
    }

    /**
     * Validate a long field is within [min, max]. If not, reset it to the default and log.
     */
    private static void validateRange(
            final String path, final long value, final long min, final long max,
            final long defaultValue, final List<String> issues
    ) {
        if (value < min || value > max) {
            LOGGER.warn("Config '{} = {}' is out of range [{}, {}], resetting to {}", path, value, min, max, defaultValue);
            setField(path, defaultValue);
            issues.add(path + " was " + value + ", reset to " + defaultValue);
        }
    }

    /**
     * Reset a Settings field to a given value using reflection, matching the existing
     * {@link Config#set(String, Object, Class)} convention.
     */
    private static void setField(final String path, final Object defaultValue) {
        Config.set(path, defaultValue, Settings.class);
    }

    // ---- Migration support ----

    /**
     * Current config schema version stored in settings.yml.
     * Returns the version stored in the YAML, or {@link #SCHEMA_VERSION} if absent.
     */
    public static int getStoredSchemaVersion(final com.plotsquared.core.configuration.file.YamlConfiguration config) {
        return config.getInt("version-schema", SCHEMA_VERSION);
    }

    /**
     * Persist the schema version into the YAML configuration.
     */
    public static void setStoredSchemaVersion(
            final com.plotsquared.core.configuration.file.YamlConfiguration config, final int version
    ) {
        config.set("version-schema", version);
    }

}
