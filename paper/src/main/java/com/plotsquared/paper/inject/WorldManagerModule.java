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
package com.plotsquared.paper.inject;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.plotsquared.paper.managers.FancyWorldsManager;
import com.plotsquared.paper.managers.PaperWorldManager;
import com.plotsquared.paper.managers.MultiverseWorldManager;
import com.plotsquared.core.util.PlatformWorldManager;
import org.bukkit.Bukkit;
import org.bukkit.World;

public class WorldManagerModule extends AbstractModule {

    @Provides
    @Singleton
    PlatformWorldManager<World> provideWorldManager() {
        // Priority: FancyWorlds > Multiverse-Core > Paper default
        if (isPluginPresent("FancyWorlds")) {
            return new FancyWorldsManager();
        } else if (isPluginPresent("Multiverse-Core")) {
            @SuppressWarnings("removal")
            final var manager = new MultiverseWorldManager();
            return manager;
        } else {
            return new PaperWorldManager();
        }
    }

    private boolean isPluginPresent(final String name) {
        return Bukkit.getPluginManager().getPlugin(name) != null;
    }

}
