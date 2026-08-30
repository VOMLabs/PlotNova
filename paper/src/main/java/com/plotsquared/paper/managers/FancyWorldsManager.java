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
package com.plotsquared.paper.managers;

import com.google.inject.Singleton;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * FancyWorlds integration for world management.
 * Uses reflection to interact with the FancyWorlds API when available.
 *
 * <p>When FancyWorlds is present, world creation is delegated to it.
 * Falls back to the default Paper world manager if FancyWorlds is not available.</p>
 *
 * @see <a href="https://fancyinnovations.com/docs/minecraft-plugins/fancyworlds/api/getting-started">FancyWorlds API</a>
 */
@Singleton
public class FancyWorldsManager extends PaperWorldManager {

    private static final Logger LOGGER = LoggerFactory.getLogger("PlotNova/" + FancyWorldsManager.class.getSimpleName());
    private static final boolean FANCYWORLDS_AVAILABLE;

    static {
        boolean available;
        try {
            Class.forName("com.fancyinnovations.fancyworlds.api.FancyWorldsApi");
            available = true;
        } catch (final ClassNotFoundException e) {
            available = false;
        }
        FANCYWORLDS_AVAILABLE = available;
    }

    @Override
    public void initialize() {
        if (FANCYWORLDS_AVAILABLE) {
            LOGGER.info("FancyWorlds manager initialized (API detected)");
        } else {
            LOGGER.warn("FancyWorlds API not found at compile time — using Bukkit fallback for world creation");
        }
    }

    @Override
    public @Nullable World handleWorldCreation(final @NonNull String worldName, final @Nullable String generator) {
        if (!FANCYWORLDS_AVAILABLE) {
            return super.handleWorldCreation(worldName, generator);
        }

        try {
            // Use FancyWorlds API via reflection to avoid compile-time dependency
            final Class<?> apiClass = Class.forName("com.fancyinnovations.fancyworlds.api.FancyWorldsApi");
            final Object apiInstance = apiClass.getMethod("getInstance").invoke(null);

            if (apiInstance == null) {
                LOGGER.warn("FancyWorlds API instance not available, falling back to default world manager");
                return super.handleWorldCreation(worldName, generator);
            }

            // Build world creation options
            final Class<?> optionsClass = Class.forName("com.fancyinnovations.fancyworlds.api.WorldCreateOptions");
            final Class<?> builderClass = Class.forName("com.fancyinnovations.fancyworlds.api.WorldCreateOptions$Builder");
            final Object builder = optionsClass.getMethod("builder", String.class).invoke(null, worldName);

            if (generator != null) {
                builderClass.getMethod("generator", String.class).invoke(builder, generator);
            }

            final Object options = builderClass.getMethod("build").invoke(builder);
            final Object future = apiClass.getMethod("createWorld", optionsClass).invoke(apiInstance, options);

            // Handle the CompletableFuture result
            if (future instanceof java.util.concurrent.CompletableFuture<?> completableFuture) {
                completableFuture.thenAccept(result -> {
                    if (result != null) {
                        LOGGER.info("Successfully created world '{}' via FancyWorlds", worldName);
                    } else {
                        LOGGER.error("Failed to create world '{}' via FancyWorlds", worldName);
                    }
                }).exceptionally(throwable -> {
                    LOGGER.error("Error creating world '{}' via FancyWorlds", worldName, throwable);
                    return null;
                });
            }

            // Return the Bukkit world if it already exists
            return Bukkit.getWorld(worldName);
        } catch (final Exception e) {
            LOGGER.error("Failed to create world '{}' via FancyWorlds, falling back to default", worldName, e);
            return super.handleWorldCreation(worldName, generator);
        }
    }

    @Override
    public String getName() {
        return "fancyworlds";
    }

}
