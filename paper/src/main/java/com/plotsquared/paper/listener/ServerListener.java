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
package com.plotsquared.paper.listener;

import com.google.inject.Inject;
import com.plotsquared.paper.PaperPlatform;
import com.plotsquared.paper.placeholder.MVdWPlaceholders;
import com.plotsquared.paper.util.PaperEconHandler;
import com.plotsquared.paper.util.VaultUnlockedEconHandler;
import com.plotsquared.core.PlotSquared;
import com.plotsquared.core.configuration.Settings;
import com.plotsquared.core.configuration.caption.TranslatableCaption;
import com.plotsquared.core.player.ConsolePlayer;
import com.plotsquared.core.util.EconHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.ServerLoadEvent;
import org.checkerframework.checker.nullness.qual.NonNull;

public class ServerListener implements Listener {

    private static final Logger LOGGER = LogManager.getLogger("PlotSquared/" + ServerListener.class.getSimpleName());

    private final PaperPlatform plugin;

    @Inject
    public ServerListener(final @NonNull PaperPlatform plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onServerLoad(ServerLoadEvent event) {
        if (Bukkit.getPluginManager().getPlugin("MVdWPlaceholderAPI") != null && Settings.Enabled_Components.USE_MVDWAPI) {
            new MVdWPlaceholders(this.plugin, this.plugin.placeholderRegistry());
            ConsolePlayer.getConsole().sendMessage(TranslatableCaption.of("placeholder.hooked"));
        }
        if (Settings.Enabled_Components.ECONOMY && (Bukkit.getPluginManager().isPluginEnabled("VaultUnlocked") || Bukkit.getPluginManager().isPluginEnabled("Vault"))) {
            EconHandler econHandler = Bukkit.getPluginManager().isPluginEnabled("VaultUnlocked")
                    ? new VaultUnlockedEconHandler()
                    : new PaperEconHandler();
            try {
                if (!econHandler.init()) {
                    LOGGER.warn("Economy is enabled but no plugin is providing an economy service. Falling back...");
                    econHandler = EconHandler.nullEconHandler();
                }
            } catch (final Exception ignored) {
                econHandler = EconHandler.nullEconHandler();
            }
            if (PlotSquared.platform().econHandler() instanceof MutableEconHandler meh) {
                meh.setImplementation(econHandler);
            }
        }
    }

    /**
     * Internal use only. Required to implement lazy econ loading using Guice.
     *
     * @since 7.2.0
     */
    public interface MutableEconHandler {
        void setImplementation(EconHandler econHandler);
    }

}
