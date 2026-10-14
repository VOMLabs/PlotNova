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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

/**
 * Validates {@link PaperSettings} fields at startup after loading.
 * <p>
 * Checks type ranges and logs warnings for invalid values, resetting them to defaults.
 */
public final class PaperConfigValidator {

    private static final Logger LOGGER = LogManager.getLogger("PlotSquared/PaperConfigValidator");

    private PaperConfigValidator() {
        // utility
    }

    /**
     * Run all validation checks against the loaded {@link PaperSettings} fields.
     * Invalid values are logged as warnings and reset to their compiled defaults.
     *
     * @return the number of issues found and corrected
     */
    public static int validate() {
        final List<String> issues = new ArrayList<>();

        // --- Dialog ---
        validateRange("Dialog.TIMEOUT_SECONDS",
                PaperSettings.Dialog.TIMEOUT_SECONDS, 0, 300, 30, issues);

        // --- MiniPlaceholder ---
        validateRange("MiniPlaceholderConfig.CACHE_TICKS",
                PaperSettings.MiniPlaceholderConfig.CACHE_TICKS, 0, 72000, 20, issues);

        if (issues.isEmpty()) {
            LOGGER.info("Paper configuration validation passed — no issues found");
        } else {
            LOGGER.warn("Paper configuration validation found {} issue(s) which have been reset to defaults:",
                    issues.size());
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
            LOGGER.warn("Config '{} = {}' is out of range [{}, {}], resetting to {}",
                    path, value, min, max, defaultValue);
            setField(path, defaultValue);
            issues.add(path + " was " + value + ", reset to " + defaultValue);
        }
    }

    /**
     * Reset a PaperSettings field to a given value using reflection.
     */
    private static void setField(final String path, final Object defaultValue) {
        Config.set(path, defaultValue, PaperSettings.class);
    }

}
